package com.azdegar.nlp;

import static com.azdegar.nlp.Parser.CLAUSE_PLACEHOLDER;
import static com.azdegar.nlp.Person.*;
import static com.azdegar.nlp.Tense.*;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.azdegar.nlp.AzdegarAnnotations.*;
import java.util.LinkedList;

/**
 *
 * @author Afshin Pouria
 */
public class EnglishAnalyzer {

    private final List<String> logs;
    protected TypeRepository typeRepository;

    public EnglishAnalyzer(List<String> logs) {
        this.logs = logs;
    }

    public TenseVoice detectTenseVoice(Clause clause, Clause parent) {
        WordGroup wg = clause.getWords();
        int idxDo = -1;
        int idxHave = 1000;
        int idxBe = 1000;
        int idxNot = -2;
        int idxHaveTo = -1;
        int idxUseTo = -1;
        int idxMainVerb = -1;
        int idxModal = -1;
        int idxNeed = parent != null ? parent.getIdxNeed() : -1;
        int idxNegator = -1;

        for (int i = 0; i < wg.size(); i++) {
            if (wg.eql(i, "do")) {
                idxDo = i;
                idxMainVerb = i;
            } else if (wg.eql(i, "have")) {
                idxHave = i;
                idxMainVerb = i;
            } else if (wg.eql(i, "have to")) {
                idxHaveTo = i;
            } else if (wg.eql(i, "use to")) {
                idxUseTo = i;
            } else if (wg.eql(i, "be")) {
                idxBe = i;
                idxMainVerb = i;
            } else if (wg.eqlt(i, "need/VBZ?") || wg.eqlt(i, "need/MD")) {
                idxNeed = i;
                idxMainVerb = i;
            } else if (wg.eql(i, "not")) {
                idxNot = i;
                if (wg.get(i - 1).isAux() && !wg.get(i + 1).isVerb()) {
                    wg.get(i - 1).negate();
                    wg.get(i).disable();
                } else {
                    int vi = wg.findVerbAfter(i, 4);
                    if (vi != -1) {
                        if (wg.get(vi).lemma().matches("be")) {
                            wg.get(vi).negate();
                            wg.get(i).disable();
                        }

                    }
                }
            } else if (wg.get(i).eqt("MD")) {
                if (!wg.get(i).matchl("will|shall")) {
                    idxModal = i;
                    if (idxMainVerb == -1) {
                        idxMainVerb = i;
                    }
                }
            } else if (wg.get(i).matchw("never|neither|nor")) {
                idxNegator = i;
            } else if (wg.mt(i, "VB[DPZ]?")) {
                if (idxMainVerb == idxModal || idxMainVerb == idxDo || idxMainVerb == idxBe || idxMainVerb == idxHave) {
                    idxMainVerb = i;
                }
            } else if (wg.eqt(i, "VBG")) {
                if (i > idxHave || i > idxBe) {
//                    if (idxHave < idxBe || (idxHave == 1000 && idxBe != -1)) { // We’re having trouble finding that site.
                    idxMainVerb = i;
//                    } else if (idxMainVerb != -1) {
//                        wg.get(i).setTag("INF");
//                    }
                }
            } else if (wg.eqt(i, "VBN")) {
                if (i > idxHave || i > idxBe) {
//                    if (idxHave == 1000 || idxHave < idxBe) {
                    idxMainVerb = i;
//                    } else if (wg.eqt(idxBe, "INF")) {
//                        idxMainVerb = i;
//                    }
                }
            }
        }

//        if (wg.prev(idxMainVerb).eqlt("there/EX")) {
//            wg.prev(idxMainVerb).disable();
//            wg.get(idxMainVerb).setWord("exist");
//        }
        if (idxNot > -1) {
            if (wg.eql(idxNot - 1, "do")) {
                if (wg.eql(idxNot + 1, "have to")) {
                    wg.get(idxNot).disable();
                    wg.get(idxNot - 1).disable();
                    wg.get(idxNot + 1).negate();
                } else if (wg.mt(idxNot + 1, "VB[DPZ]?")) {
                    wg.get(idxNot).disable();
                    wg.get(idxNot - 1).disable();
                    wg.get(idxNot + 1).negate();
                    idxMainVerb = idxNot + 1;
                } else {
                    wg.get(idxNot).disable();
                    wg.get(idxNot - 1).negate();
                }
            } else if (wg.eql(idxNot - 1, "have")) {
                if (wg.get(idxNot + 1).eql("be")) {
                    if (wg.get(idxNot + 2).isVerb()) {
                        wg.get(idxNot + 2).negate();
                        wg.get(idxNot).disable();
                    }
                } else {
                    wg.get(idxNot + 1).negate();
                    wg.get(idxNot).disable();
                }
            } else if (wg.eqt(idxNot - 1, "MD")) {
                wg.get(idxNot).disable();
                if (wg.eql(idxNot - 1, "may")) {
                    wg.get(idxMainVerb).negate();
                } else {
                    wg.get(idxNot - 1).negate();
                }
            } else if (wg.eqwci(idxNot + 1, "to") && wg.mt(idxNot + 2, "VB")) { // 𝑻𝒐𝒎 𝒘𝒂𝒔 𝒄𝒂𝒓𝒆𝒇𝒖𝒍 𝒏𝒐𝒕 𝒕𝒐 𝒍𝒆𝒕 𝒕𝒉𝒆 𝒅𝒐𝒈 𝒔𝒊𝒕 𝒐𝒏 𝒕𝒉𝒆 𝒔𝒐𝒇𝒂.
                wg.get(idxNot).disable();
                wg.get(idxNot + 1).disable();
                wg.get(idxNot + 2).negate();
                idxMainVerb = idxNot + 2;
            } else if (idxNot == idxMainVerb - 1) {
                wg.get(idxNot).disable();
                wg.get(idxMainVerb).negate();

            }
        }

        if (idxNegator != -1) {
            if (idxModal != -1 && idxModal < idxNegator && wg.mw(idxModal, "can|could")) {
                wg.get(idxModal).negate();
            } else /*if (idxMainVerb > idxNegator)*/ {
                wg.get(idxMainVerb).negate();
            }
        }
        
        if (idxHaveTo != -1) {
            idxModal = idxHaveTo;
            if (wg.prev(idxHaveTo).eql("have") && wg.get(idxHaveTo + 1).matcht("VBP?")) {
                idxMainVerb = idxHaveTo + 1;
                wg.get(idxHaveTo + 1).setTag("VBN");
            }
        } else if (idxUseTo != -1) {
            idxModal = idxUseTo;

        }

        clause.setIdxVerb(idxMainVerb);
        clause.setIdxModal(idxModal);
        clause.setIdxNeed(idxNeed);
        if (idxDo != -1 && idxDo < idxMainVerb) {
            wg.disable(idxDo);
        }

        if (idxMainVerb == -1) { //No verb detected therefore is not a sentence
            return null;
        } else {
//            if (idxTo == idxMainVerb - 1) {
//                wg.get(idxMainVerb).setTag("INF");
//                wg.get(idxTo).setTranslation("⟨برای⟩");
//            }
        }
//        if (idxModal != -1) {
//            if (wg.get(idxModal).eqw("could")) {
//                wg.get(idxMainVerb).setTag("VBD");
//            }
//        }
        switch (wg.get(idxMainVerb).tag()) {
            case "VB":
            case "VBZ":
            case "VBP":
                clause.setTense(Present_Simple);
                if (wg.get(idxDo).eqw("did") || wg.get(idxHaveTo).eqw("had to") || wg.get(idxUseTo).eqw("used to") || wg.get(idxModal).eqw("could")) {
                    clause.setTense(Past_Simple);
                }
                if (wg.get(idxMainVerb).isAux() && wg.get(idxMainVerb + 1).eql("not")) {
                    wg.get(idxMainVerb + 1).disable();
                    wg.get(idxMainVerb).negate();
                }

                wg.get(idxMainVerb).set(PartOfSentenceAnnotation.class, "verb");
//                if (wg.get(idxMainVerb).eqw("be")) {
//                    sov.setType(SentenceType.Subjunctive);
//                }
                if (idxMainVerb > 0) {
                    if (wg.eqt(idxMainVerb - 1, "EX")) { //Existential There
                        wg.get(idxMainVerb - 1).set(PartOfSentenceAnnotation.class, "verb");
                    }
                    int w = wg.findLemma("will|shall|would", 0, idxMainVerb);
                    if (w != -1) {
                        if (wg.mw(w, "will|shall|'ll")) {
                            clause.setTense(Future_Simple);
                        } else {
                            WordGroup wge = wg;
                            int vi = wg.findVerbBefore(w, 5);
                            if (vi == -1 && parent != null) { // He said he would see his brother tomorrow.
                                vi = parent.words().findVerbBefore(parent.words().size() - 1);
                                if (vi != -1) {
                                    wge = parent.words();
                                }
                            }
                            if (vi == -1) {
                                if (w == 0) { // Would you like me to come with you?

                                } else {
                                    clause.setTense(Present_Simple_Imagine);
                                }
                            } else {
                                if (wge.get(vi).eqt("VBD")) {
                                    clause.setTense(Future_Simple);
                                }
                            }
                        }
                        int n = wg.findLemma("not", w, idxMainVerb);
                        if (n != -1) {
                            wg.get(n).disable();
                            wg.get(idxMainVerb).negate();
                        }
                        if (w != 0 || !wg.get(w).eqw("would")) {
                            wg.get(w).disable();
                        }
                    } else {
                        int d = wg.findLemma("do", idxHaveTo + 1, idxMainVerb);
                        if (d != -1) {
                            ExtWord does = wg.get(d);
                            if (does.eqw("did")) {
                                clause.setTense(Past_Simple);
                            } else {
                                clause.setTense(Present_Simple);
                            }
//                            int n = wg.findLemma("not", d, idxMainVerb);
//                            if (n != -1) {
//                                wg.get(n).disable();
//                                wg.get(idxMainVerb).negate();
//                            }
                            wg.get(d).disable();
                        }
                    }
                }
                break;

            case "VBD":
                clause.setTense(Past_Simple);
                wg.get(idxMainVerb).set(PartOfSentenceAnnotation.class, "verb");
                if (idxMainVerb > 0) {
                    if (wg.eqt(idxMainVerb - 1, "EX")) { //Existential There
                        wg.get(idxMainVerb - 1).set(PartOfSentenceAnnotation.class, "verb");
                    }
                }
                if (wg.get(idxMainVerb).isAux()) {
                    if (wg.get(idxMainVerb + 1).eqw("not")) {
                        wg.get(idxMainVerb + 1).disable();
                        wg.get(idxMainVerb).negate();
                    }

                } else {
                    int d = wg.findLemma("do", 0, idxMainVerb);
                    int a = wg.findLemma("be", 0, idxMainVerb); //Your car could be sold.
                    int n = wg.findLemma("not", d, idxMainVerb);
                    if (a != -1) {
                        clause.setVoice(Voice.Passive);
                        wg.get(a).disable();
                        wg.get(a).set(PartOfSentenceAnnotation.class, "verb");
                        if (idxModal == a - 1) {
                            clause.setTense(Present_Simple);
                        }
                    }
                    if (n != -1) {
                        wg.get(n).disable();
                        wg.get(idxMainVerb).negate();
                    }
                }
                break;
            case "VBN":
                if (idxMainVerb > 0) {
                    int h = wg.findLemma("have", 0, idxMainVerb);
                    if (h != -1) {
                        ExtWord have = wg.get(h);
                        int b = wg.findWord("being", h + 1, idxMainVerb);
                        if (have.matchw("[Hh]ad")) {
                            if (b == -1) {
                                clause.setTense(Tense.Past_Perfect);
                                b = wg.findWord("been", h + 1, idxMainVerb);
                                if (b != -1) {
                                    clause.setVoice(Voice.Passive);
                                    wg.disable(b);
                                    wg.get(b).set(PartOfSentenceAnnotation.class, "verb");
                                }
                            } else {
                                clause.setTense(Tense.Past_Perfect_Continuous);
                                clause.setVoice(Voice.Passive);
                                wg.disable(b);
                                wg.get(b).set(PartOfSentenceAnnotation.class, "verb");
                                b = wg.findWord("been", h + 1, b);
                                if (b != -1) {
                                    wg.disable(b);
                                    wg.get(b).set(PartOfSentenceAnnotation.class, "verb");
                                }
                            }
                        } else {
                            if (b == -1) {
                                int w = wg.findWord("would", 0, h);
                                if (w != -1) {
                                    clause.setTense(Present_Perfect_Imagine);
                                    wg.disable(w);
                                    wg.get(w).set(PartOfSentenceAnnotation.class, "verb");
                                    if (w == idxModal) {
                                        clause.setIdxModal(-1);
                                    }
                                } else {
                                    clause.setTense(Tense.Present_Perfect);
                                }
                                b = wg.findWord("been", h + 1, idxMainVerb);
                                if (b != -1) {
                                    clause.setVoice(Voice.Passive);
                                    wg.disable(b);
                                    wg.get(b).set(PartOfSentenceAnnotation.class, "verb");
                                }
                                if (idxMainVerb > 1 && wg.eqw(idxMainVerb, "been")) {
//                                    clause.setVoice(Voice.Passive);
                                    if (wg.eqt(idxMainVerb - 2, "EX")) {
                                        wg.get(idxMainVerb - 2).set(PartOfSentenceAnnotation.class, "verb");
                                    }
                                }

                            } else {
                                clause.setTense(Tense.Present_Perfect_Continuous);
                                clause.setVoice(Voice.Passive);
                                wg.disable(b);
                                b = wg.findWord("been", h + 1, b);
                                if (b != -1) {
                                    wg.disable(b);
                                    wg.get(b).set(PartOfSentenceAnnotation.class, "verb");
                                }
                            }
                        }

//                        int n = wg.findLemma("not", h + 1, idxMainVerb);
//                        if (n != -1 ) {
//                            wg.get(idxMainVerb).negate();
//                            wg.disable(n);
//                        }
                        wg.disable(h);
                        wg.get(h).set(PartOfSentenceAnnotation.class, "verb");
                    } else {
                        int w = wg.findWord("will|'ll|shall", 0, idxMainVerb);
                        if (w != -1) {
                            clause.setVoice(Voice.Passive);
                            int b = wg.findWord("being", w + 1, idxMainVerb);
                            if (b != -1) {
                                clause.setTense(Future_Continuous);
                                wg.disable(b);
                                b = wg.findLemma("be", w + 1, b);
                                wg.disable(b);
                                wg.get(b).set(PartOfSentenceAnnotation.class, "verb");
                            } else {
                                clause.setTense(Future_Simple);
                                b = wg.findLemma("be", w + 1, idxMainVerb);
                                wg.disable(b);
                            }
                            int n = wg.findLemma("not", w + 1, idxMainVerb);
                            if (n != -1) {
                                wg.get(idxMainVerb).negate();
                                wg.disable(n);
                            }
                            wg.disable(w);

                        } else {
                            int a = wg.findLemma("be", 0, idxMainVerb);
                            if (a != -1) {
                                clause.setVoice(Voice.Passive);
                                if (clause.get(a).eqt("INF")) {
                                    clause.setTense(Infinitive);
                                }
                            }

                            int b = wg.findWord("being", a + 1, idxMainVerb);
                            if (b != -1) {
                                if (wg.get(a).matchw("was|were")) {
                                    clause.setTense(Past_Continuous);
                                } else {
                                    clause.setTense(Present_Continuous);
                                }
                                wg.disable(a);
                                wg.disable(b);
                                wg.get(a).set(PartOfSentenceAnnotation.class, "verb");
                                wg.get(b).set(PartOfSentenceAnnotation.class, "verb");

                            } else {
                                ExtWord aux = wg.get(a);
                                if (aux.matchw("was|were")) {
                                    clause.setTense(Tense.Past_Simple);
                                } else if (clause.getTense() != Infinitive) {
                                    clause.setTense(Tense.Present_Simple);
                                    if (aux.eqw("be")) {
                                        clause.setType(SentenceType.Subjunctive);
                                    }
                                }
                                if (clause.getTense() != Infinitive) {
                                    if (aux.word().toLowerCase().matches("is|was")) {
                                        clause.setPerson(THIRD_SINGULAR);
                                    } else if (aux.word().equalsIgnoreCase("am")) {
                                        clause.setPerson(FIRST_SINGULAR);
                                    }
                                } else {
                                    clause.setPerson(INFINITIVE);
                                }
                            }
                            int n = wg.findLemma("not", a + 1, idxMainVerb);
                            if (n != -1) {
                                wg.get(idxMainVerb).negate();
                                wg.disable(n);
                                wg.get(n).set(PartOfSentenceAnnotation.class, "verb");
                            }
                            wg.disable(a);
                            wg.get(a).set(PartOfSentenceAnnotation.class, "verb");
                        }
                    }
                    wg.get(idxMainVerb).setWord(wg.get(idxMainVerb).lemma());
                    wg.get(idxMainVerb).set(PartOfSentenceAnnotation.class, "verb");
                }
                break;

            case "VBG":
                if (idxMainVerb > 0) {
                    int b = wg.findLemma("be", 0, idxMainVerb);
                    if (b != -1) {
                        ExtWord be = wg.get(b);
                        int h = wg.findLemma("have", 0, idxMainVerb - 1);
                        int n = wg.findLemma("not", h, idxMainVerb);
                        if (n != -1) {
                            wg.get(n).disable();
                            wg.get(idxMainVerb).negate();
                        }

                        if (be.matchw("am|'m|are|'re|is|'s")) {
                            clause.setTense(Present_Continuous);
                        } else if (be.matchw("was|were")) {
                            clause.setTense(Past_Continuous);
                        } else if (be.eqw("be")) {
                            int w = wg.findLemma("will|shall", 0, idxMainVerb);
                            if (w != -1) {
                                clause.setTense(Tense.Future_Continuous);
                                wg.get(w).disable();
                            }
                        } else if (be.eqw("been")) {
                            if (idxMainVerb > 1) {
                                if (h != -1) {
                                    if (wg.get(h).eqw("had")) {
                                        clause.setTense(Past_Perfect_Continuous);
                                    } else {
                                        clause.setTense(Present_Perfect_Continuous);
                                    }
                                    wg.get(h).disable();
                                }
                            }
                        }
                        wg.get(idxMainVerb).setWord(wg.get(idxMainVerb).lemma());
                        wg.get(idxMainVerb).set(PartOfSentenceAnnotation.class, "verb");
                        be.disable();
                    }
                } else {
                    wg.get(idxMainVerb).setTag("INF");
                    clause.setIdxVerb(-1);
                }
                break;
            case "MD":
                clause.setTense(Present_Simple);
                if (wg.get(idxMainVerb).matchw("could|should")) {
                    clause.setTense(Past_Simple);
                }
                if (wg.get(idxMainVerb + 1).eqw("not")) {
                    wg.get(idxMainVerb + 1).disable();
                    wg.get(idxMainVerb).negate();
                }
                break;
        }
        if (wg.get(idxMainVerb).isNegative() || wg.get(idxNeed).isNegative()) {
            int idxAny = wg.findLemma("any(thing|body|one)?", idxMainVerb, wg.size());
            if (idxAny != -1) {
                wg.get(idxAny).negate();
                wg.get(idxAny + 1).setUnspecified(true);
            }
        }
        return new TenseVoice(clause.tense(), clause.voice());
    }

