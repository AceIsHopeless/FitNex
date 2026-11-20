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
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnex.app.camera.LivePoseState
import com.fitnex.app.camera.PoseDetector
import com.fitnex.app.camera.PushUpAnalyzer
import com.fitnex.app.data.CameraFacing
import com.fitnex.app.domain.BattleStatus
import com.fitnex.app.domain.PushUpCounter
import com.fitnex.app.ui.components.HealthBar
import com.fitnex.app.ui.feedback.GameFeedback
import com.fitnex.app.ui.theme.Bone
import com.fitnex.app.ui.theme.Charcoal
import com.fitnex.app.ui.theme.EmberRed
import com.fitnex.app.ui.theme.Line
import com.fitnex.app.ui.theme.Muted
import com.fitnex.app.ui.theme.Steel
import java.util.concurrent.Executors

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
        hitFlash.snapTo(0.30f)
        hitFlash.animateTo(0f, tween(durationMillis = 260))
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

        // Scrims keep the HUD readable over any camera scene.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.88f),
                        0.26f to Color.Transparent,
                        0.58f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.94f),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EmberRed.copy(alpha = hitFlash.value * 0.5f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            BattleHeader(battle, onRetreat = viewModel::abandonBattle)
            BattleFooter(battle, livePose)
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
private fun BattleHeader(battle: BattleUi, onRetreat: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(Steel, RoundedCornerShape(2.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = formatClock(battle.secondsRemaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (battle.secondsRemaining <= 30) EmberRed else Bone,
                )
            }
            TextButton(onClick = onRetreat) {
                Text(
                    "RETREAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "LEVEL ${battle.level.toString().padStart(2, '0')} · " +
                (battle.equippedTitle?.name?.uppercase() ?: "NO TITLE"),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
        Spacer(Modifier.height(6.dp))
        Text(battle.boss.name, style = MaterialTheme.typography.displayLarge, color = Bone)
        Spacer(Modifier.height(10.dp))
        HealthBar(fraction = battle.hp.toFloat() / battle.maxHp.toFloat(), height = 10.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            "HP ${battle.hp} / ${battle.maxHp}",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

@Composable
private fun BattleFooter(battle: BattleUi, livePose: LivePoseState) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Text("PUSH-UPS", style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(
                    text = battle.pushUps.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 64.sp,
                    color = Bone,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("DAMAGE", style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(
                    text = battle.damage.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmberRed,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(2.dp))
                .border(1.dp, Line, RoundedCornerShape(2.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(if (livePose.tracked) EmberRed else Muted, CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text(livePose.feedback, style = MaterialTheme.typography.bodyMedium, color = Bone)
            livePose.elbowAngle?.let { angle ->
                Spacer(Modifier.width(10.dp))
                Text(
                    "${angle.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Keep shoulders and hands inside the frame.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )
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
            .background(Charcoal)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "CAMERA OFFLINE",
            style = MaterialTheme.typography.labelMedium,
            color = EmberRed,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "FitNex needs the camera to track your push-ups. Nothing is recorded or uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onRequest) {
            Text(
                "ENABLE CAMERA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Bone,
            )
        }
    }
}

@Composable
private fun TimeUpDialog(
    bossName: String,
    canExtend: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = Charcoal,
        title = {
            Text("TIME'S UP", style = MaterialTheme.typography.labelMedium, color = EmberRed)
        },
        text = {
            Text(
                if (canExtend) {
                    "The $bossName still stands. Take one more minute? The boss heals fully and " +
                        "the fight restarts."
                } else {
                    "Your minute is spent. The $bossName holds the gate."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
        },
        confirmButton = {
            if (canExtend) {
                TextButton(onClick = onAccept) {
                    Text("ONE MORE MINUTE", style = MaterialTheme.typography.labelSmall, color = EmberRed)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(
                    if (canExtend) "RETREAT" else "ACCEPT DEFEAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }
        },
    )
}
