package osll.thatsmyfish.game.internal

import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

data class AsyncPlayer(override val name: String, override val color: Int) : Player() {
    var waitingForMove: Continuation<Triple<Tile, Int, Int>>? = null
        private set
    var waitingForPlacement: Continuation<Tile>? = null
        private set

    override suspend fun movePenguin(penguinPositions: List<Tile>): Triple<Tile, Int, Int> {
        val result = suspendCoroutine<Triple<Tile, Int, Int>> { continuation ->
            waitingForMove = continuation
        }

        waitingForMove = null
        return result
    }

    override suspend fun placePenguin(field: List<List<Tile>>): Tile {
        val result = suspendCoroutine<Tile> { continuation ->
            waitingForPlacement = continuation
        }

        waitingForPlacement = null
        return result
    }
}