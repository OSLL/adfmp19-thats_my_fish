package osll.thatsmyfish.game.internal

import java.lang.IllegalArgumentException

class Tile(val shape: Shape, val fishCount: Int) {
    var state: TileState = TileState.New
        private set
    private val neighbours: Array<Tile?> = Array(shape.moveDirections) { null }
    var occupiedBy: Player? = null
        internal set

    internal fun initializeNeighbours(initialNeighbours: Array<Tile?>) {
        if (state != TileState.New) {
            throw IllegalStateException("tile is already initialized")
        }
        if (initialNeighbours.size != neighbours.size) {
            throw IllegalArgumentException("neighbour arrays' sizes differ")
        }

        for (i in 0 until initialNeighbours.size) {
            neighbours[i] = initialNeighbours[i]
        }
        state = TileState.Initialized
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
        state = TileState.Sunken
    }

    fun freeInDirection(direction: Int): Sequence<Tile> =
            generateSequence(this) {
                it.getNeighbour(direction)?.takeUnless { tile -> tile.occupiedBy != null }
            }.drop(1)

    sealed class TileState {
        object New : TileState()
        object Initialized : TileState()
        object Sunken : TileState()
    }
}