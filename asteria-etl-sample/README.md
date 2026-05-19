# Asteria Warp ETL Project - 基幹データ連携

社内基幹（OMS / 販売管理）と DWH の間を ASTERIA Warp で繋ぐ ETL 資産。

## 概要

| 項目 | 内容 |
|------|------|
| ツール | ASTERIA Warp 1709 (2017 年 9 月版) |
| 実行基盤 | 専用 Windows Server 2012 R2 / 8 vCPU / 16GB RAM |
| 構成定義 | フローは独自 XML (.flwx) で定義 |
| スケジューラ | Warp 内蔵スケジューラ |
| 監視 | フロー終了時に SMTP で `it-ops@example.co.jp` へ通知 |

## ディレクトリ構成

```
asteria-etl-sample/
├── project.xml                       # プロジェクト定義
├── flows/
│   ├── customer_master_sync.flwx     # 取引先マスタの基幹→DWH 連携
│   ├── order_etl_daily.flwx          # 受注データ日次 ETL
│   └── inventory_reconcile.flwx      # 在庫マスタ突合
├── resources/
│   └── jdbc_connections.xml          # JDBC 接続定義
└── mappers/
    └── order_transform.xsl           # 受注変換 XSLT
```

## フロー稼働状況 (2026-05 時点)

| フロー | スケジュール | 平均所要時間 | 直近の課題 |
|--------|--------------|--------------|------------|
| customer_master_sync | 平日 06:00 | 8 分 | 取引先名に半角カナが入ると DWH 側で文字化け |
| order_etl_daily | 毎日 02:30 | 45 分 → 最近 2 時間超 | 件数増で枠オーバー、03:00 開始の在庫連携と競合 |
| inventory_reconcile | 月初 04:00 | 90 分 | 倉庫追加時に手動で flow を分岐コピペする運用 |

## 既知の問題

- TICKET-ASTR-012: order_etl_daily が件数増で 2 時間超。全件洗い替えのため切り出しが効かない。
- TICKET-ASTR-018: customer_master_sync の SMTP 通知が成功時も「FAILED」表記になる。
- TICKET-ASTR-024: inventory_reconcile の倉庫ループが直列で遅い。並列化要望あり。
- TICKET-ASTR-031: jdbc_connections.xml にパスワード平文。セキュリティ部から指摘有り、対応未定。

## 担当

- 初版設計: 外部 SI (現在は契約終了)
- 現運用: 社内情シス 鈴木 (兼任、月数時間の対応のみ)
