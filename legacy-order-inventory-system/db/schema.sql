--
-- OMS DB schema (Oracle 10g)
-- 最終更新: 2014-11-30 (担当: 田中)
-- ※ 2015 以降は ALTER 直叩きで運用。本ファイルは古い可能性あり。
--

-- ユーザマスタ
CREATE TABLE M_USER (
    USER_ID       VARCHAR2(20)  NOT NULL,
    USER_NM       VARCHAR2(60),
    PASSWORD      VARCHAR2(32),                      -- MD5 hex 32 桁
    DEPT_CD       VARCHAR2(10),
    DEL_FLG       CHAR(1) DEFAULT '0',
    CRT_DT        DATE,
    UPD_DT        DATE,
    CONSTRAINT PK_M_USER PRIMARY KEY (USER_ID)
);

-- 取引先マスタ
CREATE TABLE M_CUSTOMER (
    CUSTOMER_CD   VARCHAR2(10)  NOT NULL,
    CUSTOMER_NM   VARCHAR2(120),
    TEL           VARCHAR2(20),
    ADDR          VARCHAR2(200),
    DEL_FLG       CHAR(1) DEFAULT '0',
    CONSTRAINT PK_M_CUSTOMER PRIMARY KEY (CUSTOMER_CD)
);

-- 商品マスタ
CREATE TABLE M_ITEM (
    ITEM_CD       VARCHAR2(13)  NOT NULL,
    ITEM_NM       VARCHAR2(120),
    UNIT_PRICE    NUMBER(10,2),
    CATEGORY_CD   VARCHAR2(6),
    DEL_FLG       CHAR(1) DEFAULT '0',
    CONSTRAINT PK_M_ITEM PRIMARY KEY (ITEM_CD)
);

-- 受注ヘッダ
CREATE TABLE T_ORDER (
    ORDER_NO      VARCHAR2(14)  NOT NULL,
    ORDER_DT      DATE,
    CUSTOMER_CD   VARCHAR2(10),
    USER_ID       VARCHAR2(20),
    STATUS_CD     VARCHAR2(2),         -- 00:仮登録 10:確定 90:取消
    TOTAL_AMT     NUMBER(12,2),
    REMARKS       VARCHAR2(400),
    CRT_DT        DATE,
    UPD_DT        DATE,
    CONSTRAINT PK_T_ORDER PRIMARY KEY (ORDER_NO)
);
-- ※ ORDER_DT / CUSTOMER_CD にインデックス無し（2018 年に追加予定だったが未対応）

-- 受注明細
CREATE TABLE T_ORDER_DTL (
    ORDER_NO      VARCHAR2(14)  NOT NULL,
    LINE_NO       NUMBER(4)     NOT NULL,
    ITEM_CD       VARCHAR2(13),
    QTY           NUMBER(8),
    UNIT_PRICE    NUMBER(10,2),
    CONSTRAINT PK_T_ORDER_DTL PRIMARY KEY (ORDER_NO, LINE_NO)
);

-- 在庫
CREATE TABLE T_INVENTORY (
    ITEM_CD       VARCHAR2(13)  NOT NULL,
    WAREHOUSE_CD  VARCHAR2(4)   NOT NULL,
    STOCK_QTY     NUMBER(10),
    UPD_DT        DATE,
    CONSTRAINT PK_T_INVENTORY PRIMARY KEY (ITEM_CD, WAREHOUSE_CD)
);
-- ※ 楽観排他用の VERSION 列が無い（ISSUE-178 の原因と疑われる）

-- 在庫移動ログ (2016 追加、命名規約が他と揃っていない)
CREATE TABLE InventoryTrx (
    trx_id        NUMBER(15)    NOT NULL,
    item_code     VARCHAR2(13),
    warehouse     VARCHAR2(4),
    delta_qty     NUMBER(10),
    trx_type      VARCHAR2(10),       -- 'IN','OUT','ADJ'
    ref_order_no  VARCHAR2(14),
    created_at    TIMESTAMP,
    CONSTRAINT PK_InventoryTrx PRIMARY KEY (trx_id)
);

-- 初期データ (開発用)
INSERT INTO M_USER VALUES ('admin','管理者','21232f297a57a5a743894a0e4a801fc3','D001','0',SYSDATE,SYSDATE);
INSERT INTO M_USER VALUES ('tanaka','田中太郎','5f4dcc3b5aa765d61d8327deb882cf99','D001','0',SYSDATE,SYSDATE);
COMMIT;
