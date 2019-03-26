package osll.thatsmyfish.game

import android.content.Context
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.ViewGroup
import osll.thatsmyfish.game.internal.*
import java.lang.IllegalStateException
import kotlin.math.ceil
import kotlin.math.max

/**
 * TODO: document your custom view class.
 */
class GameFieldView(context: Context,
                    val field: List<List<Tile>>,
                    val shape: Shape,
                    val size: Pair<Int, Int>
) : ViewGroup(context) {
    private val tiles: Map<Tile, TileView>
    private lateinit var tileSize: Size
    private var trigger: Trigger? = null
    private var triggerWatcher: TriggerWatcher? = null

    init {
        tiles = field.mapIndexed { i, row ->
            row.mapIndexed { j, tile ->
                tile to TileView(tile, (i + j) % 2 != 0, context)
            }
        }.flatten().toMap()

        val tileTapListener = createTileTapListener()
        for (i in 0 until size.second) {
            for (j in 0 until size.first) {
                val tile = field[i][j]
                val view = viewByTile(tile)

                addView(view)
                view.setOnClickListener(tileTapListener)
            }
        }
    }

    fun setTrigger(value: Trigger) {
        if (trigger != null) {
            throw IllegalStateException()
        }
        trigger = value
        triggerWatcher?.onTriggerSet(value)
    }

    fun setWatcherTrigger(value: TriggerWatcher) {
        triggerWatcher = value
    }

    private fun createTileTapListener() = object : OnClickListener {
        private var focusedTile: Tile? = null

        override fun onClick(view: View) {
            if (view !is TileView || trigger == null) {
                return
            }
            val currentTrigger = trigger
            val tile = view.tile

            if (currentTrigger is PenguinPlacedTrigger) {
                trigger = null
                currentTrigger.run(tile)
            } else if (currentTrigger is PenguinMovedTrigger) {
                for (tileView in tiles.values) {
                    tileView.setTint(false)
                }
                if (currentTrigger.player == tile.occupiedBy) {
                    focusedTile = tile
                    for (direction in 0 until shape.moveDirections) {
                        for (availableTile in tile.freeInDirection(direction)) {
                            viewByTile(availableTile).apply {
                                setTint(true)
                                postInvalidate()
                            }

                        }
                    }
                } else if (focusedTile != null) {
                    for (direction in 0 until shape.moveDirections) {
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

                        if (curTile == tile && tile.occupiedBy == null) {
                            trigger = null
                            currentTrigger.run(focusedTile!!, tile)
                            break
                        }
                    }
                    focusedTile = null
                }
            }
        }
    }

    fun viewByTile(tile: Tile): TileView = tiles.getValue(tile)

    private fun tileCoordinate(row: Int, column: Int): Pair<Float, Float> =
            when (shape) {
                Hexagon -> {
                    val x = 0.75f * tileSize.width * column
                    val y = if (column % 2 == 0) {
                        0.5f * tileSize.height
                    } else {
                        0f
                    } + row * tileSize.height

                    x to y
                }
                Square -> tileSize.width.toFloat() * column to tileSize.height.toFloat() * row
                Triangle -> tileSize.width / 2f * column to tileSize.height.toFloat() *
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
            when (shape) {
                Hexagon -> it / (size.first * 0.75f + 0.25f)
                Square -> it / size.first.toFloat()
                Triangle -> it / (size.first * 0.5f + 0.5f)
            }
        }

        val tileHSpec = h?.let {
            when (shape) {
                Hexagon -> if (size.first > 1) {
                    it / (size.second + 0.5f)
                } else {
                    it / size.second.toFloat()
                }
                Square -> it / size.second.toFloat()
                Triangle -> it / size.second.toFloat()
            }
        }

        tileSize = shape.fitInto(tileWSpec, tileHSpec).let {
            Size(it.width.toInt(), it.height.toInt())
        }

        for (tileView in tiles.values) {
            tileView.measure(
                    exactSpec(tileSize.width),
                    exactSpec(tileSize.height)
            )
        }

        val maxCoords = tileCoordinate(
                size.second - 1,
                size.first - 1
        ).let {
            if (size.first > 1) {
                val anotherIt = tileCoordinate(
                        size.second - 1,
                        size.first - 2
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
            for (i in 0 until field.size) {
                for (j in 0 until field[i].size) {
                    val tile = field[i][j]
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

interface Trigger {
    val player: Player
}

interface PenguinPlacedTrigger : Trigger {
    fun run(tile: Tile)
}

interface PenguinMovedTrigger : Trigger {
    fun run(from: Tile, to: Tile)
}

interface TriggerWatcher {
    fun onTriggerSet(trigger: Trigger)
}