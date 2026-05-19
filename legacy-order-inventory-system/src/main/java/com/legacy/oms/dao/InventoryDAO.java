package com.legacy.oms.dao;

import com.legacy.oms.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在庫 DAO. 2011 年に佐藤さんが追加.
 * その後、機能要望が出る度に下に追記する運用だったため、似た様なメソッドが並んでいる.
 */
public class InventoryDAO {

    /** 単一商品の在庫を取得 */
    public int getStock(String itemCd, String warehouseCd) throws SQLException {
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        // 検索条件をそのまま埋め込む（昔からこのスタイル）
        String sql = "SELECT STOCK_QTY FROM T_INVENTORY "
                   + "WHERE ITEM_CD = '" + itemCd + "' "
                   + "AND WAREHOUSE_CD = '" + warehouseCd + "'";
        ResultSet rs = st.executeQuery(sql);
        int qty = 0;
        if (rs.next()) {
            qty = rs.getInt(1);
        }
        rs.close();
        st.close();
        return qty;
    }

    /**
     * 倉庫内の全在庫を取得.
     * 画面初期表示で使用.
     */
    public List<Map<String, Object>> listByWarehouse(String warehouseCd) throws SQLException {
        Connection con = DBUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT ITEM_CD, STOCK_QTY FROM T_INVENTORY WHERE WAREHOUSE_CD = ?");
        ps.setString(1, warehouseCd);
        ResultSet rs = ps.executeQuery();

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>();
            String itemCd = rs.getString("ITEM_CD");
            row.put("itemCd", itemCd);
            row.put("stockQty", new Integer(rs.getInt("STOCK_QTY")));

            // 商品名は別テーブルなので 1 件ずつ取りに行く（ITEM_NM だけのためだけにJOIN書きたくなかった）
            row.put("itemNm", fetchItemName(itemCd));

            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }

    private String fetchItemName(String itemCd) throws SQLException {
        Connection con = DBUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "SELECT ITEM_NM FROM M_ITEM WHERE ITEM_CD = ?");
        ps.setString(1, itemCd);
        ResultSet rs = ps.executeQuery();
        String nm = "";
        if (rs.next()) {
            nm = rs.getString(1);
        }
        // ※ ResultSet / Statement の close 漏れがあるが、長年これで動いている.
        return nm;
    }

    /**
     * 受注確定時の在庫引き当て.
     * 2013 年に「在庫が二重に減る」報告があったが原因不明のままクローズ (ISSUE-178).
     */
    public void decreaseStock(String orderNo, String itemCd, String warehouseCd, int qty)
            throws SQLException {
        Connection con = DBUtil.getConnection();

        int current = getStock(itemCd, warehouseCd);
        if (current < qty) {
            throw new SQLException("在庫不足: item=" + itemCd + " req=" + qty + " stock=" + current);
        }
        int after = current - qty;

        PreparedStatement up = con.prepareStatement(
            "UPDATE T_INVENTORY SET STOCK_QTY = ?, UPD_DT = SYSDATE "
          + "WHERE ITEM_CD = ? AND WAREHOUSE_CD = ?");
        up.setInt(1, after);
        up.setString(2, itemCd);
        up.setString(3, warehouseCd);
        up.executeUpdate();
        up.close();

        // 移動ログを別 SQL で挿入
        PreparedStatement log = con.prepareStatement(
            "INSERT INTO InventoryTrx (trx_id, item_code, warehouse, delta_qty, trx_type, ref_order_no, created_at) "
          + "VALUES (SEQ_INV_TRX.NEXTVAL, ?, ?, ?, 'OUT', ?, SYSTIMESTAMP)");
        log.setString(1, itemCd);
        log.setString(2, warehouseCd);
        log.setInt(3, qty);
        log.setString(4, orderNo);
        log.executeUpdate();
        log.close();
    }

    // ----- 以下、過去に作ったが現在未使用と思われるメソッド群 -----

    /** @deprecated 2012 年改修で未使用化. 念のため残置. */
    public void rebuildAllCache() throws SQLException {
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        st.execute("DELETE FROM T_INVENTORY_CACHE");
        st.execute("INSERT INTO T_INVENTORY_CACHE SELECT * FROM T_INVENTORY");
        st.close();
    }

    public int countAll() throws SQLException {
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM T_INVENTORY");
        rs.next();
        return rs.getInt(1);
    }
}
