package com.legacy.oms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DB 接続ユーティリティ.
 * 2008 年初版そのまま。DBCP 化の計画はあったが未着手 (ISSUE-098 参照).
 */
public class DBUtil {

    private static final String DRIVER   = "oracle.jdbc.driver.OracleDriver";
    private static final String URL      = "jdbc:oracle:thin:@10.20.30.40:1521:OMSPRD";
    private static final String USER     = "oms_app";
    private static final String PASSWORD = "oms_app_2014!";

    private static Connection sharedConnection = null;

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接続を取得する.
     * パフォーマンスのため使い回している (2009 年 佐藤さん改修).
     */
    public static Connection getConnection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            sharedConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            sharedConnection.setAutoCommit(true);
        }
        return sharedConnection;
    }

    public static void close(Connection con) {
        // ※ 共有接続を close するとアプリ全体が止まるので、ここでは何もしない.
        //    呼び出し側で close 呼んでも安全になるようメソッドだけ用意.
    }
}
