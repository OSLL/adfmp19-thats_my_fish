package osll.thatsmyfish.game.internal

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

sealed class Shape(val sides: Int, val moveDirections: Int) {
    /**
     * Fits shape inside a rectangle and returns resulting side length
     */
    abstract fun fitIn(width: Float, height: Float): Float

    /**
     * Gets shape points for a given side length
     */
    abstract fun getPoints(side: Float, flipVertical: Boolean): FloatArray

    /**
     * Gets shape center for a given side length
     */
    abstract fun getCenter(side: Float, flipVertical: Boolean): Pair<Float,Float>

    companion object {
        fun byName(name: String): Shape = when (name) {
            "square"   -> Square
            "hexagon"  -> Hexagon
            "triangle" -> Triangle
            else       -> throw IllegalArgumentException("unknown shape name: $name")
        }
    }
}

object Square : Shape(4, 4) {
    override fun fitIn(width: Float, height: Float): Float {
        return min(width, height)
    }

    override fun getPoints(side: Float, flipVertical: Boolean): FloatArray {
        return arrayOf(
                0f, 0f,
                0f, side,
                side, side,
                side, 0f,
                0f, 0f
        ).toFloatArray()
    }

    override fun getCenter(side: Float, flipVertical: Boolean) =
            side / 2f to side / 2f
}

object Hexagon : Shape(6, 6) {
    override fun fitIn(width: Float, height: Float): Float {
        return min(width / 2, height / 2 / sin(PI / 3).toFloat())
    }

    override fun getPoints(side: Float, flipVertical: Boolean): FloatArray {
        val dx: Float = (side * cos(PI / 3)).toFloat()
        val dy: Float = (side * sin(PI / 3)).toFloat()
        return arrayOf(
                0f, dy,
                dx, 0f,
                dx + side, 0f,
                dx * 2 + side, dy,
                dx + side, 2 * dy,
                dx, 2 * dy,
                0f, dy
        ).toFloatArray()
    }

    override fun getCenter(side: Float, flipVertical: Boolean) =
            side to side * sin(PI / 3).toFloat()
}

object Triangle : Shape(3, 6) {
    override fun fitIn(width: Float, height: Float): Float {
        return min(width, height / sin(PI / 3).toFloat())
    }

    override fun getPoints(side: Float, flipVertical: Boolean): FloatArray {
        val dx: Float = (side * cos(PI / 3)).toFloat()
        val dy: Float = (side * sin(PI / 3)).toFloat()
        return if (flipVertical) {
            arrayOf(
                    0f, dy,
                    dx, 0f,
                    side, dy,
                    0f, dy
            ).toFloatArray()
        } else {
            arrayOf(
                    0f, 0f,
                    side, 0f,
                    dx, dy,
                    0f, 0f
            ).toFloatArray()
        }
    }

    override fun getCenter(side: Float, flipVertical: Boolean): Pair<Float,Float> {
        val dy = side * sin(PI / 3).toFloat()

        return side / 2f to if (flipVertical) {
            dy * 2 / 3
        } else {
            dy / 3
        }
    }
}