public class PuzzleCharacter {
    private Character character;
    private int gridX;
    private int gridY;

    public PuzzleCharacter(Character character, int gridX, int gridY) {
        this.character = character;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public Character getCharacter() {
        return character;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}
