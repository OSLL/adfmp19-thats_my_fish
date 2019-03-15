package osll.thatsmyfish

import android.graphics.Color
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import osll.thatsmyfish.game.internal.*
import kotlin.random.Random

/**
 * Test player, which places its only penguin into a given position
 * and then tries to move it in a predefined way
 */
private class TestPlayer(
        val initialPosition: Pair<Int, Int>,
        val moveDirection: Pair<Int, Int>
) : Player() {
    override val name = "Test"
    override val color = Color.YELLOW

    lateinit var tileWithPenguin: Tile
        private set

    override suspend fun placePenguin(field: List<List<Tile>>) =
            field[initialPosition.first][initialPosition.second].also {
                tileWithPenguin = it
            }

    override suspend fun movePenguin(penguinPositions: List<Tile>): Triple<Tile, Int, Int> {
        assert(tileWithPenguin in penguinPositions)

        return Triple(tileWithPenguin, moveDirection.first, moveDirection.second)
    }
}

/**
 * Test player, which places its only penguin into a given position
 * and then tries to move it into an occupied cell
 */
private class TestOccupiedPlayer(
        val initialPosition: Pair<Int, Int>
) : Player() {
    override val name = "Test #2"
    override val color = Color.YELLOW

    lateinit var tileWithPenguin: Tile
        private set

    override suspend fun placePenguin(field: List<List<Tile>>) =
            field[initialPosition.first][initialPosition.second].also {
                tileWithPenguin = it
            }

    override suspend fun movePenguin(penguinPositions: List<Tile>): Triple<Tile, Int, Int> {
        assert(tileWithPenguin in penguinPositions)
        assert(tileWithPenguin.getNeighbours().any { (it ?: return@any false).occupiedBy != null })

        for (direction in 0 until tileWithPenguin.shape.moveDirections) {
            val toTile = tileWithPenguin.getNeighbour(direction)
            if (toTile?.occupiedBy != null) {
                return Triple(tileWithPenguin, direction, 1)
            }
        }

        throw IllegalStateException() // should be unreachable
    }
}

class GameHandlerTest {
    private fun testScenario(
            shape: Shape,
            direction: Int,
            count: Int,
            expectedPlacementOutcome: (Player) -> TurnInfo,
            expectedMoveOutcome: (Player) -> TurnInfo
    ) {
        val player = TestPlayer(
                1 to 1,
                direction to count
        )

        val gameHandler = GameHandler(
                3 to 3,
                shape,
                listOf(player),
                1
        )

        runBlocking {
            gameHandler.handleTurn().also {
                assertEquals(expectedPlacementOutcome(player), it)
            }

            gameHandler.handleTurn().also {
                assertEquals(PhaseStarted(Running), it)
            }

            gameHandler.handleTurn().also {
                assertEquals(expectedMoveOutcome(player), it)
            }
        }
    }

    private fun randomShape() = when (Random.nextInt(3)) {
        0 -> Hexagon
        1 -> Square
        2 -> Triangle
        else -> throw IllegalStateException() // should be unreachable
    }

    @Test
    fun testPossibleTurn() {
        val shape = randomShape()

        for (direction in 0 until shape.moveDirections) {
            testScenario(
                    shape,
                    direction,
                    1,
                    { player: Player ->
                        PenguinPlaced(player, (player as TestPlayer).tileWithPenguin)
                    },
                    { player: Player ->
                        (player as TestPlayer).run {
                            val toTile = tileWithPenguin.getNeighbour(direction)
                            assertNotNull(toTile)

                            PenguinMoved(
                                    this,
                                    tileWithPenguin,
                                    toTile!!
                            )
                        }
                    }
            )
        }
    }

    @Test
    fun testOutOfBoundsTurn() {
        val shape = randomShape()

        for (direction in 0 until shape.moveDirections) {
            testScenario(
                    shape,
                    direction,
                    3,
                    { player: Player ->
                        PenguinPlaced(player, (player as TestPlayer).tileWithPenguin)
                    },
                    { InvalidTurn }
            )
        }
    }

    @Test
    fun testOccupiedTurn() {
        val shape = randomShape()

        val player = TestOccupiedPlayer(1 to 1)

        val fillerPlayer = TestOccupiedPlayer(1 to 0)

        val allPlayers = listOf(player, fillerPlayer)

        val gameHandler = GameHandler(
                3 to 3,
                shape,
                allPlayers,
                1
        )

        runBlocking {
            for (somePlayer in allPlayers) {
                gameHandler.handleTurn().also {
                    assertEquals(PenguinPlaced(somePlayer, somePlayer.tileWithPenguin), it)
                }
            }

            gameHandler.handleTurn().also {
                assertEquals(PhaseStarted(Running), it)
            }

            gameHandler.handleTurn().also {
                assertEquals(InvalidTurn, it)
            }
        }
    }

    @Test
    fun testScoreUpdating() {
        val shape = randomShape()
        val player = Bot("Test", Color.YELLOW)

        var score = 0

        val gameHandler = GameHandler(
                3 to 3,
                shape,
                listOf(player),
                1
        )

        tailrec suspend fun handleGame() {
            when (val turnResult = gameHandler.handleTurn()) {
                is PenguinPlaced -> {
                    score += turnResult.tile.fishCount
                    assertEquals(gameHandler.getScore(player), score)
                }
                is PenguinMoved -> {
                    score += turnResult.toTile.fishCount
                    assertEquals(gameHandler.getScore(player), score)
                }
                is PhaseStarted -> when (val newState = turnResult.gameState) {
                    is Finished -> {
                        assertEquals(
                                player to score,
                                newState.gameStats.scores.single()
                        )
                        return
                    }
                }
            }

            handleGame()
        }

        runBlocking {
            handleGame()
        }
    }
}
