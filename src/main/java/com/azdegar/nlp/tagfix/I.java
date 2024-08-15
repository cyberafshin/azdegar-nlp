package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class I implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() - 1) {
            if (words.get(i + 1).tag().equals("SYM")) {
                words.get(i).setTag("SYM");
            } else {
                if (i > 0 && words.get(i - 1).word().toLowerCase().matches("chapter|part|section|volume")) {
                    words.get(i).setTag("CD");
                } else {
                    words.get(i).setTag("PRP");
                }
            }
        }
    }

}
