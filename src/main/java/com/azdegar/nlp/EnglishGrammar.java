package com.azdegar.nlp;

/**
 *
 * @author Afshin Pouria
 */
public class EnglishGrammar {

    /* Coordinating Conjunctions */
    public static final String CC = "(.*) (and|but|for|nor|or|so|yet) (.*)";

    /* Subordinating Conjunctions */
    public static final String IN = "(.*) (after|although|as|as if|as long as|as soon as|as though|because|before|despite|even if|even though|if|if only|in order that|now that|once|rather than|since|so that|than|that|till|though|unless|until|when|whenever|whereas|wherever|while) (.*)";

    public static final String GRAM_TO = "afford|agree|arrange|ask|begin|choose|continue|demand|decide|fail|forget|intend|hate|help|hope|learn|like|love|manage|mean|need|offer|plan|prefer|pretend|promise|refuse|remember|start|try|want";
    public static final String GRAM_ING = "admit.*|avoid.*|enjoy.*|finish|dislike.*|fancy.*|recommend.*";
    public static final String GRAM_INGTO = "begin.*|dread.*|forget.*|like.*|love.*|hate.*|need.*|prefer.*|remember.*|regret.*|try.*";
    public static final String GRAM_CAUSATIVE = "allow|enable|force|get|make|help|let|have|keep|hold|require|permit|persuade";
    public static final String GRAM_ATTITUDES = "knows?|knew|thinks?|believes?|believed";
    public static final String NOUNS_ENDING_ING = "evening|morning|spring|.*thing$";

    public static boolean isCausative(String lemma) {
        return lemma.matches(GRAM_CAUSATIVE);
    }


}
