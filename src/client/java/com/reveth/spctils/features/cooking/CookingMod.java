package com.reveth.spctils.features.cooking;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Koordinator utama AutoKoki Bot.
 *
 * State machine untuk Full-Auto mode:
 *
 *  ┌──────────────────────────────────────────────────────────────────┐
 *  │                                                                  │
 *  │  IDLE ──right-click──► OPENING_MENU ──GUI terbuka──► IN_COOKING_MENU
 *  │   ▲                         │                              │
 *  │   │                      timeout                       klik slot 0
 *  │   │                         │                              │
 *  │   │                         ▼                        GUI cooking tutup
 *  │   └──minigame tutup── IN_MINIGAME ◄──minigame buka── WAITING_FOR_MINIGAME
 *  │                            │                              │
 *  │                            └──────────timeout─────────────┘→ IDLE
 *  └──────────────────────────────────────────────────────────────────┘
 */
public class CookingMod {

    // ── Referensi layar ────────────────────────────────────────────────────────
    /** GUI "Instruksi Koki" minigame (di-set oleh HandledScreenMixin) */
    public static HandledScreen<?> activeKokiScreen     = null;
    /** GUI cooking selection menu, layar chest sebelum minigame (di-set oleh mixin) */
    public static HandledScreen<?> activeCookingMenu    = null;

    // ── Highlight untuk Manual mode ────────────────────────────────────────────
    /** Index slot yang harus di-highlight (dibaca oleh HandledScreenMixin.drawForeground) */
    public static int highlightedSlotIndex = -1;

    // ── State machine Full-Auto ────────────────────────────────────────────────
    public enum FullAutoState {
        /** Tidak ada GUI, menunggu untuk membuka cooking menu */
        IDLE,
        /** Sudah right-click, menunggu cooking menu terbuka (ada timeout) */
        OPENING_MENU,
        /** Cooking menu terbuka, akan klik slot 0 */
        IN_COOKING_MENU,
        /** Cooking menu tertutup, menunggu minigame terbuka (ada timeout) */
        WAITING_FOR_MINIGAME,
        /** Minigame terbuka, auto-klik item sesuai instruksi */
        IN_MINIGAME
    }

    public static FullAutoState fullAutoState = FullAutoState.IDLE;

    // ── Timer ──────────────────────────────────────────────────────────────────
    private int clickCooldown  = 0;  // jeda antar klik item
    private int reopenCooldown = 0;  // jeda sebelum right-click ulang di IDLE
    /** Timeout untuk state transisi. -1 = tidak ada timeout aktif */
    private int stateTimeout   = -1;

    private static final int REOPEN_COOLDOWN  = 20;  // 1 detik
    private static final int STATE_TIMEOUT    = 60;  // 3 detik timeout untuk state transisi

    // ── Registrasi ────────────────────────────────────────────────────────────

    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    // ── Tick loop utama ────────────────────────────────────────────────────────

    private void onTick(MinecraftClient client) {
        // 2. Turunkan semua timer
        if (clickCooldown > 0)  clickCooldown--;
        if (reopenCooldown > 0) reopenCooldown--;
        if (stateTimeout > 0)   stateTimeout--;

        // 3. Jangan jalankan logika bot saat settings screen terbuka
        if (client.currentScreen instanceof com.reveth.spctils.gui.SpctilsSettingsScreen) return;

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        // 4. Reset highlight jika tidak relevant
        if (BotConfig.mode != BotMode.MANUAL || activeKokiScreen == null) {
            highlightedSlotIndex = -1;
        }

        // 5. Jika bot mati, reset state dan keluar
        if (!BotConfig.enabled) {
            if (fullAutoState != FullAutoState.IDLE) {
                fullAutoState = FullAutoState.IDLE;
                stateTimeout  = -1;
            }
            return;
        }

        // 6. Jalankan handler sesuai mode
        switch (BotConfig.mode) {
            case MANUAL    -> tickManual();
            case AUTO      -> tickAuto(client, player);
            case FULL_AUTO -> tickFullAuto(client, player);
        }
    }

    // ── Manual Mode ────────────────────────────────────────────────────────────

    /**
     * Manual: hanya update highlightedSlotIndex agar mixin bisa render highlight.
     * Tidak ada auto-klik.
     */
    private void tickManual() {
        if (activeKokiScreen == null) return;

        ScreenHandler handler = activeKokiScreen.getScreenHandler();
        String instruksi = ItemTextReader.readActiveInstruksi(handler);

        highlightedSlotIndex = (instruksi != null)
                ? SlotMapper.findSlotForInstruksi(handler, instruksi)
                : -1;
    }

    // ── Auto Mode ──────────────────────────────────────────────────────────────

    /**
     * Auto: klik item secara otomatis saat di dalam minigame.
     * Masuk minigame dilakukan manual oleh player.
     */
    private void tickAuto(MinecraftClient client, ClientPlayerEntity player) {
        if (activeKokiScreen == null) return; // tunggu player masuk sendiri
        if (clickCooldown > 0) return;

        ScreenHandler handler = activeKokiScreen.getScreenHandler();
        String instruksi = ItemTextReader.readActiveInstruksi(handler);
        if (instruksi == null) return;

        int targetSlot = SlotMapper.findSlotForInstruksi(handler, instruksi);
        if (targetSlot < 0) {
            sendActionBar(client, "§e[AutoKoki] §fInstruksi tidak dikenali: §7" + instruksi);
            return;
        }

        clickSlot(client, player, activeKokiScreen.getScreenHandler(), targetSlot);
        clickCooldown = BotConfig.clickDelayTicks;
    }

