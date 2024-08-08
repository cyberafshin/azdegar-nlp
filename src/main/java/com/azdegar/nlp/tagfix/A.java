
package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class A implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        if (i < words.size() - 1 && words.get(i + 1).tag().equals("VBD")) {
            words.get(i + 1).setTag("JJ"); // a combined 3 million copies ...
        }
        if (i == words.size() - 1 || !words.get(i + 1).tag().matches("NNP?|JJR?|RBR?")) {
            words.get(i).setTag("SYM");
        } else if (words.get(i).word().equals("A")) {
            if (i < words.size() - 1) {
                if (words.get(i + 1).word().matches("[,;?\\.\\]\\)]")) {
                    words.get(i).setTag("SYM");
                } else if (i > 0) {
                    if (words.get(i + 1).word().matches("[a-z]+")) {
                        words.get(i).setTag("SYM");
                    }
                }
            }
        }
    }

}
