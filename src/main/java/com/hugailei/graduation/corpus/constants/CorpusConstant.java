package com.hugailei.graduation.corpus.constants;

import java.util.HashSet;

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

}
