package osll.thatsmyfish.game.internal

sealed class Shape(val sides: Int, val moveDirections: Int)
object Square : Shape(4, 4)
object Hexagon : Shape(6, 6)
object Triangle : Shape(3, 6)