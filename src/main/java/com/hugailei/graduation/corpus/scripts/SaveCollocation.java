package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.util.FileUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * @author HU Gailei
 * @date 2018/11/18
 * <p>
 * description: 提取搭配并存储
 * </p>
 **/
public class SaveCollocation {

    private static final String FILE_PATH = "E:\\毕业论文相关\\bnc-sample-text";

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static final String CORPUS = "bnc";

    /**
     * 搭配的词性组合
     */
    private static final Set<String> COLLOCATION_PATT_SET = new HashSet<String>(){
        {
            // 名词-动词
            add("(NN[A-Z]{0,1})-(VB[A-Z]{0,1})");

            // 动词-名词
            add("(VB[A-Z]{0,1})-(NN[A-Z]{0,1})");

            // 形容词-名词
            add("(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})");

            // 动词-副词
            add("(VB[A-Z]{0,1})-(RB[A-Z{0,1}])");

            // 副词-动词
            add("(RB[A-Z{0,1}])-(VB[A-Z]{0,1})");

            // 动词-介词
            add("(VB[A-Z]{0,1})-IN");

            // 动词-小品词
            add("(VB[A-Z]{0,1})-RP");

            // 副词-形容词
            add("(RB[A-Z{0,1}])-(JJ[A-Z]{0,1})");
        }
    };

    private static Map<String, String> KEY_TO_SENTENCEIDS = new HashMap<>();

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        //读取语料
        List<File> fileList = new ArrayList<>();
        fileList = FileUtil.getFilesUnderPath(FILE_PATH, fileList);
        Long sentenceId = 1L;
        for (File file : fileList) {
            System.out.println("开始分析" + file.getCanonicalPath());
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";

            StringBuilder text = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("line: " + line);
                text.append(line.replace("\r", "").replace("\n", ""));
                List<CoreMap> sentences = StanfordParserUtil.parse(line);
                for (CoreMap sentence : sentences) {
                    getCollocation(sentence, sentenceId++);
                }
            }
        }

        System.out.println("分析完毕，开始存入数据库");
        try {
            //遍历Map，把结果存入数据库中
            for (Map.Entry entry : KEY_TO_SENTENCEIDS.entrySet()) {
                String key = (String)entry.getKey();
                String[] data = key.split("_");
                String sentenceIds = KEY_TO_SENTENCEIDS.get(key);
                int freq = sentenceIds.split(",").length;
                if (freq >= 2) {
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO tb_collocation"
                            + "(first_word, first_pos, second_word, second_pos, sentence_ids, corpus, freq) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, data[0]);
                    preparedStatement.setString(2, data[1].toUpperCase());
                    preparedStatement.setString(3, data[2]);
                    preparedStatement.setString(4, data[3].toUpperCase());
                    preparedStatement.setString(5, sentenceIds);
                    preparedStatement.setString(6, CORPUS);
                    preparedStatement.setInt(7, freq);
                    preparedStatement.execute();
                }
            }
        } catch (Exception e) {
            System.out.println("存入数据库失败，开始序列化存入文件");
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                    new File("E:\\temp.txt")));
            oo.writeObject(KEY_TO_SENTENCEIDS);
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
            String firstWord, secondWord, firstPos, secondPos;
            // 根据单词在句子中的位置调整在搭配中的先后顺序
            int govIndex = edge.getGovernor().index();
            int depIndex = edge.getDependent().index();

            if (govIndex < depIndex) {
                firstWord = edge.getGovernor().lemma();
                secondWord = edge.getDependent().lemma();
                firstPos = edge.getGovernor().tag();
                secondPos = edge.getDependent().tag();
            } else {
                firstWord = edge.getDependent().lemma();
                secondWord = edge.getGovernor().lemma();
                firstPos = edge.getDependent().tag();
                secondPos = edge.getGovernor().tag();
            }

            switch (edge.getRelation().toString()) {
                case "nsubj":
                case "top":
                    String regex = "(JJ[A-Z]{0,1})-(NN[A-Z]{0,1})";
                    if ((edge.getGovernor().tag() + "-" + edge.getDependent().tag()).matches(regex)) {
                        firstWord = edge.getGovernor().lemma();
                        secondWord = edge.getDependent().lemma();
                        firstPos = edge.getGovernor().tag();
                        secondPos = edge.getDependent().tag();
                    }else{
                        firstWord = edge.getDependent().lemma();
                        secondWord = edge.getGovernor().lemma();
                        firstPos = edge.getDependent().tag();
                        secondPos = edge.getGovernor().tag();
                    }
                    break;
                case "nsubjpass":
                case "dobj":
                case "attr":
                case "iobj":
                    firstWord = edge.getGovernor().lemma();
                    secondWord = edge.getDependent().lemma();
                    firstPos = edge.getGovernor().tag();
                    secondPos = edge.getDependent().tag();
                    break;
                default:
                    break;
            }

            // 判断搭配形式是否为指定的搭配类型
            for (String regex : COLLOCATION_PATT_SET) {
                // 若满足，放入结果Map中
                if ((firstPos + "-" + secondPos).matches(regex)) {
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
                    String sentenceIds = sentenceId + ",";
                    if (KEY_TO_SENTENCEIDS.containsKey(key)) {
                        sentenceIds += KEY_TO_SENTENCEIDS.get(key);
                    }
                    KEY_TO_SENTENCEIDS.put(key, sentenceIds);
                    break;
                }
            }
        }
    }
}
