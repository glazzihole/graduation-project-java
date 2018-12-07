package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.SentencePattern;
import com.hugailei.graduation.corpus.util.SentencePatternUtil;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/12/1
 * <p>
 * description: 存储从句和被从句修饰的单词
 * </p>
 **/
public class SaveModificandAndClause {

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static final String CORPUS = "bnc";

    private static final String SENTENCE_PATTERN_COLLOCATION = "tb_sentence_pattern";

    private static Map<String, Integer> KEY_TO_FREQ = new HashMap<>();

    private static Map<String, String> KEY_TO_SENTENCE_IDS = new HashMap<>();

    private static final String TEMP_FILE1_PATH = "E:\\temp1.txt";

    private static final String TEMP_FILE2_PATH = "E:\\temp2.txt";

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        // 先读取序列化临时文件内容
        // 读取序列化文件，若不为空则从文件中读取数据并存入数据库
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(TEMP_FILE1_PATH)));
            KEY_TO_FREQ = (Map<String, Integer>)ois.readObject();

            try {
                ois = new ObjectInputStream(new FileInputStream(new File(TEMP_FILE2_PATH)));
                KEY_TO_SENTENCE_IDS = (Map<String, String>)ois.readObject();
            } catch (Exception e) {
                KEY_TO_SENTENCE_IDS = new HashMap<>();
                throw new Exception("序列化文件2无法加载，开始重新分析");
            }

        } catch (Exception e) {
            KEY_TO_FREQ = new HashMap<>();
            System.out.println("序列化文件无法全部重新加载，开始重新分析");
        }
        if (KEY_TO_FREQ.isEmpty() || KEY_TO_SENTENCE_IDS.isEmpty()) {
            // 先从数据库中读取句子
            PreparedStatement preparedStatement = con.prepareStatement("SELECT tb_sentence.id, sentence FROM tb_sentence, tb_text WHERE tb_text.corpus = '" + CORPUS + "' and " +
                    "tb_sentence.text_id = tb_text.id");
            // 遍历并分析句子
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                try {
                    String sentence = resultSet.getString("sentence");
                    System.out.println("sentence: " + sentence);
                    String sentenceId = resultSet.getString("id");
                    List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
                    List<SentencePattern> sentencePatternList = SentencePatternUtil.matchAppositiveClauseOrAttributiveClause(coreMapList.get(0));
                    // 判断是否包含定语从句或同位语从句
                    if (sentencePatternList != null) {
                        for (SentencePattern sp : sentencePatternList) {
                            String key = sp.getModificand() + "_" + sp.getModificandPos() + "_" +sp.getClauseContent();
                            int freq = 1;
                            if (KEY_TO_FREQ.containsKey(key)) {
                                freq = KEY_TO_FREQ.get(key) + 1;
                            }
                            KEY_TO_FREQ.put(key, freq);

                            String sentenceIds = sentenceId + ",";
                            if (KEY_TO_SENTENCE_IDS.containsKey(key)) {
                                sentenceIds += KEY_TO_SENTENCE_IDS.get(key);
                            }
                            KEY_TO_SENTENCE_IDS.put(key, sentenceIds);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("出错啦，不过我还能挺住");
                    continue;
                }
            }
        }

        // 将分析结果存入数据库
        System.out.println("分析完毕，开始存入数据库");
        try {
            for (Map.Entry entry : KEY_TO_FREQ.entrySet()) {
                String key = (String)entry.getKey();
                int freq = (int)entry.getValue();
                String[] data = key.split("_");
                String sentenceIds = KEY_TO_SENTENCE_IDS.get(key);
                PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + SENTENCE_PATTERN_COLLOCATION
                        + "(type, modificand, modificand_pos, clause_content, freq, sentence_ids, corpus) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)");
                preparedStatement.setInt(1, 3);
                preparedStatement.setString(2, data[0].toLowerCase());
                preparedStatement.setString(3, data[1]);
                preparedStatement.setString(4, data[2].toLowerCase());
                preparedStatement.setInt(5, freq);
                preparedStatement.setString(6, sentenceIds);
                preparedStatement.setString(7, CORPUS);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("存入数据库失败，开始序列化存入文件：" + TEMP_FILE1_PATH);
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                    new File(TEMP_FILE1_PATH)));
            oo.writeObject(KEY_TO_FREQ);

            System.out.println("开始序列化存入文件：" + TEMP_FILE2_PATH);
            oo = new ObjectOutputStream(new FileOutputStream(
                    new File(TEMP_FILE2_PATH)));
            oo.writeObject(KEY_TO_SENTENCE_IDS);
            oo.close();
        }

    }
}
