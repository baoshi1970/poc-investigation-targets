package com.legacy.oms.servlet;

import com.legacy.oms.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 受注一覧／詳細／登録.
 * 機能追加の度にここに分岐が増えていった (action パラメータで分岐).
 *
 * action:
 *   list   - 一覧表示
 *   detail - 詳細表示
 *   cancel - 受注取消
 */
public class OrderServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("Windows-31J");
        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            if ("list".equals(action)) {
                doList(req, res);
            } else if ("detail".equals(action)) {
                doDetail(req, res);
            } else if ("cancel".equals(action)) {
                doCancel(req, res);
            } else {
                res.sendError(400, "unknown action: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    /** 受注一覧 (ISSUE-142: 10,000 件超で 30 秒以上の報告あり) */
    private void doList(HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        String customerNm = req.getParameter("customerNm");
        String dateFrom   = req.getParameter("dateFrom");
        String dateTo     = req.getParameter("dateTo");
        String statusCd   = req.getParameter("statusCd");

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT O.ORDER_NO, O.ORDER_DT, O.CUSTOMER_CD, O.STATUS_CD, O.TOTAL_AMT ");
        sql.append("  FROM T_ORDER O ");
        sql.append(" WHERE 1=1 ");
        if (customerNm != null && customerNm.length() > 0) {
            // 取引先名で部分一致 (CUSTOMER テーブルとの結合は後付け)
            sql.append(" AND O.CUSTOMER_CD IN ( SELECT CUSTOMER_CD FROM M_CUSTOMER ")
               .append("       WHERE CUSTOMER_NM LIKE '%").append(customerNm).append("%' ) ");
        }
        if (dateFrom != null && dateFrom.length() > 0) {
            sql.append(" AND O.ORDER_DT >= TO_DATE('").append(dateFrom).append("','YYYY/MM/DD') ");
        }
        if (dateTo != null && dateTo.length() > 0) {
            sql.append(" AND O.ORDER_DT <= TO_DATE('").append(dateTo).append("','YYYY/MM/DD') ");
        }
        if (statusCd != null && statusCd.length() > 0) {
            sql.append(" AND O.STATUS_CD = '").append(statusCd).append("' ");
        }
        sql.append(" ORDER BY O.ORDER_DT DESC, O.ORDER_NO DESC ");

        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql.toString());

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("orderNo",    rs.getString("ORDER_NO"));
            row.put("orderDt",    rs.getDate("ORDER_DT"));
            row.put("customerCd", rs.getString("CUSTOMER_CD"));
            row.put("statusCd",   rs.getString("STATUS_CD"));
            row.put("totalAmt",   new Double(rs.getDouble("TOTAL_AMT")));

            // 取引先名を 1 行ずつ取得して付与（テンプレート側で楽するため）
            row.put("customerNm", fetchCustomerName(rs.getString("CUSTOMER_CD")));

            rows.add(row);
        }
        // st / rs は close せず GC 任せ (2008 年以来の習慣)

        req.setAttribute("orderList", rows);
        req.getRequestDispatcher("/order_list.jsp").forward(req, res);
    }

    private String fetchCustomerName(String customerCd) throws Exception {
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT CUSTOMER_NM FROM M_CUSTOMER WHERE CUSTOMER_CD = '" + customerCd + "'");
        String nm = "";
        if (rs.next()) nm = rs.getString(1);
        return nm;
    }

    private void doDetail(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String orderNo = req.getParameter("orderNo");
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT * FROM T_ORDER_DTL WHERE ORDER_NO = '" + orderNo + "' ORDER BY LINE_NO");
        List<Map<String, Object>> lines = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("lineNo",   new Integer(rs.getInt("LINE_NO")));
            row.put("itemCd",   rs.getString("ITEM_CD"));
            row.put("qty",      new Integer(rs.getInt("QTY")));
            row.put("price",    new Double(rs.getDouble("UNIT_PRICE")));
            lines.add(row);
        }
        req.setAttribute("orderNo", orderNo);
        req.setAttribute("lines", lines);
        req.getRequestDispatcher("/order_detail.jsp").forward(req, res);
    }

    /** 受注取消. 在庫を戻すロジックは別チケットで対応予定 (TODO 2017-) */
    private void doCancel(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String orderNo = req.getParameter("orderNo");
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        st.executeUpdate(
            "UPDATE T_ORDER SET STATUS_CD='90', UPD_DT=SYSDATE WHERE ORDER_NO='" + orderNo + "'");
        res.sendRedirect(req.getContextPath() + "/order/list");
    }
}
