Notes App - Frontend (Android)

Repositori ini berisi kode sumber untuk aplikasi Android Notes App. Aplikasi ini berfungsi sebagai antarmuka pengguna (frontend) yang terintegrasi dengan layanan backend berbasis Python yang di-hosting di Vercel.

Fitur Utama

* Onboarding Screens: Alur pengenalan aplikasi yang interaktif saat pertama kali dibuka.
* Authentication: Fitur Sign Up dan Login yang terhubung langsung ke database cloud melalui API.
* Notes Management: Manajemen catatan pengguna secara real-time.

Prasyarat & Instalasi
Sebelum menjalankan proyek ini, pastikan Anda telah menginstal:

* Android Studio (Versi terbaru sangat direkomendasikan).
* Android SDK (Minimal API Level 26 atau lebih baru).
* Gradle (Disertakan dalam proyek).

Langkah-Langkah Clone & Run:

1. Clone repositori ini ke penyimpanan lokal Anda: git clone [https://github.com/ivanaharsono/Notes.git](https://www.google.com/search?q=https://github.com/ivanaharsono/Notes.git)
2. Buka Android Studio, pilih Open an Existing Project, lalu arahkan ke folder hasil clone.
3. Tunggu hingga proses Gradle Sync selesai otomatis.

Konfigurasi Penting (Wajib Dibaca)

1. Jalur SDK Lokal (local.properties)
File local.properties digunakan untuk menentukan lokasi Android SDK di komputer masing-masing pengembang. File ini TIDAK dilacak oleh Git demi menghindari konflik antar-perangkat.

Jika Anda mendapati error SDK location not found, buatlah file bernama local.properties secara manual di folder akar proyek, lalu isi dengan jalur SDK komputer Anda. Contoh (Windows):
sdk.dir=C:\Users\NAMA_USER_PC_ANDA\AppData\Local\Android\Sdk

2. Konfigurasi URL API Backend
Sebelum melakukan build atau menjalankan aplikasi di Emulator/HP, pastikan file konfigurasi API sudah mengarah ke server produksi Vercel yang aktif.

Buka file berikut:
app/src/main/java/com/angels/notes/ApiConfig.kt

Pastikan variabel BASE_URL telah dikonfigurasi sebagai berikut:
private const val BASE_URL = "[https://notes-backend-rust-five.vercel.app/](https://www.google.com/search?q=https://notes-backend-rust-five.vercel.app/)"

Cara Build APK untuk Pengujian
Jika Anda ingin mendistribusikan aplikasi ke anggota tim untuk dites langsung di perangkat fisik:

1. Pada menu navigasi atas Android Studio, klik Build.
2. Pilih Build Bundle(s) / APK(s) -> Klik Build APK(s).
3. Tunggu notifikasi sukses muncul di pojok kanan bawah, lalu klik Locate.
4. Ambil file .apk di dalam folder debug tersebut untuk dibagikan.
