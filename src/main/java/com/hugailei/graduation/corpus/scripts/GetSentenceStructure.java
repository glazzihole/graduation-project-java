package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.utils.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/9/30
 * <p>
 * description: 获取句子的句法
 * </p>
 **/
public class GetSentenceStructure {


    public static void main(String[] args) {
        List<CoreMap> coreMapList = StanfordParserUtil.parse("");
        for (CoreMap sentence : coreMapList) {
            //句法依存分析
            SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
            //System.out.println( dependency.toString( OutputFormat.XML ) );

            //提取依存关系
            for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                //获取依存类型
                String dependencyType = edge.getRelation().toString();
                if (!ScriptConstant.PUNCT.equals(dependencyType)) {
                    //获取依存词的词性、词形、及原型
                    String governorWordPos = edge.getGovernor().tag();
                    String governorWord = edge.getGovernor().word();
                    String governorWordLemma = edge.getGovernor().lemma();
                    //获取支配词的词性、词形、及原型
                    String dependentWordPos = edge.getDependent().tag();
                    String dependentWord = edge.getDependent().word();
                    String dependentWordLemma = edge.getDependent().lemma();
                }
            }
        }
    }
}
