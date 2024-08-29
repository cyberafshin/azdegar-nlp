package com.azdegar.nlp;

//import com.azdegar.translator.model.Translation;
import static com.azdegar.nlp.Parser.CLAUSE_PLACEHOLDER;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Afshin Pouria
 * @since May 22, 2016
 */
@Getter
@Setter
public class ExtWord extends CoreLabel implements Serializable, Comparable {

    protected boolean negative;
    protected int depth;
    protected int sense;
    protected boolean disabled;
    protected int localIndex;
    protected boolean unspecified;
    protected int clauseId;
    protected PhraseInfo phraseInfo;
    protected String preposition;
    protected Integer brackets;

    protected List translations;
    protected boolean exactTrans;
    protected boolean persianOnly = false;

    public ExtWord() {
        setWord("");
        setLemma("");
        setTag("");
        negative = false;
        sense = -1;
        disabled = true;
    }

    public ExtWord(String lemma, int index) {
        setLemma(lemma);
        setWord(lemma);
        setTag("NA");
        setIndex(index);
    }

    public ExtWord(String word, String tag, String lemma) {
        setLemma(lemma);
        setWord(word);
        setTag(tag);
    }

    public ExtWord(CoreLabel cl) {
        setWord(cl.word());
        if (cl.category() != null && cl.word() == null) {
            setWord(cl.category());
        }
        setTag(cl.tag());
        setLemma(cl.lemma());
        set(AzdegarAnnotations.TranslationAnnotation.class, cl.get(AzdegarAnnotations.TranslationAnnotation.class));
        if (cl.sentIndex() > -1) {
            brackets = cl.sentIndex() * 10;
//            setSentIndex(cl.sentIndex());
        }
        negative = false;
    }

    public String phrase() {
        if (phraseInfo != null) {
            return phraseInfo.getType() + "-" + phraseInfo.getNumber();
        } else {
            return "";
        }
    }

    public int phraseId() {
        if (phraseInfo != null) {
            return phraseInfo.getNumber();
        } else {
            return -1;
        }
    }

    public String phraseType() {
        if (phraseInfo != null) {
            return phraseInfo.getType();
        } else {
            return "";
        }
    }

    public int phraseDepth() {
        if (phraseInfo != null) {
            return phraseInfo.getDepth();
        } else {
            return -1;
        }
    }

    public int localIndex() {
        return localIndex;
    }

    public void merge(CoreLabel cl) {
        setWord(word() + " " + cl.word());
    }

    @Override
    public void setWord(String word) {
        super.setWord(word);
        setLemma(word);
    }

    @Override
    public String lemma() {
        if (super.lemma() == null) {
            return word();
        } else {
            return super.lemma();
        }
    }

    public void disable() {
        disabled = true;
        setTranslation("", null);
    }

    public String partOfSentence() {
        return get(AzdegarAnnotations.PartOfSentenceAnnotation.class);
    }

    public List<CoreLabel> otherVerb() {
        return get(AzdegarAnnotations.MultiVerbAnnotation.class);
    }

    public String translation() {
        return get(AzdegarAnnotations.TranslationAnnotation.class);
    }

    public void setTranslation(String trans) {
        if (!"⟨برای⟩".equals(get(AzdegarAnnotations.TranslationAnnotation.class))) {
            set(AzdegarAnnotations.TranslationAnnotation.class, trans);
        }
    }

    public void setTranslation(String trans, Character transitive) {
        if (!"⟨برای⟩".equals(get(AzdegarAnnotations.TranslationAnnotation.class))) {
            set(AzdegarAnnotations.TranslationAnnotation.class, trans);
        }
        set(AzdegarAnnotations.TransitiveAnnotation.class, transitive);
    }

    public String getTranslation() {
        return get(AzdegarAnnotations.TranslationAnnotation.class);
    }

    public void setReference(WordGroup wg) {
        set(AzdegarAnnotations.ReferenceAnnotation.class, wg);
    }

    public WordGroup getReference() {
        return get(AzdegarAnnotations.ReferenceAnnotation.class);
    }

    public Character getTransitive() {
        return get(AzdegarAnnotations.TransitiveAnnotation.class);
    }

    public int depth() {
        return depth;
    }

    public void negate() {
        negative = true;
    }

    public boolean isPlaceHolder() {
        return word().matches(CLAUSE_PLACEHOLDER);
    }

    public boolean isJunctor() {
        return word().toLowerCase().matches("and|or");
    }

