# R8 Optimization & Shrinking Configuration

# Preserve line numbers and source attributes for Google Play Console stack trace symbolication
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Keep native method names and JNI bridge classes so native code (libmpv, libtorrent, curl) can resolve them
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class is.xyz.mpv.** { *; }
-keep class app.gyrolet.mpvrx.ui.player.ScriptCurlBridge { *; }
-keep class net.mediaarea.mediainfo.lib.** { *; }
-keep class org.libtorrent4j.** { *; }

# Room Database entities & DAO reflection safety
-keep class * extends androidx.room.RoomDatabase
-keep class app.gyrolet.mpvrx.database.entities.** { *; }
-dontwarn androidx.room.paging.**

# Kotlinx Serialization rules
-keepclassmembers class * {
    @kotlinx.serialization.Serializer *** companion;
}

# Google Play Services & AdMob rules
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# SMBJ & Network Protocol ProGuard Rules
-keep class com.hierynomus.smbj.** { *; }
-keep class com.hierynomus.mssmb2.** { *; }
-keep class com.hierynomus.msdtyp.** { *; }
-keep class com.hierynomus.msfscc.** { *; }
-keep class com.hierynomus.protocol.** { *; }
-keep class com.hierynomus.spnego.** { *; }
-keep class com.hierynomus.ntlm.** { *; }
-keep class com.hierynomus.security.** { *; }
-keep class com.hierynomus.asn1.** { *; }
-keep class net.engio.mbassy.** { *; }
-keep class org.bouncycastle.** { *; }

# Warnings & Notes suppressions for optional transitive dependencies
-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.ietf.jgss.**
-dontnote org.ietf.jgss.**
-dontwarn net.engio.mbassy.dispatch.el.**
-dontnote net.engio.mbassy.dispatch.el.**
-dontwarn javax.el.**
-dontnote javax.el.**
-dontwarn org.bouncycastle.**

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Kotlin Coroutines R8 Optimizations
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.android.HandlerDispatcher {
    private static final java.lang.String name;
}
-dontwarn kotlinx.coroutines.**

# Jetpack Compose & ViewModel R8 rules
-keep class androidx.compose.runtime.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# DataBinding & ViewBinding Safety
-keepclassmembers class * extends androidx.databinding.ViewDataBinding {
    public static *** inflate(...);
    public static *** bind(...);
}
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(...);
}

# Optimize R8 bytecode shrinking and optimization passes
-repackageclasses
-allowaccessmodification