    public void detectType(Clause clause, Clause parent, boolean dependent) {
        ExtWord start = clause.getEffectiveStart(true);
        boolean subjunctive = clause.getVerb().eqw("be") && clause.getTense() == Present_Simple;
        if (parent != null && parent.isCausative()) {
            subjunctive = true;
        } else if (!dependent && clause.words().indexOfLemma("have to") != -1) {
            subjunctive = true;
        }
//        if (clause.getEnd().eqw("?")) {
//            clause.setType(SentenceType.Interrogative);
//        }
//        if (start.getPhraseInfo() != null && !dependent) {
//            if (start.getPhraseInfo().getType().startsWith("SQ")) {
//                clause.setType(SentenceType.Interrogative);
//                return;
//            } else if (start.getPhraseInfo().getType().equals("WHNP")) {
//                clause.setType(SentenceType.Interrogative);
//                if (start.word().equals("Who")) {
//                    start.setTag("WPQ");
//                }
//                return;
//            }
//        }
//        if (start.eqt("WP") && !start.word().toLowerCase().startsWith("who")) {
//            if (clause.get(start.localIndex() + 1).isAux()) {
//                clause.setType(SentenceType.Interrogative);
//                start.setTag("WPQ");
//            } else {
//                start.setTag("CC");
//            }
//        } else if (start.eqt("WRB")) {
//            if (clause.get(start.localIndex() + 1).isAux()) {
//                clause.setType(SentenceType.Interrogative);
//            }
//        } else

        if (start.eqw("be")) {
            clause.setType(SentenceType.Imperative);
        } else if (start.matchl("do|have")) {
            if (hasVerb(clause.words(), start.localIndex() + 1)) {
                clause.setType(SentenceType.Interrogative);
            }
        } else if (start.isModal() || (start.isAux() && !clause.prev(start.localIndex()).eqw("there"))) {
            if (start.word().matches("[A-W][a-z]+")) { //Am ... Were
                clause.setType(SentenceType.Interrogative);
            }
        } else {
            int j = start.localIndex();
            while (clause.get(j).matcht("CC|JJ|RB|UH")) { // Please/UH let me ...
                j++;
            }
            if ((!dependent || (clause.getParent() != null && clause.getParent().getType() == SentenceType.Imperative)) && clause.get(j).eqt("VB")) {
                clause.setType(SentenceType.Imperative);
            } else {
                clause.setType(SentenceType.Affirmative);
            }

        }
        if (subjunctive) {
            if (clause.getType() == SentenceType.Interrogative) {
                clause.setType(SentenceType.InterrogativeSubjunctive);
            } else if (clause.getType() == SentenceType.Affirmative) {
                clause.setType(SentenceType.Subjunctive);
            }
        }
    }

