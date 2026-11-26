# ML Kit pose detection ships its own consumer ProGuard rules; nothing extra
# is required here for R8.

# kotlinx.serialization: keep the generated serializers for saved game state.
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class com.fitnex.app.domain.** { *; }
-keep,includedescriptorclasses class com.fitnex.app.**$$serializer { *; }
-if @kotlinx.serialization.Serializable class com.fitnex.app.**
-keep class <1>$$serializer { *; }

# DataStore writes the serialized preferences blob by name.
-keepclassmembers class * implements androidx.datastore.core.Serializer { *; }
