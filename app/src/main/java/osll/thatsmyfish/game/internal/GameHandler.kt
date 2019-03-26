package osll.thatsmyfish.game.internal

import android.util.Log
import android.view.View.INVISIBLE
import osll.thatsmyfish.game.GameFieldView
import osll.thatsmyfish.game.PenguinMovedTrigger
import osll.thatsmyfish.game.PenguinPlacedTrigger
import kotlin.random.Random

data class GameStatistics(
        val scores: List<Pair<AbstractPlayer, Int>>,
        val time: Long,
        var moves: Int,
        var totalMoves: Int
)


enum class MyGameState { Initialization, Game, Finished }

class GameHandler(
        val size: Pair<Int, Int>,
        val shape: Shape,
        val players: List<AbstractPlayer>,
        val initialPenguins: Int
) {
    var view: GameFieldView? = null

    val tiles: List<List<Tile>>

    var gameState: MyGameState = MyGameState.Initialization
        private set

    private val currentScores = HashMap<AbstractPlayer, Int>()

    private val penguinPositions = players.map {
        it to mutableSetOf<Tile>()
    }.toMap()

    var gameWatcher: GameWatcher? = null
    var moves = 0
    var totalMoves = 0
    var startTime = 0L

    val scores
        get() = currentScores.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }
                .toList()

    fun getScore(player: AbstractPlayer) = currentScores[player] ?: 0

    private val activePlayers = List(players.size) { it }.toMutableList()
    private var currentPlayer = 0

    val activePlayer
        get() = players.getOrNull(activePlayers.getOrNull(currentPlayer) ?: -1)

    init {
        tiles = List(size.second) {
            List(size.first) {
                Tile(shape, Random.nextInt(0, MAX_FISH_IN_TILE + 1))
            }
        }
        for (i in 0 until size.second) {
            for (j in 0 until size.first) {
                tiles[i][j].initializeNeighbours(
                        when (shape) {
                            Square -> arrayOf(
                                    (i - 1) to (j),
                                    (i) to (j + 1),
                                    (i + 1) to (j),
                                    (i) to (j - 1)
                            )
                            Hexagon -> {
                                val upper = j % 2 == 1
                                if (upper) {
                                    arrayOf(
                                            (i - 1) to (j),
                                            (i - 1) to (j + 1),
                                            (i) to (j + 1),
                                            (i + 1) to (j),
                                            (i) to (j - 1),
                                            (i - 1) to (j - 1)
                                    )
                                } else {
                                    arrayOf(
                                            (i - 1) to (j),
                                            (i) to (j + 1),
                                            (i + 1) to (j + 1),
                                            (i + 1) to (j),
                                            (i + 1) to (j - 1),
                                            (i) to (j - 1)
                                    )
                                }
                            }
                            Triangle -> {
                                val upper = (i + j) % 2 == 0
                                if (upper) {
                                    arrayOf(
                                            (i) to (j - 1),
                                            (i) to (j + 1),
                                            (i) to (j + 1),
                                            (i + 1) to (j),
                                            (i + 1) to (j),
                                            (i) to (j - 1)
                                    )
                                } else {
                                    arrayOf(
                                            (i - 1) to (j),
                                            (i - 1) to (j),
                                            (i) to (j + 1),
                                            (i) to (j + 1),
                                            (i) to (j - 1),
                                            (i) to (j - 1)
                                    )
                                }
                            }
                        }.map { (k, l) -> tiles.getOrNull(k)?.getOrNull(l) }.toTypedArray()
                )
            }
        }
    }

    fun start(gameView: GameFieldView) {
        view = gameView
        startTime = System.currentTimeMillis()
        skipBots()
    }

    private fun skipBots() {
        if (gameState == MyGameState.Initialization) {
            while (gameState == MyGameState.Initialization) {
                val player = players[currentPlayer]
                if (player is Player) {
                    gameWatcher?.playerWait(player)
                    view!!.setTrigger(object : PenguinPlacedTrigger {
                        override val player: Player = player
                        override fun run(tile: Tile) {
                            holdPenguinPlaced(player, tile)
                            nextPlayer()
                            skipBots()
                        }
                    })
                    return
                } else if (player is Bot) {
                    holdPenguinPlaced(player, player.placePenguin(tiles))
                    nextPlayer()
                }
            }
        }
        if (gameState == MyGameState.Game) {
            while (gameState == MyGameState.Game) {
                val player = players[currentPlayer]
                if (!checkCanMove(player)) {
                    nextPlayer()
                    continue
                }
                if (player is Player) {
                    view!!.setTrigger(object : PenguinMovedTrigger {
                        override val player: Player = player
                        override fun run(from: Tile, to: Tile) {
                            holdPenguinMoved(player, from, to)
                            nextPlayer()
                            skipBots()
                        }
                    })
                    return
                } else if (player is Bot) {
                    val step = player.movePenguin(penguinPositions[player]!!)
                    holdPenguinMoved(player, step.first, step.second)
                    nextPlayer()
                }
            }
        }
        if (gameState == MyGameState.Finished) {
            gameWatcher?.gameFinished(GameStatistics(scores,
                    System.currentTimeMillis() - startTime,
                    moves, totalMoves))
        }
    }

    fun holdPenguinPlaced(player: AbstractPlayer, tile: Tile) {
        currentScores[player] = (currentScores[player] ?: 0) + tile.fishCount
        tile.occupiedBy = activePlayer
        penguinPositions.getValue(player).add(tile)
        view!!.viewByTile(tile).postInvalidate()
        if (penguinPositions.all { it.value.size == initialPenguins }) {
            gameState = MyGameState.Game
        }
        gameWatcher?.scoreUpdate(currentScores)
    }

    fun holdPenguinMoved(player: AbstractPlayer, from: Tile, to: Tile) {
        currentScores[player] = (currentScores[player] ?: 0) + to.fishCount
        to.occupiedBy = activePlayer
        penguinPositions[player]?.remove(from)
        from.sink()
        to.occupiedBy = player
        penguinPositions[player]?.add(to)
        view!!.viewByTile(from).visibility = INVISIBLE
        view!!.viewByTile(from).postInvalidate()
        view!!.viewByTile(to).postInvalidate()
        totalMoves++
        if (player == players[0]) {
            moves++
        }
        if (players.none(this@GameHandler::checkCanMove)) {
            gameState = MyGameState.Finished
        }
    }

    fun nextPlayer() {
        currentPlayer = (currentPlayer + 1) % players.size
    }


    private fun checkCanMove(player: AbstractPlayer): Boolean {
        return penguinPositions[player]?.any { tile ->
            tile.getNeighbours().any { it != null && it.occupiedBy == null }
        } ?: false
    }

    interface GameWatcher {
        fun scoreUpdate(scores: Map<AbstractPlayer, Int>)
        fun gameFinished(gameStatistics: GameStatistics)
        fun playerWait(player: AbstractPlayer)
    }

    companion object {
        const val MAX_FISH_IN_TILE = 3
    }
}