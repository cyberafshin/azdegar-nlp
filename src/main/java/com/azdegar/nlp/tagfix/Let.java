package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class Let implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() - 1) {
            if (words.get(i + 1).tag().matches("SYM|NFP")) {
                words.get(i + 1).setTag("PRP");
            }
        }
    }

}
