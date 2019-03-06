package osll.thatsmyfish.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import osll.thatsmyfish.game.internal.*
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.sin

/**
 * TODO: document your custom view class.
 */
class GameFieldView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
    : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    lateinit var game: GameHandler
        private set
    lateinit var turnInfoChannel: ReceiveChannel<TurnInfo>

    private var tiles: Map<Tile, TileView> = mapOf()
    private var focusedTile: Tile? = null

    var tileSide = 0f

    fun init(gameHandler: GameHandler) {
        game = gameHandler
        tileSide = game.shape.fitIn(30f, 30f) // TODO: resources
        tiles.values.forEach { it.visibility = GONE }
        tiles = game.tiles.flatMap { it.map { tile -> tile to TileView(tile, context) } }.toMap()

        for (view in tiles.values) {
            view.setOnTouchListener { _, _ ->
                if (game.gameState == Running && game.activePlayer == view.tile.occupiedBy) {
                    focusedTile = view.tile
                    postInvalidate()
                } else {
                    val activePlayer = game.activePlayer
                    if (activePlayer is AsyncPlayer) {
                        if (activePlayer.waitingForPlacement != null) {
                            activePlayer.waitingForPlacement?.resume(view.tile)
                        } else if (activePlayer.waitingForMove != null && focusedTile != null) {
                            for (direction in 0 until view.tile.shape.moveDirections) {
                                var steps = 0
                                var curTile: Tile? = focusedTile
                                do {
                                    ++steps
                                    curTile = curTile?.getNeighbour(direction)
                                } while (
                                        curTile != view.tile
                                        && curTile != null
                                        && curTile.occupiedBy == null
                                )

                                if (steps > 1 && curTile == view.tile) {
                                    activePlayer.waitingForMove?.resume(Triple(focusedTile!!,
                                            direction, steps))
                                    break
                                }
                            }
                        }
                        focusedTile = null
                        postInvalidate()
                    }
                }
                true
            }
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
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = tileSide * (game.size.width + 1)    // TODO: make more accurate
        val height = tileSide * (game.size.height + 1)  // here too

        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            for (i in 0 until game.tiles.size) {
                for (j in 0 until game.tiles[i].size) {
                    val flipped = (i + j) % 2 == 0

                    val tilePosition = when (game.shape) {
                        Square   -> j * tileSide to i * tileSide
                        Hexagon  -> {
                            val tileHeight = tileSide * 2 * sin(PI / 3).toFloat()

                            if (j % 2 == 0) {
                                j * 3 * tileSide to i * tileHeight
                            } else {
                                (j * 3 + 1.5f) * tileSide to (i - 0.5f) * tileHeight
                            }
                        }
                        Triangle -> {
                            val tileHeight = tileSide * sin(PI / 3).toFloat()

                            if (flipped) {
                                (j / 2f + 0.5f) * tileSide to i * tileHeight
                            } else {
                                j / 2f * tileSide to i * tileHeight
                            }
                        }
                    }

                    val tileView = tiles.getValue(game.tiles[i][j])

                    tileView.layout(0, 0, tileView.width, tileView.height)

                    tileView.x = tilePosition.first
                    tileView.y = tilePosition.second
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        when (val newTurn = turnInfoChannel.poll()) {
            null -> {}
            else -> { // TODO: animations
                for ((tile, view) in tiles) {
                    if (tile.state == Tile.TileState.Sunken) {
                        view.visibility = GONE
                    }
                }
            }
        }

        super.onDraw(canvas)

        for (tileView in tiles.values) {
            tileView.draw(canvas)
        }
    }

    companion object {
        private val paints: MutableMap<Int, Paint> = mutableMapOf()

        fun getPaint(color: Int): Paint = paints.getOrPut(
                color
        ) {
            val paint = Paint()
            paint.color = color

            paint
        }

        val textPaint: TextPaint

        init {
            textPaint = TextPaint(getPaint(Color.BLACK))
            textPaint.textAlign = Paint.Align.CENTER
        }
    }
}
