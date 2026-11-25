# MediaPipe Tasks loads its graph and native bridge reflectively from the AAR.
-keep class com.google.mediapipe.** { *; }
-keep class com.google.protobuf.** { *; }

# MediaPipe logs through FluentLogger, whose caller finder walks the stack and
# ignores its own frames by class name. Renaming or inlining those frames makes
# the lookup throw during MediaPipe's static initialisers.
-keep class com.google.common.flogger.** { *; }

# Optional profiler protos referenced by MediaPipe but not shipped in the AAR.
-dontwarn com.google.mediapipe.proto.CalculatorProfileProto$CalculatorProfile
-dontwarn com.google.mediapipe.proto.GraphTemplateProto$CalculatorGraphTemplate

# kotlinx.serialization: keep the generated serializers for saved game state.
-keepattributes *Annotation*, InnerClasses, Signature
-keepclassmembers class com.fitnex.app.domain.** { *; }
-keep,includedescriptorclasses class com.fitnex.app.**$$serializer { *; }
-if @kotlinx.serialization.Serializable class com.fitnex.app.**
-keep class <1>$$serializer { *; }

# DataStore writes the serialized preferences blob by name.
-keepclassmembers class * implements androidx.datastore.core.Serializer { *; }
