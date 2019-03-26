package osll.thatsmyfish.game.internal

import kotlinx.coroutines.delay
import kotlin.random.Random

data class Bot(override val name: String, override val color: Int) : AbstractPlayer() {
    fun movePenguin(penguinPositions: Collection<Tile>): Pair<Tile, Tile> {
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

        var curTile: Tile? = penguinTile.getNeighbour(direction)
        val way = ArrayList<Tile>()
        while (curTile != null && curTile.occupiedBy == null) {
            way.add(curTile)
            curTile = curTile.getNeighbour(direction)
        }

        val step = Random.nextInt(way.size)

        return Pair(penguinTile, way[step])
    }

    fun placePenguin(field: List<List<Tile>>): Tile {
        val availableTiles = field.flatMap { it.filter { tile -> isFree(tile) } }

        val chosenTile = availableTiles[Random.nextInt(0, availableTiles.size)]

        return chosenTile
    }

    private fun isFree(tile: Tile?) = tile != null && tile.occupiedBy == null

    private fun <T> choose(list: List<T>) = list[Random.nextInt(0, list.size)]

    companion object {
        const val TURN_DELAY_MS = 500L
    }
}