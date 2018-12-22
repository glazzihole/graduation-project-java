package com.hugailei.graduation.corpus.scripts;

import com.hugailei.graduation.corpus.dto.TopicDto;
import com.hugailei.graduation.corpus.util.TopicClassifyUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

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

    public static void main(String[] args) throws Exception {
        //labelTextTopic();
        labelSentenceTopic();
    }

    /**
     * 标注文章的主题
     * @throws Exception
     */
    public static  void labelTextTopic() throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url, USER_NAME, USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

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
        System.out.println("执行完成");
    }

    public static  void labelSentenceTopic() throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url, USER_NAME, USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        // 先从数据库中读取文章
        PreparedStatement preparedStatement = con.prepareStatement("SELECT sentence.id, text.topic FROM tb_sentence AS sentence, tb_text AS text " +
                "WHERE sentence.text_id = text.id");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            long sentenceId = resultSet.getLong("sentence.id");
            int topic = resultSet.getInt("topic");
            PreparedStatement ps2 = con.prepareStatement("UPDATE tb_sentence SET topic = ? WHERE id = ?");
            ps2.setInt(1, topic);
            ps2.setLong(2, sentenceId);
            ps2.execute();
            System.out.println(topic);
        }

        System.out.println("执行完成");
    }
}
