package com.fitnex.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnex.app.camera.LivePoseState
import com.fitnex.app.camera.PoseDetector
import com.fitnex.app.camera.PushUpAnalyzer
import com.fitnex.app.data.CameraFacing
import com.fitnex.app.domain.BattleStatus
import com.fitnex.app.domain.PushUpCounter
import com.fitnex.app.ui.components.Eyebrow
import com.fitnex.app.ui.components.FitIcon
import com.fitnex.app.ui.components.HealthBar
import com.fitnex.app.ui.components.IconPlate
import com.fitnex.app.ui.components.PrimaryAction
import com.fitnex.app.ui.components.ProgressRail
import com.fitnex.app.ui.components.Tag
import com.fitnex.app.ui.feedback.GameFeedback
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Ember
import com.fitnex.app.ui.theme.EmberBright
import com.fitnex.app.ui.theme.Fog
import com.fitnex.app.ui.theme.FogDim
import com.fitnex.app.ui.theme.Gold
import com.fitnex.app.ui.theme.Hairline
import com.fitnex.app.ui.theme.Night
import com.fitnex.app.ui.theme.Panel
import java.util.concurrent.Executors
import kotlin.math.ceil

@Composable
fun BattleScreen(viewModel: GameViewModel, battle: BattleUi) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var livePose by remember { mutableStateOf(LivePoseState()) }
    val hitFlash = remember { Animatable(0f) }
    val feedback = remember { GameFeedback(context) }

    BackHandler { viewModel.abandonBattle() }

    DisposableEffect(feedback) {
        onDispose { feedback.close() }
    }

    LaunchedEffect(battle.pushUps) {
        if (battle.pushUps == 0) return@LaunchedEffect
        if (battle.hp == 0) feedback.finishingHit(settings) else feedback.rep(settings)
        hitFlash.snapTo(0.32f)
        hitFlash.animateTo(0f, tween(durationMillis = 280))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (hasCameraPermission) {
            CameraFeed(
                facing = settings.cameraFacing,
                onRep = viewModel::registerPushUp,
                onLiveState = { livePose = it },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CameraPermissionPanel(
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Cinematic scrims keep the HUD legible over any room.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.90f),
                        0.28f to Color.Black.copy(alpha = 0.35f),
                        0.52f to Color.Transparent,
                        0.74f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black.copy(alpha = 0.96f),
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Ember.copy(alpha = hitFlash.value * 0.45f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            CombatHeader(battle = battle, onRetreat = viewModel::abandonBattle)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                FramingGuide(Modifier.fillMaxSize())
            }
            CombatFooter(battle = battle, livePose = livePose)
        }
    }

    if (battle.status == BattleStatus.TIME_UP) {
        TimeUpDialog(
            bossName = battle.boss.name,
            canExtend = !battle.extensionUsed,
            onAccept = viewModel::acceptExtension,
            onDecline = viewModel::abandonBattle,
        )
    }
}

