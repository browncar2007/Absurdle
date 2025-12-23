// Caronn Brown
// 10/29/2025
// CSE 122
// P2: Absurdle
// TA: Katharine Zhang

// Class Comment:
// This program runs a variation of the Wordle game called absurdle.
// The user tries to guess a hidden word of a chosen length and 
// the program returns colored patterns showing how close the guess is. 
// Unlike Wordle the absurdle manager avoids reveling the secret word for as long as possible by
// adjusting the target word and pruning the dictionary as little as possible.
import java.util.*;
import java.io.*;

public class Absurdle {
    public static final String GREEN = "🟩";
    public static final String YELLOW = "🟨";
    public static final String GRAY = "⬜";

    // [[ ALL OF MAIN PROVIDED ]]
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        System.out.println("Welcome to the game of Absurdle.");

        System.out.print("What dictionary would you like to use? ");
        String dictName = console.next();

        System.out.print("What length word would you like to guess? ");
        int wordLength = console.nextInt();

        List<String> contents = loadFile(new Scanner(new File(dictName)));
        Set<String> words = prepDictionary(contents, wordLength);

        List<String> guessedPatterns = new ArrayList<>();
        while (!isFinished(guessedPatterns)) {
            System.out.print("> ");
            String guess = console.next();
            String pattern = recordGuess(guess, words, wordLength);
            guessedPatterns.add(pattern);
            System.out.println(": " + pattern);
            System.out.println();
        }
        System.out.println("Absurdle " + guessedPatterns.size() + "/∞");
        System.out.println();
        printPatterns(guessedPatterns);
    }

    // [[ PROVIDED ]]
    // Prints out the given list of patterns.
    // - List<String> patterns: list of patterns from the game
    public static void printPatterns(List<String> patterns) {
        for (String pattern : patterns) {
            System.out.println(pattern);
        }
    }

    // [[ PROVIDED ]]
    // Returns true if the game is finished, meaning the user guessed the word. Returns
    // false otherwise.
    // - List<String> patterns: list of patterns from the game
    public static boolean isFinished(List<String> patterns) {
        if (patterns.isEmpty()) {
            return false;
        }
        String lastPattern = patterns.get(patterns.size() - 1);
        return !lastPattern.contains("⬜") && !lastPattern.contains("🟨");
    }

    // [[ PROVIDED ]]
    // Loads the contents of a given file Scanner into a List<String> and returns it.
    // - Scanner dictScan: contains file contents
    public static List<String> loadFile(Scanner dictScan) {
        List<String> contents = new ArrayList<>();
        while (dictScan.hasNext()) {
            contents.add(dictScan.next());
        }
        return contents;
    }

    // B: Filters the dictionary to only include words of the specified length.
    // E: Throws IllegalArgumentException if wordLength < 1.
    // R: Set of valid words with the correct length.
    // P: contents - all words from dictionary; wordLength - word length of guess. 
    public static Set<String> prepDictionary(List<String> contents, int wordLength) {
        if (wordLength < 1) {
            throw new IllegalArgumentException("Word Length should be greater than one");
        }
        Set<String> dict = new HashSet<>();
        for (int i = 0; i < contents.size(); i++) {
            String currentWord = contents.get(i);
            if (currentWord.length() == wordLength) {
                dict.add(currentWord);
            }
        }
        return dict;
    }

    // B: Determines the pattern for the user's guess that prunes the dictionary the least,
    //    and updates the remaining possible words in the dictionary.
    //    the method updates the current set of words
    //    based on the guess that has the set with the largest amount of words.
    // E: Throws IllegalArgumentException: if the guesses length
    //    is not equal to the specified word length
    //    Throws IllegalArgumentException: if the dictionary is empty.
    // R: String - A String representing the pattern of the users guess
    // P: guess - The user's guessed word;
    //    words - all words from updated dictionary;
    //    wordLength - target word length.
    //    wordLength - target word length.
    public static String recordGuess(String guess, Set<String> words, int wordLength) {
        if (guess.length() != wordLength) {
            throw new IllegalArgumentException("Guess should be the same length as specified");
        }
        if (words.isEmpty()) {
            throw new IllegalArgumentException("dictionary cannot be empty");
        }
        Map<String, Set<String>> patternMap = new HashMap<>();
        String target = "";

        mapPatterns(words, patternMap, guess);

        int max = maxSetLength(patternMap);

        SortedSet<String> sortedSet = new TreeSet<>();
        findTargetPattern(patternMap, max, sortedSet);

        Iterator<String> it = sortedSet.iterator();
        boolean isFirst = false;
        while(it.hasNext() && !isFirst){
            isFirst = true;
            target = it.next();
        }
        
        words.clear();
        words.addAll(patternMap.get(target)); //updates the dictionary to the patterns set

        return target;
    }

    // B: Creates a wordle style pattern comparing the guessed word and the target word.
    // E: None
    // R: pattern - A String representing a pattern of the users guess compared to the target.
    // P: word - Absurdle manager's target word; guess - player's guessed word.
    public static String patternFor(String word, String guess) {
        List<String> guessList = new ArrayList<>();
        Map<Character, Integer> charMap = new HashMap<>();
        String pattern = "";
        char currLetter = 'a';
        int letterNum = 0;
        //adds the guess characters to an array List
        for (int i = 0; i < guess.length(); i++) {
            guessList.add(guess.substring(i, i + 1));
        }
        //adds the word to a <Character and Integer> map
        for (int i = 0; i < word.length(); i++) {
            if (!charMap.containsKey(word.charAt(i))) {
                charMap.put(word.charAt(i), 1);
            } else {
                int num = charMap.get(word.charAt(i));
                num++;
                charMap.put(word.charAt(i), num);
            }
        }
        //assigns the greens first
        for (int i = 0; i < guessList.size(); i++) {
            currLetter = guessList.get(i).charAt(0);
            if (charMap.containsKey(currLetter)) {
                letterNum = charMap.get(currLetter);
                //if the current character of the word matches the current character of the guess.
                if (word.substring(i, i + 1).equals(String.valueOf(currLetter))) {
                    guessList.set(i, GREEN);
                    letterNum--;
                    if (letterNum > 0) {
                        charMap.put(currLetter, letterNum);
                    } else {
                        charMap.remove(currLetter);
                    }

                }
            }
        }
        //then assigns the yellows and grays
        for (int i = 0; i < guessList.size(); i++) {
            currLetter = guessList.get(i).charAt(0);
            if (charMap.containsKey(currLetter)) {
                letterNum = charMap.get(currLetter);
                if (!guessList.get(i).equals(GREEN)) {
                    guessList.set(i, YELLOW);
                    letterNum--;
                    if (letterNum > 0) {
                        charMap.put(currLetter, letterNum);
                    } else {
                        charMap.remove(currLetter);
                    }
                }
            } else if (!guessList.get(i).equals(GREEN)) {
                guessList.set(i, GRAY);
            }
            //adds the updated arrayList to a string
        }
        for (int i = 0; i < guessList.size(); i++) {
            pattern += guessList.get(i);
        }
        return pattern;
    }

    // B: Creates a mapping of patterns to all words that match each pattern.
    // E: None
    // R: None
    // P: words - set of possible words; PatternMap - map storing pattern-word groups;
    //    guess - user's current guess.
    public static void mapPatterns(Set<String> words, Map<String,
             Set<String>> patternMap, String guess) {
        //create the nested collection of patterns
        for (String word : words) {
            String pattern = patternFor(word, guess);
            if (patternMap.containsKey(pattern)) {
                patternMap.get(pattern).add(word);

            } else {
                patternMap.put(pattern, new HashSet<String>());
                patternMap.get(pattern).add(word);
            }
        }
    }

    // B: Finds and returns the size of the biggest pattern group.
    // E: None
    // R: None
    // P: patternMap - map storing pattern-word groups.
    public static int maxSetLength(Map<String, Set<String>> patternMap) {
        int max = 0;
        for (String pattern : patternMap.keySet()) {
            Set<String> keySet = patternMap.get(pattern);
            int size = keySet.size();
            if (size > max) {
                max = size;
            }
        }
        return max;
    }

    // B: searches the map to see if any of the sets have the same length
    //    and keeps only those.
    // E: None
    // R: None
    // P: patternMap - map storing pattern-word groups; 
    //    max - Integer representing the set with he longest words;
    //    sortedSet - A set to order the patterns.
    public static void findTargetPattern(Map<String, Set<String>> patternMap,
            int max, Set<String> sortedSet) {
        Iterator<String> itr = patternMap.keySet().iterator();
        while (itr.hasNext()) {
            String pattern = itr.next();
            if (patternMap.get(pattern).size() == max) {
                sortedSet.add(pattern);
            } else {
                itr.remove();
            }
        }
    }
}