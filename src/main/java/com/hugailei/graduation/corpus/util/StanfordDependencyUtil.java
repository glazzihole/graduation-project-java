package com.hugailei.graduation.corpus.util;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author HU Gailei
 * @date 2018/10/4
 * <p>
 * description: 依存句法分析
 * </p>
 **/
public class StanfordDependencyUtil {

    public static List<SemanticGraphEdge> getDependency(String text) {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<SemanticGraphEdge> dependencyResult = new ArrayList<>();
        for (CoreMap sentence : sentences) {
            //句法依存分析
            SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
            //提取依存关系
            dependencyResult.addAll(dependency.edgeListSorted());
        }

        return dependencyResult;
    }

    public static void main(String[] args) {
        String text = "If I can help further please do not hesitate to contact me on 081 840 7879 Peter Fabian Director of Fundraising \n" +
                "FACTSHEET BECOMING AN ACET HOME CARE VOLUNTEER\n" +
                "About ACET's Home Care\n" +
                "Many people with AIDS have to spend long periods of time in hospital unless there is someone at home who can help and look after them.\n" +
                "ACET volunteers work as part of a team and provide help in many different ways to ensure that people don't spend time in hospital unnecessarily.\n" +
                "What do ACET volunteers do?\n" +
                "Transport clients to and from hospital\n" +
                "Housework\n" +
                "Shopping including collection of prescriptions\n" +
                "Daysitting and nightsitting\n" +
                "How much time to I need to give?\n" +
                "The simple answer is as much or as little as you feel able to give.\n" +
                "Many of our existing volunteers have families and jobs and are often very busy.\n" +
                "You don't have to make a firm commitment but obviously we like you to give us some idea of your availability.\n" +
                "This is so we can respond effectively to the needs of our clients.\n" +
                "Do I need any training?\n" +
                "Yes — but you are not expected to be a nurse.\n" +
                "You will be asked to complete an application form and subsequently to attend an ACET training course one evening a week for six weeks.\n" +
                "The subjects covered will include:\n" +
                "Death and Dying\n" +
                "Grief and Loss\n" +
                "Sex and Sexuality\n" +
                "Medical Aspects of HIV/AIDS\n" +
                "Race and Racism\n" +
                "Practical Issues\n" +
                "What if I find certain issues or situations difficult?\n" +
                "Your course leader will be available to help you.";
        List<SemanticGraphEdge> dependencyResult = getDependency(text);
        for (SemanticGraphEdge edge : dependencyResult) {
            //获取依存类型
            String dependencyType = edge.getRelation().toString();
            if (CorpusConstant.SENTENCE_STRUCTURE_SET.contains(dependencyType)) {
                System.out.println(edge.toString());
                //获取依存词的词性、词形、及原型
                String governorWordPos = edge.getGovernor().tag();
                String governorWord = edge.getGovernor().word();
                String governorWordLemma = edge.getGovernor().lemma();
                //获取支配词的词性、词形、及原型
                String dependentWordPos = edge.getDependent().tag();
                String dependentWord = edge.getDependent().word();
                String dependentWordLemma = edge.getDependent().lemma();
            }
        }//for
    }
}
