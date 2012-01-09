<!--

    Copyright (c) 2011, University of Konstanz, Distributed Systems Group
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of the University of Konstanz nor the
          names of its contributors may be used to endorse or promote products
          derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

-->
<xsl:transform
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="2.0"
>

<!-- This style sheet displays the books.xml file.  -->

<xsl:key name="authkey" match="ITEM" use="AUTHOR"/>
<xsl:key name="codekey" match="CATEGORY" use="@CODE"/>

<xsl:decimal-format name="comma" decimal-separator="," grouping-separator="."/>

<xsl:variable name="categories" select="//CATEGORY"/>

<xsl:variable name="now" select="current-dateTime()"/>

<xsl:param name="top-author">Jasper Fforde</xsl:param>

<xsl:template match="/">

    <html>
    <!-- <xsl:comment>Generated at <xsl:value-of select="$now"/></xsl:comment> -->

    <xsl:call-template name="header">
        <xsl:with-param name="title" select="'Book List'"/>
    </xsl:call-template>
    
    <body leftmargin="100" >
    <xsl:apply-templates/>

    </body>
    </html>
</xsl:template>

<xsl:template name="header">
    <xsl:param name="title" select="'Default Title'"/>
    <head>
      <xsl:choose>
      <xsl:when test="$title">
        <title><xsl:value-of select="$title"/></title>
		  </xsl:when>
      <xsl:otherwise>
        <title>Untitled</title>
      </xsl:otherwise>
      </xsl:choose>
    </head>
</xsl:template>

<xsl:template match="BOOKLIST">

    <h2>This week's top author is <xsl:value-of select="$top-author"/></h2>
    <xsl:variable name="top-authors-books" select="key('authkey', $top-author)"/>
    
    <xsl:if test="$top-authors-books">
      <p>We stock the following <xsl:value-of select="count($top-authors-books)"/> books by this author:</p>
      <ul>
        <xsl:for-each select="$top-authors-books">
          <li><xsl:value-of select="TITLE"/></li>

        </xsl:for-each>
      </ul>
    
      <p>This author has written books in the following categories:</p>
      <ul>
        <xsl:for-each select="key('codekey', $top-authors-books/@CAT)/@DESC">
          <li><xsl:value-of select="."/></li>
        </xsl:for-each>
      </ul>

      <p>The average price of these books is: 
        <xsl:value-of select="format-number(
                                 sum($top-authors-books/PRICE)
                                   div count($top-authors-books),
                                     '$####.00')"/>
      </p>
    </xsl:if>

    <h2>A complete list of books, grouped by author</h2>
    <xsl:apply-templates select="BOOKS" mode="by-author"/>

    <h2>A complete list of books, grouped by category</h2>

    <xsl:apply-templates select="BOOKS" mode="by-category"/>

    <h2>List of categories</h2>
    <xsl:apply-templates select="$categories">
        <xsl:sort select="@DESC" order="descending"/>
        <xsl:sort select="@CODE" order="descending"/>
    </xsl:apply-templates>

</xsl:template>   

<xsl:template match="BOOKS" mode="by-author">

    <div>
    <xsl:for-each-group select="ITEM" group-by="AUTHOR">
    <xsl:sort select="AUTHOR" order="ascending"/>
    <xsl:sort select="TITLE" order="ascending"/>
    <h3>AUTHOR: <xsl:value-of select="AUTHOR"/></h3>
        <table>
        <xsl:for-each select="current-group()">
            <tr>

              <td width="100" valign="top"><xsl:number value="position()" format="i"/></td>
              <td>
                TITLE: <xsl:value-of select="TITLE"/><br/>
                CATEGORY: <xsl:value-of select="id(@CAT)/@DESC" />
                        (<xsl:value-of select="@CAT" />)
              </td>
            </tr>
        </xsl:for-each>

        </table>
        <hr/>
    </xsl:for-each-group>
    </div>
</xsl:template>

<xsl:template match="BOOKS" mode="by-category">
    <div>
    <xsl:for-each-group select="ITEM" group-by="@CAT">
    <xsl:sort select="id(@CAT)/@DESC" order="ascending"/>

    <xsl:sort select="TITLE" order="ascending"/>
    <h3>CATEGORY: <xsl:value-of select="id(@CAT)/@DESC" /></h3>
        <ol>
        <xsl:for-each select="current-group()">
            <li>AUTHOR: <xsl:value-of select="AUTHOR"/><br/>
                TITLE: <xsl:value-of select="TITLE"/>
            </li>                
        </xsl:for-each>

        </ol>
        <hr/>
    </xsl:for-each-group>
    </div>
</xsl:template>

<xsl:template match="CATEGORY" >
    <h4>CATEGORY <xsl:number value="position()" format="I"/></h4>
    <ul>
    <xsl:for-each select="@*">
        <li><xsl:value-of select="name(.)"/></li>
    </xsl:for-each>
    </ul>
    <hr/>
</xsl:template>

<!-- <xsl:template match="CATEGORY" >
    <h4>CATEGORY <xsl:number value="position()" format="I"/></h4>
    <table>
    <xsl:for-each select="@*">

        <tr>
          <td><xsl:value-of select="name(.)"/></td>
          <td><xsl:value-of select="."/></td>
        </tr>
    </xsl:for-each>
    </table>
    <hr/>
</xsl:template> -->

</xsl:transform>	

<!-- Stylus Studio meta-information - (c)1998-2004. Sonic Software Corporation. All rights reserved.
<metaInformation>
<scenarios ><scenario default="yes" name="render books.xml" userelativepaths="yes" externalpreview="no" url="..\data\books.xml" htmlbaseurl="" outputurl="" processortype="saxon8" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext=""/></scenarios><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition></MapperMetaTag>
</metaInformation>
-->