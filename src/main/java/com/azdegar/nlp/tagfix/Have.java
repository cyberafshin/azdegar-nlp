package com.azdegar.nlp.tagfix;

import edu.stanford.nlp.ling.TaggedWord;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class Have implements Fixable {

    @Override
    public void fix(int i, List<TaggedWord> words) {
        int j = i + 1;
        while (j < words.size() && j < i + 3 && !words.get(j).tag().matches("\\.|,|WRB")) {
            if (words.get(j).tag().equals("VBD")) {
                words.get(j).setTag("VBN");
            }
            j++;
        }
    }

}
