import java.io.*;
import java.util.ArrayList;

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
        try (BufferedReader reader = new BufferedReader(new FileReader(dictionary))) {
            String line = reader.readLine();
            int count = 0;

            while (line != null) {
                knownWords.add(new DictionaryWord(count, line));
                line = reader.readLine();
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public void solvePuzzle() {
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

        printSolution();
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

                    solvedWords.add(s);
                    solved = true;
                } else {
                    if (appearsAfter(s.getWord(), knownWords.get(pointer).getWord())) searchPointerBegin = pointer;
                    else searchPointerEnd = pointer;
                }
            }
        }
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

    public void printSolution() {
        System.out.println("PRINT FORMAT: <WORD, START_X, START_Y, ORIENTATION, DIRECTION>");
        System.out.println("==============================================================");

        for (PuzzleWord word : solvedWords) {
            System.out.printf("%s - %d - %d - %s - %s\n", word.getWord(), word.getStartGridx(), word.getStartGridY(), word.getOrientation(), word.getDirection());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        WordSearch search = new WordSearch("words_alpha.txt", "WordSearch.txt");

        search.solvePuzzle();
    }
}
