package com.legacy.oms.servlet;

import com.legacy.oms.dao.InventoryDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 在庫照会／調整.
 * 2011 年 佐藤さん追加. 2016 年に CSV アップロード機能を追記.
 */
public class InventoryServlet extends HttpServlet {

    // 画面間で在庫キャッシュを共有 (2013 年改修時、何回も DB 叩かないように)
    private static Map<String, Integer> stockCache = new java.util.HashMap<String, Integer>();

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String warehouseCd = req.getParameter("warehouseCd");
        if (warehouseCd == null) warehouseCd = "W001";

        try {
            InventoryDAO dao = new InventoryDAO();
            List<Map<String, Object>> rows = dao.listByWarehouse(warehouseCd);

            // キャッシュ更新
            for (Map<String, Object> r : rows) {
                stockCache.put((String) r.get("itemCd"), (Integer) r.get("stockQty"));
            }

            req.setAttribute("warehouseCd", warehouseCd);
            req.setAttribute("rows", rows);
            req.getRequestDispatcher("/inventory.jsp").forward(req, res);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String itemCd      = req.getParameter("itemCd");
        String warehouseCd = req.getParameter("warehouseCd");
        String deltaStr    = req.getParameter("delta");
        String orderNo     = req.getParameter("orderNo");

        try {
            int delta = Integer.parseInt(deltaStr);
            InventoryDAO dao = new InventoryDAO();

            if (delta < 0) {
                dao.decreaseStock(orderNo, itemCd, warehouseCd, -delta);
            } else {
                // 入庫処理は将来対応 (TODO)
                throw new UnsupportedOperationException("入庫処理は未実装");
            }

            // キャッシュも更新
            Integer cur = (Integer) stockCache.get(itemCd);
            if (cur != null) {
                stockCache.put(itemCd, new Integer(cur.intValue() + delta));
            }

            res.sendRedirect(req.getContextPath() + "/inventory?warehouseCd=" + warehouseCd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
