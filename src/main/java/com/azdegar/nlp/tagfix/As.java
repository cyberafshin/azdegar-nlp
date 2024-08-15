package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class As implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i > 0) {
            if (words.get(i - 2).word().equalsIgnoreCase("as")) {
                if (words.get(i - 1).tag().matches("JJR?")) {
                    words.get(i).setTag("RB");
                    words.get(i - 2).setTag("RB");
                } else if (words.get(i - 1).tag().equals("RB")) {
                    words.get(i).setTag("RB");
                    words.get(i - 1).setTag("JJ");
                    words.get(i - 2).setTag("RB");
                }
            }
        }
    }

}
