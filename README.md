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

2. **Android uygulaması** (`android/` klasörü, henüz kurulmadı) — telefonda arka planda çalışır,
   yeni fikir hazır olduğunda sesli olarak sorar ("Furkan, ordamısın?"), cevaba göre fikri
   anlatır, erteler veya bekleyen fikirler listesine ekler.

## Klasör yapısı

- `ideas/` — Her gün üretilen fikir belgeleri (`YYYY-MM-DD.md` formatında).
- `ideas/index.md` — Tüm fikirlerin durumunu (bekliyor / anlatıldı / ertelendi) takip eden liste.
- `bekleyen_fikirler.txt` — Kullanıcıya hiç ulaşılamamış (sesli anlatılamamış) fikirlerin listesi.
