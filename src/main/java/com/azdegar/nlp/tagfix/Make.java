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
public class Make implements Fixable {

    /*
    
    Jeffrey's poem about his mother made the class cry_VB.
    Sunny weather makes my skin turn_VB brown.
    
     */
    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() - 1) {
            int f = findWordAfter("cry|turn", i + 2, words);
            if (f != -1 && words.get(f).tag().matches("NN")) {
                words.get(f).setTag("VB");
            }
        }
    }

    private int findWordAfter(String word, int i, List<TaggedWord> words) {
        int max = Math.min(i + 5, words.size());
        while (i < max && !words.get(i).word().matches("[,;\\.\\?\\)\\]]")) {
            if (words.get(i).word().matches(word)) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
