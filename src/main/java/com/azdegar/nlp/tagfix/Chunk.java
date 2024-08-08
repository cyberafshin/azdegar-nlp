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
public class Chunk implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (words.get(i + 1).tag().equals("DT")) {
            if (words.get(i).tag().equals("NNS")) {
                words.get(i).setTag("VBZ");
            } else if (words.get(i).tag().equals("NN")) {
                words.get(i).setTag("VB");
            }
        }
    }

}
