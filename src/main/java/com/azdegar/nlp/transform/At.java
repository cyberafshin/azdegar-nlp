package com.azdegar.nlp.transform;

import edu.stanford.nlp.ling.CoreLabel;
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
