/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class What implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() - 2) {
            if (words.get(i + 1).word().equalsIgnoreCase("does")) {
                int e = findWordTagAfter("mean/NN", i + 1, words);
                if (e != -1) {
                    words.get(e).setTag("VBZ");
                }
            } else if (words.get(i + 1).word().equalsIgnoreCase("do")) {
                int e = findWordTagAfter("mean/NN", i + 1, words);
                if (e != -1) {
                    words.get(e).setTag("VB");
                }
            } else if (words.get(i + 1).word().equalsIgnoreCase("did")) {
                int e = findWordTagAfter("mean/NN", i + 1, words);
                if (e != -1) {
                    words.get(e).setTag("VBD");
                }
            }
        }
    }

    private int findWordTagAfter(String wordTag, int i, List<TaggedWord> words) {
        int max = Math.min(i + 5, words.size());
        while (i < max && !words.get(i).word().matches("[,;\\.\\?\\)\\]]")) {
            if ((words.get(i).word().toLowerCase() + "/" + words.get(i).tag()).matches(wordTag)) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
