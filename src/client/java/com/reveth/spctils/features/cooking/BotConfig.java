package com.reveth.spctils.features.cooking;

/**
 * Konfigurasi runtime AutoKoki Bot.
 * Semua field static agar bisa diakses dari mana saja tanpa instance.
 * (Untuk persistensi antar session, tambahkan serialisasi ke file di sini)
 */
public class BotConfig {

    /** Apakah bot sedang aktif */
    public static boolean enabled = false;

    /**
     * Jeda antar klik dalam satuan tick (20 tick = 1 detik).
     * Range: 1 – 100 tick.
     * Shift+klik pada tombol [-]/[+] mengubah ±10 sekaligus.
     */
    public static int clickDelayTicks = 10;

    /** Mode operasi bot */
    public static BotMode mode = BotMode.AUTO;
}
