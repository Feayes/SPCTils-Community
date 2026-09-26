package com.reveth.spctils.features.cooking;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Membaca instruksi aktif dari item buku yang ada di dalam ScreenHandler.
 *
 * Format lore buku:
 *   Baris 0: "MANUAL INSTRUKSI KOKI"          ← header (diabaikan)
 *   Baris 1: ""                                 ← baris kosong (diabaikan)
 *   Baris 2: "Lakukan: <Nama Instruksi> (Klik 1x)"  ← INI yang kita baca
 *   Baris 3: "Progress Masakan: X/4 ..."
 */
public class ItemTextReader {

    /**
     * Menelusuri semua slot di ScreenHandler, mencari item buku,
     * lalu mengekstrak nama instruksi aktif dari lore-nya.
     *
     * @return nama instruksi (lowercase, sudah di-strip format code), atau null jika tidak ditemukan
     */
    public static String readActiveInstruksi(ScreenHandler handler) {
        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            // Cari item buku (book atau written_book)
            // Gunakan Items.WRITTEN_BOOK atau Items.WRITABLE_BOOK tergantung server
            net.minecraft.item.Item item = stack.getItem();
            if (item != net.minecraft.item.Items.WRITTEN_BOOK
                    && item != net.minecraft.item.Items.WRITABLE_BOOK
                    && item != net.minecraft.item.Items.BOOK) {
                continue;
            }

            // Coba baca dari LORE (DataComponentTypes.LORE)
            // Di 1.21+, lore disimpan sebagai ItemLore yang berisi List<Text>
            net.minecraft.component.type.LoreComponent lore =
                    stack.get(DataComponentTypes.LORE);
            if (lore == null) continue;

            List<Text> loreLines = lore.lines();
            if (loreLines == null || loreLines.isEmpty()) continue;

            for (Text line : loreLines) {
                String raw = line.getString(); // ambil plain string (sudah tanpa §-code internal Text)
                // Strip format code Minecraft (§x) yang mungkin masih tersisa
                String stripped = stripFormatCodes(raw);

                // Cari baris yang dimulai dengan "Lakukan:"
                if (stripped.contains("Lakukan:")) {
                    // Format: "Lakukan: <Nama Instruksi> (Klik 1x)"
                    // Ambil teks di antara "Lakukan: " dan " (Klik"
                    int start = stripped.indexOf("Lakukan:") + "Lakukan:".length();
                    int end   = stripped.indexOf("(Klik");

                    if (start < 0 || end < 0 || end <= start) {
                        // Fallback: ambil semua setelah "Lakukan:"
                        return stripped.substring(start).trim().toLowerCase();
                    }

                    return stripped.substring(start, end).trim().toLowerCase();
                }
            }
        }
        return null; // tidak ada buku / instruksi tidak terbaca
    }

    /**
     * Menghapus karakter format Minecraft (§ diikuti satu karakter apapun)
     * dari sebuah string. Diperlukan karena server terkadang menyimpan
     * teks lore dengan embedded color/format code.
     */
    public static String stripFormatCodes(String text) {
        if (text == null) return "";
        // Regex: § diikuti karakter apapun (termasuk digit, huruf, dll)
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
}
