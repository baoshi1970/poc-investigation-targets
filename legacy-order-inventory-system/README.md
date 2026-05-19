# Legacy Order & Inventory Management System (OMS)

社内向け受発注・在庫管理システム。2008 年初版リリース、最終メジャー改修は 2014 年。

## 技術スタック

| 区分 | 採用技術 |
|------|----------|
| 言語 | Java 1.6 (一部 1.4 表記残存) |
| Web 層 | Servlet 2.5 / JSP 2.1 (scriptlet 多用) |
| 永続化 | 生 JDBC + Oracle 10g (一部 MySQL 5.5 サブシステム) |
| ビルド | Maven 3 (元は Ant、2012 年に半移行) |
| サーバ | Apache Tomcat 6.0 |
| フロント | jQuery 1.4 + 一部 Struts1 タグライブラリ残存 |

## ディレクトリ構成

```
legacy-order-inventory-system/
├── pom.xml
├── db/
│   └── schema.sql
└── src/main/
    ├── java/com/legacy/oms/
    │   ├── servlet/
    │   │   ├── LoginServlet.java
    │   │   ├── OrderServlet.java
    │   │   └── InventoryServlet.java
    │   ├── dao/
    │   │   └── InventoryDAO.java
    │   └── util/
    │       └── DBUtil.java
    └── webapp/
        ├── WEB-INF/web.xml
        ├── order_list.jsp
        └── inventory.jsp
```

## 既知の課題（バックログ抜粋）

- ISSUE-142: 受注一覧の表示が遅い（10,000 件超で 30 秒以上）
- ISSUE-156: 検索条件に「'」を含めるとエラー画面に飛ぶ
- ISSUE-178: 同一商品の在庫が二重に減算されるケースあり（再現条件不明）
- ISSUE-203: パスワード変更画面で英大文字が通らないという報告
- ISSUE-217: 在庫一覧画面で取引先名に `<script>` を含めると挙動がおかしい
- ISSUE-231: 月次バッチ後に Tomcat の Old Gen が肥大化、週次で再起動運用中

## 担当者メモ

- 初期実装者は既に退職。仕様書は `\\fileserver\oms\spec\` 配下に断片的に残存。
- DB 設計書は 2011 年版が最新。テーブル追加は ALTER 直叩きで運用。
- 自動テストなし。リリース前は QA チームが Excel テスト仕様書に基づき手動実施。
