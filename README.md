Azdegar NLP provides an advanced and high level API for natural language processing on top of Stanford CoreNLP library.

### Usage
``` Java
Properties properties = new Properties();
MaxentTagger tagger = new MaxentTagger("english-caseless-left3words-distsim.tagger", properties);
ParserGrammar grammar = LexicalizedParser.loadModel("englishPCFG.caseless.ser.gz");

Parser parser = new Parser(tagger, grammar);
MultiWordRepository multiWordRepository = new FarzanegiMultiWordRepository();

parser.setMultiWordRepository(multiWordRepository);
    
Map phrasalVerbs = new TreeMap();
phrasalVerbs.put("break", new TreeSet(Arrays.asList("in", "up", "out")));
phrasalVerbs.put("look", new TreeSet(Arrays.asList("after", "for", "into","out")));
        ... 
parser.setPhrasalVerbs(phrasalVerbs);
Map<Integer, Clause> clauses = parser.parse(text, null);
