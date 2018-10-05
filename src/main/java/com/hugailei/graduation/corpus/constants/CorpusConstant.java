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

    public static final HashSet<String> SENTENCE_STRUCTURE_SET = new HashSet<String>(){
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
        }
    };

    public static final String PUNCT = "punct";

    public final static String REQUEST_ID = "REQUEST_ID";
}
