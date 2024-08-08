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
public class Lot implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() && words.get(i-1).word().equalsIgnoreCase("a")) {
            if (i==words.size()-1 || (i<words.size()-1) && words.get(i+1).tag().matches("[\\.,;]")) {
                words.get(i).setTag("JJ");
                words.get(i).setWord("a lot");
                words.remove(i-1);
//                words.get(i-1).setWord("");
//                words.get(i-1).setTag("");                     
            } 
        }
    }

}
