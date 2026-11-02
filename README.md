# DietKu

DietKu adalah aplikasi Android offline-first untuk pemakaian pribadi. Aplikasi tidak memakai akun atau server; profil, makanan, dan catatan tersimpan di Room/DataStore pada perangkat.

## Menjalankan di Android Studio

1. Pasang Android Studio versi stabil (JDK 17 atau 21), Android SDK Platform 36, dan Build Tools yang ditawarkan SDK Manager.
2. Buka folder proyek ini di Android Studio dan izinkan Gradle melakukan sinkronisasi.
3. Pilih emulator atau perangkat Android (minSdk 26), lalu jalankan konfigurasi `app`.
4. Untuk membuat APK debug: `./gradlew assembleDebug`.
5. Untuk menjalankan unit test: `./gradlew testDebugUnitTest`.
6. Untuk menjalankan UI test pada emulator/perangkat: `./gradlew connectedDebugAndroidTest`.

Jika Android SDK belum tersedia, pasang melalui **Tools > SDK Manager** di Android Studio. Build APK memerlukan SDK tersebut.

## Fitur

- Profil dapat diedit: jenis kelamin, usia, tinggi, berat awal/saat ini, target kalori/protein, aktivitas, dan target berat tahap berikutnya.
- Kalkulasi BMI WHO/Asia-Pasifik, BMR Mifflin-St Jeor, perkiraan kebutuhan harian, target defisit, dan rentang tahap awal 5-10%.
- Konfirmasi keselamatan dan saran konsultasi ahli gizi sebelum menyimpan target di bawah 1.500 kkal.
- Katalog makanan Indonesia, pencarian, kategori, porsi, makanan custom, favorit, salin dari kemarin, dan catatan makro.
- Dashboard kalori/protein, air, olahraga, berat, serta status dan timer jendela puasa.
- Jadwal puasa 12:12, 14:10, 16:8, 18:6; relaksasi 14:10 berlaku untuk hari tersebut. Durasi di atas 18 jam tidak tersedia.
- Grafik berat, kalori, dan kepatuhan puasa; catatan berat bertanggal dan indikator perubahan.
- Checklist olahraga, timer jalan interval, dan sensor langkah `TYPE_STEP_COUNTER` bila perangkat mendukung.
- Rencana menu mingguan, pengacakan dengan variasi sumber protein, dan daftar belanja yang bisa dicentang.
- Pengingat lokal lewat AlarmManager/WorkManager, jam jangan-ganggu, izin notifikasi, serta pemulihan jadwal setelah reboot.
- Ekspor CSV melalui Android Sharesheet, tema terang/gelap, panduan makanan, dan informasi keamanan.

## Uji dan rilis

- Jalankan `testDebugUnitTest` untuk menguji rumus BMI/BMR/kalori serta jendela puasa.
- Jalankan `connectedDebugAndroidTest` di emulator untuk memeriksa navigasi utama.
- Uji manual izin notifikasi/aktivitas fisik, alarm tepat waktu, rentang jangan-ganggu, dan pemulihan setelah reboot.
- Uji migrasi Room dari database versi sebelumnya sebelum mengirim pembaruan.
- Periksa tema terang/gelap, ukuran teks besar, navigasi satu tangan, input target kalori rendah, timer puasa, grafik tanpa data, dan berbagi CSV.
- Untuk tahap pengembangan ini gunakan APK debug; jangan distribusikan sebagai nasihat atau layanan medis.

## Catatan kesehatan

DietKu hanya panduan umum, bukan pengganti dokter atau ahli gizi. Hentikan puasa jika pusing berat, gemetar, atau hampir pingsan; konsultasikan perubahan pola makan atau target kalori dengan tenaga kesehatan. Pertimbangkan pemeriksaan gula darah, tekanan darah, dan kolesterol.
