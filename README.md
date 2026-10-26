# Harman

Sade ve şık bir kişisel bütçe ve birikim takip uygulaması. Aylık harcama limitini belirle, gelir–giderlerini kaydet, tasarruf hedeflerini takip et. Tüm veriler cihazında saklanır; hesap, internet bağlantısı veya sunucu gerekmez.

> Arayüz Türkçedir ve tutarlar Türk lirası (₺) olarak gösterilir.

## Özellikler

- **Aylık harcama limiti:** Limit belirle, ay boyunca ne kadar harcadığını ve ne kadar kaldığını halka grafikte gör. Limitin %80'ine ulaşınca uyarı, aşınca "Limit aşıldı" durumu gösterilir.
- **Gelir ve gider takibi:** Tutar, kategori, tarih ve isteğe bağlı not ile işlem ekle, düzenle, sil.
- **Kategoriler:** Giderler için Market, Faturalar, Ulaşım, Alışveriş, Eğlence, Sağlık, Eğitim, Diğer; gelirler için Maaş ve Ek gelir.
- **Aylık rapor:** Ay ay gezinerek gelir, gider, net durum ve kategori bazlı harcama dağılımını incele.
- **Birden fazla tasarruf hedefi:** Hedef adı ve tutarı belirle, birikim ekle veya çıkar, hedefe ulaşınca kartta tebrik mesajı gör.
- **Ayarlar:** Sistem / açık / koyu tema, onboarding'i yeniden başlatma, tüm verileri sıfırlama.
- **Bento tarzı arayüz:** Pastel kutucuklar, yüzen gezinme çubuğu ve pakete gömülü Outfit yazı tipi.

## Ekranlar

| Ekran | Açıklama |
|-------|----------|
| Onboarding | İlk açılışta aylık limit belirleme (atlanabilir) |
| Ana Sayfa | Kalan bütçe, bu ayın işlemleri, hızlı işlem ekleme |
| Rapor | Aylık özet ve kategori dağılımı |
| Hedefler | Aylık limit ve tasarruf hedefleri |
| Ayarlar | Tema, onboarding, veri sıfırlama, hakkında |

## Teknolojiler

- **Dil:** Kotlin 2.2
- **Arayüz:** Jetpack Compose, Material 3, Navigation Compose
- **Yerel veri:** Room (işlemler ve tasarruf hedefleri), DataStore Preferences (limit, tema, onboarding durumu)
- **Mimari:** ViewModel + Repository, `Flow` / coroutines
- **Derleme:** Android Gradle Plugin 9.4, KSP, sürüm kataloğu (`gradle/libs.versions.toml`)
- **Android:** `minSdk 26`, `targetSdk 36`

## Proje yapısı

```
app/src/main/java/app/azracelik/harman/
├── data/
│   ├── local/        # Room veritabanı, DAO'lar ve migration'lar
│   ├── model/        # Transaction, SavingsGoal, Category
│   └── prefs/        # DataStore ayarları
├── domain/           # BudgetRepository, aylık özet hesapları, para biçimlendirme/ayrıştırma
├── ui/
│   ├── components/   # Ortak bileşenler
│   ├── home/ report/ goals/ settings/ onboarding/ transaction/
│   ├── theme/        # Renk, tema, tipografi
│   └── ViewModels.kt
├── HarmanApp.kt      # Application sınıfı
└── MainActivity.kt   # Navigasyon ve yüzen alt çubuk
```

## Tasarım notları

- **Para kuruş cinsinden `Long` olarak tutulur** (`amountMinor`); `Double` yuvarlama hatalarından kaçınmak için.
- Tutar girişi hem Türkçe (`1.250,50`) hem düz (`1250.50`) biçimleri kabul eder.
- **Limit ay bazlıdır.** Limiti değiştirmek yalnızca o aydan itibaren geçerli olur; geçmiş ayların raporu değişmez.
- **Veri güvenliği:** Room şeması `app/schemas/` altına export edilir ve her şema değişikliği için gerçek bir migration (`data/local/Migrations.kt`) yazılır. Yıkıcı (veriyi silen) migration kullanılmaz.
- Kategori listesi sabittir; kullanıcı tanımlı kategori yoktur.

## Başlarken

### Gereksinimler

- Android Studio (AGP 9.4 destekleyen güncel sürüm)
- JDK 11 veya üzeri
- Android SDK 36

### Çalıştırma

1. Depoyu klonla ve Android Studio ile aç.
2. Gradle senkronizasyonunun bitmesini bekle.
3. Bir emülatör veya cihaz (Android 8.0 / API 26+) seçip **Run** de.

Komut satırından:

```bash
./gradlew assembleDebug      # debug APK üretir
./gradlew installDebug       # bağlı cihaza kurar
```

### Testler

```bash
./gradlew test                    # birim testleri (para ayrıştırma, aylık özet, bütçe seviyeleri)
./gradlew connectedAndroidTest    # cihaz/emülatör gerektiren testler (Room migration testi dahil)
```

## Sürüm

`1.0` (versionCode 1)
