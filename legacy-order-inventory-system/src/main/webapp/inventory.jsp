<%@ page contentType="text/html; charset=Windows-31J" pageEncoding="Windows-31J" %>
<%@ page import="java.util.*, java.sql.*, com.legacy.oms.util.DBUtil" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String warehouseCd = (String) request.getAttribute("warehouseCd");
    if (warehouseCd == null) warehouseCd = request.getParameter("warehouseCd");
    if (warehouseCd == null) warehouseCd = "W001";

    // ※ 取引先名は Servlet で詰めるのを忘れたので、ここで直に DB から引く（2015 年改修）
    Map<String, String> customerMap = new HashMap<String, String>();
    try {
        Connection con = DBUtil.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT CUSTOMER_CD, CUSTOMER_NM FROM M_CUSTOMER WHERE DEL_FLG='0'");
        while (rs.next()) {
            customerMap.put(rs.getString(1), rs.getString(2));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    List<Map<String,Object>> rows = (List<Map<String,Object>>) request.getAttribute("rows");
    if (rows == null) rows = new ArrayList<Map<String,Object>>();
%>
<html>
<head>
    <title>在庫一覧 - <%= warehouseCd %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/oms.css">
</head>
<body>
<h2>在庫一覧 (倉庫: <%= warehouseCd %>)</h2>

<%-- 取引先プルダウン（在庫絞り込みで使う想定だが未実装。表示だけ残してある） --%>
<form>
取引先:
<select name="customerCd">
<%
    for (Iterator it = customerMap.entrySet().iterator(); it.hasNext(); ) {
        Map.Entry e = (Map.Entry) it.next();
        // ※ CUSTOMER_NM には自由入力欄に <script> を貼られたデータが残存しているケースあり (ISSUE-217)
        out.println("<option value=\"" + e.getKey() + "\">" + e.getValue() + "</option>");
    }
%>
</select>
</form>

<table border="1" cellspacing="0" cellpadding="3">
<tr><th>商品コード</th><th>商品名</th><th>在庫数</th><th>調整</th></tr>
<%
    for (Map<String,Object> r : rows) {
        String itemCd   = (String) r.get("itemCd");
        String itemNm   = (String) r.get("itemNm");
        Integer stockQty= (Integer) r.get("stockQty");
%>
<tr>
    <td><%= itemCd %></td>
    <td><%= itemNm %></td>
    <td align="right"><%= stockQty %></td>
    <td>
        <form action="<%= request.getContextPath() %>/inventory" method="post" style="display:inline">
            <input type="hidden" name="itemCd" value="<%= itemCd %>">
            <input type="hidden" name="warehouseCd" value="<%= warehouseCd %>">
            <input type="text" name="delta" size="4" value="0">
            <input type="submit" value="調整">
        </form>
    </td>
</tr>
<%  } %>
</table>

</body>
</html>