    private Person identifyPersonByWord(ExtWord word) {
        if (word.tag().matches("NNP?")) {
            return Person.THIRD_SINGULAR;
        }
        if (word.tag().matches("NNP?S")) {
            return Person.THIRD_PLURAL;
        }

        switch (word.word().toLowerCase()) {
            case "i":
            case "me":
                return Person.FIRST_SINGULAR;
            case "a":
            case "an":
            case "he":
            case "she":
            case "it":
            case "himself":
            case "herself":
            case "itself":
            case "this":
            case "that":
            case "who":
            case "one":
            case "everyone":
            case "someone":
            case "somebody":
            case "nobody":
                return Person.THIRD_SINGULAR;
            case "we":
            case "us":
            case "many of us":
                return Person.FIRST_PLURAL;
            case "you":
                return Person.SECOND_PLURAL;
            case "they":
            case "these":
            case "those":
            case "those who":
            case "many":
            case "themselves":
            case "so many":
                return Person.THIRD_PLURAL;
            default:
                return Person.NONE;
        }

    }

    public Person identifyPerson(WordGroup wg) {
        if (wg == null || wg.isEmpty()) {
            return Person.NONE;
        }
        int a = wg.findWord("and", 1, wg.size());
        if (a != -1) {
            int i = wg.findWordTag("I/PRP", 0, wg.size());
            if (i != -1) {
                return Person.FIRST_PLURAL;
            } else {
                i = wg.findWord("you", 0, wg.size());
                if (i != -1) {
                    return Person.SECOND_PLURAL;
                } else {
                    i = wg.findTag("PRP\\$?|NNP?S?", 0, wg.size());
                    if (i != -1) {
                        return Person.THIRD_PLURAL;
                    }
                }
            }
        }
        if (wg.getEffectiveStart().matchw("an?")) {
            return Person.THIRD_SINGULAR;
        }
        int i = wg.size() - 1;
        if (wg.size() > 1 && wg.get(i).eqt("DT")) { // We all/DT argued about it for hours.
            i--;
        }
        return identifyPersonByWord(wg.get(i));
    }

