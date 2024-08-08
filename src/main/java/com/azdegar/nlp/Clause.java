/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp;

import static com.azdegar.nlp.Parser.CLAUSE_PLACEHOLDER;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Afshin Pouria
 */
@Getter
@Setter
public class Clause {

    protected WordGroup words;
    protected Map<Integer, Clause> subs;
    protected Clause parent;
    protected int place;
    protected ExtWord opening;
    protected ExtWord closing;

    protected SentenceType type;
    protected Tense tense;
    protected Voice voice;
    protected Person person;

    protected Map<String, WordGroup> parts;

    protected int idxVerb = -1;
    protected int idxModal = -1;
    protected int idxNeed = -1;

    private boolean marked;

    public Clause() {
        words = new WordGroup();
        subs = new TreeMap();
        tense = Tense.NONE;
        type = SentenceType.None;
        voice = Voice.Active;
        person = Person.NONE;
    }

    public Clause(int place) {
        this();
        this.place = place;
    }

    public void add(ExtWord tw) {
        tw.setLocalIndex(words.size());
        words.add(tw);
    }

    public WordGroup words() {
        return words;
    }

    public WordGroup wordsEnclosing() {
        if (opening == null && closing == null) {
            return words;
        }
        WordGroup ret = new WordGroup(words);
        if (opening != null) {
//            opening.setTranslation(opening.word());
            ret.add(0, opening);
        }
        if (closing != null) {
//            closing.setTranslation(closing.word());
            ret.add(closing);
        }
        return ret;
    }

    public WordGroup wordsEnclosing(Map<Integer, Clause> map) {
        if (opening == null && closing == null) {
            return words;
        }
        WordGroup ret = new WordGroup(words);
        for (int i = 0; i < words().size(); i++) {
            Integer brackets = words().get(i).getBrackets();
            if (brackets != null) {
                words().addAll(i + 1, map.get(brackets).wordsEnclosing());
            }
            for (Clause sub : subs().values()) {
                brackets = sub.words().get(i).getBrackets();
                if (brackets != null) {
                    sub.words().addAll(i + 1, map.get(brackets).wordsEnclosing());
                }
            }
        }

        if (opening != null) {
//            opening.setTranslation(opening.word());
            ret.add(0, opening);
        }
        if (closing != null) {
//            closing.setTranslation(closing.word());
            ret.add(closing);
        }
        return ret;
    }

    public Map<Integer, Clause> subs() {
        return subs;
    }

    public Clause getSub(int i) {
        return subs.get(i);
    }

    public ExtWord getWord(int i) {
        return words.get(i);
    }

