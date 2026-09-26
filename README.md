# SPCTils

Mod client-side [Fabric](https://fabricmc.net/) untuk Minecraft yang dibuat khusus untuk server **SPC** - sebuah server survival/RPG bahasa Indonesia. SPCTils menambahkan berbagai HUD, quality-of-life, dan tools bantu untuk mempermudah dan memperbagus pengalaman bermain di server SPC.

> Mod ini **client-only**. Tidak ada perubahan apa pun terhadap server — semua fitur bekerja dengan membaca dan menampilkan ulang informasi yang sudah dikirim server (chat, boss bar, lore item, GUI) ke tampilan yang lebih rapi dan mudah dibaca.

## Fitur

- **Daily Quest Tracker** - melacak progress quest harian (Dojo, Mamarat, Anomaly, Guild) secara otomatis lewat parsing chat & GUI quest, lengkap dengan reset otomatis tiap pergantian hari (WIB).
- **Objective HUD** - menampilkan ulang boss bar "OBJEKTIF" server di posisi HUD yang bisa dipindah, alih-alih menumpuk di atas layar.
- **Booster HUD** - menampilkan status Weekend/Weekday Booster dalam kotak ringkas yang bisa disembunyikan/diganti posisinya.
- **Damage Tracker** - menghitung Total Damage, DPS (3 detik terakhir), dan DPM (60 detik terakhir) dari combat log.
- **Player Stat Summary (Debug)** - menjumlahkan semua stat dari lore item di inventory (misal total Damage dari seluruh equipment) untuk membantu cek build gear.
- **AutoKoki Highlight** - menyoroti slot item yang harus diklik pada minigame "Instruksi Koki", membaca instruksi dari lore buku secara otomatis. Klik tetap dilakukan manual oleh pemain.
- **Layout Editor** - drag-and-drop posisi tiap elemen HUD, dengan 3 profile layout yang bisa disimpan berbeda.
- **SPC Server Shortcut** - tombol cepat di Title Screen untuk connect ke server-server SPC (`play`, `mc`, `indie`).
- **Packet Recorder (Debug)** - merekam traffic paket jaringan selama 60 detik untuk keperluan debugging/development mod ini sendiri.

## Requirement

| Komponen | Versi |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.19.5 |
| Fabric API | 0.141.6+1.21.11 |
| Java | 21 |

## Instalasi

1. Pasang [Fabric Loader](https://fabricmc.net/use/) untuk Minecraft 1.21.11.
2. Unduh [Fabric API](https://modrinth.com/mod/fabric-api) versi yang sesuai dan taruh di folder `mods`.
3. Unduh file `.jar` SPCTils dari [Releases](../../releases) dan taruh juga di folder `mods`.
4. Jalankan Minecraft dengan profil Fabric seperti biasa.

## Cara Pakai

| Aksi | Keybind Default |
|---|---|
| Buka menu Settings SPCTils | `=` |
| Toggle AutoKoki Highlight | `-` |

Menu Settings (`=`) memiliki 4 tab:

- **Utilities** - toggle HUD (Daily Quest, Objective, Damage Counter), ganti tracker quest aktif, mode tampilan Booster, dan reset data quest.
- **Cooking** - toggle AutoKoki Highlight dan warna highlight slot.
- **Debug** - toggle debug entity/player, serta merekam paket jaringan.
- **Layout** - pilih profile layout dan buka editor drag-and-drop posisi HUD.

Konfigurasi disimpan otomatis di `config/spctils.json`.

## Kontribusi

Pull request dan issue dipersilakan. Untuk perubahan besar, buka issue dulu untuk didiskusikan.

## Lisensi

Lihat [LICENSE.txt](LICENSE.txt).

## Disclaimer

Mod ini adalah proyek independen/tidak resmi dan tidak berafiliasi dengan pengembang server SPC. Gunakan sesuai dengan aturan/ToS server tempat kamu bermain.
