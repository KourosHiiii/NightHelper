# NightHelper keep rules (minify disabled by default, rules kept for safety)
-keep class com.nighthelper.app.data.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