    public int indexOf(String s) {
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).word().equalsIgnoreCase(s)) {
                return i;
            }
        }
        return -1;
    }

    public WordGroup getMerged() {
        WordGroup wg = new WordGroup();
        int i = 0;
        if (opening != null) {
//            opening.setTranslation(opening.word());
            wg.add(opening);
        }
        while (i < words.size()) {
            if (words.get(i).matchw(CLAUSE_PLACEHOLDER)) {
                int clauseId = Integer.parseInt(words.get(i).word().substring(1, words.get(i).word().length() - 1));
                if (subs.get(clauseId).opening != null) {
//                    subs.get(clauseId).opening.setTranslation(subs.get(clauseId).opening.word());
                    wg.add(subs.get(clauseId).opening);
                }
                wg.addAll(subs.get(clauseId).words);
                if (subs.get(clauseId).closing != null) {
//                    subs.get(clauseId).closing.setTranslation(subs.get(clauseId).closing.word());
                    wg.add(subs.get(clauseId).closing);
                }
            } else {
                wg.add(words.get(i));
            }
            i++;
        }
        if (closing != null) {
//            closing.setTranslation(closing.word());
            wg.add(closing);
        }
        return wg;
    }

    public int size() {
        return words.size();
    }

    @Override
    public String toString() {
        return words.toString();
    }

    public String getKey() {
        return words.getKey();
    }

    public ExtWord getEffectiveStart() {
        return getEffectiveStart(false);
    }

    public ExtWord getEffectiveStart(boolean skipAdverbs) {
        String tags = "SYM|CD|LS|CC|:";
        if (skipAdverbs) {
            tags += "|RB";
        }
        int i = 0;
        while (i < words.size() && words.get(i).matcht(tags) || words.get(i).matchw("-LRB-|,|[\\d\\.]+") || words.get(i).isDisabled()) {
            i++;
        }
        words.get(i).setLocalIndex(i);
        return words.get(i);
    }

    public ExtWord getVerb() {
        if (idxVerb != -1) {
            return words.get(idxVerb);
        }
        return new ExtWord();
    }

    public ExtWord get(int i) {
        return words.get(i);
    }

    public ExtWord prev(int i) {
        return words.prev(i);
    }

    public ExtWord next(int i) {
        return words.next(i);
    }

    public int length() {
        return words.size();
    }

    public ExtWord getEnd() {
        if (!words.isEmpty()) {
            return words.get(words.size() - 1);
        } else {
            return new ExtWord();
        }
    }

    public ExtWord getEndingWord() {
        if (!words.isEmpty()) {
            int i = words.size() - 1;
            while (i > 0 && (words.get(i).matcht(",|\\.") || words.get(i).isDisabled() || words.get(i).isPlaceHolder())) {
                i--;
            }
            words.get(i).setLocalIndex(i);
            return words.get(i);
        } else {
            return new ExtWord();
        }
    }

    public boolean hasModal() {
        return idxModal != -1;
    }

    public boolean isActive() {
        return voice.equals(Voice.Active);
    }

    public boolean isPassive() {
        return voice.equals(Voice.Passive);
    }

    public boolean isInterrogative() {
        return type.equals(SentenceType.Interrogative);
    }

    public boolean isImperative() {
        return type.equals(SentenceType.Imperative);
    }

    public void setIdxVerb(int idxVerb) {
        this.idxVerb = idxVerb;
        if (idxVerb >= 0) {
            words.get(idxVerb).set(AzdegarAnnotations.PartOfSentenceAnnotation.class, "verb");
        }
    }

    public ExtWord returnIfStartsWith(String word) {
        ExtWord tw = getEffectiveStart();
        if (tw.word().equalsIgnoreCase(word)) {
            return tw;
        } else {
            return null;
        }
    }

    public boolean isIfStmt() {
        if (getEffectiveStart().eqw("if")) {
            return true;
        } else if (parent != null) {
            ExtWord effectiveStart = parent.getEffectiveStart();
            if (effectiveStart.eqw("if")) {
                if (place == effectiveStart.index() + 1) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isLetStmt() {
        return getEffectiveStart().eqw("let");
    }

    public boolean isCausative() {
        return getVerb().isCausative();
    }

    public boolean endsWith(String word) {
        return words.get(words.size() - 1).eqw(word);
    }

    public String time() {
        return tense.time();
    }

    public Tense tense() {
        return tense;
    }

    public Voice voice() {
        return voice;
    }

    public String tenseVoiceString() {
        return tense.toString().replace("_", " ") + " - " + voice;
    }

    public WordGroup subject() {
        return part("subj");
    }

    public Map<String, WordGroup> parts() {
        return parts;
    }

    public WordGroup part(String part) {
        return parts.get(part);
    }

    public int partIndex(String part) {
        return (parts != null && parts.get(part) != null) ? parts.get(part).get(0).localIndex() : -1;
    }

    public int partEndIndex(String part) {
        return (parts != null && parts.get(part) != null) ? parts.get(part).get(parts.get(part).size() - 1).localIndex() : -1;
    }

    public WordGroup getDirectObject() {
        return parts.get("dobj");
    }

    public WordGroup getIndirectObject() {
        return parts.get("iobj");
    }
}
