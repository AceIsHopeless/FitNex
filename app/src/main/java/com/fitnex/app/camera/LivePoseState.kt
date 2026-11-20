package com.fitnex.app.camera

import com.fitnex.app.domain.RepPhase

/** Snapshot of the tracker for on-screen feedback. */
data class LivePoseState(
    val tracked: Boolean = false,
    val feedback: String = "Get into position",
    val phase: RepPhase = RepPhase.IDLE,
    val elbowAngle: Float? = null,
)
