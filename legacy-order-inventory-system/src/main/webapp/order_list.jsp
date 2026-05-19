<%@ page contentType="text/html; charset=Windows-31J" pageEncoding="Windows-31J" %>
<%@ page import="java.util.*, java.text.*" %>
<%
    // 認証チェック (簡易)
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<Map<String,Object>> orderList = (List<Map<String,Object>>) request.getAttribute("orderList");
    if (orderList == null) orderList = new ArrayList<Map<String,Object>>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
%>
<html>
<head>
    <title>受注一覧</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/oms.css">
    <script src="<%= request.getContextPath() %>/js/jquery-1.4.min.js"></script>
</head>
<body>
<h2>受注一覧</h2>
<p>ようこそ <%= session.getAttribute("userNm") %> さん</p>

<form action="<%= request.getContextPath() %>/order/list" method="get">
    取引先名: <input type="text" name="customerNm" value="<%= request.getParameter("customerNm")==null?"":request.getParameter("customerNm") %>" size="20">
    受注日: <input type="text" name="dateFrom" value="<%= request.getParameter("dateFrom")==null?"":request.getParameter("dateFrom") %>" size="10">
       〜  <input type="text" name="dateTo"   value="<%= request.getParameter("dateTo")==null?"":request.getParameter("dateTo") %>" size="10">
    状態:
    <select name="statusCd">
        <option value="">全て</option>
        <option value="00">仮登録</option>
        <option value="10">確定</option>
        <option value="90">取消</option>
    </select>
    <input type="submit" value="検索">
</form>

<table border="1" cellspacing="0" cellpadding="3">
<tr>
    <th>受注番号</th><th>受注日</th><th>取引先</th><th>状態</th><th>金額</th><th>操作</th>
</tr>
<%
    for (Map<String,Object> row : orderList) {
        String orderNo    = (String) row.get("orderNo");
        Date orderDt      = (Date)   row.get("orderDt");
        String customerNm = (String) row.get("customerNm");
        String statusCd   = (String) row.get("statusCd");
        Double totalAmt   = (Double) row.get("totalAmt");

        String statusLabel = "";
        if ("00".equals(statusCd)) statusLabel = "仮登録";
        else if ("10".equals(statusCd)) statusLabel = "確定";
        else if ("90".equals(statusCd)) statusLabel = "<font color=gray>取消</font>";
%>
<tr>
    <td><a href="<%= request.getContextPath() %>/order/detail?orderNo=<%= orderNo %>"><%= orderNo %></a></td>
    <td><%= orderDt == null ? "" : sdf.format(orderDt) %></td>
    <td><%= customerNm %></td>
    <td><%= statusLabel %></td>
    <td align="right"><%= new DecimalFormat("#,##0").format(totalAmt) %> 円</td>
    <td>
        <a href="javascript:void(0)"
           onclick="if(confirm('受注 <%= orderNo %> を取消しますか?')) location.href='<%= request.getContextPath() %>/order/cancel?orderNo=<%= orderNo %>'">
           取消
        </a>
    </td>
</tr>
<%
    }
%>
</table>

<p>合計 <%= orderList.size() %> 件</p>

</body>
</html>
