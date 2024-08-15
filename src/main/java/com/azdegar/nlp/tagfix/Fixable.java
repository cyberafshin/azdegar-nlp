package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
@FunctionalInterface
public interface Fixable {

    void fix(int index, List<TaggedWord> words);

}