    public boolean isAux() {
        if (word() == null) {
            return false;
        }
        if (tag().equals("AUX")) {
            return true;
        }
        return lemma().matches("be|have|do") && !word().toLowerCase().matches("having|doing");
    }

    public boolean isModal() {
        if (word() == null) {
            return false;
        }
        return word().toLowerCase().matches("can|could|may|might|must|should|would|have to|be to|going to") || lemma().equals("can"); // ca (can) can't
    }

    public boolean isPlural() {
        return word() != null ? EnglishNoun.isPlural(word()) : false;
    }

    public String singular() {
        if (isPlural()) {
            return EnglishNoun.singularOf(word());
        } else {
            return word();
        }
    }

    public boolean eqw(String s) {
        return (word() != null) ? word().equalsIgnoreCase(s) : false;
    }

    public boolean eqt(String t) {
        return (tag() != null) ? tag().equalsIgnoreCase(t) : false;
    }

    public boolean eql(String l) {
        return (lemma() != null) ? lemma().equals(l) : false;
    }

    public boolean eqlt(String lt) {
        return lt().matches(lt);
    }

    public boolean eqp(String c) {
        return partOfSentence() != null ? partOfSentence().equals(c) : false;
    }

    public boolean eqwt(String wt) {
        return wt().equalsIgnoreCase(wt);
    }

    public boolean matcht(String pattern) {
        return tag() != null ? tag().matches(pattern) : false;
    }

    public boolean startt(String st) {
        if (tag() == null) {
            return false;
        }
        return tag().startsWith(st);
    }

    public boolean matchl(String pattern) {
        return lemma() != null ? lemma().matches(pattern) : false;
    }

    public boolean matchw(String pattern) {
        return word() != null ? word().toLowerCase().matches(pattern) : false;
    }

    public boolean isProper() {
        return matcht("NN?P?S?");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CoreLabel) {
            CoreLabel iw = (CoreLabel) o;
            return word().equalsIgnoreCase(iw.word());
        } else if (o instanceof String) {
            String s = (String) o;
            return word().equalsIgnoreCase(s);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String s = "(" + index() + "," + localIndex + ") " + word();
        if (negative) {
            s = "~" + s;
        }
        if (word() != null && !word().equalsIgnoreCase(lemma())) {
            s += "(" + lemma() + ")";
        }
        if (tag() != null && !tag().isBlank()) {
            s += "/" + tag();
        }
        s += "[Clause: " + clauseId;
        if (phraseInfo != null) {
            s += ", Phrase: " + phraseInfo.getType() + "-" + phraseInfo.getNumber();
        }

        if (partOfSentence() != null) {
            s += ", SentencePart: " + partOfSentence();
        }
        s += "]";
        return s;
    }

    public boolean isVerb() {
        if (tag() == null) {
            return false;
        }
//        return tag().matches("VB[D|P|Z]?");
        return tag().startsWith("VB");
    }

    public boolean isConnective() {
        if (lemma() == null) {
            return false;
        }
        return lemma().matches("and|or");
    }

    public boolean isParticle() {
        return tag().equals("RP");
    }

    public String lt() {
        return lemma() + "/" + tag();
    }

    public String wt() {
        return word() + "/" + tag();
    }

    public String info() {
        String s = word();
        if (negative) {
            s = "~" + s;
        }
        if (word() != null && !word().equalsIgnoreCase(lemma())) {
            s += "(" + lemma() + ")";
        }
        if (tag() != null && !tag().isBlank()) {
            s += "/" + tag();
        }
//        s += String.format("<%02d, %02d>", index, depth);
        if (disabled) {
            return strikethrough(s);
        } else {
            return s;
        }
    }

    public static String strikethrough(String in) {
        String out = "";
        for (int i = 0; i < in.length(); i++) {
            out += in.charAt(i) + "\u0336";
        }
        return out;
    }

    public void cleanup() {
        setWord(word().replace("_", " "));
        setTag("RB");
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ExtWord) {
            ExtWord other = (ExtWord) o;
            return lt().compareTo(other.lt());
        } else {
            return -1;
        }
    }

    public boolean isCausative() {
//        return lemma().matches(EnglishGrammar.GRAM_CAUSATIVE) || lemma().contains("⇒");
        return lemma().contains("⇒");
    }
    

    public void change(String word, String tag, String lemma) {
        setWord(word);
        setTag(tag);
        setLemma(lemma);
    }

    public void change(String word, String tag) {
        change(word, tag, word);
    }

}