package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.domain.WordWithTopic;
import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.util.StanfordParserUtil;
import com.hugailei.graduation.corpus.util.TopicClassifyUtil;
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
 * @date 2018/12/21
 * <p>
 * description: 给数据库中的文章进行主题标注
 * </p>
 **/
public class LabelTopic {

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String TEXT_COLL_NAME="tb_text";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    private static Connection con;

    static {

        try {
            //连接mysql数据库
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver);
            String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
            con = DriverManager.getConnection(url, USER_NAME, USER_PASSWORD);
            if (!con.isClosed()) {
                System.out.println("成功连接至数据库!");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
//        formatText();
//        labelTextTopic();
//        labelSentenceTopic();
        labelCollocationTopic();
        labelNgramTopic();
        labelWordTopic();
    }

    /**
     * 重新格式化tb_text中text的数据
     * @throws Exception
     */
    private static void formatText() throws Exception{
        // 先从数据库中读取文章
        PreparedStatement preparedStatement = con.prepareStatement("SELECT id FROM tb_text");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            long textId = resultSet.getLong("id");
            PreparedStatement ps = con.prepareStatement("SELECT sentence FROM tb_sentence WHERE text_id = ? ORDER BY id ASC");
            ps.setLong(1, textId);
            ResultSet rs = ps.executeQuery();
            StringBuilder text = new StringBuilder();
            while (rs.next()) {
                String sentence = rs.getString("sentence");
                text.append(sentence);
                if (!(sentence.endsWith(".") || sentence.endsWith("?") || sentence.endsWith("!"))) {
                    text.append(". ");
                } else {
                    text.append(" ");
                }
            }
            System.out.println(text.toString());

            ps = con.prepareStatement("UPDATE tb_text SET text = ? WHERE id = ?");
            ps.setString(1, text.toString());
            ps.setLong(2, textId);
            ps.execute();
        }
        System.out.println("formatText操作完成");
    }

