package osll.thatsmyfish.game.internal

import java.lang.IllegalArgumentException

class Tile(shape: Shape, val fishCount: Int) {
    private var initialized = false
    private val neighbours: Array<Tile?> = Array(shape.moveDirections) { null }
    var occupiedBy: Player? = null
        internal set

    internal fun initializeNeighbours(initialNeighbours: Array<Tile?>) {
        if (initialized) {
            throw IllegalStateException("tile is already initialized")
        }
        if (initialNeighbours.size != neighbours.size) {
            throw IllegalArgumentException("neighbour arrays' sizes differ")
        }

        for (i in 0 until initialNeighbours.size) {
            neighbours[i] = initialNeighbours[i]
        }
        initialized = true
    }

    fun getNeighbours() = neighbours.toList()

    fun getNeighbour(direction: Int): Tile? = neighbours[direction]

    private fun neighbourSank(tile: Tile) {
        when (val direction = neighbours.indexOf(tile)) {
            -1   -> return
            else -> neighbours[direction] = null
        }
    }

    internal fun sink() {
        for (neighbour in neighbours) {
            neighbour?.neighbourSank(this)
        }
    }
}