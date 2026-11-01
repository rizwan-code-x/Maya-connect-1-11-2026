# REXLIFY / Maya Connect Release & Security Obfuscation Rules

# 1. Strip verbose debug/trace logging in release builds to prevent sensitive logcat leakage
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# 2. Hide original source file names in stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 3. Preserve Room Database entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# 4. Preserve Moshi / Serialization models
-keep class com.example.data.local.** { *; }
-keep class com.example.data.security.** { *; }
