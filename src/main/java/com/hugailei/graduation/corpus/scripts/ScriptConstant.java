package com.hugailei.graduation.corpus.scripts;

import java.util.HashSet;

/**
 * @author HU Gailei
 * @date 2018/9/30
 * <p>
 * description: 脚本里用到的一些常量
 * </p>
 **/
public class ScriptConstant {
    public static final HashSet<String> SENTENCE_STRUCTURE = new HashSet<String>(){
        {
            add("(NN.*)(_)(VB.*)");
            add("(VB.*)(_)(NN.*)");
            add("(JJ.*)(_)(NN.*)");
            add("(RD.*)(_)(JJ.*)");
            add("(RD.*)(_)(VB.*)");
            add("(VB.*)(_)(RD.*)");
            add("(VB.*)(_)(JJ.*)");
            add("(VB.*)(_)(IN)");
            add("(IN_)(NN.*)");
            add("(DT_)(NN.*)");
        }
    };

    public static final String PUNCT = "punct";
}
