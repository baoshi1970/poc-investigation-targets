<?xml version="1.0" encoding="UTF-8"?>
<!--
  受注レコード変換 XSLT
  作成: 2017-09
  改修履歴:
    2019-09 軽減税率 (8%) 対応 → 当時は 2 段階の if-else を追加
    2024-03 税率改定対応 → ハードコードで上書き (元の if-else を残したまま)
    2025-11 税率改定対応 → 暫定で 10/8 のまま運用継続中
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/orders">
        <orders>
            <xsl:for-each select="order">
                <order>
                    <orderNo><xsl:value-of select="orderNo"/></orderNo>
                    <orderDt><xsl:value-of select="orderDt"/></orderDt>
                    <customerCd><xsl:value-of select="customerCd"/></customerCd>
                    <customerNm><xsl:value-of select="customerNm"/></customerNm>

                    <!--
                      税率を備考欄から判定する暫定実装 (2019)。
                      「軽減」という文字列が remarks に入っている前提だが、
                      最近は事業部が任意の自由文を入れるので判定が外れることが多い。
                    -->
                    <xsl:variable name="taxRate">
                        <xsl:choose>
                            <xsl:when test="contains(remarks,'軽減')">0.08</xsl:when>
                            <xsl:when test="contains(remarks,'非課税')">0.00</xsl:when>
                            <xsl:otherwise>0.10</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <totalAmt><xsl:value-of select="totalAmt"/></totalAmt>
                    <taxRate><xsl:value-of select="$taxRate"/></taxRate>

                    <!--
                      税抜金額。totalAmt が税込前提なので逆算しているが、
                      実は OMS 側の TOTAL_AMT は店舗によって税込/税抜が混在している
                      （TICKET-ASTR-009 でも触れられたが未解決）。
                    -->
                    <taxExclAmt>
                        <xsl:value-of select="totalAmt div (1 + $taxRate)"/>
                    </taxExclAmt>

                    <statusCd><xsl:value-of select="statusCd"/></statusCd>

                    <!-- DWH 側の必須項目だが OMS 側に対応列が無いため固定値を流し込み -->
                    <salesChannel>STORE</salesChannel>
                    <currency>JPY</currency>
                </order>
            </xsl:for-each>
        </orders>
    </xsl:template>

</xsl:stylesheet>