    /**
     * 标注文章的主题
     * @throws Exception
     */
    public static void labelTextTopic() throws Exception{
        // 先从数据库中读取文章
        PreparedStatement preparedStatement = con.prepareStatement("SELECT id, text FROM tb_text");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            // 分析文章主题并更新数据库
            String text = resultSet.getString("text");
            long id = resultSet.getLong("id");
            List<TopicDto> topicDtoList = TopicClassifyUtil.getTopicInfoList(text);
            int topicNum = topicDtoList.get(0).getTopicNum();
            System.out.println(topicDtoList.get(0).toString());
            PreparedStatement ps = con.prepareStatement("UPDATE " + TEXT_COLL_NAME + " SET topic = ? WHERE id = ?");
            ps.setInt(1, topicNum);
            ps.setLong(2, id);
            ps.execute();
        }
        System.out.println("labelTextTopic执行完成");
    }

    /**
     * 标注句子主题
     * @throws Exception
     */
    public static  void labelSentenceTopic() throws Exception{
        // 从数据库中读取句子
        PreparedStatement preparedStatement = con.prepareStatement("SELECT id, topic FROM tb_text");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            long textId = resultSet.getLong("id");
            int topic = resultSet.getInt("topic");

            PreparedStatement ps2 = con.prepareStatement("UPDATE tb_sentence SET topic = ? WHERE text_id = ?");
            ps2.setInt(1, topic);
            ps2.setLong(2, textId);
            ps2.execute();
            System.out.println("句子主题：" + topic);

        }

        System.out.println("labelSentenceTopic执行完成");
    }

    /**
     * 标注搭配主题
     * @throws Exception
     */
    public static void labelCollocationTopic() throws Exception{
        Map<String, String> key2SenteceIds = new HashMap<>();
        String[] corpusArray = {"bnc","chinadaily"};
        for (String corpus : corpusArray) {
            // 从数据库中读取搭配信息
            PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM tb_collocation WHERE corpus = '" + corpus + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String sentenceIds = resultSet.getString("sentence_ids");
                String firstWord = resultSet.getString("first_word");
                String firstPos = resultSet.getString("first_pos");
                String secondWord = resultSet.getString("second_word");
                String secondPos = resultSet.getString("second_pos");
                String thirdWord = resultSet.getString("third_word");
                String thirdPos = resultSet.getString("third_pos");

                for (String sentenceIdString : sentenceIds.split(",")) {
                    long sentenceId = Long.valueOf(sentenceIdString);
                    PreparedStatement ps = con.prepareStatement("SELECT topic FROM tb_sentence WHERE id = ?");
                    ps.setLong(1, sentenceId);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int topic = rs.getInt("topic");
                        String key = firstWord + "~" + firstPos + "~"
                                + secondWord + "~" + secondPos + "~"
                                + thirdWord + "~" + thirdPos + "~"
                                + corpus + "~" + topic ;
                        String newSentenceIds = sentenceIdString + ",";
                        if (key2SenteceIds.containsKey(key)) {
                            newSentenceIds = newSentenceIds + key2SenteceIds.get(key);
                        }
                        key2SenteceIds.put(key, newSentenceIds);
                        System.out.println(key);
                    }
                }
            }

            System.out.println("分析完成，开始存入数据库");
            try {
                for (Map.Entry entry : key2SenteceIds.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    String[] data = key.split("~");
                    if (data.length == 8) {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO tb_collocation_with_topic(" +
                                "first_word, " +
                                "first_pos, " +
                                "second_word, " +
                                "second_pos, " +
                                "third_word, " +
                                "third_pos, " +
                                "corpus, " +
                                "topic, " +
                                "sentence_ids, " +
                                "freq" +
                                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        ps.setString(1, data[0]);
                        ps.setString(2, data[1]);
                        ps.setString(3, data[2]);
                        ps.setString(4, data[3]);
                        ps.setString(5, data[4]);
                        ps.setString(6, data[5]);
                        ps.setString(7, data[6]);
                        ps.setInt(8, Integer.valueOf(data[7]));
                        ps.setString(9, value);
                        ps.setInt(10, value.split(",").length);
                        ps.execute();
                        System.out.println(ps.toString());
                    } else if (data.length == 6) {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO tb_collocation_with_topic(" +
                                "first_word, " +
                                "first_pos, " +
                                "second_word, " +
                                "second_pos, " +
                                "corpus, " +
                                "topic, " +
                                "sentence_ids, " +
                                "freq" +
                                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                        ps.setString(1, data[0]);
                        ps.setString(2, data[1]);
                        ps.setString(3, data[2]);
                        ps.setString(4, data[3]);
                        ps.setString(5, data[4]);
                        ps.setInt(6, Integer.valueOf(data[5]));
                        ps.setString(7, value);
                        ps.setInt(8, value.split(",").length);
                        ps.execute();
                        System.out.println(ps.toString());
                    }
                }
                System.out.println("labelCollocationTopic操作完成");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("操作失败，存入序列化文件");
                ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                        new File("E:\\collocatin_with_topic.txt")));
                oo.writeObject(key2SenteceIds);
                oo.close();
            }
        }
    }

    /**
     * 标注ngram主题
     * @throws Exception
     */
    public static void labelNgramTopic() throws Exception {
        Map<String, Integer> key2Freq = new HashMap<>();
        // 从数据库中读取ngram信息
        PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM tb_ngram");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String corpus = resultSet.getString("corpus");
            int nValue = resultSet.getInt("n_value");
            String nGramString = resultSet.getString("ngram_str");

            PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_sentence WHERE corpus = " + corpus);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String text = rs.getString("text");
                int topic = rs.getInt("topic");

                if (text.toLowerCase().contains(nGramString)) {
                    int temp = text.toLowerCase().split(nGramString).length;
                    int freq =  temp == 1 ? 1 : temp - 1;
                    String key = nGramString + "~" + nValue + "~" + corpus + "~" + topic;
                    if (key2Freq.containsKey(key)) {
                        freq = key2Freq.get(key) + freq;
                    }
                    key2Freq.put(key, freq);
                    System.out.println(key2Freq.toString());
                }
            }
        }
        System.out.println("分析完成，开始存入数据库");
        try {
            for (Map.Entry entry : key2Freq.entrySet()) {
                String key = (String) entry.getKey();
                int freq = (Integer) entry.getValue();
                String nGramString = key.split("~")[0];
                int nValue = Integer.valueOf(key.split("~")[1]);
                String corpus = key.split("~")[2];
                int topic = Integer.valueOf(key.split("~")[3]);
                PreparedStatement ps = con.prepareStatement("INSERT INTO tb_ngram_with_topic(" +
                        "ngram_str, " +
                        "n_value, " +
                        "freq, " +
                        "corpus, " +
                        "topic, " +
                        ") VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, nGramString);
                ps.setInt(2, nValue);
                ps.setInt(3, freq);
                ps.setString(4, corpus);
                ps.setInt(5, topic);
                ps.execute();
            }
            System.out.println("labelNgramTopic处理完成");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("操作失败，存入序列化文件");
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                    new File("E:\\ngram_with_topic.txt")));
            oo.writeObject(key2Freq);
            oo.close();
        }
    }

    /**
     * 标注单词的主题
     * @throws Exception
     */
    public static void labelWordTopic() throws Exception {
        Map<String, WordWithTopic> key2WordWithTopic = new HashMap<>();
        // 从数据库中读取句子信息
        PreparedStatement preparedStatement = con.prepareStatement("SELECT id, sentence, topic, corpus FROM tb_sentence");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String sentence = resultSet.getString("sentence");
            Long sentenceId = resultSet.getLong("id");
            int topic = resultSet.getInt("topic");
            String corpus = resultSet.getString("corpus");
            List<CoreMap> coreMapList = StanfordParserUtil.parse(sentence);
            for (CoreLabel coreLabel : coreMapList.get(0).get(CoreAnnotations.TokensAnnotation.class)) {
                String word = coreLabel.get(CoreAnnotations.TextAnnotation.class);
                String pos = coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class);
                String key = word + "~" + pos + "~" + lemma + "~" + topic;
                if (key2WordWithTopic.containsKey(key)) {
                    WordWithTopic wordWithTopic = key2WordWithTopic.get(key);
                    wordWithTopic.setFreq(wordWithTopic.getFreq() + 1);
                    wordWithTopic.setSentenceIds(wordWithTopic.getSentenceIds() + sentenceId + ",");
                    key2WordWithTopic.put(key, wordWithTopic);
                    System.out.println(wordWithTopic.toString());
                } else {
                    WordWithTopic wordWithTopic = new WordWithTopic();
                    wordWithTopic.setCorpus(corpus);
                    wordWithTopic.setForm(word);
                    wordWithTopic.setLemma(lemma);
                    wordWithTopic.setPos(pos);
                    wordWithTopic.setSentenceIds(sentenceId + ",");
                    wordWithTopic.setTopic(topic);
                    wordWithTopic.setFreq(1);
                    key2WordWithTopic.put(key, wordWithTopic);
                    System.out.println(wordWithTopic.toString());
                }
            }
        }
        System.out.println("分析完成，开始存入数据库");
        try {
            for (Map.Entry entry : key2WordWithTopic.entrySet()) {
                WordWithTopic value = (WordWithTopic) entry.getValue();
                PreparedStatement ps = con.prepareStatement("INSERT INTO tb_word_with_topic"
                        + "(form, pos, lemma, freq, sentence_ids, corpus, topic) "
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, value.getForm());
                ps.setString(2, value.getPos());
                ps.setString(3, value.getLemma());
                ps.setInt(4, value.getFreq());
                ps.setString(5, value.getSentenceIds());
                ps.setString(6, value.getCorpus());
                ps.setInt(7, value.getTopic());
                ps.execute();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("操作失败，存入序列化文件");
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(
                    new File("E:\\word_with_topic.txt")));
            oo.writeObject(key2WordWithTopic);
            oo.close();
        }
        System.out.println("labelWordTopic完成");
    }
}
