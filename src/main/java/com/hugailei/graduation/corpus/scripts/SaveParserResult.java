package com.hugailei.graduation.corpus.scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author HU Gailei
 * @date 2018/9/22
 * <p>
 * description: 脚本程序，对语料库进行标注，并将标注结果存放至mysql数据库。
 *              大致流程：
 * </p>
 **/
public class SaveParserResult {
    //文件路径
    private static final String FILE_PATH = "";

    //数据库主机地址
    private static final String DB_HOST = "";

    //数据库端口
    private static final String DB_PORT = "";

    //数据库名称
    private static final String DB_NAME="";

    //数据库表名称
    private static final String COLL_NAME="";

    //数据库用户名
    private static final String USER_NAME="";

    //数据库用户密码
    private static final String USER_PASSWORD="";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //连接mysql数据库
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver);
        String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Connection con = DriverManager.getConnection(url,USER_NAME,USER_PASSWORD);
        if (!con.isClosed()) {
            System.out.println("成功连接至数据库!");
        }

        Statement statement = con.createStatement();
        String sql = "select * from emp";
        statement.execute(sql);

        con.close();
    }
}
