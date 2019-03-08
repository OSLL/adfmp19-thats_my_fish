package osll.thatsmyfish.game

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.GridView
import com.github.florent37.shapeofview.ShapeOfView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import osll.thatsmyfish.game.internal.*
import kotlin.coroutines.resume
import kotlin.math.max

/**
 * TODO: document your custom view class.
 */
class GameFieldView(context: Context, val game: GameHandler) : GridLayout(context) {
    lateinit var turnInfoChannel: ReceiveChannel<TurnInfo>
    val tiles: Map<Tile, ShapeOfView>

    private var focusedTile: Tile? = null

    init {
        // TODO: resources
        tiles = game.tiles.mapIndexed { i, row ->
            row.mapIndexed { j, tile ->
                tile to createTileView(tile, context, (i + j) % 2 != 0)
            }
        }.flatten().toMap()

        rowCount = game.size.height
        columnCount = game.size.width

        for (i in 0 until game.size.height) {
            for (j in 0 until game.size.width) {
                val tile = game.tiles[i][j]
                val view = tiles.getValue(tile)

                addView(view)
                view.setOnTouchListener { _, event ->
                    if (event.action != ACTION_DOWN) {
                        return@setOnTouchListener false
                    }
                    Log.d("Tile", "[$i, $j] tapped")

                    val activePlayer = game.activePlayer

                    if (activePlayer is AsyncPlayer) {
                        if (activePlayer.waitingForPlacement != null) {
                            activePlayer.waitingForPlacement?.resume(tile)
                        } else if (activePlayer.waitingForMove != null) {
                            if (game.gameState == Running && game.activePlayer == tile.occupiedBy) {
                                focusedTile = tile
                            } else if (focusedTile != null) {
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

                                    if (steps > 1 && curTile == tile) {
                                        activePlayer.waitingForMove?.resume(Triple(focusedTile!!,
                                                direction, steps))
                                        break
                                    }
                                }
                                focusedTile = null
                            } else {
                                focusedTile = null
                            }
                        }
                        this@GameFieldView.postInvalidate()
                    }
                    true
                }
            }
        }

        if (game.shape is Hexagon) {
            game.tiles.last().forEachIndexed { j, tile ->
                if (j % 2 == 0) {
                    tile.sink()
                }
            }
            postInvalidate()
        }

        GlobalScope.launch {
            turnInfoChannel = produce(capacity = 1) {
                var finished = false

                while (!finished) {
                    val turnInfo = game.handleTurn()

                    finished = finished
                            || (turnInfo is PhaseStarted && turnInfo.gameState is Finished)

                    send(turnInfo)
                }

                this@GameFieldView.postInvalidate()
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            for (i in 0 until game.tiles.size) {
                for (j in 0 until game.tiles[i].size) {
                    val tile = game.tiles[i][j]
                    val tileView = tiles.getValue(tile)

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

    override fun dispatchDraw(canvas: Canvas) {
        for ((tile, tileView) in tiles) {
            if (tile.state == Tile.TileState.Sunken) {
                tileView.visibility = GONE
            }
        }
        super.dispatchDraw(canvas)
    }
}