    public Person identifyPerson(Clause clause, Clause main, Clause parent) {
        if (clause.getPerson() != null && clause.getPerson() != Person.NONE) {
            return clause.getPerson();
        }
        if (clause.getType().equals(SentenceType.Imperative)) {
            clause.setPerson(Person.SECOND_PLURAL);
            return clause.getPerson();
        }

        if (clause.getVerb().matchw("is|was")) {
            return Person.THIRD_SINGULAR;
        }
        if (clause.part("subj") != null && !clause.part("subj").isEmpty()) {
            Person p = identifyPerson(clause.part("subj"));
            if (!p.equals(Person.NONE)) {
                return p;
            }
        }
        if (clause.getIdxVerb() == -1) {
            if (clause.getPerson() != null && !clause.getPerson().equals(Person.NONE)) {
                return clause.getPerson();
            } else {
                int i = 0;
                while (i < clause.length() && clause.get(i).matcht("JJR?|RB|DT")) {
                    i++;
                }
                if (i < clause.length() && !clause.get(i).word().startsWith("〔")) {
                    return identifyPersonByWord(clause.get(i));
                }
                return Person.NONE;
            }
        } else {
            int i = clause.getIdxVerb();
            if (clause.getIdxNeed() != -1) {
                i = clause.getIdxNeed();
            }
            if (clause.getIdxModal() != -1 && clause.getType() != SentenceType.Interrogative && clause.getType() != SentenceType.InterrogativeSubjunctive) { //𝑪𝒂𝒏 𝑰 𝒋𝒖𝒔𝒕 𝒃𝒐𝒓𝒓𝒐𝒘 𝒕𝒉𝒆 𝒔𝒄𝒊𝒔𝒔𝒐𝒓𝒔 𝒇𝒐𝒓 𝒂 𝒔𝒆𝒄𝒐𝒏𝒅?
                i = clause.getIdxModal();
            }

            if (i == 0) {
                if (parent != null && parent.isCausative()) {
                    clause.setPerson(identifyPerson(parent.parts().get("dobj")));
                }
            } else {
                WordGroup wg = clause.words();

                ExtWord prev = wg.prev(i);
                if (clause.getType() == SentenceType.Interrogative || clause.getType() == SentenceType.InterrogativeSubjunctive) {
//                sov.setPerson(detectPersonFromIndex(wg, i + 1));
                    clause.setPerson(identifyPersonByWord(wg.get(1))); // Do you / Is he
                    return clause.getPerson();
                } else {
                    while (prev.matcht("RB|JJ|MD|PRP\\$") || prev.matchw("neither") || prev.matchw(CLAUSE_PLACEHOLDER)) { // He'd have 𝒂𝒍𝒓𝒆𝒂𝒅𝒚 left. / I've 𝒋𝒖𝒔𝒕 𝒂𝒃𝒐𝒖𝒕 finished painting the living room.
                        prev = wg.prev(prev.localIndex());
                    }
                }

                if (prev.matcht("PRP|IN")) {
                    if (prev.eqw("I") && clause.getVerb().matchw("is|does|has")) { // I is an interpretation for B.
                        clause.setPerson(Person.THIRD_SINGULAR);
                    } else {
                        clause.setPerson(identifyPersonByWord(prev));
                    }
                } else if (prev.tag().matches("NNS")) {
                    ExtWord we = wg.prev(prev.getLocalIndex());
                    if (we.eql("we")) {
                        clause.setPerson(Person.FIRST_PLURAL);
                    }
                } else if (prev.tag().startsWith("NN") || prev.eqt("SYM")) {
                    boolean singular = prev.matcht("NNP?") || prev.eqt("SYM");

                    int j = i - 1;
                    while (j > 0 && !clause.get(j).matchw("[,:;]")) {
                        if (clause.get(j).eqt("NNS") || clause.get(j).eqw("and")) {
                            singular = false;
                        }
                        j--;
                    }
                    if (singular) {
                        clause.setPerson(THIRD_SINGULAR);
                    } else {
                        clause.setPerson(THIRD_PLURAL);
                    }

                } else if (prev.matchw(CLAUSE_PLACEHOLDER)) {
                    int clauseId = Integer.valueOf(prev.word().substring(1, prev.word().length() - 1));
                    Clause sub = main.getSub(clauseId);

                    if (sub != null && sub.indexOf("let us") != -1) {
                        clause.setPerson(FIRST_PLURAL);
                    } else if (sub != null && sub.indexOf("let me") != -1) {
                        clause.setPerson(FIRST_SINGULAR);
                    } else {
                        prev = wg.prev(prev.localIndex());
                        while (prev.matchw(CLAUSE_PLACEHOLDER) || prev.matchw("[,:;]")) {
                            prev = wg.prev(prev.localIndex());
                        }
                        if (prev.eqt("NNP") || prev.word().toLowerCase().matches("everyone|someone")) {
                            clause.setPerson(THIRD_SINGULAR);
                        } else if (prev.eqt("NNS")) {
                            clause.setPerson(THIRD_PLURAL);
                        }
                    }
                } else {
                    if (clause.getVerb().matchw("does|has|is|was") || clause.getVerb().eqt("VBZ")) {
                        clause.setPerson(Person.THIRD_SINGULAR);
                    }
                    if (clause.getPerson() == null || clause.getPerson().equals(Person.NONE)) {
                        if (clause.getType().equals(SentenceType.Imperative)) {
                            clause.setPerson(Person.SECOND_PLURAL);
                        }
                    }
                }
            }
            if (clause.getPerson() == Person.NONE) {
                if (clause.getParent() != null) {
                    if (clause.getParent().words().findLemma("let us", 0, 10) != -1) {
                        clause.setPerson(FIRST_PLURAL);
                        clause.setType(SentenceType.Imperative);
                    } else if (clause.getParent().words().findLemma("let me", 0, 10) != -1) {
                        clause.setPerson(FIRST_SINGULAR);
                        clause.setType(SentenceType.Imperative);
                    } else if (clause.getParent().isCausative()) {
                        clause.setPerson(identifyPersonByWord(clause.getParent().words().get(clause.getParent().getIdxVerb() + 1)));
                    } else {
                        clause.setPerson(clause.getParent().getPerson());
                    }
                }
            }
            return clause.getPerson();
        }
    }

