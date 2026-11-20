package com.fitnex.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutRecord(
    val battleId: String,
    val bossId: String,
    val bossName: String,
    val pushUps: Int,
    val damage: Int,
    val secondsUsed: Int,
    val victory: Boolean,
    val timestampMillis: Long,
)

/** Persisted player progress. Every field has a default so older saves still load. */
@Serializable
data class PlayerState(
    val level: Int = 1,
    val lifetimePushUps: Long = 0L,
    val bossesDefeated: Int = 0,
    val currentBossIndex: Int = 0,
    val defeatedBossIds: List<String> = emptyList(),
    val rewardedBattleIds: List<String> = emptyList(),
    val history: List<WorkoutRecord> = emptyList(),
    val unlockedTitleIds: List<String> = emptyList(),
    val equippedTitleId: String? = null,
    val battleAttempts: Int = 0,
    val battleWins: Int = 0,
    val bestPushUpsInBattle: Int = 0,
) {
    val rank: Rank get() = Rank.forLevel(level)
    val currentBoss: Boss get() = BossCatalog.byIndex(currentBossIndex)

    /** Highest boss ever beaten, or null before the first victory. */
    val highestBossDefeated: Boss?
        get() = BossCatalog.all.lastOrNull { it.id in defeatedBossIds }

    val equippedTitle: Title?
        get() = TitleCatalog.byId(equippedTitleId)?.takeIf { it.id in unlockedTitleIds }

    val unlockedTitles: List<Title>
        get() = TitleCatalog.all.filter { it.id in unlockedTitleIds }

    val battlesLost: Int get() = (battleAttempts - battleWins).coerceAtLeast(0)
}

data class BattleResult(
    val battleId: String,
    val boss: Boss,
    val pushUps: Int,
    val damage: Int,
    val secondsUsed: Int,
    val victory: Boolean,
    val timestampMillis: Long = System.currentTimeMillis(),
)

/**
 * Applies a finished battle exactly once. A battle id that has already been
 * rewarded can never grant another level, no matter how often this runs.
 *
 * Attempt counters are updated for every battle (win or loss) because history
 * is capped, while the totals must never shrink.
 */
fun PlayerState.applyBattleResult(result: BattleResult): PlayerState {
    if (result.battleId in rewardedBattleIds) return this

    val record = WorkoutRecord(
        battleId = result.battleId,
        bossId = result.boss.id,
        bossName = result.boss.name,
        pushUps = result.pushUps,
        damage = result.damage,
        secondsUsed = result.secondsUsed,
        victory = result.victory,
        timestampMillis = result.timestampMillis,
    )

    val base = copy(
        rewardedBattleIds = (rewardedBattleIds + result.battleId).takeLast(MAX_REMEMBERED_BATTLES),
        history = (listOf(record) + history).take(MAX_HISTORY),
        battleAttempts = battleAttempts + 1,
        bestPushUpsInBattle = maxOf(bestPushUpsInBattle, result.pushUps),
    )

    if (!result.victory) return base

    val earnedTitle = TitleCatalog.forBoss(result.boss.id)
    return base.copy(
        level = level + 1,
        bossesDefeated = bossesDefeated + 1,
        battleWins = battleWins + 1,
        currentBossIndex = (currentBossIndex + 1).coerceAtMost(BossCatalog.all.lastIndex),
        defeatedBossIds = (defeatedBossIds + result.boss.id).distinct(),
        unlockedTitleIds = if (earnedTitle != null && earnedTitle.id !in unlockedTitleIds) {
            unlockedTitleIds + earnedTitle.id
        } else {
            unlockedTitleIds
        },
    )
}

/** Equips an unlocked title, or clears it with null. Locked ids are ignored. */
fun PlayerState.equipTitle(titleId: String?): PlayerState = when {
    titleId == null -> copy(equippedTitleId = null)
    titleId in unlockedTitleIds -> copy(equippedTitleId = titleId)
    else -> this
}

private const val MAX_HISTORY = 40
private const val MAX_REMEMBERED_BATTLES = 100
