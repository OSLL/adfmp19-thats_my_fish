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
data class PhaseStarted(val gameState: GameState): TurnInfo()
data class PenguinPlaced(val player: Player, val tile: Tile): TurnInfo()
data class PenguinMoved(val player: Player, val fromTile: Tile, val toTile: Tile): TurnInfo()
data class PlayerFinished(val player: Player): TurnInfo()

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
        get() = players.getOrNull(activePlayers.getOrNull(currentPlayer) ?: -1)

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
                            Hexagon -> {
                                val upper = j % 2 == 1
                                if (upper) {
                                    arrayOf(
                                            (i - 1) to (j),
                                            (i - 1) to (j + 1),
                                            (i)     to (j + 1),
                                            (i + 1) to (j),
                                            (i)     to (j - 1),
                                            (i - 1) to (j - 1)
                                    )
                                } else {
                                    arrayOf(
                                            (i - 1) to (j),
                                            (i)     to (j + 1),
                                            (i + 1) to (j + 1),
                                            (i + 1) to (j),
                                            (i + 1) to (j - 1),
                                            (i)     to (j - 1)
                                    )
                                }
                            }
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

    private suspend fun handlePenguinPlacement(): TurnInfo {
        if (penguinPositions.getValue(activePlayer!!).size == PENGUINS_AVAILABLE) {
            gameState = Running

            return PhaseStarted(gameState)
        } else {
            val chosenTile = activePlayer!!.placePenguin(tiles)

            return if (chosenTile.occupiedBy == null) {
                currentScores[activePlayers[currentPlayer]] += chosenTile.fishCount
                chosenTile.occupiedBy = activePlayer
                penguinPositions.getValue(activePlayer!!).add(chosenTile)

                PenguinPlaced(activePlayer!!, chosenTile)
            } else {
                InvalidTurn
            }
        }
    }

    private suspend fun handlePenguinMovement(): TurnInfo {
        val playerPenguins by lazy {
            penguinPositions.getValue(activePlayer!!)
        }
        val noTurnsExist by lazy {
            playerPenguins.all {
                tile -> tile.getNeighbours().all { it == null || it.occupiedBy != null }
            }
        }

        when {
            activePlayers.isEmpty() -> {
                gameState = Finished(GameStats(
                        scores
                ))

                return PhaseStarted(gameState)
            }
            noTurnsExist ->
                return PlayerFinished(activePlayer!!)
            else -> {
                val (tile, direction, count) = activePlayer!!.movePenguin(playerPenguins.toList())
                if (tile in playerPenguins) {
                    val jumpedTiles = tile.freeInDirection(direction).take(count).toList()
                    if (jumpedTiles.size == count && jumpedTiles.all { it.occupiedBy == null }) {
                        val toTile = jumpedTiles.last()

                        currentScores[activePlayers[currentPlayer]] += toTile.fishCount
                        playerPenguins.remove(tile)
                        tile.sink()
                        toTile.occupiedBy = activePlayer
                        playerPenguins.add(toTile)

                        return PenguinMoved(activePlayer!!, tile, toTile)
                    }
                }
            }
        }
        return InvalidTurn
    }

    suspend fun handleTurn(): TurnInfo {
        val result = when (gameState) {
            InitialPlacement -> handlePenguinPlacement()
            Running          -> handlePenguinMovement()
            is Finished      -> PhaseStarted(gameState)
        }

        when (result) {
            is InvalidTurn    -> {}
            is PhaseStarted   -> {}
            is PlayerFinished -> {
                activePlayers.removeAt(currentPlayer)
                if (activePlayers.isNotEmpty()) {
                    currentPlayer %= activePlayers.size
                }
            }
            else              -> {
                currentPlayer = (currentPlayer + 1) % activePlayers.size
            }
        }

        return result
    }

    companion object {
        const val MAX_FISH_IN_TILE = 3
        const val PENGUINS_AVAILABLE = 1
    }
}