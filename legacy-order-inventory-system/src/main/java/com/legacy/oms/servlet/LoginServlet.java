package com.legacy.oms.servlet;

import com.legacy.oms.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * ログイン処理.
 * 2008 年初版 (担当: 田中). 2014 年に paramater 名変更のみ実施.
 */
public class LoginServlet extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("Windows-31J");

        String userId = req.getParameter("userId");
        String pass   = req.getParameter("password");

        try {
            String hashed = md5(pass);

            Connection con = DBUtil.getConnection();
            Statement st = con.createStatement();
            // ユーザIDとパスワードで認証
            String sql = "SELECT USER_NM, DEPT_CD FROM M_USER "
                       + "WHERE USER_ID = '" + userId + "' "
                       + "AND PASSWORD = '" + hashed + "' "
                       + "AND DEL_FLG = '0'";
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", userId);
                session.setAttribute("userNm", rs.getString("USER_NM"));
                session.setAttribute("deptCd", rs.getString("DEPT_CD"));
                res.sendRedirect(req.getContextPath() + "/order/list");
            } else {
                req.setAttribute("errorMsg", "ユーザIDまたはパスワードが違います");
                req.getRequestDispatcher("/login.jsp").forward(req, res);
            }
        } catch (Exception e) {
            // 障害解析のためスタックトレースを画面にも出力 (2009 年改修時)
            e.printStackTrace();
            res.getWriter().println("Login error: " + e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                res.getWriter().println("  at " + el);
            }
        }
    }

    private static String md5(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        // ※ パスワード変更画面で大文字が通らない報告 (ISSUE-203) → ここで lower してるのが原因かも
        byte[] b = md.digest(s.toLowerCase().getBytes("Windows-31J"));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(Integer.toHexString((b[i] & 0xff) | 0x100).substring(1));
        }
        return sb.toString();
    }
}
