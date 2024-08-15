package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class Connective implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() - 1 && words.get(i + 1).tag().equals("NN")) {
            words.get(i).setTag("JJ");
        } else if (i > 0 && words.get(i - 1).tag().matches("JJ[RS]?")) {
            words.get(i).setTag("NN");
        }
    }

}
