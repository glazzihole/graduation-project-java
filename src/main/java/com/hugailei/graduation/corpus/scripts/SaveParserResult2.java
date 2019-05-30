package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.Word;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description:
 * </p>
 **/
public class SaveParserResult2 {
    //数据库主机地址
    private static final String DB_HOST = "192.168.99.100";

    //数据库端口
    private static final String DB_PORT = "3307";

    //数据库名称
    private static final String DB_NAME="corpus";

    //数据库表名称
    private static final String WORD_COLL_NAME="tb_word";

    //数据库用户名
    private static final String USER_NAME="root";

    //数据库用户密码
    private static final String USER_PASSWORD="123456";

    //语料库名称
    private static final String CORPUS_NAME="bnc";

    private static final String TEMP_FILE_PATH = "C:\\Users\\GAILEI\\Desktop\\毕业论文相关\\temp.txt";

    public static void main(String[] args) throws Exception {
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url, USER_NAME, USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        //读取语料
        Map<String, Word> wordLemmaPos2Word = new HashMap<>();
//        try {
//            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(TEMP_FILE_PATH)));
//            wordLemmaPos2Word = (Map<String, Word>)ois.readObject();
//        } catch (Exception e) {
//            e.printStackTrace();
//            wordLemmaPos2Word = new HashMap<>();
//        }

        if (wordLemmaPos2Word.isEmpty()) {
            // 先从数据库中读取句子
            PreparedStatement preparedStatement = con.prepareStatement("SELECT id, sentence FROM tb_sentence where corpus = '"+ CORPUS_NAME +"'");
            // 遍历并分析句子
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                try {
                    String sentence = resultSet.getString("sentence");
                    System.out.println("sentence: " + sentence);
                    String sentenceId = resultSet.getString("id");
                    // 获取标注结果
                    List<CoreMap> result = StanfordParserUtil.parse(sentence);
                    for (CoreMap sentenceCoreMap : result) {
                        for (CoreLabel token : sentenceCoreMap.get(CoreAnnotations.TokensAnnotation.class)) {
                            // 单词
                            String word = token.get(CoreAnnotations.TextAnnotation.class);
                            // 词性
                            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                            // 原型
                            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                            if (word.toLowerCase().matches("[\\p{P}0-9]")) {
                                continue;
                            }

                            //存储单词
                            String key = word.toLowerCase() + "_" + lemma.toLowerCase() + "_" + pos;
                            if (wordLemmaPos2Word.containsKey(key)) {
                                Word newWord = wordLemmaPos2Word.get(key);
                                int newFreq = newWord.getFreq() + 1;
                                String newSentenceIds = newWord.getSentenceIds() + sentenceId + ",";
                                newWord.setFreq(newFreq);
                                newWord.setSentenceIds(newSentenceIds);
                                wordLemmaPos2Word.put(key, newWord);
                            } else {
                                Word newWord = new Word(null,
                                        word.toLowerCase(),
                                        pos,
                                        lemma.toLowerCase(),
                                        1,
                                        sentenceId+",",
                                        CORPUS_NAME
                                );
                                wordLemmaPos2Word.put(key, newWord);
                            }
                        } //for token
                    } // for sentence
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("出错了，不过我还能挺住");
                }
            } // while
        }

        System.out.println("数据统计完成，现在开始存入数据库");

        try {
            for (Map.Entry<String, Word> entry : wordLemmaPos2Word.entrySet()) {
                Word word = entry.getValue();
                System.out.println("WORD: " + word.toString());
                PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + WORD_COLL_NAME
                        + "(form, pos, lemma, freq, sentence_ids, corpus) "
                        + " VALUES (?, ?, ?, ?, ?, ?)");
                preparedStatement.setString(1, word.getForm());
                preparedStatement.setString(2, word.getPos());
                preparedStatement.setString(3, word.getLemma());
                preparedStatement.setInt(4, word.getFreq());
                preparedStatement.setString(5, word.getSentenceIds());
                preparedStatement.setString(6, word.getCorpus());
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("存入数据库出错，正在写入临时文件");
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                    new File(TEMP_FILE_PATH)));
            oo.writeObject(wordLemmaPos2Word);
        }
    }
}