    private boolean hasVerb(List tokens, int begin) {
        for (int i = begin; i < tokens.size(); i++) {
            if (((CoreLabel) tokens.get(i)).tag().startsWith("VB")) {
                return true;
            }
        }
        return false;
    }

    public ExtWord identifyQuantifier(Clause clause) {
        if (clause.getIdxVerb() < 1) {
            return null;
        } else {
//            if (clause.isActive()) {
            int i = 0;
            while (i < clause.getIdxVerb()) {
                if (clause.get(i).matchw("all|every|each|some|no")) {
                    return clause.get(i);
                }
                i++;
            }
//            }
        }
        return null;
    }

    public Map<String, WordGroup> identifyParts(Clause clause) {
        ExtWord endingWord = clause.getEndingWord();
        if (clause.getIdxVerb() == -1 /*|| clause.getIdxVerb() == endingWord.getLocalIndex()*/) {
            return new LinkedHashMap();
        }
        int s1 = clause.getEffectiveStart().localIndex();
        int e1 = clause.getIdxVerb();
        int s2 = e1 + 1;
        while (clause.get(e1 - 1).isDisabled()) {
            e1--;
        }
        int e2 = endingWord.localIndex();

        if (typeRepository != null) {
            clause.words().forEach(w -> {
                List<String> types = typeRepository.find(w.lemma(), w.tag());
                if (types != null && !types.isEmpty()) {
                    w.set(AzdegarAnnotations.TypeAnnotation.class, types);
                }
            });
        }
        identifyAdverbs(clause.words(), s1, e1);
        identifyAdverbs(clause.words(), s2, e2);
        if (clause.getVerb().eql("be") && clause.getTense() == Present_Simple) {
            if (clause.get(e1 - 1).eqlt("there/EX")) { // 𝑻𝒉𝒆𝒓𝒆 𝒂𝒓𝒆 𝒚𝒐𝒖𝒕𝒉 𝒉𝒐𝒔𝒕𝒆𝒍𝒔 𝒏𝒆𝒂𝒓𝒃𝒚.

                clause.get(e1 - 1).disable();
                for (int i = s2; i <= e2; i++) {
                    if (clause.get(i).partOfSentence() == null || clause.get(i).partOfSentence().isBlank()) {
                        clause.get(i).set(PartOfSentenceAnnotation.class, "subj");
                    }
                }
            } else if (clause.get(s2).eqt("IN")) { // If John is in Paris he is in France.
                identifyPartsActive(clause, s1, e1, s2, e2);
            } else {
                clause.getVerb().set(PartOfSentenceAnnotation.class, "copula");
                identifyPartsPredicate(clause, s1, e1, s2, e2);
            }
        } else {
            if (clause.isActive()) {
                identifyPartsActive(clause, s1, e1, s2, e2);
            } else {
                identifyPartsPassive(clause, s1, e1, s2, e2);
            }
        }

        Map<String, WordGroup> m = new LinkedHashMap();
        for (ExtWord w : clause.words()) {
            if (!w.isDisabled() && w.partOfSentence() != null && !w.partOfSentence().isBlank()) {
                WordGroup wg = m.get(w.partOfSentence());
                if (wg == null) {
                    wg = new WordGroup();
                    m.put(w.partOfSentence(), wg);
                }
                wg.add(w);
            }
        }
        if (clause.isPassive() && m.get("predicate") == null) {
            WordGroup predicate = new WordGroup(m.get("verb"));
            if (m.get("comp") != null) {
                predicate.addAll(m.get("comp"));
            }
            m.put("predicate", predicate);
        }
        clause.setParts(m);
        return m;
    }

