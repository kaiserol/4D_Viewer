package de.uzk.utils.language;

import java.util.ArrayList;
import java.util.List;
// todo: Noch notwendig?
public abstract class Word {
    private static final List<Character> VOWELS;
    private static final List<Character> CONSONANTS;

    static {
        VOWELS = new ArrayList<>();
        for (char c : "aeiou".toCharArray()) VOWELS.add(c);

        CONSONANTS = new ArrayList<>();
        for (char c : "bcdfghjklmnpqrstvwxyz".toCharArray()) CONSONANTS.add(c);
    }

    private Word() {
    }

    public static String normalizeWord(String word) {
        // 1. lowercase
        String normalized = word.toLowerCase();

        // 2. Removes accents und umlauts
        normalized = normalized.replaceAll("[àáâäãå]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replace("ß", "ss");

        // 3. Remove all characters that are not letters or numbers (lower or upper case)
        normalized = normalized.replaceAll("[^a-zA-Z,;.:\\-_]+", "");

        // 4. Removes the grammatical ending
        return removeGrammaticalEnding(normalized);
    }

    private static String removeGrammaticalEnding(String word) {
        if (word.length() >= 3) {
            char char1 = word.charAt(word.length() - 1);
            char char2 = word.charAt(word.length() - 2);
            char char3 = word.charAt(word.length() - 3);

            if (char1 != char2) {
                // char1: vowel
                if (isVowel(char1)) {
                    // example: esse -> ess [see -> see (bleibt bestehen)]
                    if (isConsonant(char2)) return substring(word, 1);

                    // char1: consonant
                } else if (isConsonant(char1)) {
                    // remove ending with consonant
                    return removeConsonantEnding(word, char2, char3);
                }
            }
        }
        return word;
    }

    private static String removeConsonantEnding(String word, char char2, char char3) {
        // char2: vowel
        if (isVowel(char2)) {
            // char3: vowel
            if (isVowel(char3)) {
                // example: seen -> see
                if (char2 == char3) return substring(word, 1);

                // char3: consonant
                // example: essen -> ess
            } else if (isConsonant(char3)) return substring(word, 2);

            // char2: consonant
        } else if (isConsonant(char2) && isConsonant(char3) && char2 != char3) {
            // example: Tests -> test
            return substring(word, 1);
        }
        return word;
    }

    private static boolean isVowel(char c) {
        return VOWELS.contains(c);
    }

    private static boolean isConsonant(char c) {
        return CONSONANTS.contains(c);
    }

    private static String substring(String word, int end) {
        return word.substring(0, word.length() - end);
    }

    public static boolean areWordsSimilar(String word1, String word2) {
        String normalizedWord1 = normalizeWord(word1);
        String normalizedWord2 = normalizeWord(word2);
        int abs = Math.abs(normalizedWord1.length() - normalizedWord2.length());

        if (abs <= 3) {
            return normalizedWord1.startsWith(normalizedWord2) ||
                    normalizedWord2.startsWith(normalizedWord1);
        } else return false;
    }
}
