package com.azdegar.nlp.transform;

import edu.stanford.nlp.ling.CoreLabel;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
@FunctionalInterface
public interface Transformable {

    void check(int index, List<CoreLabel> words);

}
