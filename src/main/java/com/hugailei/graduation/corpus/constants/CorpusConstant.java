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
            add("make");
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
     * 时间状语从句修饰词
     */
    public static final HashSet<String>  TEMPORAL_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("when");
            add("while");
            add("since");
            add("until");
            add("till");
            add("before");
            add("after");
        }
    };

    /**
     * 时间状语从句修饰短语/词组
     */
    public static final HashSet<String>  TEMPORAL_ADVERBIAL_CLAUSE_PHRASE_SET = new HashSet<String>() {
        {
            add("as soon as");
            add("the moment");
            add("every time");
            add("each time");
            add("the minute");
            add("the instant");
            add("the day");
            add("the week");
            add("the month");
            add("the year");
            add("the first time");
            add("next time");
            add("any time");
            add("the last time");
            add("all the time");
            add("from the time");
            add("by the time");

        }
    };

    /**
     * 地点状语从句修饰词
     */
    public static final HashSet<String>  PLACE_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("where");
            add("wherever");
            add("anywhere");
            add("everywhere");
        }
    };

    /**
     * 原因状语从句修饰词
     */
    public static final HashSet<String>  CAUSE_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("because");
        }
    };

    /**
     * 原因状语从句修饰词组/短语
     */
    public static final HashSet<String>  CAUSE_ADVERBIAL_CLAUSE_PHRASE_SET = new HashSet<String>() {
        {
            add("seeing that");
            add("seeing as");
            add("considering that");
            add("now that");
            add("for the reason that");
        }
    };

    /**
     * 条件状语从句修饰词
     */
    public static final HashSet<String>  CONDITION_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("if");
            add("whether");
            add("unless");
        }
    };

    /**
     * 条件状语从句修饰词组/短语
     */
    public static final HashSet<String>  CONDITION_ADVERBIAL_CLAUSE_PHRASE_SET = new HashSet<String>() {
        {
            add("as long as");
            add("so long as");
            add("unless");
        }
    };

    /**
     * 目的状语从句修饰词
     */
    public static final HashSet<String>  PURPOSE_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("idea");
            add("plan");
        }
    };

    /**
     * 让步状语从句修饰词
     */
    public static final HashSet<String>  CONCESSION_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("idea");
            add("plan");
        }
    };

    /**
     * 方式状语从句修饰词
     */
    public static final HashSet<String>  MANNER_ADVERBIAL_CLAUSE_WORD_SET = new HashSet<String>() {
        {
            add("idea");
            add("plan");
        }
    };
}
