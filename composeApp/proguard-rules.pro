# Regras de otimização R8 / ProGuard para o projeto Pxrioverde

# --- Kotlin X Serialization (usado pelo Supabase / DTOs) ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class kotlinx.serialization.json.** { *; }

# --- Supabase & Ktor Client ---
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.sqlite.db.SupportSQLiteOpenHelper
-keep class * implements androidx.sqlite.db.SupportSQLiteDatabase

# --- SQLCipher ---
-keep class net.zetetic.** { *; }
-dontwarn net.zetetic.**

# --- Rive Android ---
-keep class app.rive.runtime.android.** { *; }
-dontwarn app.rive.runtime.android.**

# --- Koin Dependency Injection ---
-keep class org.koin.** { *; }

# --- Coil (Image Loader) ---
-keep class coil3.** { *; }
-dontwarn coil3.**

# Regras gerais de estabilidade para Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable class *;
}
