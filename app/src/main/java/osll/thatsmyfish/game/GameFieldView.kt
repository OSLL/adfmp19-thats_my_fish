package osll.thatsmyfish.game

import android.content.Context
import android.view.View
import android.widget.GridLayout
import osll.thatsmyfish.game.internal.*
import kotlin.coroutines.resume

/**
 * TODO: document your custom view class.
 */
class GameFieldView(context: Context, val game: GameHandler) : GridLayout(context) {
    private val tiles: Map<Tile, TileView>

    init {
        tiles = game.tiles.mapIndexed { i, row ->
            row.mapIndexed { j, tile ->
                tile to TileView(tile, (i + j) % 2 != 0, context)
            }
        }.flatten().toMap()

        rowCount = game.size.height
        columnCount = game.size.width

        val tileTapListener = createTileTapListener()
        for (i in 0 until game.size.height) {
            for (j in 0 until game.size.width) {
                val tile = game.tiles[i][j]
                val view = viewByTile(tile)

                addView(view)
                view.setOnClickListener(tileTapListener)
            }
        }

        if (game.shape is Hexagon) {
            game.tiles.last().forEachIndexed { j, tile ->
                if (j % 2 == 0) {
                    viewByTile(tile).visibility = View.INVISIBLE
                    tile.sink()
                }
            }
            postInvalidate()
        }
    }

    private fun createTileTapListener() = object : OnClickListener {
        private var focusedTile: Tile? = null

        override fun onClick(view: View) {
            if (view !is TileView || game.gameState is Finished) {
                return
            }

            val tile = view.tile
            val activePlayer = game.activePlayer

            if (activePlayer is AsyncPlayer) {
                if (activePlayer.waitingForPlacement != null) {
                    activePlayer.waitingForPlacement?.resume(tile)
                } else if (activePlayer.waitingForMove != null) {
                    for (tileView in tiles.values) {
                        tileView.setTint(false)
                    }
                    if (game.activePlayer == tile.occupiedBy) {
                        focusedTile = tile
                        for (direction in 0 until game.shape.moveDirections) {
                            for (availableTile in tile.freeInDirection(direction)) {
                                viewByTile(availableTile).apply {
                                    setTint(true)
                                    postInvalidate()
                                }

                            }
                        }
                    } else {
                        if (focusedTile != null) {
                            for (direction in 0 until game.shape.moveDirections) {
                                var steps = 0
                                var curTile: Tile? = focusedTile
                                do {
                                    ++steps
                                    curTile = curTile?.getNeighbour(direction)
                                } while (
                                        curTile != tile
                                        && curTile != null
                                        && curTile.occupiedBy == null
                                )

                                if (curTile == tile) {
                                    activePlayer.waitingForMove?.resume(Triple(focusedTile!!,
                                            direction, steps))
                                    break
                                }
                            }
                        }
                        focusedTile = null
                    }
                }
            }
        }
    }

    fun viewByTile(tile: Tile): TileView = tiles.getValue(tile)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            for (i in 0 until game.tiles.size) {
                for (j in 0 until game.tiles[i].size) {
                    val tile = game.tiles[i][j]
                    val tileView = viewByTile(tile)

                    when (game.shape) {
                        Hexagon -> {
                            if (j % 2 == 0) {
                                tileView.y += 0.5f * tileView.height
                            }
                        }
                    }
                }
            }
        }
    }
}
