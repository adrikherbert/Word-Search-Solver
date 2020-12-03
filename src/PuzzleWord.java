public class PuzzleWord {
    private final int startGridX;
    private final int startGridY;
    private final String word;
    private String orientation;
    private String direction;
    private DictionaryWord knownWord;

    public PuzzleWord(int startGridX, int startGridY, String word, String orientation) {
        this.startGridX = startGridX;
        this.startGridY = startGridY;
        this.word = word;
        this.orientation = orientation;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getWord() {
        return word;
    }

    public int getStartGridx() {
        return startGridX;
    }

    public int getStartGridY() {
        return startGridY;
    }

    public String getDirection() {
        return direction;
    }

    public String getOrientation() {
        return orientation;
    }

    public DictionaryWord getKnownWord() {
        return knownWord;
    }

    public void setKnownWord(DictionaryWord knownWord) {
        this.knownWord = knownWord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PuzzleWord that = (PuzzleWord) o;
        return word.equals(that.word);
    }
}
