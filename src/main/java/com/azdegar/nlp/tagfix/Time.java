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
public class Time implements Fixable {

    // Rehearse your answers and time_VB them.
    @Override
    public void fix(int i, List<TaggedWord> words) {
        while (words.get(i).tag().matches("CC|IN|RB|UH") && i < words.size() - 2) {
            i++;
        }
        if (words.get(i).word().equalsIgnoreCase("time") && words.get(i).tag().equals("NN")) {
            if (words.get(i + 1).word().matches("it|them|my|your")) {
                words.get(i).setTag("VB");
            }
        }
    }

}
