public class DictionaryWord {
    private int index;
    private String word;

    public DictionaryWord(int index, String word) {
        this.index = index;
        this.word = word;
    }

    public int getIndex() {
        return index;
    }

    public String getWord() {
        return word;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictionaryWord that = (DictionaryWord) o;
        return index == that.index &&
                word.equals(that.word);
    }
}
