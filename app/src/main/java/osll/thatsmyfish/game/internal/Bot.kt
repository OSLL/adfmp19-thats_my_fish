package osll.thatsmyfish.game.internal

import kotlin.random.Random

class Bot(override val name: String, override val color: Int) : Player() {
    override suspend fun movePenguin(penguinPositions: List<Tile>): Triple<Tile, Int, Int> {
        val penguinTile = choose(
                penguinPositions.filter {
                    val neighbours = it.getNeighbours()
                    neighbours.any { tile -> tile != null && tile.occupiedBy == null }
                }
        )

        val direction = choose(
                List(penguinTile.shape.moveDirections) { it }.filter {
                    isFree(penguinTile.getNeighbour(it))
                }
        )

        var maxSteps = 0
        var curTile: Tile? = penguinTile
        do {
            ++maxSteps
            curTile = curTile!!.getNeighbour(direction)
        } while (isFree(curTile))

        val count = Random.nextInt(1, maxSteps)

        return Triple(penguinTile, direction, count)
    }

    override suspend fun placePenguin(field: List<List<Tile>>): Tile {
        val availableTiles = field.flatMap { it.filter { tile -> isFree(tile) } }

        return availableTiles[Random.nextInt(0, availableTiles.size)]
    }

    private fun isFree(tile: Tile?) = tile != null && tile.occupiedBy == null

    private fun <T> choose(list: List<T>) = list[Random.nextInt(0, list.size)]
}