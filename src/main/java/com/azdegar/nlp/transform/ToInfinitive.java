/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp.transform;

import com.azdegar.nlp.AzdegarAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class ToInfinitive implements Transformable {

    @Override
    public void check(int i, List<CoreLabel> words) {
        if (i > 0 && i + 1 < words.size() && words.get(i + 1).tag().matches("VBP?") && !words.get(i + 1).word().endsWith("ing")) {
            if (words.get(i - 1).lemma().matches("have|be") || (words.get(i - 1).lemma().equals("use") 
                    && (i < 2 || !words.get(i - 2).tag().matches("VB[DPZ]?")) /*  This technology could be used to cure diabetes. */
                    
                    )) {
                String lemma = words.get(i - 1).lemma();
                words.get(i - 1).setWord(words.get(i - 1).word() + " to");
                words.get(i - 1).setLemma(lemma + " to");
                words.get(i - 1).setTag("MD");
                words.remove(i);
            } else if (words.get(i - 1).lemma().matches("get")) {
                String lemma = words.get(i - 1).lemma();
                words.get(i - 1).setWord(words.get(i - 1).word() + " to");
                words.get(i - 1).setLemma(lemma + " to");
                words.get(i - 1).setTag("AUX");
                words.remove(i);
            } else if (words.get(i - 1).lemma().equals("not")) {
                if (i - 1 > 0 && words.get(i - 2).lemma().equals("be")) {
                    words.get(i - 2).setWord(words.get(i - 2).word() + " to");
                    words.get(i - 2).setLemma("be to");
                    words.get(i - 2).setTag("MD");
                    words.remove(i);
                }
            } else if (words.get(i - 1).word().equalsIgnoreCase("going")) {
                if (i > 1 && words.get(i - 2).lemma().equals("be")) {
                    words.get(i - 1).setWord("be going to");
                    words.get(i - 1).setTag("MD");
                    words.remove(i);
                    words.remove(i - 2);
                }
            } else {
                words.get(i + 1).setTag("INF");
                words.get(i).set(AzdegarAnnotations.TranslationAnnotation.class, "⟨برای⟩");
            }
        }
    }

}
