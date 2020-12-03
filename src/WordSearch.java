import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class WordSearch {
    private final ArrayList<DictionaryWord> knownWords;
    private PuzzleCharacter[][] puzzlePieces;
    private final File dictionary;
    private final File puzzle;
    private final ArrayList<PuzzleWord> solvedWords;

    public WordSearch(String dictionaryFileName, String puzzleFileName) throws FileNotFoundException {
        dictionary = new File(dictionaryFileName);
        puzzle = new File(puzzleFileName);
        knownWords = new ArrayList<>();
        solvedWords = new ArrayList<>();

        if (!dictionary.exists()) throw new FileNotFoundException("Dictionary file could not be found");
        if (!puzzle.exists()) throw new FileNotFoundException("Puzzle file could not be found");

        parseDictionary();
        parsePuzzle();
    }

    public void parseDictionary() {
        ArrayList<String> sortedWords = new ArrayList<>();
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(dictionary))) {
            String line = reader.readLine();

            while (line != null) {
                sortedWords.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(sortedWords);

        for (String word : sortedWords) {
            knownWords.add(new DictionaryWord(count, word));
            count++;
        }
    }

    public void parsePuzzle() {
        try (BufferedReader reader = new BufferedReader(new FileReader(puzzle))) {
            ArrayList<String> lines = new ArrayList<>();
            String readLine = reader.readLine();

            while (readLine != null) {
                StringBuilder line = new StringBuilder();

                for (int i = 0; i < readLine.length(); i++) {
                    if (Character.isLetter(readLine.charAt(i))) line.append(Character.toLowerCase(readLine.charAt(i)));
                }

                lines.add(line.toString());
                readLine = reader.readLine();
            }

            if (lines.size() != lines.get(0).length()) {
                throw new InvalidPuzzleException("Invalid Puzzle Format: Puzzle is not square " + lines.size() + " " + lines.get(0).length());
            } else {
                puzzlePieces = new PuzzleCharacter[lines.size()][lines.size()];
            }

            for (int i = 0; i < puzzlePieces.length; i++) {
                for (int j = 0; j < puzzlePieces[i].length; j++) {
                    puzzlePieces[i][j] = new PuzzleCharacter(lines.get(i).charAt(j), i, j);
                }
            }
        } catch (IOException | InvalidPuzzleException e) {
            e.printStackTrace();
        }
    }

    public void solvePuzzle() throws FileNotFoundException {
        for (PuzzleCharacter[] row : puzzlePieces) {
            PuzzleWord[] candidates = compileCandidates(row);

            for (PuzzleWord puzzleWord : candidates) {
                puzzleWord.setDirection("horizontal");
            }

            searchCandidates(candidates);
        }

        for (int i = 0; i < puzzlePieces.length; i++) {
            PuzzleCharacter[] column = new PuzzleCharacter[puzzlePieces.length];

            for (int j = 0; j < puzzlePieces.length; j++) {
                column[j] = puzzlePieces[j][i];
            }

            PuzzleWord[] candidates = compileCandidates(column);

            for (PuzzleWord puzzleWord : candidates) {
                puzzleWord.setDirection("vertical");
            }

            searchCandidates(candidates);
        }

        int compileArrayLength = 0;
        int adjust = 0;
        for (int i = 0; i < puzzlePieces.length + puzzlePieces.length - 1; i++) {
            if (i < puzzlePieces.length) compileArrayLength++;
            else {
                adjust++;
                compileArrayLength--;
            }

            PuzzleCharacter[] diagonal = new PuzzleCharacter[compileArrayLength];

            for (int j = 0; j < compileArrayLength; j++) {
                diagonal[j] = puzzlePieces[i - j - adjust][puzzlePieces.length - 1 - j - adjust];
            }

            PuzzleWord[] candidates = compileCandidates(diagonal);

            for (PuzzleWord puzzleWord : candidates) {
                puzzleWord.setDirection("diagonal right");

                if (puzzleWord.getOrientation().equals("forwards")) {
                    puzzleWord.setOrientation("backwards");
                } else puzzleWord.setOrientation("forwards");
            }

            searchCandidates(candidates);
        }

        compileArrayLength = 0;
        adjust = 0;
        for (int i = 0; i < puzzlePieces.length + puzzlePieces.length - 1; i++) {
            if (i < puzzlePieces.length) compileArrayLength++;
            else {
                adjust++;
                compileArrayLength--;
            }

            PuzzleCharacter[] diagonal = new PuzzleCharacter[compileArrayLength];

            for (int j = 0; j < compileArrayLength; j++) {
                diagonal[j] = puzzlePieces[puzzlePieces.length - 1 - i + j + adjust][puzzlePieces.length - 1 - j - adjust];
            }

            PuzzleWord[] candidates = compileCandidates(diagonal);

            for (PuzzleWord puzzleWord : candidates) {
                puzzleWord.setDirection("diagonal left");
            }

            searchCandidates(candidates);
        }

        exportToFile();
    }

    private PuzzleWord[] compileCandidates(PuzzleCharacter[] row) {
        ArrayList<PuzzleWord> candidates = new ArrayList<>();

        for (int i = 0; i < row.length; i++) {
            ArrayList<PuzzleCharacter> candidate = new ArrayList<>();

            for (int j = i; j < row.length; j++) {
                candidate.add(row[j]);

                StringBuilder candidateBuilder = new StringBuilder();
                for (PuzzleCharacter c : candidate) {
                    candidateBuilder.append(c.getCharacter());
                }

                candidates.add(new PuzzleWord(candidate.get(0).getGridX(), candidate.get(0).getGridY(), candidateBuilder.toString(),"forwards"));
            }
        }

        for (int i = row.length - 1; i >= 0; i--) {
            ArrayList<PuzzleCharacter> candidate = new ArrayList<>();

            for (int j = i; j >= 0; j--) {
                candidate.add(row[j]);

                StringBuilder candidateBuilder = new StringBuilder();
                for (PuzzleCharacter c : candidate) {
                    candidateBuilder.append(c.getCharacter());
                }

                if (candidateBuilder.length() > 1) candidates.add(new PuzzleWord(candidate.get(0).getGridX(), candidate.get(0).getGridY(), candidateBuilder.toString(), "backwards"));
            }
        }

        return candidates.toArray(new PuzzleWord[0]);
    }

    private void searchCandidates(PuzzleWord[] candidates) {
        for (PuzzleWord s : candidates) {
            int searchPointerBegin = 0;
            int searchPointerEnd = knownWords.size();
            int lastPointer;
            int pointer = 0;
            boolean solved = false;
            boolean appearsOnce = false;
            String currentKnownWord;

            if (s.getWord().equals("chlorofluoromethanes")) findBilk(s);

            while (!solved) {
                lastPointer = pointer;
                pointer = (searchPointerEnd + searchPointerBegin) / 2;
                currentKnownWord = knownWords.get(pointer).getWord();

                if (pointer == lastPointer) break;

                if (s.getWord().equals(currentKnownWord)) {
                    for (PuzzleWord word : solvedWords) {
                        if (currentKnownWord.equals(word.getWord())) {
                            appearsOnce = true;
                            break;
                        }
                    }

                    if (appearsOnce) break;

                    s.setKnownWord(knownWords.get(pointer));

                    solvedWords.add(s);
                    solved = true;
                } else {
                    if (appearsAfter(s.getWord(), currentKnownWord)) searchPointerBegin = pointer;
                    else searchPointerEnd = pointer;
                }
            }
        }
    }

    public void findBilk(PuzzleWord s) {
        System.out.println("I'm chlorofluoromethanes");
    }

    public boolean appearsAfter(String s, String knownWord) {
        if (s.length() >= knownWord.length()) {
            for (int i = 0; i < knownWord.length(); i++) {
                if (s.charAt(i) > knownWord.charAt(i)) return true;
                else if (s.charAt(i) < knownWord.charAt(i)) return false;
            }

            return true;
        } else {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) > knownWord.charAt(i)) return true;
                else if (s.charAt(i) < knownWord.charAt(i)) return false;
            }

            return false;
        }
    }

    private void exportToFile() throws FileNotFoundException {
        File file = new File("FoundWords.txt");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
            writer.println(solvedWords.size() + " words found!");
            writer.println("PRINT FORMAT: <WORD, ROW, COLUMN, ORIENTATION, DIRECTION>");
            writer.println("==============================================================");

            for (PuzzleWord word : solvedWords) {
                writer.printf("%s - %d - %d - %s - %s\n", word.getWord(), word.getStartGridx(), word.getStartGridY(), word.getOrientation(), word.getDirection());
            }
        }
    }

    public void exportDictionaryToFile() throws FileNotFoundException {
        File file = new File("ExportDictionary.txt");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
            for (DictionaryWord word : knownWords) {
                writer.println(word.getWord());
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        WordSearch search = new WordSearch("words_alpha.txt", "WordSearch.txt");

        search.solvePuzzle();
        search.exportDictionaryToFile();
    }
}
