package com.fitnex.app.domain

/** Boss encounters of the campaign ladder. */
data class Boss(
    val id: String,
    val name: String,
    val hp: Int,
    val timeLimitSeconds: Int,
    val lore: String,
    val isFinal: Boolean = false,
)

object BossCatalog {
    val slime = Boss(
        id = "slime",
        name = "Slime",
        hp = 20,
        timeLimitSeconds = 120,
        lore = "A shivering mass of rot. Easy prey for a hero who can drop low.",
    )
    val zombie = Boss(
        id = "zombie",
        name = "Zombie",
        hp = 40,
        timeLimitSeconds = 180,
        lore = "It does not tire. It does not stop. Neither should you.",
    )
    val skeleton = Boss(
        id = "skeleton",
        name = "Skeleton",
        hp = 60,
        timeLimitSeconds = 240,
        lore = "Bones bound by old spite. Break the armor of its ribs.",
    )
    val vampire = Boss(
        id = "vampire",
        name = "Vampire",
        hp = 80,
        timeLimitSeconds = 300,
        lore = "It drinks the strength of the hesitant. Move with intent.",
    )
    val darkKnight = Boss(
        id = "dark_knight",
        name = "Dark Knight",
        hp = 100,
        timeLimitSeconds = 360,
        lore = "The last gate. One hundred heartbeats of war stand between you and the throne.",
        isFinal = true,
    )

    val all: List<Boss> = listOf(slime, zombie, skeleton, vampire, darkKnight)

    fun byIndex(index: Int): Boss = all[index.coerceIn(0, all.lastIndex)]

    fun indexOf(boss: Boss): Int = all.indexOfFirst { it.id == boss.id }.coerceAtLeast(0)
}
