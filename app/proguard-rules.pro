# Proguard rules for CareerForge AI Resume Builder
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep class com.pynexia.resumebuilder.data.model.** { *; }
