package osll.thatsmyfish.game.internal

import android.util.Size
import kotlin.random.Random

data class GameStats(
        val scores: List<Pair<Player,Int>>
)

sealed class GameState
object InitialPlacement : GameState()
object Running : GameState()
data class Finished(val gameStats: GameStats) : GameState()

sealed class TurnInfo
object InvalidTurn: TurnInfo()
class PhaseStarted(val gameState: GameState): TurnInfo()
class PenguinPlaced(val player: Player, val tile: Tile): TurnInfo()
class PenguinMoved(val player: Player, val fromTile: Tile, val toTile: Tile): TurnInfo()
class TurnPassed(val player: Player): TurnInfo()
class PlayerFinished(val player: Player): TurnInfo()

class GameHandler(
        val size: Size,
        val shape: Shape,
        val players: List<Player>
) {
    val tiles: List<List<Tile>>

    var gameState: GameState = InitialPlacement
        private set
    private val currentScores = Array(players.size) { 0 }
    private val penguinPositions = players.map {
        it to mutableSetOf<Tile>()
    }.toMap()

    val scores
        get() = players.zip(currentScores).sortedByDescending { it.second }

    fun getScore(player: Player) = currentScores[players.indexOf(player)]

    private val activePlayers = List(players.size) { it }.toMutableList()
    private var currentPlayer = 0

    val activePlayer
        get() = players[activePlayers[currentPlayer]]

    init {
        tiles = List(size.height) {
            List(size.width) {
                Tile(shape, Random.nextInt(0, MAX_FISH_IN_TILE + 1))
            }
        }
        for (i in 0 until size.height) {
            for (j in 0 until size.width) {
                tiles[i][j].initializeNeighbours(
                        when (shape) {
                            Rectangle -> arrayOf(
                                    (i - 1) to (j),
                                    (i)     to (j + 1),
                                    (i + 1) to (j),
                                    (i)     to (j - 1)
                            )
                            Hexagon -> arrayOf(
                                    (i - 1) to (j),
                                    (i)     to (j + 1),
                                    (i + 1) to (j + 1),
                                    (i + 1) to (j),
                                    (i + 1) to (j - 1),
                                    (i)     to (j - 1)
                            )
                            Triangle -> {
                                val upper = (i + j) % 2 == 0
                                if (upper) {
                                    arrayOf(
                                            (i)     to (j - 1),
                                            (i)     to (j + 1),
                                            (i)     to (j + 1),
                                            (i + 1) to (j),
                                            (i + 1) to (j),
                                            (i)     to (j - 1)
                                    )
                                } else {
                                    arrayOf(
                                            (i - 1) to (j),
                                            (i - 1) to (j),
                                            (i)     to (j + 1),
                                            (i)     to (j + 1),
                                            (i)     to (j - 1),
                                            (i)     to (j - 1)
                                    )
                                }
                            }
                        }.map { (k, l) -> tiles.getOrNull(k)?.getOrNull(l) }.toTypedArray()
                )
            }
        }
    }

    suspend fun handleTurn(): TurnInfo {
        val result = when (gameState) {
            InitialPlacement -> {
                if (penguinPositions.getValue(activePlayer).size == PENGUINS_AVAILABLE) {
                    gameState = Running

                    return PhaseStarted(gameState)
                }

                val chosenTile = activePlayer.placePenguin(tiles)
                return if (chosenTile.occupiedBy == null) {
                    currentScores[activePlayers[currentPlayer]] += chosenTile.fishCount
                    chosenTile.occupiedBy = activePlayer
                    penguinPositions.getValue(activePlayer).add(chosenTile)

                    PenguinPlaced(activePlayer, chosenTile)
                } else {
                    InvalidTurn
                }
            }
            Running -> {
                when {
                    activePlayers.isEmpty() -> {
                        gameState = Finished(GameStats(
                                scores
                        ))

                        PhaseStarted(gameState)
                    }
                    penguinPositions.getValue(activePlayer).all { tile ->
                        tile.getNeighbours().all { it == null }
                    } -> PlayerFinished(activePlayer)
                    penguinPositions.getValue(activePlayer).all { tile ->
                        tile.getNeighbours().all { it == null || it.occupiedBy != null }
                    } -> TurnPassed(activePlayer)
                    else -> {
                        val (tile, direction, count) = activePlayer.movePenguin(
                                penguinPositions.getValue(activePlayer).toList()
                        )
                        val jumpedTiles = generateSequence(tile) { it.getNeighbour(direction) }
                                .drop(1)
                                .take(count - 1)
                                .toList()
                        if (tile in penguinPositions.getValue(activePlayer) &&
                                jumpedTiles.size == count - 1 &&
                                jumpedTiles.all { it.occupiedBy == null }
                        ) {
                            val toTile = jumpedTiles.last()

                            currentScores[activePlayers[currentPlayer]] += toTile.fishCount
                            penguinPositions.getValue(activePlayer).remove(tile)
                            tile.sink()
                            penguinPositions.getValue(activePlayer).add(toTile)

                            PenguinMoved(activePlayer, tile, toTile)
                        } else {
                            InvalidTurn
                        }
                    }
                }


            }
            is Finished -> PhaseStarted(gameState)
        }

        when (result) {
            is InvalidTurn    -> {}
            is PhaseStarted   -> {}
            is PlayerFinished -> {
                activePlayers.removeAt(currentPlayer)
                currentPlayer %= activePlayers.size
            }
            else              -> {
                currentPlayer = (currentPlayer + 1) % activePlayers.size
            }
        }

        return result
    }

    companion object {
        const val MAX_FISH_IN_TILE = 3
        const val PENGUINS_AVAILABLE = 5
    }
}