package com.hugailei.graduation.corpus.scripts;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.util.SentenceAnalysisUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * @author HU Gailei
 * @date 2019/3/29
 * <p>
 * description: 搭配提取召回率校验
 * </p>
 **/
public class CheckCollocation {
    private static final String FILE_PATH = "E:\\毕业论文相关\\搭配验证\\句子.txt";

    private static final String OUTPUT_FILE_PATH1 = "E:\\毕业论文相关\\搭配验证\\搭配.txt";

    private static final String OUPUT_FILE_PATH2 = "E:\\毕业论文相关\\搭配验证\\所有搭配.txt";

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static Set<String> SENTENCE_COLLOCATION = new LinkedHashSet<>();

    private static Set<String> ALL_COLLOCATION = new HashSet<>();

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        FileReader fileReader = new FileReader(FILE_PATH);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        FileWriter fileWriter = new FileWriter(new File(OUTPUT_FILE_PATH1), true);
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            List<CoreMap> coreMaps = StanfordParserUtil.parse(line);
            for (CoreMap sentence : coreMaps) {
                // 获取依存关系
                SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
                for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
                    String relation = edge.getRelation().toString();
                    int govIndex = edge.getGovernor().index();
                    int depIndex = edge.getDependent().index();
                    int firstIndex = 0, secondIndex = 0, thirdIndex = 0;
                    boolean found = false;
                    String firstWord = null, secondWord = null, firstPos = null, secondPos = null, thirdWord = null, thirdPos = null;
                    if (CorpusConstant.COLLOCATION_DEPENDENCY_RELATION_SET.contains(relation)) {
                        if ((relation.startsWith("nsubj") && !relation.startsWith("nsubjpass")) ||
                                "nmod:agent".equals(relation)) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            String nounverbRegex = "((NN[A-Z]{0,1})|(PRP))-(VB[A-Z]{0,1})";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                firstPos = edge.getGovernor().tag();
                                secondPos = edge.getDependent().tag();
                                found = true;
                            } else if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(nounverbRegex)) {
                                firstWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                firstIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                secondWord = edge.getGovernor().lemma();
                                firstPos = edge.getDependent().tag();
                                secondPos = edge.getGovernor().tag();
                                secondIndex = edge.getGovernor().index();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("dobj") || relation.startsWith("nsubjpass")) {
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                firstPos = edge.getGovernor().tag();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondPos = edge.getDependent().tag();
                                secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                found = true;
                            }
                            else {
                                firstWord = edge.getGovernor().lemma();
                                firstPos = edge.getGovernor().tag();
                                firstIndex = edge.getGovernor().index();
                                secondWord = edge.getDependent().lemma();
                                secondPos = edge.getDependent().tag();
                                secondIndex = edge.getDependent().index();
                                found = true;
                            }
                        }
                        else if (relation.startsWith("csubj")) {
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getGovernor().index(), sentence);
                            if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(verbNounRegex)) {
                                firstWord = edge.getDependent().lemma();
                                firstIndex = edge.getDependent().index();
                                secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                                firstPos = edge.getDependent().tag();
                                secondPos = edge.getGovernor().tag();
                                secondIndex = (temp == null ? edge.getGovernor().index() : temp.getIndex());
                                found = true;
                            }
                        }
                        else if (relation.startsWith("amod")) {
                            String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getGovernor().index(), sentence);
                            if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(adjNounRegex)) {
                                firstWord = edge.getDependent().lemma();
                                firstIndex = edge.getDependent().index();
                                firstPos = edge.getDependent().tag();
                                secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                                secondPos = edge.getGovernor().tag();
                                secondIndex = (temp == null ? edge.getGovernor().index() : temp.getIndex());
                                found = true;
                            }
                        }
                        else if (relation.startsWith("advmod")) {
                            String verbAdvRegex = "(VB[A-Z]{0,1})-(RB[A-Z]{0,1})";
                            String adjAdvRegex = "(JJ[A-Z]{0,1})-(RB[A-Z]{0,1})";
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdvRegex) ||
                                    (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjAdvRegex)) {
                                // 根据单词在句子中的位置调整在搭配中的先后顺序
                                if (govIndex < depIndex) {
                                    firstWord = edge.getGovernor().lemma();
                                    firstPos = edge.getGovernor().tag();
                                    firstIndex = edge.getGovernor().index();
                                    secondWord = edge.getDependent().lemma();
                                    secondPos = edge.getDependent().tag();
                                    secondIndex = edge.getDependent().index();
                                } else {
                                    firstWord = edge.getDependent().lemma();
                                    firstIndex = edge.getDependent().index();
                                    secondWord = edge.getGovernor().lemma();
                                    firstPos = edge.getDependent().tag();
                                    secondPos = edge.getGovernor().tag();
                                    secondIndex = edge.getGovernor().index();
                                }
                                found = true;
                            }
                        }
                        else if ("compound:prt".equals(relation) || "nmod".equals(relation)) {
                            firstWord = edge.getGovernor().lemma();
                            firstIndex = edge.getGovernor().index();
                            firstPos = edge.getGovernor().tag();
                            secondWord = edge.getDependent().lemma();
                            secondPos = edge.getDependent().tag();
                            secondIndex = edge.getDependent().index();
                            found = true;
                        }
                        else if ("compound".equals(relation)) {
                            if (edge.getDependent().tag().matches("NN.*") && edge.getGovernor().tag().matches("NN.*")) {
                                int governorIndex = edge.getGovernor().index();
                                int dependentIndex = edge.getDependent().index();
                                CoreLabel governorToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(governorIndex - 1);
                                String governorNer = governorToken.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                                CoreLabel dependentToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(dependentIndex - 1);
                                String dependetNer = dependentToken.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                                if (!(CorpusConstant.PROPER_NOUN_SET.contains(governorNer) && CorpusConstant.PROPER_NOUN_SET.contains(dependetNer))) {
                                    found = true;
                                    firstWord = edge.getDependent().lemma();
                                    firstPos = "NN";
                                    firstIndex = edge.getDependent().index();
                                    secondWord = edge.getGovernor().lemma();
                                    secondPos = "NN";
                                    secondIndex = edge.getGovernor().index();
                                }
                            }
                        }
                        else if (relation.startsWith("xcomp")) {
                            String verbAdjRegex = "(VB[A-Z]{0,1})-(JJ[A-Z]{0,1})";
                            String verbNounRegex = "(VB[A-Z]{0,1})-((NN[A-Z]{0,1})|(PRP))";
                            if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdjRegex) ||
                                    (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                                SentenceAnalysisUtil.Edge temp = null;
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                                }
                                firstWord = edge.getGovernor().lemma();
                                firstIndex = edge.getGovernor().index();
                                secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                secondPos = edge.getDependent().tag();
                                firstPos = edge.getGovernor().tag();
                                secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                found = true;
                            }

                            // 当第二个词为形容词是，判断动词是否为系统词，若是，则后面的形容词也可以修饰该动词的主语
                            if (edge.getDependent().tag().matches("JJ[A-Z]{0,1}")) {
                                if (CorpusConstant.COPULA_LEMMA_SET.contains(edge.getGovernor().lemma())) {
                                    int verbIndex = edge.getGovernor().index();
                                    for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                                        if (semanticGraphEdge.getRelation().toString().startsWith("nsubj") &&
                                                !semanticGraphEdge.getRelation().toString().startsWith("nsubjpass") &&
                                                semanticGraphEdge.getGovernor().index() == verbIndex) {
                                            firstIndex = edge.getDependent().index();
                                            firstWord = edge.getDependent().lemma();
                                            firstPos = "JJ";
                                            firstIndex = edge.getDependent().index();
                                            int subjectIndex = semanticGraphEdge.getDependent().index();
                                            SentenceAnalysisUtil.Edge subjectTemp = SentenceAnalysisUtil.getRealNounEdge(subjectIndex, sentence);
                                            secondWord = (subjectTemp == null ? semanticGraphEdge.getDependent().lemma() : subjectTemp.getLemma());
                                            secondPos = "NN";
                                            secondIndex = (subjectTemp == null ? semanticGraphEdge.getDependent().index() : subjectTemp.getIndex());
                                            found = true;
                                        }
                                    }
                                }
                            }
                        }
                        else if ("dep".equals(relation)) {
                            if (edge.getGovernor().tag().matches("VB[A-Z]{0,1}")) {
                                firstWord = edge.getGovernor().lemma();
                                firstPos = edge.getGovernor().tag();
                                firstIndex = edge.getGovernor().index();
                                secondWord = edge.getDependent().lemma();
                                secondPos = edge.getDependent().tag();
                                secondIndex = edge.getDependent().index();
                                if (edge.getDependent().tag().startsWith("NN")) {
                                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                                    secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                                    secondIndex = (temp == null ? edge.getDependent().index() : temp.getIndex());
                                }
                                found = true;
                            }
                        }
                        else if ("mwe".equals(relation)) {
                            found = true;
                            firstWord = edge.getGovernor().lemma();
                            firstPos = edge.getGovernor().tag();
                            firstIndex = edge.getGovernor().index();
                            secondWord = edge.getDependent().lemma();
                            secondPos = edge.getDependent().tag();
                            secondIndex = edge.getDependent().index();
                        }
                    } else if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(relation)) {
                        firstWord = edge.getGovernor().lemma();
                        firstPos = edge.getGovernor().tag();
                        firstIndex = edge.getGovernor().index();
                        secondWord = relation.split(":")[1];
                        secondPos = "IN";
                        thirdWord = edge.getDependent().lemma();
                        thirdPos = edge.getDependent().tag();
                        thirdIndex = edge.getDependent().index();
                        if (thirdPos.startsWith("NN")) {
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            if (temp != null) {
                                thirdWord = temp.getLemma();
                            }
                        }
                        found = true;
                    }

                    if (found) {
                        // 查询搭配中的动词是否存在词组搭配，若存在，则需要将所有搭配中的该动词替换为词组
//                        if (
//                                (firstPos.matches("VB.*") || secondPos.matches("VB.*"))
//                                        &&
//                                        (!relation.equals("compound:prt"))
//                        ) {
                        if (!relation.equals("compound:prt")) {
                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getGovernor().index() == firstIndex && e.getRelation().toString().equals("compound:prt")) {
                                    String phrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                                    firstWord = phrase;
                                    firstPos = "PHRASE";
                                }
                            }

                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getGovernor().index() == secondIndex && e.getRelation().toString().equals("compound:prt")) {
                                    String phrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                                    secondWord = phrase;
                                    secondPos = "PHRASE";
                                }
                            }

                            for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                                if (e.getGovernor().index() == thirdIndex && e.getRelation().toString().equals("compound:prt")) {
                                    String phrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                                    thirdWord = phrase;
                                    thirdPos = "PHRASE";
                                }
                            }
                        }

                        // 词性同一存储为该词性下原型的词性
                        for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
                            String posRegex = (String) entry.getKey();
                            String lemmaPos = (String) entry.getValue();
                            if (firstPos.matches(posRegex)) {
                                firstPos = lemmaPos;
                                break;
                            }
                        }

                        // 词性同一存储为该词性下原型的词性
                        for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
                            String posRegex = (String) entry.getKey();
                            String lemmaPos = (String) entry.getValue();
                            if (secondPos.matches(posRegex)) {
                                secondPos = lemmaPos;
                                break;
                            }
                        }

                        String key = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos).toLowerCase();
                        SENTENCE_COLLOCATION.add(key);
                        ALL_COLLOCATION.add(key);
                        if (!StringUtils.isBlank(thirdWord)) {
                            key = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + thirdWord + "_" + thirdPos).toLowerCase();
                            SENTENCE_COLLOCATION.add(key);
                            ALL_COLLOCATION.add(key);
                        }
                    }
                }
            }
            for (String collocation : SENTENCE_COLLOCATION) {
                fileWriter.write(collocation + "    ");
                fileWriter.flush();
            }
            fileWriter.write( "\r\n");
            fileWriter.flush();
            SENTENCE_COLLOCATION.clear();
        }// while

        fileWriter = new FileWriter(new File(OUPUT_FILE_PATH2));
        for (String collocation : ALL_COLLOCATION) {
            fileWriter.write(collocation + "\r\n");
            fileWriter.flush();
        }
        System.out.println("处理完成！");
    }
}