    public void identifyPartsActive(Clause clause, int s1, int e1, int s2, int e2) {
        int i = s1;
        while (i < e1 && (clause.get(i).isDisabled() || clause.get(i).eqp("adv") || clause.get(i).matcht("IN|CC") || clause.get(i).isPlaceHolder())) {
            i++;
        }
        while (i < e1 && !clause.get(i).eqp("adv") && !clause.get(i).eqt("MD")) {
            clause.get(i).set(PartOfSentenceAnnotation.class, "subj");
            i++;
        }

        int k = s2;
        i = s2;
        int phraseId = clause.get(s2).phraseId();
        while (k <= e2 && i == s2) {
            if (clause.get(k).phraseId() > phraseId) {
                if (clause.get(k).eqw("of")) {
                    phraseId = clause.get(k + 1).phraseId();
                } else {
                    i = k;
                }
            } else if (clause.get(k).matchw("and|or")) {
                phraseId = clause.get(k + 1).phraseId();
            }
            k++;
        }
        if (clause.get(i).phraseType().equals("PP")) { // The old man placed the new parcel on the table.
            for (int j = s2; j < i; j++) {
                if (clause.get(j).partOfSentence() == null || clause.get(j).partOfSentence().isBlank()) {
                    clause.get(j).set(PartOfSentenceAnnotation.class, "dobj");
                }
            }
            List types = extractTypes(clause.words(), i + 1, e2 + 1);
            String pos = "obj complement";
            if (types.contains("Person")) {
                pos = "iobj";
            }
            for (int j = i + 1; j < e2 + 1; j++) {
                if (clause.get(j).partOfSentence() == null || clause.get(j).partOfSentence().isBlank()) {
                    clause.get(j).set(PartOfSentenceAnnotation.class, pos);
                }
            }
        } else { // 𝑺𝒖𝒆 𝒑𝒂𝒔𝒔𝒆𝒅 𝑨𝒏𝒏 𝒕𝒉𝒆 𝒃𝒂𝒍𝒍.
            int e = i;
            while (e <= e2 && !clause.get(e).eqp("adv")
                    && !clause.get(e).eqwt("by/IN") // 𝑯𝒆 𝒗𝒆𝒏𝒕𝒆𝒅 𝒉𝒊𝒔 𝒂𝒏𝒈𝒆𝒓 𝒃𝒚/IN 𝒌𝒊𝒄𝒌𝒊𝒏𝒈 𝒕𝒉𝒆 𝒅𝒐𝒐𝒓.
                    && !clause.get(e).matcht("VB[DPZ]?")
                    && !clause.get(e).matchw(CLAUSE_PLACEHOLDER)) {
                e++;
            }

            for (int j = s2; j < i; j++) {
                if (!clause.get(j).eqt("IN")) {
                    if (clause.get(j).partOfSentence() == null || clause.get(j).partOfSentence().isBlank()) {
                        clause.get(j).set(PartOfSentenceAnnotation.class, "iobj");
                    }
                }
            }
            for (int j = i; j < e; j++) {
                if (!clause.get(j).eqt("IN")) {
                    if (clause.get(j).partOfSentence() == null || clause.get(j).partOfSentence().isBlank()) {
                        clause.get(j).set(PartOfSentenceAnnotation.class, "dobj");
                    }
                }
            }
        }
    }

