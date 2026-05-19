# POC Investigation Targets

調査 Agent システム POC 用の試点ターゲット資産。

2026-05-18 客先打合せで合意した「Java/JSP 老舊系統」と「Asteria（XML 配置）ETL ツール」をシミュレートした骨架級サンプル。Agent に「ISSUE-xxx を調査して」「TICKET-ASTR-xxx の根本原因を特定して」といったタスクを投げる演習素材として使う。

## 構成

```
poc-investigation-targets/
├── legacy-order-inventory-system/   # Servlet + JSP + JDBC + Oracle 老舊 OMS
└── asteria-etl-sample/              # ASTERIA Warp 風 XML フロー (基幹↔DWH ETL)
```

それぞれの詳細は各サブディレクトリの README を参照。

## 埋め込み済みの「調査目標」サマリ

### legacy-order-inventory-system

| 種別 | 代表例 | チケット |
|------|--------|----------|
| Security | SQL インジェクション、MD5 弱ハッシュ、DB パスワード平文 | — |
| Bug | 楽観排他なしによる在庫二重減算 | ISSUE-178 |
| Bug | パスワードを `toLowerCase()` してから MD5 | ISSUE-203 |
| Performance | 受注一覧での N+1 と相関サブクエリ | ISSUE-142 |
| Design Debt | 静的フィールドで Connection 使い回し / `close()` が no-op | — |
| Design Debt | JSP scriptlet 内で直接 DB アクセス、`<script>` 未エスケープ | ISSUE-217 |

### asteria-etl-sample

| 種別 | 代表例 | チケット |
|------|--------|----------|
| Security | JDBC 接続定義パスワード平文 | TICKET-ASTR-031 |
| Bug | SMTP 完了通知が常に `[FAILED]` 件名 | TICKET-ASTR-018 |
| Bug | W003 倉庫の DWH 側テーブル名が違い、常に差分全件扱い | — |
| Performance | LookupDB cache=false で SQL 連発 (数十万件) | TICKET-ASTR-012 |
| Performance | 倉庫ごとの突合フローが直列、手動コピペ運用 | TICKET-ASTR-024 |
| Design Debt | XSLT に税率改定対応コードが層状に残存 | — |

## 注意

- すべて **架空の組織・架空のシステム**。実在企業のコードや実データは含まれない。
- ホスト名・IP・パスワードは演習用ダミー。実在の社内環境を指すものは一つも無い。
- 「故意に問題を埋め込んだ教材」なので、本リポジトリ自体に対する脆弱性指摘は意図通り。
