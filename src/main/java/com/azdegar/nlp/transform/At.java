/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp.transform;

import com.azdegar.nlp.tagfix.Fixable;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class At implements Transformable {

    @Override
    public void check(int i, List<CoreLabel> words) {
        if (words.get(i + 1).word().equalsIgnoreCase("all")) {
            if (i == words.size() - 2) {
                words.get(i + 1).setWord(words.get(i).word() + " " + words.get(i + 1).word());
                words.get(i + 1).setTag("RB");
                words.get(i + 1).setLemma(words.get(i + 1).word().toLowerCase());
                words.remove(i);
            }
            if (i < words.size() - 2) {
                if (words.get(i + 2).tag().matches(".|IN|CC|RB|TO|WRB")) {
                    words.get(i + 1).setWord(words.get(i).word() + " " + words.get(i + 1).word());
                    words.get(i + 1).setTag("RB");
                    words.get(i + 1).setLemma(words.get(i + 1).word().toLowerCase());
                    words.remove(i);
                }
            }
        }

    }

}
