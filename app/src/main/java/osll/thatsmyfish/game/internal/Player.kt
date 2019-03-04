package osll.thatsmyfish.game.internal

/**
 * Represents player
 */
abstract class Player {
    abstract val name: String

    /**
     * Move one of player's penguins to another tile
     * @param penguinPositions positions of penguins, belonging to player
     * @return a triple of numbers -- starting tile, direction and number of tiles to jump
     */
    abstract suspend fun movePenguin(penguinPositions: List<Tile>): Triple<Tile,Int,Int>

    /**
     * Place one of player's penguins
     * @param field game field tiles
     * @return a pair of numbers -- direction and number of tiles to jump
     */
    abstract suspend fun placePenguin(field: List<List<Tile>>): Tile
}