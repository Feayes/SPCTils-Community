package com.reveth.spctils.mixin;

import com.reveth.spctils.features.cooking.CookingMod;
import com.reveth.spctils.features.cooking.CookingMod.FullAutoState;
import com.reveth.spctils.features.cooking.BotConfig;
import com.reveth.spctils.features.cooking.BotMode;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin ke HandledScreen (base class semua GUI container Minecraft).
 *
 * Tiga inject:
 *  1. init()          → deteksi GUI terbuka, update state machine Full-Auto
 *  2. removed()       → deteksi GUI tertutup, update state machine Full-Auto
 *  3. drawForeground()→ gambar highlight hijau pulsing pada slot target (Manual mode)
 */
@Mixin(HandledScreen.class)
public class CookingHandledScreenMixin {

    private static final String KOKI_TITLE = "Instruksi Koki";

    /**
     * Shadow field HandledScreen.x / HandledScreen.y
     * (posisi top-left background GUI di layar, dipakai untuk koordinat highlight).
     */
    @Shadow protected int x;
    @Shadow protected int y;

    // ─── onInit ───────────────────────────────────────────────────────────────

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        Text title = self.getTitle();
        if (title == null) return;

        if (title.getString().contains(KOKI_TITLE)) {
            // ── Minigame "Instruksi Koki" terbuka ─────────────────────────────
            CookingMod.activeKokiScreen  = self;
            CookingMod.activeCookingMenu = null; // cooking menu pasti sudah tutup

            // Full-Auto: dari state apapun yang sedang menunggu → masuk minigame
            if (CookingMod.fullAutoState == FullAutoState.OPENING_MENU
                    || CookingMod.fullAutoState == FullAutoState.IN_COOKING_MENU
                    || CookingMod.fullAutoState == FullAutoState.WAITING_FOR_MINIGAME) {
                CookingMod.fullAutoState = FullAutoState.IN_MINIGAME;
            }

        } else {
            // ── GUI lain terbuka (cooking selection menu / chest biasa) ────────
            CookingMod.activeCookingMenu = self;

            // Full-Auto: cooking menu terbuka setelah right-click
            if (CookingMod.fullAutoState == FullAutoState.OPENING_MENU) {
                CookingMod.fullAutoState = FullAutoState.IN_COOKING_MENU;
            }
        }
    }

    // ─── onRemoved ────────────────────────────────────────────────────────────

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        HandledScreen<?> self = (HandledScreen<?>) (Object) this;

        if (CookingMod.activeKokiScreen == self) {
            // Minigame tertutup
            CookingMod.activeKokiScreen = null;
            // Full-Auto: minigame selesai → kembali IDLE untuk loop ulang
            // (CookingMod.tickFullAuto() juga meng-handle ini via null check)
            if (CookingMod.fullAutoState == FullAutoState.IN_MINIGAME) {
                CookingMod.fullAutoState = FullAutoState.IDLE;
            }

        } else if (CookingMod.activeCookingMenu == self) {
            // Cooking menu tertutup
            CookingMod.activeCookingMenu = null;
            // Full-Auto: cooking menu tutup, kemungkinan minigame akan segera buka
            if (CookingMod.fullAutoState == FullAutoState.IN_COOKING_MENU) {
                CookingMod.fullAutoState = FullAutoState.WAITING_FOR_MINIGAME;
                // stateTimeout sudah berjalan dari saat masuk IN_COOKING_MENU
            }
        }
    }

    // ─── onDrawForeground (highlight slot — Manual mode) ──────────────────────

    /**
     * Inject di akhir drawForeground() untuk menggambar highlight slot target.
     *
     * WHY drawForeground (bukan render TAIL)?
     *   drawForeground() dipanggil DALAM konteks matrix yang sudah ditranslasi ke (x, y) GUI.
     *   Artinya slot.x dan slot.y langsung bisa dipakai sebagai koordinat fill — tidak perlu
     *   tambah offset this.x / this.y.
     *   Selain itu, drawForeground dipanggil SEBELUM tooltip, sehingga highlight
     *   tidak menutupi tooltip item.
     */
    @Inject(method = "drawForeground", at = @At("TAIL"))
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        // Hanya aktif di Manual mode
        if (!BotConfig.enabled || BotConfig.mode != BotMode.MANUAL) return;
        // Pastikan ini adalah layar minigame yang sedang kita track
        if (CookingMod.activeKokiScreen != (HandledScreen<?>) (Object) this) return;
        if (CookingMod.highlightedSlotIndex < 0) return;

        ScreenHandler handler = ((HandledScreen<?>) (Object) this).getScreenHandler();
        if (CookingMod.highlightedSlotIndex >= handler.slots.size()) return;

        Slot slot = handler.slots.get(CookingMod.highlightedSlotIndex);

        // ── Solid highlight (tanpa efek kelip) ────────────────────────────────
        com.reveth.spctils.config.HighlightColor color =
                com.reveth.spctils.config.SpctilsConfig.get().cookingHighlightColor;

        // Overlay semi-transparan dengan warna pilihan
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.fillArgb());

        // Border putih solid di tepi slot (1px)
        int borderColor = 0xFFFFFFFF;
        int sx = slot.x, sy = slot.y;
        context.fill(sx,      sy,      sx + 16, sy + 1,  borderColor); // atas
        context.fill(sx,      sy + 15, sx + 16, sy + 16, borderColor); // bawah
        context.fill(sx,      sy,      sx + 1,  sy + 16, borderColor); // kiri
        context.fill(sx + 15, sy,      sx + 16, sy + 16, borderColor); // kanan
    }
}
