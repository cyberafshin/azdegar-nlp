package com.azdegar.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Afshin
 */
public class WordGroup extends ArrayList<ExtWord> {

    private String label;

    public WordGroup() {
    }

    public WordGroup(List<ExtWord> in) {
        addAll(in);
    }

    public boolean matches(String regex) {
        return toString().matches(regex);
    }

    /* NULL safe getter */
    @Override
    public ExtWord get(int index) {
        if (index >= 0 && index < size()) {
            return super.get(index);
        } else {
            return new ExtWord("", index);
        }
    }

    public void disable(int index) {
        if (index >= 0 && index < size()) {
            get(index).disable();
        }
    }

    @Override
    public String toString() {
//        return this.stream().filter(e -> !e.isDisabled()).map(e -> e.info()).collect(Collectors.joining(" "));
        return this.stream().map(e -> e.info()).collect(Collectors.joining(" "));
    }

    public String getKey() {
        return this.stream().filter(e -> !e.isDisabled()).map(e -> e.info()).collect(Collectors.joining(" "));
    }

    public String toString(int start, int end) {
        return this.subList(start, end).stream().filter(e -> !e.isDisabled()).map(e -> e.word()).collect(Collectors.joining(" "));
    }

    public int indexOf(String word) {
        for (int i = 0; i < size(); i++) {
            if (word.equalsIgnoreCase(get(i).word())) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfLemma(String lemma) {
        for (int i = 0; i < size(); i++) {
            if (get(i).eql(lemma)) {
                return i;
            }
        }
        return -1;
    }

    public ExtWord getEffectiveStart() {
        int i = 0;
        while (i < size() && get(i).eqt("SYM|CD|LS|CC") || get(i).matchw("-LRB-|,|[\\d\\.]+") || get(i).isDisabled()) {
            i++;
        }
//        get(i).setIndex(i);
        return get(i);
    }

    public int findWordTag(String wt, int beginIndex, int endIndex) {
        for (int i = beginIndex; i < endIndex; i++) {
            if (get(i).lt().equals(wt)) {
                return i;
            }
        }
        return -1;
    }

    public int findLemma(String lemma, int from, int to) {
        int i = from;
        while (i < size() && i < to) {
            if (get(i).lemma().matches(lemma)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findWord(String pattern, int from, int to) {
        int i = from;
        while (i < size() && i < to) {
            if (get(i).matchw(pattern)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findTag(String pattern, int from, int to) {
        int i = from;
        while (i < size() && i < to) {
            if (get(i).matcht(pattern)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findSequence(String sequence, int from, int to) {
        int i = from;
        int l = sequence.split(" ").length;
        while (i + l <= size() && i < to) {
            String s = subList(i, i + l).stream().map(e -> e.lemma()).collect(Collectors.joining(" "));
            if (sequence.equals(s)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findType(String type, int from, int to) {
        int i = from;
        while (i < size() && i < to) {
            if (get(i).matchw(",|\\(|\\)|:")) {
                return -1;
            }
            if (get(i).get(AzdegarAnnotations.TypeAnnotation.class) != null && get(i).get(AzdegarAnnotations.TypeAnnotation.class).contains(type)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findPartOfSentence(String pos, int from, int to) {
        int i = from;
        while (i < size() && i < to) {
            if (get(i).matchw(",|\\(|\\)|:")) {
                return -1;
            }
            if (pos.equals(get(i).get(AzdegarAnnotations.PartOfSentenceAnnotation.class))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int findVerbBefore(int index) {

        return findVerbBefore(index, 100);
    }

    public int findVerbBefore(int index, int limit) {
        int i = index - 1;
        int min = 0;
        if (index - limit > 0) {
            min = index - limit;
        }
        while (i > min) {
            if (get(i).isVerb()) {
                return i;
            } else if (get(i).eqt(",")) {
                return -1;
            }
            i--;
        }
        return -1;
    }

    public int findVerbAfter(int index) {
        return findVerbAfter(index, 100);
    }

    public int findVerbAfter(int index, int limit) {
        int max = size();
        if (index + limit < max) {
            max = index + limit;
        }
        int i = index + 1;
        while (i < max) {
            if (get(i).isVerb()) {
                return i;
            } else if (get(i).eqt(",")) {
                return -1;
            }
            i++;
        }
        return -1;
    }

    public ExtWord prev(int index) {
        if (index > 0) {
            int i = index - 1;
            while (i > 0 && get(i).isDisabled()) {
                i--;
            }
            get(i).setLocalIndex(i);
            return get(i);
        }
        return new ExtWord("", index);
    }

    public ExtWord next(int index) {
        if (index < size() - 1) {
            int i = index + 1;
            while (i < size() && get(i).isDisabled()) {
                i++;
            }
            get(i).setLocalIndex(i);
            return get(i);
        }
        return new ExtWord("", index);
    }

    public boolean eql(int index, String lemma) {
        if (index < 0 || index >= size() || get(index).lemma() == null) {
            return false;
        }
        return get(index).lemma().equals(lemma);
    }

    public boolean eqlt(int index, String lt) {
        if (index < 0 || index >= size() || get(index).lemma() == null) {
            return false;
        }
        return get(index).eqlt(lt);
    }

    public boolean eqt(int index, String tag) {
        if (index < 0 || index >= size() || get(index).tag() == null) {
            return false;
        }
        return get(index).eqt(tag);
    }

    public boolean eqw(int index, String word) {
        if (index < 0 || index >= size() || get(index).word() == null) {
            return false;
        }
        return get(index).word().equals(word);
    }

    public boolean eqwci(int index, String word) {
        if (index < 0 || index >= size() || get(index).word() == null) {
            return false;
        }
        return get(index).word().equalsIgnoreCase(word);
    }

    public boolean mw(int index, String regex) {
        if (index < 0 || index >= size() || get(index).word() == null) {
            return false;
        }
        return get(index).word().matches(regex);
    }

    public boolean ml(int index, String regex) {
        if (index < 0 || index >= size() || get(index).lemma() == null) {
            return false;
        }
        return get(index).lemma().matches(regex);
    }

    public boolean mt(int index, String regex) {
        if (index < 0 || index >= size() || get(index).tag() == null) {
            return false;
        }
        return get(index).tag().matches(regex);
    }

    public int regexOccurAfter(String regex, int from, int to) {
        int i = from;
        while (i < size() && i < to) {
            if (get(i).matchw(",|\\(|:")) {
                return -1;
            }
            if (get(i).matchw(regex)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
