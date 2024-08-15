package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class Even implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i > 0 && words.get(i - 1).tag().matches("DT")) {
            words.get(i).setTag("JJ");
        }
    }

}