    // ── Full-Auto Mode ─────────────────────────────────────────────────────────

    private void tickFullAuto(MinecraftClient client, ClientPlayerEntity player) {
        // Cek timeout state transisi (OPENING_MENU / WAITING_FOR_MINIGAME)
        if (stateTimeout == 0) {
            // Timer habis dan masih di state transisi — reset ke IDLE
            if (fullAutoState == FullAutoState.OPENING_MENU
                    || fullAutoState == FullAutoState.WAITING_FOR_MINIGAME) {
                sendActionBar(client, "§c[AutoKoki] Timeout! Kembali ke IDLE...");
                fullAutoState = FullAutoState.IDLE;
                stateTimeout  = -1;
                reopenCooldown = REOPEN_COOLDOWN;
            }
        }

        switch (fullAutoState) {
            // ── IDLE: Coba buka cooking menu dengan right-click ───────────────
            case IDLE -> {
                if (client.currentScreen == null && reopenCooldown <= 0) {
                    tryOpenCookingMenu(client);
                    fullAutoState = FullAutoState.OPENING_MENU;
                    stateTimeout  = STATE_TIMEOUT;
                    reopenCooldown = REOPEN_COOLDOWN;
                }
            }

            // ── OPENING_MENU: Menunggu, transisi di-handle oleh mixin ─────────
            case OPENING_MENU -> {
                // Mixin.onInit() akan set fullAutoState = IN_COOKING_MENU
                // saat HandledScreen (bukan minigame) terbuka
            }

            // ── IN_COOKING_MENU: Klik slot 0 dari cooking selection menu ──────
            case IN_COOKING_MENU -> {
                if (activeCookingMenu != null && clickCooldown <= 0) {
                    // Klik slot sesuai konfigurasi minigame
                    int slot = com.reveth.spctils.config.SpctilsConfig.get().autoCookingMinigameSlot;
                    clickSlot(client, player, activeCookingMenu.getScreenHandler(), slot);
                    // Beri waktu lebih panjang untuk server menutup GUI ini & membuka minigame
                    clickCooldown = Math.max(BotConfig.clickDelayTicks, 10);
                }
                // Mixin.onRemoved() akan set state ke WAITING_FOR_MINIGAME
                // Mixin.onInit() minigame akan set state ke IN_MINIGAME
            }

            // ── WAITING_FOR_MINIGAME: Menunggu minigame terbuka ───────────────
            case WAITING_FOR_MINIGAME -> {
                // Mixin.onInit() minigame akan set fullAutoState = IN_MINIGAME
            }

            // ── IN_MINIGAME: Auto-klik item per instruksi ─────────────────────
            case IN_MINIGAME -> {
                if (activeKokiScreen == null) {
                    // Minigame selesai/tertutup — loop ulang
                    fullAutoState  = FullAutoState.IDLE;
                    reopenCooldown = REOPEN_COOLDOWN;
                    return;
                }
                if (clickCooldown > 0) return;

                ScreenHandler handler = activeKokiScreen.getScreenHandler();
                String instruksi = ItemTextReader.readActiveInstruksi(handler);
                if (instruksi == null) return;

                int targetSlot = SlotMapper.findSlotForInstruksi(handler, instruksi);
                if (targetSlot < 0) return;

                clickSlot(client, player, handler, targetSlot);
                clickCooldown = BotConfig.clickDelayTicks;
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Right-click block di crosshair untuk membuka cooking menu */
    private void tryOpenCookingMenu(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        HitResult hit = client.crosshairTarget;
        if (hit == null) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            // ── OPSI A (default): target berupa Block ──
            client.interactionManager.interactBlock(
                    client.player, Hand.MAIN_HAND, (BlockHitResult) hit);

        } else if (hit.getType() == HitResult.Type.ENTITY) {
            // ── OPSI B: target berupa Entity ──
            // Uncomment jika server menggunakan entity (Villager, Armor Stand, dll)
            /*
            EntityHitResult entityHit = (EntityHitResult) hit;
            client.interactionManager.interactEntity(
                    client.player, entityHit.getEntity(), Hand.MAIN_HAND);
            */
        }
    }

    /** Klik satu slot di ScreenHandler dengan klik kiri (PICKUP button 0) */
    private static void clickSlot(MinecraftClient client, ClientPlayerEntity player,
                                   ScreenHandler handler, int slotIndex) {
        client.interactionManager.clickSlot(
                handler.syncId, slotIndex, 0, SlotActionType.PICKUP, player);
    }

    /** Kirim pesan singkat ke action bar (bukan chat) */
    public static void sendActionBar(MinecraftClient client, String message) {
        if (client.player != null) {
            // true = tampilkan di action bar (tengah bawah layar), bukan chat
            client.player.sendMessage(Text.literal(message), true);
        }
    }
}
