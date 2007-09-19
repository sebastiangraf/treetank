<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/PLAYS">
        <html>
            <head>
                <title>Shakespeare Plays</title>
            </head>
            <body>
                <xsl:apply-templates select="./PLAY/TITLE"/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="TITLE">
        <p>
            <xsl:value-of select="text()"/> (<xsl:value-of select="count(./parent::node()//LINE)"/> lines)
        </p>
    </xsl:template>
</xsl:stylesheet>
