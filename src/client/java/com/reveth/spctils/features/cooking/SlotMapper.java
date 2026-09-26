package com.reveth.spctils.features.cooking;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Memetakan nama instruksi (dari lore buku) ke item Minecraft yang harus diklik,
 * lalu mencari slot yang berisi item tersebut di dalam ScreenHandler.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * MAPPING ITEM BERDASARKAN DATA AKTUAL GUI:
 *
 * Baris 1 (slot GUI atas, kiri → kanan):
 *   1. Tambah Rempah Rawa   → Items.BLAZE_POWDER    (blaze_powder)
 *   2. Besarkan Api Kuali   → Items.CAMPFIRE         (campfire)
 *   3. Aduk Rebusan         → Items.STICK            (stick)
 *   4. Cicipi Kuah Masakan  → Items.BOWL             (bowl)
 *   5. Tabur Garam Ajaib    → Items.GLOWSTONE_DUST  (glowstone_dust)
 *
 * Baris 2 (slot GUI bawah, kiri → kanan):
 *   6. Siram Kaldunya       → Items.WATER_BUCKET    (water_bucket)
 *   7. Tabur Gula Manis     → Items.SUGAR           (sugar)
 *   8. Kocok Adonan         → Items.WHEAT            (wheat)
 *   9. Tambah Arang Briket  → Items.CHARCOAL         (charcoal)
 *  10. Teteskan Madu Hutan  → Items.HONEY_BOTTLE    (honey_bottle)
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class SlotMapper {

    /**
     * Map dari keyword instruksi (lowercase) ke Item yang harus diklik.
     *
     * Kita pakai partial match (instruksi.contains(key)), sehingga key boleh
     * berupa substring dari nama instruksi. Ini membuat matching lebih toleran
     * terhadap variasi spasi atau typo minor.
     *
     * LinkedHashMap menjaga urutan agar partial match diprioritaskan dari yang
     * paling spesifik ke paling umum.
     */
    private static final Map<String, Item> INSTRUKSI_MAP = new LinkedHashMap<>();

    static {
        // Baris 1
        INSTRUKSI_MAP.put("tambah rempah rawa",  Items.BLAZE_POWDER);  // 1: blaze_powder
        INSTRUKSI_MAP.put("besarkan api kuali",  Items.CAMPFIRE);       // 2: campfire
        INSTRUKSI_MAP.put("aduk rebusan",        Items.STICK);          // 3: stick
        INSTRUKSI_MAP.put("cicipi kuah",         Items.BOWL);           // 4: bowl
        INSTRUKSI_MAP.put("tabur garam",         Items.GLOWSTONE_DUST); // 5: glowstone_dust

        // Baris 2
        INSTRUKSI_MAP.put("siram kaldunya",      Items.WATER_BUCKET);   // 6: water_bucket
        INSTRUKSI_MAP.put("tabur gula manis",    Items.SUGAR);          // 7: sugar
        INSTRUKSI_MAP.put("kocok adonan",        Items.WHEAT);          // 8: wheat
        INSTRUKSI_MAP.put("tambah arang",        Items.CHARCOAL);       // 9: charcoal
        INSTRUKSI_MAP.put("teteskan madu",       Items.HONEY_BOTTLE);   // 10: honey_bottle
    }

    /**
     * Mencari nomor slot di dalam ScreenHandler yang berisi item
     * yang sesuai dengan instruksi aktif.
     *
     * @param handler    ScreenHandler GUI yang sedang terbuka
     * @param instruksi  Nama instruksi aktif (lowercase, sudah di-strip)
     * @return index slot (untuk dipakai di clickSlot()), atau -1 jika tidak ditemukan
     */
    public static int findSlotForInstruksi(ScreenHandler handler, String instruksi) {
        // Tentukan item target dari mapping
        Item targetItem = resolveItem(instruksi);
        if (targetItem == null) return -1;

        // Telusuri semua slot di handler (termasuk inventory player)
        // Slot GUI biasanya ada di index awal sebelum inventory player
        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && stack.getItem() == targetItem) {
                return i; // kembalikan index slot pertama yang cocok
            }
        }

        return -1; // tidak ditemukan
    }

    /**
     * Mencocokkan teks instruksi ke Item menggunakan partial match.
     * Misalnya instruksi "tabur gula manis" akan cocok dengan key "tabur gula manis".
     *
     * @param instruksi teks instruksi aktif (lowercase)
     * @return Item yang harus diklik, atau null jika tidak ada yang cocok
     */
    public static Item resolveItem(String instruksi) {
        if (instruksi == null || instruksi.isEmpty()) return null;

        for (Map.Entry<String, Item> entry : INSTRUKSI_MAP.entrySet()) {
            if (instruksi.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
