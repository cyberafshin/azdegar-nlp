package com.azdegar.nlp;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static java.lang.Math.min;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.common.ParserGrammar;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;

import com.azdegar.nlp.tagfix.TagRules;
import com.azdegar.nlp.tagfix.Fixable;
import com.azdegar.nlp.transform.Transformable;
import com.azdegar.nlp.tagfix.A;
import com.azdegar.nlp.tagfix.About;
import com.azdegar.nlp.tagfix.As;
import com.azdegar.nlp.tagfix.Connective;
import com.azdegar.nlp.tagfix.Even;
import com.azdegar.nlp.tagfix.Have;
import com.azdegar.nlp.tagfix.I;
import com.azdegar.nlp.tagfix.Let;
import com.azdegar.nlp.tagfix.Make;
import com.azdegar.nlp.tagfix.Time;
import com.azdegar.nlp.tagfix.What;
import com.azdegar.nlp.transform.At;
import com.azdegar.nlp.transform.ToInfinitive;
import java.util.LinkedList;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import static com.azdegar.nlp.EnglishGrammar.GRAM_ING;
import static com.azdegar.nlp.EnglishGrammar.GRAM_INGTO;
import static com.azdegar.nlp.EnglishGrammar.NOUNS_ENDING_ING;
import com.azdegar.nlp.tagfix.Chunk;
import com.azdegar.nlp.tagfix.Lot;
import com.azdegar.nlp.tagfix.That;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Sentence;
import java.io.File;
import java.util.Collection;

/**
 *
 * @author Afshin Pouria
 * @since May 13, 2016
 */
public class Parser {

    protected final ParserGrammar grammar;
    protected final MaxentTagger tagger;
    protected MultiWordRepository multiWordRepository;
    protected Map<String, Set> phrasalVerbs = new LinkedHashMap();

    private List<CoreLabel> lemmatized;

    public static final String CLAUSE_PLACEHOLDER = "〔\\d+〕";
    public static boolean DEV_MODE = System.getProperty("os.version").startsWith("6.5.0-");

    public Parser(MaxentTagger tagger, ParserGrammar grammar) {
        this.tagger = tagger;
        this.grammar = grammar;
    }

    private void echoCoreLabels(List<CoreLabel> list) {
        if (DEV_MODE) {
            String s = list.stream().map(label -> (label.value() == null) ? ((CoreLabel) label).word() + "/" + ((CoreLabel) label).tag() : label.value()).collect(Collectors.joining(" "));
            System.out.println(s);
        }
    }

    private void echoTaggedWords(List<TaggedWord> list) {
        if (DEV_MODE) {
            String s = list.stream().map(label -> label.word() + "/" + label.tag()).collect(Collectors.joining(" "));
            System.out.println(s);
        }
    }

