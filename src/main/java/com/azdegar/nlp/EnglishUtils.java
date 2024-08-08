/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp;

/**
 *
 * @author Afshin Pouria
 */
public class EnglishUtils {

    public static boolean hasVowel(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (isVowel(s.toLowerCase().charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVowel(char c) {
        String vowel = "aeiou";
        for (int i = 0; i < vowel.length(); i++) {
            if (vowel.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPronounceable(String word) {
        if (word.length() < 3) {
            return false;
        }

        for (int i = 0; i < word.length() - 3; i++) {
            if ((!isVowel(word.charAt(i)) && !isVowel(word.charAt(i + 1)) && !isVowel(word.charAt(i + 2)))
                    || (isVowel(word.charAt(i)) && isVowel(word.charAt(i + 1)) && isVowel(word.charAt(i + 2)))) {
                return false;

            }
        }
        return true;
    }

    public static boolean isOrdinalNumber(String word) {
        if (word.matches("[0-9]+(?:st|nd|rd|th)")) {
            return true;
        } else if (word.matches("first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|eleventh|twelfth|nth|hundredth")) {
            return true;
        } else if (word.matches("[e-w]+(teenth|tieth)")) {
            return true;
        } else if (word.matches("(twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)\\-[a-z]+")) {
            return true;
        }
        return false;
    }


}
