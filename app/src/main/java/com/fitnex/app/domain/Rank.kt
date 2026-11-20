package com.fitnex.app.domain

/** Hero rank, kept entirely separate from the boss title. */
enum class Rank(val title: String, val minLevel: Int) {
    NO_RANK("No Rank", 1),
    C_CLASS("C-Class Hero", 3),
    B_CLASS("B-Class Hero", 6),
    A_CLASS("A-Class Hero", 10),
    S_CLASS("S-Class Hero", 15);

    companion object {
        fun forLevel(level: Int): Rank = entries.last { level >= it.minLevel }
    }
}