    public Map<Integer, Clause> parse(String text, List<String> logs) throws Exception {
        text = text.replaceAll("(.+)(\\.\\))(.+?\\.)", "$1)$3");
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(substitute(text)));
        Map<Integer, Clause> mapClauses = new LinkedHashMap();
        int nsent = 0;
        for (List<HasWord> sentence : tokenizer) {

            List<TaggedWord> tagged = tagger.tagSentence(sentence);
            int start = findActualStart(tagged);
            fixTaggerBugs(tagged, start);
            mergeHyphen(tagged);
            fixWrongAdjective(tagged, start);

            tagged = mergeSymbols(tagged);
            mergePhrases(tagged);
            lemmatized = grammar.lemmatize(tagged);
            fixLemmatizerBugs(lemmatized);
//            List<CoreLabel> otherVerb = 
            checkMultiVerb(lemmatized);
//            System.out.println(otherVerb);

            transform(lemmatized);
            Map<Integer, Parenthetical> brackets = brackets(lemmatized, nsent + 1);
            Map<Integer, List<Parenthetical>> decomposed = new LinkedHashMap();
            brackets.forEach((key, value) -> {
                List<Parenthetical> firsts = decompose(value);
                List<Parenthetical> seconds = new ArrayList();
                firsts.forEach(first -> {
                    seconds.addAll(decompose(first));
                });

                decomposed.put(key, seconds);
            });
            Clause parent = null;

            for (Integer key : decomposed.keySet()) {
                int i = 0;
                for (Parenthetical part : decomposed.get(key)) {
                    Tree tree = grammar.parse(part);
//                    semanticGraph(tree);
                    if (tree.firstChild() != null) {
                        StringWriter sw = new StringWriter();
                        tree.firstChild().pennPrint(new PrintWriter(sw), label -> (label.value() == null) ? ((CoreLabel) label).word() : label.value());
                        String s = sw.toString();
                        if (s.endsWith("\n")) {
                            s = s.substring(0, s.length() - 1);
                        }
                        if (logs != null) {
                            logs.add(s.replace("\n", "<br/>").replace("  ", "&nbsp;&nbsp;&nbsp;&nbsp;"));
                        }
                        try {
                            Clause clause = structural(tree);
                            if (part.getOpening() != null) {
                                clause.setOpening(new ExtWord(part.getOpening()));
                            }
                            if (part.getClosing() != null) {
                                clause.setClosing(new ExtWord(part.getClosing()));
                            }

                            if (part.get(0).word().toLowerCase().startsWith("let ") || part.get(0).word().equalsIgnoreCase("let")) {
                                parent = clause;
                            }
                            if (parent == null) {
                                if (part.get(part.size() - 1).word().equals(",")) {
                                    parent = clause;
                                }
                            }
                            if (parent != null && !parent.getWord(parent.size() - 1).eqt(":")) {
                                if (!clause.equals(parent)) {
                                    clause.setParent(parent);
                                }
                            }
                            fixWhTag(clause);
                            mapClauses.put(key * 10 + i, clause);
                        } catch (Exception ex) {
                            if (logs != null) {
                                logs.add("<span class='w3-red'>" + ex.getMessage() + "</span> <br/>");
                            } else {
                                System.err.println(ex.getMessage());
                            }
                        }
                    }

                    i++;
                }
            }
            nsent++;
        }
        return mapClauses;
    }

    private void semanticGraph(Tree tree) {
        SemanticGraph sg = SemanticGraphFactory.generateEnhancedDependencies(tree);
//        sg.prettyPrint();
//        System.out.println(sg);
        List<IndexedWord> vertices = sg.vertexListSorted();

        for (IndexedWord iw : vertices) {
            CoreLabel cl = iw.backingLabel();
            if (cl.value() == null) {
                cl.setValue(cl.word());
            }
            System.out.println(iw);
            List<SemanticGraphEdge> outs = sg.getOutEdgesSorted(iw);
            for (SemanticGraphEdge edge : outs) {
                System.out.println(edge);
            }
        }
    }

    public void openIE(String text) { // "Barack Obama was born in Hawaii."
        String ss[] = text.split("\\.");
        for (String s : ss) {
            Sentence sent = new Sentence(s);
            Collection<RelationTriple> triples = sent.openieTriples();

            for (RelationTriple triple : triples) {
                System.out.println(triple.confidence + "\t"
                        + triple.subjectGloss() + "\t"
                        + triple.relationGloss() + "\t"
                        + triple.objectGloss());
            }
        }

    }

    private Map<Integer, Parenthetical> brackets(List<CoreLabel> in, int n) {
        Map<Integer, Parenthetical> ret = new LinkedHashMap();

        Map<Integer, Integer> map = new LinkedHashMap();
        int depth = 0;
        map.put(depth, 0);
        int quote = 1000;
        for (int i = 0; i < in.size(); i++) {
            String word = in.get(i).word().toLowerCase();
            String tag = in.get(i).tag();

            if (tag.equals("-LRB-") || word.matches("[‘“]") || (quote == 1000 && (word.equals("\"") || word.equals("—")))) {
                depth++;
                Integer k = map.get(depth);
                if (k == null) {
                    map.put(depth, 0);
                } else {
                    map.put(depth, k + 1);
                }
                if (i > 0) {
                    in.get(i - 1).setSentIndex(n * 100 + depth * 10 + map.get(depth));
                }
                if (quote == 1000 && (word.equals("\"") || word.equals("—"))) {
                    quote = i;
                }
            }
            Integer key = n * 100 + depth * 10 + map.get(depth);
            Parenthetical list = ret.get(key);
            if (list == null) {
                list = new Parenthetical();
                ret.put(key, list);
            }
            list.add(in.get(i));
            if (tag.equals("-RRB-")) {
                if (depth > 0) {
                    depth--;
                }
//                if (i < in.size() - 1 && in.get(i + 1).tag().equals(".")) {
//                    for (int j = i - 1; j > 0; j--) {
//                        if (in.get(j).sentIndex() != -1) {
//                            in.get(i + 1).setSentIndex(in.get(j).sentIndex());
//                            in.get(j).setSentIndex(-1);
//                            break;
//                        }
//                    }
//                }
            } else if (word.matches("[’”]")) {
                if (depth > 0) {
                    depth--;
                }
            } else if ((word.equals("\"") || word.equals("—")) && i > quote) {
                if (depth > 0) {
                    depth--;
                }
                quote = 1000;
            }
        }
        return ret;
    }

    public static int findMatching(List<CoreLabel> list, String pattern, int start, boolean skip) {
        if (start < 0) {
            return -1;
        }
        int i = start;
        int found = -1;
        boolean once = true;
        if (pattern.startsWith("+")) {
            once = false;
            pattern = pattern.substring(1);
        }
        int ri = pattern.indexOf("→");
        String replace = null;
        if (ri != -1) {
            replace = pattern;//.substring(ri + 1);
            pattern = pattern.substring(0, ri);
        }

        while (i < list.size()) {
            if (list.get(i).tag().matches(pattern) || list.get(i).word().toLowerCase().matches(pattern) || (list.get(i).lemma() != null && list.get(i).lemma().matches(pattern))) {
                if (replace != null) {
                    list.get(i).setLemma(replace);
                }
                if (once) {
                    return i;
                }
                found = i;
            } else {
                if (!skip) {
                    return found;
                }
            }
            i++;
        }
        return found;
    }

    private static int findMatchingWordBefore(List<TaggedWord> list, String pattern, int start, int limit) {
        int i = start;
        while (i > 0 && i >= start - limit) {
            if (list.get(i).word().toLowerCase().matches(pattern)) {
                return i;
            }
            i--;
        }
        return -1;
    }

    private List<Integer> matches(List<CoreLabel> list, String pattern) {
        int ip = 0;
        int it = 0;
        String[] tokens = pattern.split(" ");
        List<Integer> m = new ArrayList();
        List<Integer> breaks = new ArrayList();
        int count = 0;
        boolean skip = true;
        while (ip < tokens.length && it != -1) {
            String token = tokens[ip].replace("_", " ");
            if (token.startsWith("$")) {
                token = token.substring(1);
            } else if (token.endsWith("$")) {
                token = token.substring(0, token.length() - 1);
            }
            if (token.equals("*")) {
                if (tokens[ip].startsWith("$")) {
                    breaks.add(it);
                } else if (tokens[ip].endsWith("$")) {
                    breaks.add(it + 1);
                }
                skip = true;
            } else if (token.equals("+")) {
                skip = true;
                it++;
            } else {
                it = findMatching(list, token, it, skip);
                if (it != -1) {
                    m.add(it);
                    skip = false;
                    if (tokens[ip].startsWith("$")) {
                        breaks.add(it);
                    } else if (tokens[ip].endsWith("$")) {
                        breaks.add(it + 1);
                    }
                    it++;
                }
                count++;
            }
            ip++;
        }
        if (m.size() == count) {
            list.forEach(cl -> {
                if (cl.lemma() != null) {
                    int ri = cl.lemma().indexOf("→");
                    if (ri != -1) {
                        String p2 = cl.lemma().substring(ri + 1);
                        if (p2.matches("[A-Z]+")) {
                            cl.setTag(p2);
                            cl.setLemma(cl.lemma().substring(0, ri));
                        } else {
                            String lemma = cl.lemma().replace("→", "⇒");
                            cl.setLemma(lemma);
                        }
                    }
                }
            });
            return breaks;
        } else {
            list.forEach(cl -> {
                if (cl.lemma() != null) {
                    int ri = cl.lemma().indexOf("→");
                    if (ri != -1) {
                        cl.setLemma(cl.lemma().substring(0, ri));
                    }
                }
            });
            return null;
        }
    }

    protected List<Parenthetical> decompose(Parenthetical in) {

        List<Parenthetical> out = new ArrayList();

        Set<Integer> js = new TreeSet();
        int i = 0;
        while (i < decomposePatterns.length) {
            List<Integer> matches = matches(in, decomposePatterns[i]);
            if (matches != null) {
//                String s = decomposePatterns[i - 1];
                js.addAll(matches);
            }
            i++;
        }
        if (js.isEmpty()) {
            out.add(in);
        } else {
            int last = 0;
//            for (i = 0; i < js.size(); i++) {
            for (Integer j : js) {
//                if (js.get(i) > last) {
//                    out.add(new Parenthetical(in.subList(last, js.get(i))));
                out.add(new Parenthetical(in.subList(last, j)));
//                }
                last = j;
//                last = js.get(i);
            }
            if (last < in.size()) {
                out.add(new Parenthetical(in.subList(last, in.size())));
            }
        }
        if (in.getOpening() != null) {
            out.get(0).setOpening(in.getOpening());
        }
        if (in.getClosing() != null) {
            out.get(out.size() - 1).setClosing(in.getClosing());
        }
        return out;
    }

    protected void mergePhrases(List<TaggedWord> tagged) {
        int i = 0;
        while (i < tagged.size() - 1) {
            if (tagged.get(i).word().matches("[a-z]+_[a-z]+")) {
                tagged.get(i).setWord(tagged.get(i).word().replace("_", " "));
            }
            i++;
        }
        if (multiWordRepository != null) {
            int window = Math.min(7, tagged.size());
            while (window > 1) {
                i = 0;
                while (i + window <= tagged.size()) {
                    String s = tagged.get(i).word();
                    int j = i + 1;
                    while (j < i + window) {
                        s += " " + tagged.get(j++).word();
                    }
                    if (j == i + window) {
                        TaggedWord next = null;
                        if (j < tagged.size()) {
                            next = tagged.get(j);
                        }
                        String pos = multiWordRepository.find(s.toLowerCase(), window, next);
                        if (pos != null) {
                            tagged.get(i).setWord(s);
                            tagged.get(i).setTag(pos);
                            j = 0;
                            while (j < window - 1) {
                                tagged.remove(i + 1);
                                j++;
                            }
                        }
                    }
                    i++;
                }
                window--;
            }
        }
        i = 0;
        while (i < tagged.size() - 1) {
            if (tagged.get(i).tag().matches("NNS?P") && tagged.get(i + 1).tag().matches("NNS?P")) {
                tagged.get(i).setWord(tagged.get(i).word() + " " + tagged.get(i + 1).word());
                tagged.get(i).setTag("NNP");
                tagged.remove(i + 1);
            } else {
                i++;
            }
        }

    }

    protected void checkMultiVerb(List<CoreLabel> tagged) {
//        List<CoreLabel> ret = new ArrayList();
        for (int i = 0; i < tagged.size() - 2; i++) {
            if (tagged.get(i).tag().startsWith("VB")) {
                if (tagged.get(i + 1).word().matches("or|and")) {
                    if (tagged.get(i + 2).tag().startsWith("VB")) {
//                        ret.add(tagged.remove(i + 1));
//                        ret.add(tagged.remove(i + 1));
                        tagged.get(i).set(AzdegarAnnotations.MultiVerbAnnotation.class, List.of(tagged.remove(i + 1), tagged.remove(i + 1)));
//                        return List.of(tagged.remove(i + 1), tagged.remove(i + 1));
//                        return ret;
                    }
                }
            }
        }
//        return null;
    }

    protected Tree getRootClause(Tree root, Tree leaf) {
        Tree parent = leaf;
        while ((parent = parent.parent(root)) != null) {
            CoreLabel cl = (CoreLabel) parent.label();
            cl.setBefore(CLAUSE_PLACEHOLDER);
            if (cl.category() != null) {
                if (cl.category().matches("SQ?")) {
                    Tree sbar = parent.parent(root);
                    if (sbar != null) {
                        cl = (CoreLabel) sbar.label();
                        if (cl.category().startsWith("SBAR")) { //SBARQ
                            return sbar;
                        }
                    }
                    return parent;
                } else if (cl.category().startsWith("SBAR")) {
                    return parent;
                }
            } else {
                System.out.println(cl.value());
            }

        }
        if (parent != null) {
            return parent;
        } else {
            return root;
        }
    }

    protected PhraseInfo getPhrase(Tree root, Tree leaf) {
        Tree parent = leaf;
        while ((parent = parent.parent(root)) != null) {
            CoreLabel cl = (CoreLabel) parent.label();
            if (cl.category() != null) {
                if (cl.category().matches("[VNP]P") || cl.category().matches("AD[VJ]P") || cl.category().equals("WHNP")) {
                    return new PhraseInfo(cl.category(), parent.nodeNumber(root), root.depth(parent));
                } else if (cl.category().equals("SQ")) {
                    return new PhraseInfo(cl.category(), parent.nodeNumber(root), root.depth(parent));
                }
            }

        }
        return null;
    }

    protected Clause structural(Tree tree) throws Exception {
        List<Tree> leaves = tree.getLeaves();

        Clause main = new Clause();
        Map<Tree, Integer> map = new LinkedHashMap();
        Tree prevRootClause = tree;
        int clauseId = 0;
        Set<Integer> set = new TreeSet();
        boolean cc = false;
        for (int i = 0; i < leaves.size(); i++) {
            Tree leaf = leaves.get(i);
            CoreLabel label = (CoreLabel) leaf.label();
            if (label.word() == null) {
                throw new Exception("CoreNLP Fault: word is null. CoreLabel value:" + label.value() + ", index: " + i);
            }
//            label.set(AzdegarAnnotations.OntologyAnnotation.class, "Place");
            ExtWord tw = new ExtWord(label);
            tw.setIndex(i);
            tw.setDepth(tree.depth(leaf));
            tw.setPhraseInfo(getPhrase(tree, leaf));
            List l = label.get(AzdegarAnnotations.MultiVerbAnnotation.class);
            if (l != null) {
                tw.set(AzdegarAnnotations.MultiVerbAnnotation.class, l);
            }

            Tree rootClause;
//            if (label.word() == null) {
//                label.setWord(label.value());
//                label.setLemma(label.word().toLowerCase());
//                label.setTag("IN");
//            }
            if (label.word().equals(",") || (label.tag().matches("VBP?") && i > 0 && ((CoreLabel) leaves.get(i - 1).label()).word().equalsIgnoreCase("to")) || (cc && !label.word().equalsIgnoreCase("because"))) {
                rootClause = prevRootClause;
            } else {
                rootClause = getRootClause(tree, leaf);
            }

            boolean q = false;
            Tree t = (Tree) rootClause.getChild(0);
            if (t.parent(tree) != null) {
                CoreLabel cl = (CoreLabel) t.parent(tree).label();
                q = cl.category() != null && cl.category().endsWith("Q");
            }
            Integer id = map.get(rootClause);

            if (id == null) {
                map.put(rootClause, clauseId);
                id = clauseId;
                clauseId++;
            }
            tw.setClauseId(id);

            if (tw.getClauseId() == 0 || (tw.eqt(".") && i == leaves.size() - 1)) {
                main.add(tw);
                if (q) {
                    main.setType(SentenceType.Interrogative);
                }
            } else {
                Clause c = main.getSubs().get(tw.getClauseId());
                if (c == null) {
                    main.getSubs().put(tw.getClauseId(), new Clause(tw.getClauseId()));
                    c = main.getSubs().get(tw.getClauseId());
                    c.setParent(main);
                }
                if (tw.word().equalsIgnoreCase("then")) {
                    tw.setClauseId(clauseId - 1);
                    main.add(tw);
                } else {
                    c.add(tw);
                }
                if (q) {
                    main.setType(SentenceType.Interrogative);
                }

                if (!set.contains(tw.getClauseId())) {
                    ExtWord placeholder = new ExtWord("〔" + tw.getClauseId() + "〕", "", "");
                    placeholder.setClauseId(tw.getClauseId());
                    main.add(placeholder);
                    set.add(tw.getClauseId());
                }
            }
            if (label.tag().equals("CC") && !label.word().equalsIgnoreCase("and")) {
                cc = true;
            }
            prevRootClause = rootClause;
        }
        return main;
    }

    private void fixWhTag(Clause clause) {
        if (clause.subs() != null && !clause.subs().isEmpty()) {
            clause.subs().values().forEach(sub -> {
                if (!clause.isInterrogative()) {
                    if (sub.get(0).matchw("which|where|when")) {
                        sub.get(0).setTag("IN");
                    } else if (sub.get(0).matchw("who")) {
                        sub.get(0).setTag("CC");
                    }
                }
            });
        }
        if (clause.getParent() != null) {
            if (clause.getParent().endsWith(",")) {
                if (!clause.isInterrogative()) {
                    if (clause.get(0).matchw("which|where|when")) {
                        clause.get(0).setTag("IN");
                    } else if (clause.get(0).matchw("who")) {
                        clause.get(0).setTag("CC");
                    }
                }
            }
        }
    }

    public void processPhrasal(WordGroup wg) {
        for (int i = 0; i < wg.size() - 1; i++) {
            if (wg.get(i).isVerb()) {
                ExtWord verb = wg.get(i);
                Set set = phrasalVerbs.get(verb.lemma());

                if (set != null && !set.isEmpty()) {
                    List<String> rps = new ArrayList(set);
                    Collections.sort(rps,
                            (e1, e2) -> {
                                return e2.split(" ").length - e1.split(" ").length;
                            });

                    int index = -1;
                    int k = 0;
                    while (k < rps.size() && index == -1) {
                        index = wg.findSequence(rps.get(k), i + 1, i + 5);
                        k++;
                    }
                    if (index != -1) {
                        k--;
                        ExtWord particle = wg.get(index);
                        if (!particle.isParticle()) {
                            particle.setTag("RP");
                        }
                        String sth = " ";
                        List<Integer> excludes = new LinkedList();
                        if (index > i + 1) {
                            boolean f = false;
                            for (int j = i + 1; j < index; j++) {
                                if (!wg.get(j).matcht("JJR?S?|RB")) {
                                    wg.get(j).set(AzdegarAnnotations.PartOfSentenceAnnotation.class, "dobj");
                                    f = true;
                                }
                                excludes.add(j);
                            }
                            if (f) {
                                sth = " sth ";
                            }
                        }
                        verb.setLemma(verb.lemma() + sth + rps.get(k));
                        verb.setWord(verb.lemma());
                        int n = rps.get(k).split(" ").length + excludes.size();
                        for (int j = i + 1; j < i + 1 + n; j++) {
                            if (!excludes.contains(j)) {
                                wg.get(j).disable();
                            }
                        }
                    }
                }
            }
        }
    }

    public List<ExtWord> convert(List<CoreLabel> in) {
        List<ExtWord> out = new ArrayList(in.size());
        in.forEach((cl) -> {
            out.add(new ExtWord(cl));
        });
        return out;
    }

    private int find(List<TaggedWord> words, String s, int idx) {
        while (idx > 0 && words.get(idx).word().matches(",|:")) {
            idx--;
        }
        for (int i = idx; i < words.size(); i++) {
            TaggedWord w = words.get(i);
            if (w.tag().matches(s) || w.word().matches(s)) {
                return i;
            } else if (w.word().matches(",|:")) {
                return -1;
            }
        }
        return -1;
    }

    private void fixWrongVerb(List<TaggedWord> words, int start) {
        for (int i = start; i < words.size(); i++) {
            if (words.get(i).tag().equals("VBG")) {
                if (i > 0) {
                    if (findMatchingWordBefore(words, "am|'m|are|'re|is|'s|was|were|be|been", i - 1, 2) != -1) {
                        // VBG
                    } else if (words.get(i - 1).word().matches(GRAM_INGTO + "|" + GRAM_ING)) {
                        // VBG
                    } else if (words.get(i - 1).word().toLowerCase().matches("so")) {
                        words.get(i).setTag("RB");
                    } else {
                        if (words.get(i - 1).tag().matches("DT|CD|RB")) {
                            words.get(i).setTag("JJ");
                        } else {
                            words.get(i).setTag("NN");
                        }
                    }
                } else {
                    words.get(i).setTag("NN");
                }
            } else if (words.get(i).tag().matches("VBN")) {
                if (i > 0) {
                    if (findMatchingWordBefore(words, "have|has|am|are|is|was|were|be|been", i - 1, 4) != -1) {
                        // VBN
                    } else if (words.get(i - 1).word().toLowerCase().matches("so")) {
                        words.get(i).setTag("RB");
                    } else if (words.get(i - 1).word().equals(",")) {
                        words.get(i).setTag("JJ");
                    } else {
                        if (i < words.size() - 1 && words.get(i + 1).tag().matches("NNP?S?") && find(words, "VB[DPZ]?", i) != -1) {
                            words.get(i).setTag("JJ");
                        } else if (words.get(i - 1).tag().matches("DT")) {
                            words.get(i).setTag("JJ");
                        } else {
                            if (words.get(words.size() - 1).tag().matches("\\.|,")) {
                                words.get(i).setTag("VBD");
                            }
                        }
                    }
                } else {
                    words.get(i).setTag("JJ");
                }
            }
        }
    }

    private void fixWrongNoun(List<TaggedWord> words, int start) {
        boolean verbDetected = false;
        for (int i = start; i < words.size(); i++) {
            String word = words.get(i).word().toLowerCase();
            if (word.endsWith("ing")) {
                if (words.get(i).tag().equals("NN")) {
                    if (word.matches(NOUNS_ENDING_ING)) {
                    } else {
                        if (i > 0 && words.get(i - 1).tag().equals("IN")) { // He prefers playing golf to swimming_VBG.
                            words.get(i).setTag("VBG");
                        }
                    }
                }
            }
            if (words.get(i).tag().startsWith("VB")) {
                verbDetected = true;
            }
        }
        if (!verbDetected && words.get(words.size() - 1).tag().equals(".")) {
            boolean subjDetected = false;
            for (int i = start; i < words.size() - 1; i++) {
                TaggedWord tw = words.get(i);
                if (tw.tag().matches("NNS?|PRP")) { // This plane lands_VBZ in Paris.
                    if (!subjDetected) {
                        subjDetected = true;
                    } else {
                        if (tw.tag().equals("NNS")) {
                            tw.setTag("VBZ");
                        } else if (tw.tag().equals("NN")) {
                            tw.setTag("VB");
                        }
                    }
                }
            }
        }
    }

    private void fixWrongAdjective(List<TaggedWord> words, int start) {
        for (int i = start; i < words.size(); i++) {
            String word = words.get(i).word().toLowerCase();
            if (words.get(i).tag().equals("JJ")) {
                if (EnglishUtils.isOrdinalNumber(word)) {
                    words.get(i).setTag("CD");
                }
            }
        }
    }

    private int findActualStart(List<TaggedWord> words) {
        int i = 0;
        while (i < words.size() && (words.get(i).word().matches("\\(\\[") || words.get(i).tag().equals("SYM"))) {
            i++;
        }
        return i;
    }

    protected void fixTaggerBugs(List<TaggedWord> words, int i) {

        fixWrongVerb(words, i);
        fixWrongNoun(words, i);
        while (i < words.size()) {

            Class c = fixerMap.get(words.get(i).word().toLowerCase());
            if (c != null) {
                try {
//                    Class<Fixable> c = (Class<Fixable>) Class.forName("com.azdegar.nlp.tagfix." + clazz);
                    Fixable fixable = (Fixable) c.getDeclaredConstructor().newInstance();
                    fixable.fix(i, words);
                } catch (Exception ex) {
                }
            }

            i++;
        }
        words.stream()
                .filter(tw -> (tw.word().matches("[A-Za-z]+_[A-Z]+"))) //ingests, normalizes, enriches, triages_VBZ, and stores
                .forEachOrdered(tw -> {
                    String[] s = tw.word().split("_");
                    tw.setWord(s[0]);
                    tw.setTag(s[1]);
                });

        words.stream()
                .filter(tw -> (tw.word().toLowerCase().matches("everyday|yesterday")))
                .forEachOrdered(tw -> {
                    tw.setTag("RB");
                });
        words.stream()
                .filter(tw -> (tw.word().startsWith("ν")
                || tw.word().matches("[B-HJ-Zb-z]")
                || tw.word().matches("[FR][abcwxyz]+") //                || tw.tag().equals("NFP")
                ))
                .forEachOrdered(tw -> {
                    tw.setTag("SYM");
                });

        words.stream()
                .filter(tw -> (tw.word().matches("\\d+[0-9\\.]*"))) // \d*\.?\d*
                .forEachOrdered(tw -> {
                    tw.setTag("CD");
                });
        words.stream()
                .filter(tw -> (tw.word().matches("[a-z]\\.")))
                .forEachOrdered(tw -> {
                    tw.setTag("LS");
                });
        words.stream()
                .filter(tw -> (tw.word().matches("[a-z\\-]{2,}") && tw.tag().equals("NNP")))
                .forEachOrdered(tw -> {
                    tw.setTag("NN");
                });

//        for (i = 0; i < words.size(); i++) {
//            if (words.get(i).word().matches("[A-Z]\\.")) {
//                words.get(i).setWord(words.get(i).word().substring(0, 1));
//                words.get(i).setTag("SYM");
//                words.add(i + 1, new TaggedWord(".", "."));
//            }
//        }
        for (i = 0; i < words.size() - 1; i++) {
            Object o = TagRules.getTagRules(words.get(i).tag());
            if (o != null) {
                if (o instanceof List) {
                    List<String> l = (List<String>) o;
                    for (String s : l) {
                        changeTag(words, i, s);
                    }
                } else {
                    changeTag(words, i, (String) o);
                }
            }
        }
//        
        for (i = 0; i < words.size() - 1; i++) {
            Object o = TagRules.getWordRules(words.get(i).word().toLowerCase() + "/" + words.get(i).tag());
            if (o == null) {
                if (words.get(i).word().matches("[A-Za-z]{2,}ed") && words.get(i).tag().equals("JJ")) {
                    o = TagRules.getWordRules("[A-Za-z]{2,}ed/JJ");
                }
            }
            if (o != null) {
                if (o instanceof List) {
                    List<String> l = (List<String>) o;
                    for (String s : l) {
                        changeTag(words, i, s);
                    }
                } else {
                    String s = (String) o;
                    changeTag(words, i, s);
                }
            }
        }

        boolean have = false;
        for (i = 0; i < words.size(); i++) {
            String w = words.get(i).word().toLowerCase();
            if (w.matches("ha(ve|s|d)") || w.matches("am|is|are|was|were|be(ing)?")) {
                have = true;
            } else if (words.get(i).tag().equals("VBN") && !have) {
                words.get(i).setTag("VBD");
            }
        }

    }

    protected void transform(List<CoreLabel> words) {
        int i = 0;
        while (i < words.size()) {
            Class c = transformMap.get(words.get(i).word().toLowerCase());
            if (c != null) {
                try {
//                    Class<Transformable> c = (Class<Transformable>) Class.forName("com.azdegar.nlp.transform." + clazz);
                    Transformable transformable = (Transformable) c.getDeclaredConstructor().newInstance();
                    transformable.check(i, words);
                } catch (Exception ex) {
                }
            }
            i++;
        }
    }

    private boolean changeTag(List<TaggedWord> words, int i, String s) {
        String[] split = s.split(":");
        if (!split[0].isBlank() && !split[1].isBlank()) {
            if (i > 0 && i < words.size() - 1) {
                if ((words.get(i - 1).tag().matches(split[0]) || words.get(i - 1).word().toLowerCase().matches(split[0]))
                        && (words.get(i + 1).tag().matches(split[1]) || words.get(i + 1).word().toLowerCase().matches(split[1]))) {
                    words.get(i).setTag(split[2]);
                    return true;
                }
            }
        } else if (!split[0].isBlank()) {
            int j = i - 1;
            int limit = i;
            String pattern = split[0];
            if (split[0].startsWith("{")) {
                limit = min(Integer.parseInt(split[0].substring(1, 2)), i);
                pattern = pattern.substring(3);
            }
            while (j >= i - limit) {
                if (!pattern.startsWith("^")) {
                    if (words.get(j).tag().matches(pattern) || words.get(j).word().toLowerCase().matches(pattern)) {
                        words.get(i).setTag(split[2]);
                        return true;
                    }
                } else {
                    if (words.get(j).tag().matches(pattern) && words.get(j).word().toLowerCase().matches(pattern)) {
                        words.get(i).setTag(split[2]);
                        return true;
                    }

                }
                j--;
            }
        } else {
            String pattern = split[1];
            if (pattern.isBlank()) {
                words.get(i).setTag(split[2]);
            } else {
                int j = i + 1;
                int limit = words.size();
                if (split[1].startsWith("{")) {
                    limit = min(Integer.parseInt(split[1].substring(1, 2)), words.size() - 1);
                    pattern = pattern.substring(3);
                }
                while (j < limit) {
                    if (words.get(j).tag().matches(pattern) || words.get(j).word().toLowerCase().matches(pattern)) {
                        words.get(i).setTag(split[2]);
                        return true;
//                    break;
                    }
                    j++;
                }
            }

        }
        return false;
    }

    private String buildLemma(String word) {

        if (word.endsWith("ing")) {
            String l = word.substring(0, word.length() - 3);
            if (l.length() > 1) {
                if (l.charAt(l.length() - 2) == l.charAt(l.length() - 1)) { // swimming
                    l = l.substring(0, l.length() - 1);
                }
//                if (l.matches("amaz|chang|liv|hav|mak|oppos|tak")) {
                if (EnglishUtils.isVowel(l.charAt(l.length() - 2)) || l.equals("chang")) {
                    l += "e";
                }
            }
            return l;
        }
        return word;
    }

    protected void fixLemmatizerBugs(List<CoreLabel> list) {
        list.forEach(cl -> {
            String word = cl.word().toLowerCase();
            if (word.equals("could")) {
                cl.setLemma("can");
            } else if (word.matches("lts|os")) {
                cl.setLemma(word);
            } else if (word.endsWith("ing")) {
                if (!word.matches(NOUNS_ENDING_ING)) {
                    if (word.equals(cl.lemma())) {
                        cl.setLemma(buildLemma(word));
                    }
                }
            } else if (word.equals("expresses")) {
                cl.setLemma(word.substring(0, word.length() - 2));
            } else if (word.equals("constitutes")) {
                cl.setLemma(word.substring(0, word.length() - 1));
            } else if (word.equals("sharpened")) {
                cl.setLemma(word.substring(0, word.length() - 2));
            } else if (cl.tag().equals("VBN") && cl.lemma().endsWith("ed")) {
                cl.setLemma(word.substring(0, word.length() - 1));
            }
        });
    }

    public String substitute(String text) {
        for (int i = 0; i < replacement.length; i++) {
            text = text.replaceAll(replacement[i][0], replacement[i][1]);
        }
        return text;
    }

    protected List<TaggedWord> mergeHyphen(List<TaggedWord> tagged) {
        int i = 1;
        while (i < tagged.size() - 1) {
            if (tagged.get(i).word().equals("-")) {
                tagged.get(i - 1).setWord(tagged.get(i - 1).word() + "-" + tagged.get(i + 1).word());
                if (tagged.get(i - 1).tag().matches("CD|IN")) {
                    tagged.get(i - 1).setTag("JJ");
                }
                tagged.remove(i);
                tagged.remove(i);
            } else {
                i++;
            }
        }
        return tagged;
    }

    protected List<TaggedWord> mergeSymbols(List<TaggedWord> tagged) {
        int i = 1;
        boolean changed = false;
        while (i < tagged.size() - 1) {
            if ((isMath(tagged.get(i)) && isMath(tagged.get(i - 1)) && !tagged.get(i - 1).word().equals(")") && isMath(tagged.get(i + 1)) && !tagged.get(i + 1).word().equals("("))
                    || (isMath(tagged.get(i)) && tagged.get(i - 1).word().matches("[-~¬⇁+◻◇◊∀∃]"))) {
                String w = tagged.get(i - 1).word() + tagged.get(i).word();
                int j = i + 1;
                if (isMath(tagged.get(i + 1))) {
                    w += tagged.get(i + 1).word();
                    tagged.get(i + 1).setWord(null);
                    j++;
                }
                tagged.get(i - 1).setWord(w);
                tagged.get(i - 1).setTag("SYM");
                tagged.get(i).setWord(null);
                while (j < tagged.size() - 1 && isMath(tagged.get(j))) {
                    tagged.get(i - 1).setWord(tagged.get(i - 1).word() + tagged.get(j).word());
                    tagged.get(j).setWord(null);
                    j++;
                }
                if (tagged.get(i - 1).word().matches(".+[,:;?!]")) {
                    tagged.get(j - 1).setWord(tagged.get(i - 1).word().substring(tagged.get(i - 1).word().length() - 1));
                    tagged.get(i - 1).setWord(tagged.get(i - 1).word().substring(0, tagged.get(i - 1).word().length() - 1));
                }
                i = j;
                changed = true;
            }
            i++;
        }
        if (!changed) {
            return tagged;
        } else {
            return tagged.stream().filter(e -> e.word() != null).collect(Collectors.toList());
        }
    }

    private boolean isMath(TaggedWord tw) {
        String w = tw.word();
        return switch (w.length()) {
            case 1 ->
                !w.matches("[Ia,;?\\.\\(\\)]");
            case 2 ->
                !w.toLowerCase().matches("an|am|as|at|be|by|do|go|he|hi|if|in|is|it|me|my|no|of|on|or|so|to|up|us|we") && !w.matches("Mr|Dr|Sr|Jr|CD");
            default ->
                (!w.matches("[A-Za-z\\-]+") && !w.equals("pp.")) || w.matches("[FR][abcwxyz]+");
        };
    }

    public List<CoreLabel> getLemmatized() {
        return lemmatized;
    }

    public void setPhrasalVerbs(Map<String, Set> phrasalVerbs) {
        this.phrasalVerbs = phrasalVerbs;
    }

    public void setMultiWordRepository(MultiWordRepository multiWordRepository) {
        this.multiWordRepository = multiWordRepository;
    }

    private static Map<String, Class> fixerMap = new HashMap();

    static {
        Map<String, Class> map = new HashMap();
        map.put("a", A.class);
        map.put("about", About.class);
        map.put("as", As.class);
        map.put("connective", Connective.class);
        map.put("chunk", Chunk.class);
        map.put("chunks", Chunk.class);
        map.put("even", Even.class);
        map.put("had", Have.class);
        map.put("has", Have.class);
        map.put("have", Have.class);
        map.put("i", I.class);
        map.put("let", Let.class);
        map.put("lot", Lot.class);
        map.put("make", Make.class);
        map.put("makes", Make.class);
        map.put("made", Make.class);
        map.put("time", Time.class);
        map.put("that", That.class);
        map.put("what", What.class);

        fixerMap = Collections.unmodifiableMap(map);
    }

    public static Map<String, Class> transformMap = new HashMap();

    static {
        Map<String, Class> mapT = new HashMap();
        mapT.put("at", At.class);
        mapT.put("to", ToInfinitive.class);

        transformMap = Collections.unmodifiableMap(mapT);
    }

    private final static String[] decomposePatterns = {
        "VB[DPZ]? * $, * VB[DPZ]? * $, * VB[DPZ]? * $, * VB[DPZ]? * $, * VB[DPZ]?",
        "VB[DPZ]? * $, * VB[DPZ]? * $, * VB[DPZ]? * $, * VB[DPZ]?",
        "VB[DPZ]? * $, * VB[DPZ]? * $, * VB[DPZ]?",
        "VB[DPZ]? * $, * VB[DPZ]? * $,",
        "VB[DPZ]? * ,$ * VB[DPZ]?",
        "* $iff $*",
        "* $, RB $, *",
        "hence ,$ *",
        //        "VB[DPZ]? * $and $* VB[DPZ]",
        "if * VB[DPZ]? * $then * VB[DPZ]",
        "PRP|NNP?S? have→request +DT|PRP\\$?|NNP?S?|JJ $VB[PN]?",
        "make→force +DT|PRP\\$?|NNP?S?|JJ $VBP?", /* She made her children do their homework before going to bed. */
        "let +DT|PRP\\$?|NNP?S?|JJ $VBP?",
        "let_us $VBP?",
        "VB that→IN $*", /* Suppose that all of the simplest formulas ... */
        "that_is_, $*",
        "TO VB +DT|PRP\\$?|NNP?S?|JJ $TO VB", /* A person didn't need to say it to feel it. */
        "allow|enable|force|get|help|keep|hold|require|permit|persuade +DT|PRP\\$?|NNP?S?|JJ $TO VB", // Spring helps you to mitigate these.
        "allow|enable|force|get|help|keep|hold|require|permit|persuade +DT|PRP\\$?|NNP?S?|JJ $VB[DPZ]?", // Spring helps you mitigate these.
        "VB[DPZ]? * $before|but|n?or|so $* VB[DPZ]?",
        "VB[DPZ]? * $because * VB[DPZ]? * VB[DPZ]?", /* Many people try to change but feel fearful and anxious because they aren't sure the changes will last. */
        //        "VB[DPZ]? * troubles?|problems? $VBG", /* We’re having trouble finding that site. */
        "VB[DPZ]? * $whose * VB[DPZ]?",
        "WDT VB[DPZ]? * ,$", // Which is your car, the red one or the blue one?
        "* : $*"
    //        "VB[DPZ]? $knows?|knew|thinks?|believes?|believed $that VB[P|Z|D]?"
    };

    private final static String replacement[][] = {
        //        {"\\s‘", "\""},
        {"s’ ", "s' "},
        {"’s ", "'s "},
        {"’m ", "'m "},
        {"’t ", "'t "},
        {"’d ", "'d "},
        {"’ve ", "'ve "},
        {"’re ", "'re "},
        {"’ll ", " will "},
        {"'ll ", " will "},
        {"e\\.g\\.", "for example"},
        {"'d better ", " had better "},
        {"cannot", "can not"},
        {"can't", "can not"},
        {"can’t", "can not"},
        {"[Ww]hat's", "what is"},
        {"[Ll]et's ", "let us "},
        {"There's ", " There is "},
        //        {"[G|g]et started", "start"},
        {"is to going to ", "is to "}, // Their house is to going to be sold right away.
        {"are to going to ", "are to "},
        {"am to going to ", "am to "},
        //        {"would then ", "then would "},
        {"would rather ", "prefer "},
        {"[Ii]t's time to", "it is time to to "},
        {"[Ii]t is time to", "it is time to to "},
        {"[Ii]t is time now to", "it is time now to to "},
        // British to American
        //        {" [S|s]hall ", " will "},
        {"[Tt]owards ", "toward "},
        {"[Ff]orwards ", "forward "},
        {"[B|b]ackwards ", "backward "},
        {"[U|u]pwards ", "upward "},
        {"[D|d]ownwards ", "downward "},
        {"[A|a]fterwards ", "afterward "},
        {"[C|c]olour", "color"},};

}
