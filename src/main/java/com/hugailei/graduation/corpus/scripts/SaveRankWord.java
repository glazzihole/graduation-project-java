package com.hugailei.graduation.corpus.scripts;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * @author HU Gailei
 * @date 2019/1/9
 * <p>
 * description: 存储等级词汇表
 * </p>
 **/
public class SaveRankWord {

    private static final String DICT_DIRECTORY_PATH = "E:\\资源\\ShanBei\\GMAT";

    private static final int RANK_NUM = 6;

    private static final String RANK_NAME = "GRE/GMAT";

    private static final String DB_HOST = "192.168.99.100";

    private static final String DB_PORT = "3307";

    private static final String DB_NAME="corpus";

    private static final String COLL_NAME="tb_rank_word";

    private static final String USER_NAME="root";

    private static final String USER_PASSWORD="123456";

    public static void main(String[] args) throws Exception{
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        File directoryPath = new File(DICT_DIRECTORY_PATH);
        File[] directorys = directoryPath.listFiles();
        long maxFileSize = 0;
        String maxFilePath = "";
        for (File directory : directorys) {
            long directorySize = directory.length();
            if (directorySize >= maxFileSize) {
                maxFileSize = directorySize;
                maxFilePath = directory.getCanonicalPath();
            }
        }
        System.out.println(maxFilePath);
        File rootFile = new File(maxFilePath);
        File[] files = rootFile.listFiles();
        for(File file:files){
            if(file.getName().endsWith( ".json" )){
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = "";
                while ((line = bufferedReader.readLine()) != null){
                    JSONObject json = JSONObject.parseObject(line);
                    String word = json.getString( "form" );
                    JSONArray meaningJsonArray = json.getJSONArray("meaning");
                    StringBuilder meaning = new StringBuilder();
                    for (Object oneMeaning : meaningJsonArray) {
                        meaning.append(oneMeaning.toString()).append(";");
                    }
                    PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO " + COLL_NAME
                            + "(word, meaning, rank_num, rank_name) VALUES (?, ?, ?, ?)");
                    preparedStatement.setString(1, word);
                    preparedStatement.setString(2, meaning.toString());
                    preparedStatement.setInt(3, RANK_NUM);
                    preparedStatement.setString(4, RANK_NAME);
                    preparedStatement.execute();
                    System.out.println(json);
                }
            }
        }
    }
}
