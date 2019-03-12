package osll.thatsmyfish.game.internal

import android.util.SizeF
import kotlin.math.*

sealed class Shape(val sides: Int, val moveDirections: Int) {
    /**
     * Gets shape points for a given bounding box
     */
    abstract fun getPoints(width: Float, height: Float, flipVertical: Boolean): FloatArray

    /**
     * Fits the shape into a given bounding box and returns its dimensions
     */
    fun fitInto(width: Float?, height: Float?) = fitIntoInner(
            width ?: defaultSide,
            height ?: defaultSide
    )

    internal abstract fun fitIntoInner(width: Float, height: Float): SizeF

    companion object {
        fun byName(name: String): Shape = when (name) {
            "square"   -> Square
            "hexagon"  -> Hexagon
            "triangle" -> Triangle
            else       -> throw IllegalArgumentException("unknown shape name: $name")
        }

        private const val defaultSide = 200f
    }
}

object Square : Shape(4, 4) {
    override fun getPoints(width: Float, height: Float, flipVertical: Boolean): FloatArray {
        return arrayOf(
                0f, 0f,
                0f, height,
                width, height,
                width, 0f
        ).toFloatArray()
    }

    override fun fitIntoInner(width: Float, height: Float): SizeF {
        val minCoordinate = min(width, height)

        return SizeF(minCoordinate, minCoordinate)
    }
}

object Hexagon : Shape(6, 6) {
    override fun getPoints(width: Float, height: Float, flipVertical: Boolean): FloatArray {
        val dx: Float = (height / 2 / tan(PI / 3)).toFloat()
        return arrayOf(
                0f, height / 2,
                dx, 0f,
                width - dx, 0f,
                width, height / 2,
                width - dx, height,
                dx, height
        ).toFloatArray()
    }

    override fun fitIntoInner(width: Float, height: Float): SizeF {
        val minCoordinate = min(width, height / tan(PI / 3).toFloat() * 2)

        return SizeF(minCoordinate, minCoordinate / 2 * tan(PI / 3).toFloat())
    }
}

object Triangle : Shape(3, 6) {
    override fun getPoints(width: Float, height: Float, flipVertical: Boolean): FloatArray {
        return if (flipVertical) {
            arrayOf(
                    0f, 0f,
                    width, 0f,
                    width / 2, height
            ).toFloatArray()
        } else {
            arrayOf(
                    0f, height,
                    width / 2, 0f,
                    width, height
            ).toFloatArray()
        }
    }

    override fun fitIntoInner(width: Float, height: Float): SizeF {
        val minCoordinate = min(width, height / sin(PI / 3).toFloat())

        return SizeF(minCoordinate, minCoordinate * sin(PI / 3).toFloat())
    }
}