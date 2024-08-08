/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.azdegar.nlp.tagfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Afshin Pouria
 */
public class TagRules {

    private final static HashMap<String, Object> wordRules = new HashMap(10);
    private final static HashMap<String, Object> tagRules = new HashMap(10);

    static {
        ArrayList vbd = new ArrayList(2);
        vbd.add("being: :VBN");
//        vbd.add("be: :VBN");
        tagRules.put("VBD", vbd);

//        ArrayList vb = new ArrayList(2);
//        vbd.add("have: :VBN");
//        tagRules.put("VB", vb);

        tagRules.put("VBN", "{1}JJ|DT|CD: :JJ"); // several completed trees

        ArrayList lastJJ = new ArrayList(2);
        lastJJ.add(" :night|day|week|month|year|decade|century|millennium:DT");
        wordRules.put("last/JJ", lastJJ);
        wordRules.put("next/JJ", lastJJ);

//        ArrayList need = new ArrayList(2);
//        need.add("no one|nobody|nothing: :MD");
//        need.add(" :not:MD");
//        wordRules.put("need/VBP", need);
//        ArrayList reasonNN = new ArrayList(2);
//        reasonNN.add("i|you|we|they|to: :VB");
//        reasonNN.add("NNS: :VB");
//        wordRules.put("reason/NN", reasonNN);
//        wordRules.put("reasons/NNS", "he|she|it|everyone|everybody: :VBZ");
        wordRules.put("states/NNS", Arrays.asList("{1}that: :VBZ", " :{1}that:VBZ"));

//        wordRules.put("land/NN", " :DT|IN:VB");
//        wordRules.put("lands/NNS", " :DT|IN:VBZ");
        wordRules.put("store/NN", "to:data:VB");
        wordRules.put("group/NN", "to:by:VB");
        wordRules.put("iff/NN", " : :IN");
        
        wordRules.put("simplest/JJ", " : :JJS");

        wordRules.put("bet/VB", "{3}(have|has|had): :VBN");
        wordRules.put("cut/VB", "{3}(have|has|had): :VBN");
        wordRules.put("fit/VB", "{3}(have|has|had): :VBN");
        wordRules.put("forcast/VB", "{3}(have|has|had): :VBN");
        wordRules.put("hit/VB", "{3}(have|has|had): :VBN");
        wordRules.put("hurt/VB", "{3}(have|has|had): :VBN");
        wordRules.put("let/VB", "{3}(have|has|had): :VBN");
        wordRules.put("put/VB", "{3}(have|has|had): :VBN");
        wordRules.put("put/VBD", "{3}(have|has|had): :VBN");
        wordRules.put("quit/VB", "{3}(have|has|had): :VBN");
        wordRules.put("read/VB", "{3}(have|has|had): :VBN");
        wordRules.put("set/VB", "{3}(have|has|had): :VBN");
        wordRules.put("shut/VB", "{3}(have|has|had): :VBN");
        wordRules.put("spread/VB", "{3}(have|has|had): :VBN");
        wordRules.put("split/VB", "{3}(have|has|had): :VBN");
        wordRules.put("upset/VB", "{3}(have|has|had): :VBN");


        ArrayList it = new ArrayList(2);
        it.add(" :{9}(rain|sun|snow|cloud|dust|wind)(y|s|ed|ing)?:NN");
        it.add(" :{9}(today|yesterday|week):NN");
        wordRules.put("it/PRP", it);

        wordRules.put("rain/NN", "{1}not: :VB");
        wordRules.put("bear/NN", "^(?!.*(DT|JJR?)).*$: :VB");
        wordRules.put("bears/NNS", "^(?!.*(DT|JJR?)).*$: :VBZ");
        wordRules.put("rises/NNS", "{1}NN: :VBZ"); // 𝑻𝒉𝒆 𝒊𝒄𝒆 𝒎𝒆𝒍𝒕𝒔 𝒂𝒔 𝒕𝒉𝒆 𝒕𝒆𝒎𝒑𝒆𝒓𝒂𝒕𝒖𝒓𝒆/NN 𝒓𝒊𝒔𝒆𝒔/NNS.
        wordRules.put("willing/JJ", "{2}(am|is|are|was|were): :VBG"); //When leaders are willing to prioritize trust over performance, performance almost always follows.
        wordRules.put("saw/JJ", " : :VBD");
        wordRules.put("wet/JJ", "get|got|gotten: :RP");
        wordRules.put("'s/POS", " :being:VBZ");
        wordRules.put("ca/MD", " : :SYM");
        wordRules.put("[A-Za-z]{2,}ed/JJ", "{3}(have|'ve|has|had): :VBN"); //𝑰'𝒗𝒆 𝒋𝒖𝒔𝒕 𝒂𝒃𝒐𝒖𝒕 𝒇𝒊𝒏𝒊𝒔𝒉𝒆𝒅 𝒑𝒂𝒊𝒏𝒕𝒊𝒏𝒈 𝒕𝒉𝒆 𝒍𝒊𝒗𝒊𝒏𝒈 𝒓𝒐𝒐𝒎.

        wordRules.put("parmenides/NNS", " : :NNP");
        wordRules.put("anaximenes/NNS", " : :NNP");
        wordRules.put("empedocles/NNS", " : :NNP");
        wordRules.put("descartes/NNS", " : :NNP");
        wordRules.put("cosmos/NNS", " : :NN");
        wordRules.put("lts/NNS", " : :NN");
        wordRules.put("ago/IN", " : :RB");

    }

    public static Object getWordRules(String key) {
        return wordRules.get(key);
    }

    public static Object getTagRules(String key) {
        return tagRules.get(key);
    }

//    public static void main(String[] args){
//        boolean matches = "PRP".matches("^(?!.*(NN|NNP)).*$");
//        System.out.println(matches);
//    }
}
