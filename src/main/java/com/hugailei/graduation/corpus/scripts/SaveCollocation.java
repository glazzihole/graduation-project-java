package com.hugailei.graduation.corpus.scripts;

import com.bfsuolframework.core.utils.StringUtils;
import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.util.SentenceAnalysisUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description: 提取搭配并存储
 * </p>
 **/
public class SaveCollocation {

    private static final String CORPUS = "chinadaily";

    private static final String TEMP_FILE_PATH = "E:\\chinadaily-collocation-temp.txt";

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static Map<String, Set<String>> KEY_TO_SENTENCEIDSET = new HashMap<>();

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        // 读取序列化文件，若不为空则从文件中读取数据并存入数据库
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(TEMP_FILE_PATH)));
            KEY_TO_SENTENCEIDSET = (Map<String, Set<String>>)ois.readObject();
        } catch (Exception e) {
            KEY_TO_SENTENCEIDSET = new HashMap<>();
            System.out.println("序列化文件无法加载，开始重新分析");
        }
        if (KEY_TO_SENTENCEIDSET.isEmpty()) {
            // 先从数据库中读取句子
            PreparedStatement preparedStatement = con.prepareStatement("SELECT COUNT(*) as count FROM tb_sentence where corpus = '" + CORPUS + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            int totalCount = 0;
            if (resultSet.next()) {
                totalCount = resultSet.getInt("count");
            }
            preparedStatement = con.prepareStatement("SELECT id, sentence FROM tb_sentence where corpus = '" + CORPUS + "'");
            // 遍历并分析句子
            resultSet = preparedStatement.executeQuery();
            int i = 1;
            while (resultSet.next()) {
                String sentence = resultSet.getString("sentence");
                System.out.println(i++ + " / " + totalCount);
                Long sentenceId = resultSet.getLong("id");
                List<CoreMap> sentences = StanfordParserUtil.parse(sentence);
                for (CoreMap text : sentences) {
                    getCollocation(text, sentenceId);
                }
            }
        }
        System.out.println("分析完毕，开始存入数据库");
        try {
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO tb_collocation"
                    + "(first_word, first_pos, second_word, second_pos, third_word, third_pos, sentence_ids, corpus, freq, word_pair) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            //遍历Map，把结果存入数据库中
            for (Map.Entry entry : KEY_TO_SENTENCEIDSET.entrySet()) {
                String key = (String)entry.getKey();
                System.out.println(key);
                String[] data = key.split("_");
                Set<String> sentenceIdSet = KEY_TO_SENTENCEIDSET.get(key);
                int freq = sentenceIdSet.size();
                String sentenceIds = "";
                for (String setneceId : sentenceIdSet) {
                    sentenceIds += setneceId + ",";
                }
                if (freq >= 2) {
                    if (data.length == 4) {
                        preparedStatement.setString(1, data[0]);
                        preparedStatement.setString(2, data[1].toUpperCase());
                        preparedStatement.setString(3, data[2]);
                        preparedStatement.setString(4, data[3].toUpperCase());
                        preparedStatement.setString(5, "");
                        preparedStatement.setString(6, "");
                        preparedStatement.setString(7, sentenceIds);
                        preparedStatement.setString(8, CORPUS);
                        preparedStatement.setInt(9, freq);
                        preparedStatement.setString(10, data[0] + " " + data[2]);
                        preparedStatement.execute();
                    } else if (data.length == 6) {
                        preparedStatement.setString(1, data[0]);
                        preparedStatement.setString(2, data[1].toUpperCase());
                        preparedStatement.setString(3, data[2]);
                        preparedStatement.setString(4, data[3].toUpperCase());
                        preparedStatement.setString(5, data[4]);
                        preparedStatement.setString(6, data[5].toUpperCase());
                        preparedStatement.setString(7, sentenceIds);
                        preparedStatement.setString(8, CORPUS);
                        preparedStatement.setInt(9, freq);
                        preparedStatement.setString(10, data[0] + " " + data[2] + " " + data[4]);
                        preparedStatement.execute();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("存入数据库失败，开始序列化存入文件");
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                    new File(TEMP_FILE_PATH)));
            oo.writeObject(KEY_TO_SENTENCEIDSET);
            oo.close();
        }
    }

    /**
     * 通过句法分析，提取指定形式的搭配
     *
     * @param sentence
     * @param sentenceId
     * @return
     */
    private static void getCollocation (CoreMap sentence, long sentenceId) {
        // 获取依存关系
        SemanticGraph dependency = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        for (SemanticGraphEdge edge : dependency.edgeListSorted()) {
            String relation = edge.getRelation().toString();
            int govIndex = edge.getGovernor().index();
            int depIndex = edge.getDependent().index();
            boolean found = false;
            String firstWord = null, secondWord = null, firstPos = null, secondPos = null, thirdWord = null, thirdPos = null;
            if (CorpusConstant.COLLOCATION_DEPENDENCY_RELATION_SET.contains(relation)) {
                if ((relation.startsWith("nsubj") && !relation.startsWith("nsubjpass")) ||
                    "nmod:agent".equals(relation)) {
                    String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                    String nounverbRegex = "(NN[A-Z]{0,1})-(VB[A-Z]{0,1})";
                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                    if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjNounRegex)) {
                        found = true;
                        firstWord = edge.getGovernor().lemma();
                        secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                        firstPos = edge.getGovernor().tag();
                        secondPos = edge.getDependent().tag();
                    } else if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(nounverbRegex)) {
                        found = true;
                        firstWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                        secondWord = edge.getGovernor().lemma();
                        firstPos = edge.getDependent().tag();
                        secondPos = edge.getGovernor().tag();
                    }
                }
                else if (relation.startsWith("dobj") || relation.startsWith("nsubjpass")) {
                    String verbNounRegex = "(VB[A-Z]{0,1})-(NN[A-Z]{0,1})";
                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                    if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                        found = true;
                        firstWord = edge.getGovernor().lemma();
                        firstPos = edge.getGovernor().tag();
                        secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                        secondPos = edge.getDependent().tag();
                    }
                }
                else if (relation.startsWith("csubj")) {
                    String verbNounRegex = "(VB[A-Z]{0,1})-(NN[A-Z]{0,1})";
                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getGovernor().index(), sentence);
                    if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(verbNounRegex)) {
                        found = true;
                        firstWord = edge.getDependent().lemma();
                        secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                        firstPos = edge.getDependent().tag();
                        secondPos = edge.getGovernor().tag();
                    }
                }
                else if (relation.startsWith("amod")) {
                    String adjNounRegex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getGovernor().index(), sentence);
                    if ((edge.getDependent().tag() + "-" + edge.getGovernor().tag()).matches(adjNounRegex)) {
                        found = true;
                        firstWord = edge.getDependent().lemma();
                        firstPos = edge.getDependent().tag();
                        secondWord = (temp == null ? edge.getGovernor().lemma() : temp.getLemma());
                        secondPos = edge.getGovernor().tag();
                    }
                }
                else if (relation.startsWith("advmod")) {
                    String verbAdvRegex = "(VB[A-Z]{0,1})-(RB[A-Z]{0,1})";
                    String adjAdvRegex = "(JJ[A-Z]{0,1})-(RB[A-Z]{0,1})";
                    if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdvRegex) ||
                        (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(adjAdvRegex)) {
                        found = true;
                        // 根据单词在句子中的位置调整在搭配中的先后顺序
                        if (govIndex < depIndex) {
                            firstWord = edge.getGovernor().lemma();
                            firstPos = edge.getGovernor().tag();
                            secondWord = edge.getDependent().lemma();
                            secondPos = edge.getDependent().tag();
                        } else {
                            firstWord = edge.getDependent().lemma();
                            secondWord = edge.getGovernor().lemma();
                            firstPos = edge.getDependent().tag();
                            secondPos = edge.getGovernor().tag();
                        }
                    }
                }
                else if ("compound:prt".equals(relation) || "nmod".equals(relation)) {
                    found = true;
                    firstWord = edge.getGovernor().lemma();
                    firstPos = edge.getGovernor().tag();
                    secondWord = edge.getDependent().lemma();
                    secondPos = edge.getDependent().tag();
                }
                else if (relation.startsWith("xcomp")) {
                    String verbAdjRegex = "(VB[A-Z]{0,1})-(JJ[A-Z]{0,1})";
                    String verbNounRegex = "(VB[A-Z]{0,1})-(NN[A-Z]{0,1})";
                    if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbAdjRegex) ||
                        (edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(verbNounRegex)) {
                        SentenceAnalysisUtil.Edge temp = null;
                        if (edge.getDependent().tag().startsWith("NN")) {
                            temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                        }
                        firstWord = edge.getGovernor().lemma();
                        secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                        secondPos = edge.getDependent().tag();
                        firstPos = edge.getGovernor().tag();
                        found = true;
                    }

                    // 当第二个词为形容词是，判断动词是否为系统词，若是，则后面的形容词也可以修饰该动词的主语
                    if (edge.getDependent().tag().matches("JJ[A-Z]{0,1}")) {
                        if (CorpusConstant.COPULA_LEMMA_SET.contains(edge.getGovernor().lemma())) {
                            int verbIndex = edge.getGovernor().index();
                            for (SemanticGraphEdge semanticGraphEdge : dependency.edgeListSorted()) {
                                if (semanticGraphEdge.getRelation().toString().startsWith("nsubj") &&
                                    !semanticGraphEdge.getRelation().toString().startsWith("nsubjpass") &&
                                    semanticGraphEdge.getDependent().tag().matches("NN[A-Z]{0,1}") &&
                                    semanticGraphEdge.getGovernor().index() == verbIndex) {
                                    firstWord = edge.getDependent().lemma();
                                    firstPos = "JJ";

                                    int subjectIndex = semanticGraphEdge.getDependent().index();
                                    SentenceAnalysisUtil.Edge subjectTemp = SentenceAnalysisUtil.getRealNounEdge(subjectIndex, sentence);
                                    secondWord = (subjectTemp == null ? semanticGraphEdge.getDependent().lemma() : subjectTemp.getLemma());
                                    secondPos = "NN";
                                    found = true;
                                }
                            }
                        }
                    }
                }
                else if ("dep".equals(relation)) {
                    if (edge.getGovernor().tag().matches("VB[A-Z]{0,1}")) {
                        found = true;
                        firstWord = edge.getGovernor().lemma();
                        firstPos = edge.getGovernor().tag();
                        secondWord = edge.getDependent().lemma();
                        secondPos = edge.getDependent().tag();
                        if (edge.getDependent().tag().startsWith("NN")) {
                            SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                            secondWord = (temp == null ? edge.getDependent().lemma() : temp.getLemma());
                        }
                    }
                }
            } else if (CorpusConstant.COLLOCATION_NOMD_RELATION_SET.contains(relation)) {
                firstWord = edge.getGovernor().lemma();
                firstPos = edge.getGovernor().tag();
                secondWord = relation.split(":")[1];
                secondPos = "IN";
                thirdWord = edge.getDependent().lemma();
                thirdPos = edge.getDependent().tag();
                if (thirdPos.startsWith("NN")) {
                    SentenceAnalysisUtil.Edge temp = SentenceAnalysisUtil.getRealNounEdge(edge.getDependent().index(), sentence);
                    if (temp != null) {
                        thirdWord = temp.getLemma();
                    }
                }
                if (!thirdPos.equals("PRP")) {
                    found = true;
                }
            }

            if (found) {
                // 查询搭配中的动词是否存在词组搭配，若存在，则需要将所有搭配中的改动次替换为词组
                if (
                    (firstPos.matches("VB.*") || secondPos.matches("VB.*"))
                        &&
                    (!relation.equals("compound:prt"))
                ){
                    int verbIndex = edge.getGovernor().index();
                    for (SemanticGraphEdge e : dependency.edgeListSorted()) {
                        if (e.getGovernor().index() == verbIndex && e.getRelation().toString().equals("compound:prt")) {
                            String verbPhrase = e.getGovernor().lemma() + " " + e.getDependent().lemma();
                            if (firstPos.matches("VB.*")) {
                                firstWord = verbPhrase;
                                firstPos = "PHRASE";
                            }
                            else {
                                secondWord = verbPhrase;
                                secondPos = "PHRASE";
                            }
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

                // 更新例句
                if (KEY_TO_SENTENCEIDSET.containsKey(key)) {
                    Set<String> sentenceIdSet = KEY_TO_SENTENCEIDSET.get(key);
                    sentenceIdSet.add(sentenceId + "");
                    KEY_TO_SENTENCEIDSET.put(key, sentenceIdSet);
                } else {
                    Set<String> sentenceIdSet = new HashSet<>();
                    sentenceIdSet.add(sentenceId + "");
                    KEY_TO_SENTENCEIDSET.put(key, sentenceIdSet);
                }

                // 若搭配中存在第三个词，则三个词的搭配再存储一次
                if (!StringUtils.isBlank(thirdWord)) {
                    // 词性同一存储为该词性下原型的词性
                    for (Map.Entry entry : CorpusConstant.POS_REGEX_TO_LEMMA_POS.entrySet()) {
                        String posRegex = (String) entry.getKey();
                        String lemmaPos = (String) entry.getValue();
                        if (thirdPos.matches(posRegex)) {
                            thirdPos = lemmaPos;
                            break;
                        }
                    }

                    key = (firstWord + "_" + firstPos + "_" + secondWord + "_" + secondPos + "_" + thirdWord + "_" + thirdPos).toLowerCase();

                    // 更新例句
                    if (KEY_TO_SENTENCEIDSET.containsKey(key)) {
                        Set<String> sentenceIdSet = KEY_TO_SENTENCEIDSET.get(key);
                        sentenceIdSet.add(sentenceId + "");
                        KEY_TO_SENTENCEIDSET.put(key, sentenceIdSet);
                    } else {
                        Set<String> sentenceIdSet = new HashSet<>();
                        sentenceIdSet.add(sentenceId + "");
                        KEY_TO_SENTENCEIDSET.put(key, sentenceIdSet);
                    }
                }
            }
        } // for (SemanticGraphEdge edge : dependency.edgeListSorted())
    }

}
