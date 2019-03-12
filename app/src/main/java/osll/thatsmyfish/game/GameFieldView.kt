package osll.thatsmyfish.game

import android.content.Context
import android.util.Size
import android.util.SizeF
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup
import osll.thatsmyfish.game.internal.*
import kotlin.coroutines.resume
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * TODO: document your custom view class.
 */
class GameFieldView(context: Context, val game: GameHandler) : ViewGroup(context) {
    private val tiles: Map<Tile, TileView>
    private lateinit var tileSize: Size

    init {
        tiles = game.tiles.mapIndexed { i, row ->
            row.mapIndexed { j, tile ->
                tile to TileView(tile, (i + j) % 2 != 0, context)
            }
        }.flatten().toMap()

        val tileTapListener = createTileTapListener()
        for (i in 0 until game.size.second) {
            for (j in 0 until game.size.first) {
                val tile = game.tiles[i][j]
                val view = viewByTile(tile)

                addView(view)
                view.setOnClickListener(tileTapListener)
            }
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

    private fun tileCoordinate(row: Int, column: Int): Pair<Float, Float> =
            when (game.shape) {
                Hexagon   -> {
                    val x = 0.75f * tileSize.width * column
                    val y = if (column % 2 == 0) {
                        0.5f * tileSize.height
                    } else {
                        0f
                    } + row * tileSize.height

                    x to y
                }
                Square -> tileSize.width.toFloat() * column to tileSize.height.toFloat() * row
                Triangle  -> tileSize.width / 2f * column to tileSize.height.toFloat() *
                        row
            }

    private fun exactSpec(value: Int) =
            MeasureSpec.makeMeasureSpec(value, MeasureSpec.EXACTLY)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = if (MeasureSpec.getMode(widthMeasureSpec) == UNSPECIFIED) {
            null
        } else {
            MeasureSpec.getSize(widthMeasureSpec)
        }

        val h = if (MeasureSpec.getMode(heightMeasureSpec) == UNSPECIFIED) {
            null
        } else {
            MeasureSpec.getSize(heightMeasureSpec)
        }

        val tileWSpec = w?.let {
            when (game.shape) {
                Hexagon -> it / (game.size.first * 0.75f + 0.25f)
                Square -> it / game.size.first.toFloat()
                Triangle -> it / (game.size.first * 0.5f + 0.5f)
            }
        }

        val tileHSpec = h?.let {
            when (game.shape) {
                Hexagon -> if (game.size.first > 1) {
                    it / (game.size.second + 0.5f)
                } else {
                    it / game.size.second.toFloat()
                }
                Square -> it / game.size.second.toFloat()
                Triangle -> it / game.size.second.toFloat()
            }
        }

        tileSize = game.shape.fitInto(tileWSpec, tileHSpec).let {
            Size(it.width.toInt(), it.height.toInt())
        }

        for (tileView in tiles.values) {
            tileView.measure(
                    exactSpec(tileSize.width),
                    exactSpec(tileSize.height)
            )
        }

        val maxCoords = tileCoordinate(
                game.size.second - 1,
                game.size.first - 1
        ).let {
            if (game.size.first > 1) {
                val anotherIt = tileCoordinate(
                        game.size.second - 1,
                        game.size.first - 2
                )

                max(it.first, anotherIt.first) to max(it.second, anotherIt.second)
            } else {
                it
            }
        }

        super.onMeasure(
                exactSpec(ceil(maxCoords.first + tileSize.width).toInt()),
                exactSpec(ceil(maxCoords.second + tileSize.height).toInt())
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            for (i in 0 until game.tiles.size) {
                for (j in 0 until game.tiles[i].size) {
                    val tile = game.tiles[i][j]
                    val tileView = viewByTile(tile)

                    val coords = tileCoordinate(i, j)

                    tileView.layout(l, t, l + tileSize.width, t + tileSize.height)

                    tileView.x = coords.first
                    tileView.y = coords.second
                }
            }
        }
    }
}
