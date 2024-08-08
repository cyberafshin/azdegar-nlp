package com.azdegar.nlp;

import edu.stanford.nlp.ling.TaggedWord;

/**
 *
 * @author Afshin Pouria
 */
public interface MultiWordRepository {

    String find(String words, int length, TaggedWord next);
}
