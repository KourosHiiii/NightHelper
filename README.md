# NightHelper — چک‌لیست هوشمند شب 🌙 (بدون ایموجی! فقط SVG)

<div dir="rtl">

**NightHelper** یه اپلیکیشن اندروید مینیمال و لوکس برای شب‌هاته: چک‌لیست تعاملی با کارت‌های تک‌تک و انیمیشن اسپرینگ، تریگرهای سریع (ژست چرخش گوشی، ویجت، اعلان دائمی)، تم تیره بنفش با ماه و ستاره‌های چشمک‌زن، و گزارش شبانه با تقویم شمسی.

تمام آیکون‌های اپ (ماه، ستاره، قرص، مسواک، کتاب، قلب، ...) با **SVG وکتور** رسم شدن — هیچ ایموجی‌ای توی رابط کاربری وجود نداره.

</div>

---

## ✨ امکانات

<div dir="rtl">

- **چک‌لیست تعاملی**: هر سؤال یه کارت جدا با انیمیشن spring نرم؛ انواع «بله/نه» و «متن کوتاه» (مثلاً شکرگزاری)
- **صفحه شب بخیر**: ماه درخشان با glow پالس‌دار، کار کوچک شبانه، لیست کارهای نیمه‌تمام و ذخیره گزارش
- **ژست چرخش گوشی (~180°)**: با شتاب‌سنج + آستانه‌های ضد اجرای تصادفی (ثبات ۱۲۰ms، کول‌داون ۳ ثانیه، فیلتر لرزش)
- **ویجت صفحه اصلی**: ماه طلایی + پیشرفت «۲ از ۴ انجام شد» + دکمه شروع
- **اعلان دائمی**: با دکمه‌های «شروع شب» و «تنظیمات» (قابل غیرفعال‌سازی)
- **تنظیمات کامل**: هر تریگر جداگانه سوییچ داره + مدیریت آیتم‌ها (افزودن/ویرایش/حذف با انتخاب آیکون SVG)
- **گزارش شبانه**: نوار پیشرفت، شکرگزاری، تقویم شمسی
- **بازنشانی روزانه خودکار**: آیتم‌ها هر روز صبح ریست و شب قبل آرشیو می‌شه
- **Haptic feedback** روی ثبت هر کار

</div>

## 📲 دانلود APK

| روش | لینک |
|---|---|
| آخرین Release (عمومی) | `https://github.com/KourosHiiii/NightHelper/releases/tag/latest-apk` |
| Artifact بیلد | `https://github.com/KourosHiiii/NightHelper/actions` → آخرین run → `NightHelper-APK` |

<div dir="rtl">

بعد از اولین بیلد موفق در Actions، فایل `NightHelper.apk` رو دانلود و نصب کن (اجازه نصب از منبع ناشناس رو بده).

</div>

## 🛠 تکنولوژی

- **Kotlin 2.0.20 + Jetpack Compose + Material 3** (تم تیره اختصاصی بنفش/یاسی/طلایی ماه)
- **DataStore Preferences** با kotlinx.serialization (آیتم‌ها، وضعیت‌ها، تاریخچه، تنظیمات)
- **Foreground Service + Accelerometer** برای ژست چرخش (FGS type `specialUse`)
- **AppWidget (RemoteViews)** — minSdk 26 / targetSdk 35

## 🔁 ساخت خودکار با GitHub Actions

`.github/workflows/build.yml` روی هر `push` به `main` و همچنین `workflow_dispatch` اجرا می‌شه:

1. JDK 17 (Temurin) + Gradle با کش
2. اگر Secrets امضا موجود باشه → `assembleRelease` **امضاشده**
3. اگر نباشه → fallback به `assembleDebug`
4. آپلود Artifact + انتشار در Release عمومی با تگ `latest-apk`

### 🖊 راهنمای امضا (اختیاری ولی پیشنهادشده)

<div dir="rtl">

۱. ساخت keystore روی سیستم خودت:

```bash
keytool -genkeypair -v -keystore nighthelper.keystore \
  -alias nighthelper -keyalg RSA -keysize 2048 -validity 10000
```

۲. تبدیل به base64:

```bash
# لینوکس
base64 -w0 nighthelper.keystore > keystore.b64
# مک
base64 -i nighthelper.keystore | pbcopy
# ویندوز (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("nighthelper.keystore")) | Set-Content keystore.b64
```

۳. در GitHub ریپو → `Settings` → `Secrets and variables` → `Actions` → `New repository secret` و این چهار تا رو بساز:

| Secret | مقدار |
|---|---|
| `KEYSTORE_BASE64` | محتوای `keystore.b64` |
| `KEYSTORE_PASSWORD` | پسورد keystore |
| `KEY_ALIAS` | `nighthelper` |
| `KEY_PASSWORD` | پسورد کلید |

۴. دوباره `Actions` → `Build NightHelper APK` → `Run workflow` — حالا خروجی **release امضاشده** است.

</div>

## 💻 بیلد محلی

```bash
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk
```

## 📁 ساختار پروژه

```
app/src/main/java/com/nighthelper/app/
├── MainActivity.kt / MainViewModel.kt
├── data/          # مدل‌ها + NightRepository (DataStore)
├── service/       # FlipGestureDetector + FlipService (ژست چرخش + اعلان)
├── receiver/      # BootReceiver (اجرای مجدد بعد از ری‌استارت)
├── widget/        # NightWidgetReceiver
├── util/          # اعداد فارسی + تقویم شمسی
└── ui/            # تم تیره، MoonSky (ستاره‌ها)، ۴ صفحه اصلی
app/src/main/res/drawable/   # همه آیکون‌ها SVG وکتور (بدون ایموجی)
.github/workflows/build.yml  # CI بیلد APK
```

## 🔐 نکته‌های مجوزها

<div dir="rtl">

- **POST_NOTIFICATIONS** (اندروید ۱۳+) موقع فعال‌کردن اعلان پرسیده می‌شه
- **نمایش روی سایر برنامه‌ها (Overlay)**: برای باز شدن تضمینی اپ بعد از چرخش گوشی (محدودیت اندروید ۱۰+) پیشنهاد می‌شه؛ اگر ندی، اپ به‌جاش یه هشدار تمام‌صفحه/heads-up نشون می‌ده
- سرویس پیش‌زمینه از نوع `specialUse` فقط برای مانیتورینگ شتاب‌سنجه

</div>

## License

MIT
