package io.github.homchom.recode.multiplayer.state

import io.github.homchom.recode.multiplayer.DIAMOND_CHAR

sealed interface Rank {
    val displayName: String
}

enum class DonorRank(override val displayName: String) : Rank {
    NOBLE("Noble"),
    EMPEROR("Emperor"),
    MYTHIC("Mythic"),
    OVERLORD("${DIAMOND_CHAR}Overlord$DIAMOND_CHAR")
}