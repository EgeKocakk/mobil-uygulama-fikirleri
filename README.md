# Mobil Uygulama Fikir Sistemi

Bu depo, günlük olarak çalışan bir bulut ajanının ürettiği mobil uygulama/oyun fikirlerini
ve bu fikirleri sesli olarak sunacak Android uygulamasının kodunu barındırır.

## Bileşenler

1. **Fikir üretim ajanı** (`ideas/` klasörü) — her gün bulutta zamanlanmış olarak çalışır,
   araştırma yapar ve para kazanma potansiyeli olan bir mobil uygulama/oyun fikri üretir.
   Fikri "güzel olur" diye değil, şu üç soruyu tam olarak cevapladıktan sonra kaydeder:
   - Kullanıcı bunu neden oynasın/kullansın?
   - Kullanıcı reklam izlemeye neden mecbur kalsın?
   - Kullanıcı uygulama içi satın almaya neden ihtiyaç duysun?

2. **Android uygulaması** (`android/` klasörü) — telefonda arka planda çalışır, her 15 dakikada
   bir depoyu kontrol eder (Firebase/push kullanmaz, basit polling). Yeni fikir bulduğunda sesli
   olarak sorar ("Furkan, ordamısın?"):
   - 15 saniye cevap yoksa bir kez daha sorar.
   - Yine cevap yoksa sessiz kalır ve fikri cihazdaki `bekleyen_fikirler.txt` dosyasına ekler.
   - "Buradayım" derse fikri sesli anlatır.
   - "15 dakika/yarım saat sonra tekrar sor" derse o süre sonra aynı soruyu tekrar sorar.

   Android Studio ile açmak için: `android/` klasörünü Android Studio'da "Open" ile aç, Gradle
   senkronizasyonunu bekle, gerçek bir Android cihaza (emülatörde mikrofon/konuşma tanıma
   güvenilir çalışmaz) yükleyip çalıştır. İlk açılışta mikrofon ve bildirim izni vermen gerekir.

## Klasör yapısı

- `ideas/` — Her gün üretilen fikir belgeleri (`YYYY-MM-DD.md` formatında).
- `ideas/index.md` — Tüm fikirlerin durumunu (bekliyor / anlatıldı / ertelendi) takip eden liste.
- `bekleyen_fikirler.txt` — Kullanıcıya hiç ulaşılamamış (sesli anlatılamamış) fikirlerin listesi.
