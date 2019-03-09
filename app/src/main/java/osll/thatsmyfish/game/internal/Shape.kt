package osll.thatsmyfish.game.internal

import kotlin.math.*

sealed class Shape(val sides: Int, val moveDirections: Int) {
    /**
     * Gets shape points for a given bounding box
     */
    abstract fun getPoints(width: Float, height: Float, flipVertical: Boolean): FloatArray

    companion object {
        fun byName(name: String): Shape = when (name) {
            "rectangle"   -> Rectangle
            "hexagon"  -> Hexagon
            "triangle" -> Triangle
            else       -> throw IllegalArgumentException("unknown shape name: $name")
        }
    }
}

object Rectangle : Shape(4, 4) {
    override fun getPoints(width: Float, height: Float, flipVertical: Boolean): FloatArray {
        return arrayOf(
                0f, 0f,
                0f, height,
                width, height,
                width, 0f
        ).toFloatArray()
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
}