package com.azdegar.nlp;

import edu.stanford.nlp.ling.CoreAnnotation;
import java.util.List;

/**
 *
 * @author Afshin Pouria
 */
public class AzdegarAnnotations {

    public static class PartOfSentenceAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    public static class TranslationAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    public static class OntologyAnnotation implements CoreAnnotation<Character> {

        @Override
        public Class<Character> getType() {
            return Character.class;
        }
    }

    public static class TypeAnnotation implements CoreAnnotation<List> {

        @Override
        public Class<List> getType() {
            return List.class;
        }
    }

    public static class TransitiveAnnotation implements CoreAnnotation<Character> {

        @Override
        public Class<Character> getType() {
            return Character.class;
        }
    }

    public static class MultiVerbAnnotation implements CoreAnnotation<List> {

        @Override
        public Class<List> getType() {
            return List.class;
        }
    }
}