@Composable
private fun CombatHeader(battle: BattleUi, onRetreat: () -> Unit) {
    val timeFraction = if (battle.boss.timeLimitSeconds > 0) {
        battle.secondsRemaining.toFloat() / battle.boss.timeLimitSeconds.toFloat()
    } else {
        0f
    }
    val urgent = battle.secondsRemaining <= 30

    Column {
        ProgressRail(
            progress = timeFraction,
            height = 2.dp,
            accent = if (urgent) EmberBright else Bone.copy(alpha = 0.55f),
            track = Color.White.copy(alpha = 0.10f),
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconPlate(
                icon = FitIcon.Close,
                onClick = onRetreat,
                contentDescription = "Retreat from battle",
                tint = Bone.copy(alpha = 0.85f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FitIcon(
                        FitIcon.Clock,
                        tint = if (urgent) EmberBright else Fog,
                        iconSize = 14.dp,
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        formatClock(battle.secondsRemaining),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (urgent) EmberBright else Bone,
                    )
                }
                Eyebrow("On the clock", color = FogDim)
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Target", color = Fog)
                Spacer(Modifier.height(4.dp))
                Text(
                    battle.boss.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Bone,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Tag(
                    text = "Lv ${battle.level.toString().padStart(2, '0')}",
                    color = Gold,
                )
                Spacer(Modifier.height(6.dp))
                Eyebrow(
                    battle.equippedTitle?.name ?: "No title",
                    color = FogDim,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HealthBar(
            fraction = battle.hp.toFloat() / battle.maxHp.toFloat(),
            height = 9.dp,
            segments = 24,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "HP ${battle.hp} / ${battle.maxHp}",
                style = MaterialTheme.typography.labelSmall,
                color = Fog,
            )
            Text(
                "${battle.damage} damage dealt",
                style = MaterialTheme.typography.labelSmall,
                color = Ember.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun CombatFooter(battle: BattleUi, livePose: LivePoseState) {
    val repScale = remember { Animatable(1f) }
    val deltaAlpha = remember { Animatable(0f) }

    LaunchedEffect(battle.pushUps) {
        if (battle.pushUps == 0) return@LaunchedEffect
        repScale.snapTo(1.16f)
        repScale.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 620f))
        deltaAlpha.snapTo(1f)
        deltaAlpha.animateTo(0f, tween(durationMillis = 720))
    }

    val hpFraction = battle.hp.toFloat() / battle.maxHp.toFloat()
    val repsToSlay = ceil(battle.hp / 2.0).toInt()

    Column {
        TrackingStrip(livePose)

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Push-ups", color = Fog)
                Text(
                    text = battle.pushUps.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 74.sp,
                        lineHeight = 78.sp,
                    ),
                    color = Bone,
                    modifier = Modifier.graphicsLayer {
                        scaleX = repScale.value
                        scaleY = repScale.value
                    },
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Box {
                    Column(horizontalAlignment = Alignment.End) {
                        Eyebrow("To slay", color = FogDim)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (repsToSlay == 0) "—" else "$repsToSlay reps",
                            style = MaterialTheme.typography.titleLarge,
                            color = Bone,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "DMG ${battle.damage}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Ember,
                        )
                    }
                    Text(
                        "+2",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmberBright,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .graphicsLayer {
                                alpha = deltaAlpha.value
                                translationY = -34f * (1f - deltaAlpha.value)
                            },
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        ProgressRail(
            progress = 1f - hpFraction,
            height = 3.dp,
            accent = Ember,
            track = Color.White.copy(alpha = 0.12f),
        )
        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Eyebrow(
                if (battle.extensionUsed) "Extension spent" else "Hold form",
                color = FogDim,
            )
            Eyebrow("${((1f - hpFraction) * 100).toInt()}% broken", color = FogDim)
        }
    }
}

@Composable
private fun TrackingStrip(livePose: LivePoseState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (livePose.tracked) Ember.copy(alpha = 0.35f) else Hairline,
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(if (livePose.tracked) EmberBright else FogDim, CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            livePose.feedback,
            style = MaterialTheme.typography.bodyMedium,
            color = Bone,
            modifier = Modifier.weight(1f),
        )
        livePose.elbowAngle?.let { angle ->
            Text(
                "${angle.toInt()}°",
                style = MaterialTheme.typography.labelSmall,
                color = Fog,
            )
        }
    }
}

/** Four corner brackets that frame the set-up without boxing the viewfinder. */
@Composable
private fun FramingGuide(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val inset = 26.dp.toPx()
        val arm = 26.dp.toPx()
        val stroke = 1.2.dp.toPx()
        val color = Bone.copy(alpha = 0.22f)
        val corners = listOf(
            Offset(inset, inset) to Offset(1f, 1f),
            Offset(size.width - inset, inset) to Offset(-1f, 1f),
            Offset(inset, size.height - inset) to Offset(1f, -1f),
            Offset(size.width - inset, size.height - inset) to Offset(-1f, -1f),
        )
        corners.forEach { (point, direction) ->
            drawLine(
                color = color,
                start = point,
                end = Offset(point.x + arm * direction.x, point.y),
                strokeWidth = stroke,
            )
            drawLine(
                color = color,
                start = point,
                end = Offset(point.x, point.y + arm * direction.y),
                strokeWidth = stroke,
            )
        }
    }
}

@Composable
private fun CameraFeed(
    facing: CameraFacing,
    onRep: () -> Unit,
    onLiveState: (LivePoseState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnRep by rememberUpdatedState(onRep)
    val currentOnLiveState by rememberUpdatedState(onLiveState)
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val counter = remember { PushUpCounter() }
    val detector = remember {
        PoseDetector(context.applicationContext) { pose, timestampMs ->
            val counted = counter.onPose(pose, timestampMs)
            val snapshot = LivePoseState(
                tracked = counter.isTracking,
                feedback = counter.feedback,
                phase = counter.phase,
                elbowAngle = pose?.leftElbowAngle ?: pose?.rightElbowAngle,
            )
            mainHandler.post {
                currentOnLiveState(snapshot)
                if (counted) currentOnRep()
            }
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember { PushUpAnalyzer(detector) }

    DisposableEffect(lifecycleOwner, facing) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = runCatching { future.get() }.getOrNull() ?: return@addListener
            val preview = Preview.Builder()
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }

            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                Size(640, 480),
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                            ),
                        )
                        .build(),
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { it.setAnalyzer(executor, analyzer) }

            val selector = when (facing) {
                CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
            }

            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { if (future.isDone) future.get().unbindAll() }
            detector.close()
            executor.shutdown()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

@Composable
private fun CameraPermissionPanel(onRequest: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Night)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(64.dp)
                .border(1.dp, Hairline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            FitIcon(FitIcon.Camera, tint = Ember, iconSize = 26.dp)
        }
        Spacer(Modifier.height(22.dp))
        Eyebrow("Camera offline", color = Ember)
        Spacer(Modifier.height(10.dp))
        Text(
            "The hunt needs your eyes on the floor. FitNex tracks your reps in memory — nothing is recorded, stored or uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = Fog,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(26.dp))
        PrimaryAction(
            text = "Enable camera",
            onClick = onRequest,
            icon = FitIcon.Camera,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TimeUpDialog(
    bossName: String,
    canExtend: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Dialog(onDismissRequest = { }) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Panel, RoundedCornerShape(24.dp))
                .border(1.dp, Hairline, RoundedCornerShape(24.dp))
                .padding(24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FitIcon(FitIcon.Clock, tint = Ember, iconSize = 16.dp)
                Spacer(Modifier.width(8.dp))
                Eyebrow("The clock has spoken", color = Ember)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "TIME'S UP",
                style = MaterialTheme.typography.headlineLarge,
                color = Bone,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (canExtend) {
                    "The $bossName still stands. Take one more minute? It heals fully and the round restarts."
                } else {
                    "Your minute is spent. The $bossName holds the gate."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Fog,
            )
            Spacer(Modifier.height(22.dp))
            if (canExtend) {
                PrimaryAction(
                    text = "One more minute",
                    onClick = onAccept,
                    icon = FitIcon.Flame,
                )
                Spacer(Modifier.height(10.dp))
            }
            TextButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (canExtend) "Retreat" else "Accept defeat",
                    style = MaterialTheme.typography.labelMedium,
                    color = Fog,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