    public void identifyPartsPassive(Clause clause, int s1, int e1, int s2, int e2) {
        int i = s1;
        while (i < e1 && (clause.get(i).isDisabled() || clause.get(i).eqp("adv") || clause.get(i).eqt("IN"))) {
            i++;
        }
        while (i < e1 && !clause.get(i).eqp("adv") && !clause.get(i).eqt("MD")) {
            clause.get(i).set(PartOfSentenceAnnotation.class, "dobj");
            i++;
        }
        int by = clause.words().findWord("by", e1, e2); // This house was built by my uncle twenty years ago.
        if (by != -1) {
            i = by + 1;
            while (i <= e2 && clause.get(i).depth() > clause.get(by).depth() && !clause.get(i).eqp("adv")) {
                clause.get(i).set(PartOfSentenceAnnotation.class, "subj");
                i++;
            }
        } else {
            i = s2;
            while (i <= e2 && !clause.get(i).eqp("adv")) {
                clause.get(i).set(PartOfSentenceAnnotation.class, "comp");
                i++;
            }
        }
    }

    public void identifyAdverbs(WordGroup wg, int s, int e) {
        int i = s;

        while (i <= e) {
            if ((wg.get(i).eqt("RB") && !wg.get(i).eql("not")) || wg.get(i).eqw("please")) {
                if (!wg.get(i - 1).eqt("DT")) {
                    wg.get(i).set(PartOfSentenceAnnotation.class, "adv");
                    if (i + 1 <= e && wg.get(i + 1).eqt("JJ")) {
                        wg.get(i + 1).set(PartOfSentenceAnnotation.class, "adv");
                        i++;
                    }
                }
            } else if (wg.get(i).matchw("at|in|on|for") && wg.get(i).getPhraseInfo() != null && wg.get(i).getPhraseInfo().getType().equals("PP")) {  // at this time
                int t = wg.findType("Time", i + 1, i + 4);
                if (t != -1) {
                    wg.get(i).set(PartOfSentenceAnnotation.class, "adv");
                    int j = i + 1;
                    while (wg.get(j).depth() > wg.get(i).depth()) {
                        wg.get(j).set(PartOfSentenceAnnotation.class, "adv");
                        j++;
                    }
                    i = j - 1;
                }
            } else if (i > 0 && wg.get(i).matchw("seconds?|minutes?|hours?|days?|weeks?|months?|years?|mornings?|nights?")) {
                if (wg.get(i - 1).matcht("DT|CD")) {
                    wg.get(i).set(PartOfSentenceAnnotation.class, "adv");
                    wg.get(i - 1).set(PartOfSentenceAnnotation.class, "adv");
                }
                if (wg.get(i + 1).matchw("ago")) {
                    i++;
                    wg.get(i).set(PartOfSentenceAnnotation.class, "adv");
                }
//            } else if (wg.get(i).matchw("next|this|last")) {
//                if (i + 1 <= e && wg.get(i + 1).matchw("seconds?|minutes?|hours?|days?|weeks?|months?|years?")) {
//                    for (int j = i; j <= i + 1; j++) {
//                        wg.get(j).set(PartOfSentenceAnnotation.class, "adv");
//                    }
//                    i++;
//                }
            } else if (wg.get(i).matchw("today|tomorrow|tonight|yesterday")) {
                wg.get(i).set(PartOfSentenceAnnotation.class, "adv");
            }
            i++;
        }
    }

