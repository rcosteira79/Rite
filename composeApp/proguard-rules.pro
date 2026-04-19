# Kotlinx Serialization: keep @Serializable classes + generated serializer companions
-keep,includedescriptorclasses @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **.*$Companion { kotlinx.serialization.KSerializer serializer(...); }
-keepclasseswithmembers class ** { kotlinx.serialization.KSerializer Companion; }
-keep class kotlinx.serialization.** { *; }

# kotlin-inject generated classes (DI wiring)
-keep class com.ricardocosteira.rite.di.** { *; }
-keep class **.Inject* { *; }

# SQLDelight-generated database classes
-keep class com.ricardocosteira.rite.data.database.** { *; }

# Navigation3 NavKey routes are looked up by class, so keep route classes
-keep class com.ricardocosteira.rite.presentation.navigation.** { *; }

# Compose runtime internals occasionally use reflection — keep names
-keep class androidx.compose.runtime.** { *; }

# Keep line numbers for release crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
