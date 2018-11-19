package com.hugailei.graduation.corpus.scripts;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/11/19
 * <p>
 * description: 存储单词的同义词，近义词，反义词，相似词等信息
 * </p>
 **/
public class SaveWordExtension {
    //文件路径
    private static final String FILE_PATH = "E:\\单词辨析\\urlResult.txt";

    //数据库主机地址
    private static final String DB_HOST = "192.168.99.100";

    //数据库端口
    private static final String DB_PORT = "3307";

    //数据库名称
    private static final String DB_NAME="corpus";

    private static final String COLL_NAME = "tb_word_extension";

    //数据库用户名
    private static final String USER_NAME="root";

    //数据库用户密码
    private static final String USER_PASSWORD="123456";

    private static String[] TYPE_ARRAY = {"syn","ant","sim","rel"};

    public static void main( String[] args ) throws Exception {

        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("连接成功");
        }


        FileInputStream fileInputStream = new FileInputStream( new File( FILE_PATH ) );
        InputStreamReader inputStreamReader = new InputStreamReader( fileInputStream );
        BufferedReader bufferReader = new BufferedReader ( inputStreamReader );
        String contentStr;
        while ( ( contentStr = bufferReader.readLine() ) != null ) {

            for( String type : TYPE_ARRAY ) {

                // 单词
                String word = contentStr.split( " : " )[0];
                // json串
                String jsonStr = contentStr.split( " : " )[1];

                JSONObject contentObject = new JSONObject( jsonStr );
                JSONObject nounObject = contentObject.isNull( "noun" ) ? null : contentObject.getJSONObject( "noun" );
                JSONObject adjectiveObject = contentObject.isNull( "adjective" ) ? null : contentObject.getJSONObject( "adjective" );
                JSONObject verbObject = contentObject.isNull( "verb" ) ? null : contentObject.getJSONObject( "verb" );
                JSONObject adverbObject = contentObject.isNull( "adverb" ) ? null : contentObject.getJSONObject( "adverb" );

                List<Object> nounList = ( nounObject != null && !nounObject.isNull( type ) ) ? nounObject.getJSONArray( type ).toList() : null;
                List<Object> verbList = ( verbObject != null && !verbObject.isNull( type ) ) ? verbObject.getJSONArray( type ).toList() : null;
                List<Object> adjectiveList = ( adjectiveObject != null && !adjectiveObject.isNull( type ) ) ? adjectiveObject.getJSONArray( type ).toList() : null;
                List<Object> adverbList = ( adverbObject != null && !adverbObject.isNull( type ) ) ? adverbObject.getJSONArray( type ).toList() : null;

                if(nounList != null && nounList.size() > 0) {
                    StringBuilder results = new StringBuilder();
                    for (Object result : nounList) {
                        results.append(result.toString()).append(",");
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "NN");
                    preparedStatement.setString(3, type);
                    preparedStatement.setString(4, results.toString());
                    preparedStatement.execute();
                }

                if(verbList != null) {
                    StringBuilder results = new StringBuilder();
                    for (Object result : verbList) {
                        results.append(result.toString()).append(",");
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "VB");
                    preparedStatement.setString(3, type);
                    preparedStatement.setString(4, results.toString());
                    preparedStatement.execute();
                }

                if(adjectiveList != null) {
                    StringBuilder results = new StringBuilder();
                    for (Object result : adjectiveList) {
                        results.append(result.toString()).append(",");
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "JJ");
                    preparedStatement.setString(3, type);
                    preparedStatement.setString(4, results.toString());
                    preparedStatement.execute();
                }

                if(adverbList != null) {
                    StringBuilder results = new StringBuilder();
                    for (Object result : adverbList) {
                        results.append(result.toString()).append(",");
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, pos, relation, results) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, "RB");
                    preparedStatement.setString(3, type);
                    preparedStatement.setString(4, results.toString());
                    preparedStatement.execute();
                }

            }//for TYPE_ARRAY

        }//while

        System.out.println( "完成！" );
    }
}
