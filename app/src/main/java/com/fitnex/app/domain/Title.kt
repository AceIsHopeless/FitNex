package com.fitnex.app.domain

/**
 * An equippable title, unlocked by defeating its boss for the first time.
 * Titles are separate from [Rank]: a rank tracks level, a title records a kill.
 */
data class Title(
    val id: String,
    val name: String,
    val bossId: String,
    val description: String,
)

object TitleCatalog {
    val slimeSlayer = Title(
        id = "slime_slayer",
        name = "Slime Slayer",
        bossId = BossCatalog.slime.id,
        description = "First blood against the rot.",
    )
    val zombieBreaker = Title(
        id = "zombie_breaker",
        name = "Zombie Breaker",
        bossId = BossCatalog.zombie.id,
        description = "You outlasted the thing that never tires.",
    )
    val skeletonCrusher = Title(
        id = "skeleton_crusher",
        name = "Skeleton Crusher",
        bossId = BossCatalog.skeleton.id,
        description = "Old bones, broken armor, no mercy.",
    )
    val vampireHunter = Title(
        id = "vampire_hunter",
        name = "Vampire Hunter",
        bossId = BossCatalog.vampire.id,
        description = "The night takes nothing from you now.",
    )
    val darkKnightsBane = Title(
        id = "dark_knights_bane",
        name = "Dark Knight's Bane",
        bossId = BossCatalog.darkKnight.id,
        description = "The last gate opened for you.",
    )

    val all: List<Title> = listOf(
        slimeSlayer,
        zombieBreaker,
        skeletonCrusher,
        vampireHunter,
        darkKnightsBane,
    )

    fun forBoss(bossId: String): Title? = all.firstOrNull { it.bossId == bossId }

    fun byId(id: String?): Title? = all.firstOrNull { it.id == id }
}
