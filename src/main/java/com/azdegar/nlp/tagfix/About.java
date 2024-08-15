package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class About implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (words.size()==1 || (i < words.size() - 1 && words.get(i + 1).tag().matches("DT|NNP?S?|.|:"))) {
            words.get(i).setTag("IN");
        }

    }

}
