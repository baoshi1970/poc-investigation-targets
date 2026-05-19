# POC Investigation Targets

調査 Agent システム POC 用の試点ターゲット資産。

2026-05-18 客先打合せで合意した「Java/JSP 老舊系統」と「Asteria（XML 配置）ETL ツール」をシミュレートした骨架級サンプル。Agent に「ISSUE-xxx を調査して」「TICKET-ASTR-xxx の根本原因を特定して」といったタスクを投げる演習素材として使う。

## 構成

```
poc-investigation-targets/
├── legacy-order-inventory-system/   # Servlet + JSP + JDBC + Oracle 老舊 OMS
│   └── docs/                        # 設計書一式 (Excel / Word)
├── asteria-etl-sample/              # ASTERIA Warp 風 XML フロー (基幹↔DWH ETL)
│   └── docs/                        # 設計書一式 (Excel / Word)
└── _scripts/                        # 設計書生成用 Python スクリプト
```

それぞれの詳細は各サブディレクトリの README を参照。

## 設計書

各プロジェクト直下の `docs/` に SI 案件で一般的な設計書一式を配置している。**故意に実コードと食い違うポイントを多数埋め込んでいる** (2011-2014 当時の設計書のまま放置され、実装は数次の改修で乖離している、という想定)。Agent に「設計書と実装の整合性チェック」タスクを投げる演習素材としても使える。

| プロジェクト | 文書 | 主な乖離例 |
|--------------|------|-----------|
| OMS | テーブル定義書.xlsx | `T_INVENTORY.VERSION` 列を記載するも実 DDL に無い (ISSUE-178 の遠因) / MD5 を SHA-256 と誤記 |
| OMS | 機能・画面一覧.xlsx | 「アカウントロック」「入庫処理」を実装済と記載するも実コード無し |
| OMS | 基本設計書.docx | DBCP / セッション 30 分 / SHA-256 + Salt と記載するも実装は静的接続/240 分/MD5 ノーソルト |
| OMS | IF仕様書.xlsx | 廃止済 EDI 連携を「稼働中」と表記 / 旧連携先名のまま |
| Asteria | DWH連携テーブル定義書.xlsx | `DIM_INVENTORY_W003` を別テーブル運用と記載 (実装は同一テーブル) |
| Asteria | フロー一覧.xlsx | 想定所要時間が現実と乖離 / FLW-099 廃止済を「現役」と記載 |
| Asteria | 基本設計書.docx | 差分更新と謳うが実装は全件 TRUNCATE / リトライ・暗号化保管も未実装 |
| Asteria | 連携IF一覧.xlsx | スケジュールが実 cron と乖離 / 税率値域が古い記載のまま |

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