    public void identifyPartsPredicate(Clause clause, int s1, int e1, int s2, int e2) {
        int i;
        if (clause.getType() == SentenceType.Interrogative) { //Is it too cold over there?
            i = s2;
            String phraseType = clause.get(i).phraseType();
            while (i <= e2 && !phraseType.equals("ADVP")) {
                if (phraseType.equals("NP")) {
                    clause.get(i).set(PartOfSentenceAnnotation.class, "subj");
                } else { // ADJP
                    clause.get(i).set(PartOfSentenceAnnotation.class, "predicate");
                }
                i++;
                phraseType = clause.get(i).phraseType();
            }
        } else {
            i = s1;
            while (i < e1 && (clause.get(i).isDisabled() || clause.get(i).matcht("IN|,") || clause.get(i).eqp("adv"))) {
                i++;
            }
            while (i < e1 && !clause.get(i).eqp("adv")) {
                clause.get(i).set(PartOfSentenceAnnotation.class, "subj");
                i++;
            }

            PhraseInfo pinfo = clause.get(e2).getPhraseInfo();
            while (e2 < clause.length() && !clause.get(e2).matcht("IN|,|\\.") && (pinfo != null && (clause.get(e2).phraseId() == pinfo.getNumber() || clause.get(e2).phraseDepth() > pinfo.getDepth()))) {
                e2++;
            }
            if (clause.get(s2).eqt("RB")) {
                s2++;
            }
            for (i = s2; i < e2; i++) {
                clause.get(i).set(PartOfSentenceAnnotation.class, "predicate");
            }
        }
    }

    private List extractTypes(WordGroup wg, int s, int e) {
        List types = new LinkedList();
        for (int j = s; j < e; j++) {
            List l = wg.get(j).get(TypeAnnotation.class);
            if (l != null && !l.isEmpty()) {
                types.addAll(l);
            }
        }
        return types;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setTypeRepository(TypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

}
