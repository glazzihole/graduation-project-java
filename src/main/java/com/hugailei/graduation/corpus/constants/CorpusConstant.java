package com.hugailei.graduation.corpus.constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
public class CorpusConstant {
    public static final String DEFAULT_CORPUS_NAME = "bnc";

    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final int SUCCESS_CODE = 200;
    public static final int FAILED_CODE = 999999;

    public static final HashSet<String> SYNTACTIC_PRIMING_STRUCTURE_SET = new HashSet<String>(){
        {
            add("ex");
            add("pobj");
            add("to");
            add("advcl");
            add("csubj");
            add("csubjpass");
            add("purpcl");
            add("dobj");
            add("iobj");
            add("tclaus");
            add("rcmod");
            add("expl");
            add("infmod");
        }
    };

    public static final HashSet<String> SENTENCE_STRUCTURE_SET = new HashSet<String>(){
        {
            add("ip");
            add("lcp");
            add("pp");
            add("cp");
            add("dnp");
            add("advp");
            add("dp");
            add("qp");
            add("ex");
            add("md");
            add("to");
            add("wp");
            add("wp$");
            add("wrb");
            add("acomp");
            add("advcl");
            add("agent");
            add("csubj");
            add("csubjpass");
            add("dep");
            add("dobj");
            add("iobj");
            add("expl");
            add("infmod");
            add("npadvmod");
            add("nsubj");
            add("nsubjpass");
            add("parataxis");
            add("partmod");
            add("poss");
            add("prt");
            add("purpcl");
            add("rcmod");
            add("csubj");
            add("xsubj");
            add("pobj");
        }
    };

    public final static String REQUEST_ID = "REQUEST_ID";

    public final static String LEMMA = "lemma";
    public final static String POS = "pos";
    public final static String FORM = "form";
    public static final String PUNCT = "punct";

    public static final String STOP_WORD_REG = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]";


    /**
     * 常见的系动词，均为原型
     */
    public static final HashSet<String> COPULA_LEMMA_SET = new HashSet<String>() {
        {
            add("be");
            add("look");
            add("sound");
            add("taste");
            add("smell");
            add("feel");
            add("seem");
            add("appear");
            add("become");
            add("get");
            add("turn");
            add("grow");
//            add("make");
            add("come");
            add("go");
            add("fall");
            add("run");
            add("remain");
            add("keep");
            add("stay");
            add("continue");
            add("stand");
            add("rest");
            add("lie");
            add("hold");
            add("prove");
        }
    };

    /**
     * 常见的同位语从句先行词
     */
    public static final HashSet<String> APPOSITIVE_ANTECEDENT_SET = new HashSet<String>() {
        {
            add("idea");
            add("plan");
            add("fact");
            add("theory");
            add("promise");
            add("hope");
            add("news");
            add("doubt");
            add("truth");
            add("information");
            add("suggestion");
            add("question");
            add("thought");
            add("belief");
            add("conclusion");
        }
    };

    /**
     * 双宾语动词集合
     */
    public static final HashSet<String> DOUBLE_OBJECT_VERB_SET = new HashSet<String>() {
        {
            add("award");
            add("buy");
            add("give");
            add("leave");
            add("lend");
            add("offer");
            add("pay");
            add("show");
            add("teach");
            add("tell");
            add("bring");
            add("make");
            add("pass");
            add("sell");
            add("send");
            add("sing");
            add("write");
            add("answer");
            add("deny");
            add("envy");
            add("refuse");
            add("save");
            add("spare");
        }
    };

    /**
     * 短语标识集合
     */
    public static final HashSet<String> PHRASE_LABEL_SET = new HashSet<String>() {
        {
            add("NP");
            add("VP");
            add("ADJP");
            add("ADVP");
            add("CONJP");
            add("FRAG");
            add("LCP");
            add("PP");
            add("CP");
            add("DNP");
            add("DP");
            add("QP");
        }
    };

    /**
     * 被动语态介词集合
     */
    public static final HashSet<String> PASSIVE_PREP_SET = new HashSet<String>() {
        {
            add("by");
            add("to");
            add("from");
            add("at");
            add("for");
        }
    };

    /**
     * 可做单位量词的名词，比如 a cup of，a bottle of等
     */
    public static  final  HashSet<String> PARTITIVE_NOUN_SET = new HashSet<String>() {
        {
            add("lot");     add("amount");      add("quantity");    add("heap");    add("ton");         add("bit");
            add("pinch");   add("spot");        add("grain");       add("cup");     add("can");         add("box");
            add("bag");     add("bowl");        add("bottle");      add("bucket");  add("spoon");       add("glass");
            add("pack");    add("pot");         add("basket");      add("tube");    add("spoonful");    add("basketful");
            add("group");   add("gang");        add("troop");       add("flock");   add("swarm");       add("cloud");
            add("herd");    add("mountain");    add("thread");      add("stream");  add("type");        add("tape");
        }
    };

    /**
     * 词性正则表达式-原型词性
     */
    public static final Map<String, String> POS_REGEX_TO_LEMMA_POS= new HashMap<String, String>() {
        {
            put("VB.*", "VB");
            put("NN.*", "NN");
            put("JJ.*", "JJ");
            put("RB.*", "RB");
        }
    };
}
