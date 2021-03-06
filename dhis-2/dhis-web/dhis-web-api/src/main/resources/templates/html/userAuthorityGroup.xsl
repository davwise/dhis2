<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:d="http://dhis2.org/schema/dxf/2.0"
    >

  <xsl:template match="d:userRole">
    <div class="userRole">
      <h2>
        <xsl:value-of select="@name" />
      </h2>
      <table>
        <tr>
          <td>ID</td>
          <td> <xsl:value-of select="@id" /> </td>
        </tr>
        <tr>
          <td>Last Updated</td>
          <td> <xsl:value-of select="@lastUpdated" /> </td>
        </tr>
        <tr>
          <td>Code</td>
          <td> <xsl:value-of select="@code" /> </td>
        </tr>
      </table>

      <xsl:apply-templates select="d:authorities|d:dataSets" mode="short" />
    </div>
  </xsl:template>

  <xsl:template match="d:authorities" mode="short">
    <xsl:if test="count(child::*) > 0">
      <h3>Authorities</h3>
      <ul>
        <xsl:apply-templates select="child::*" mode="row"/>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template match="d:authority" mode="row">
    <li><xsl:value-of select="." /> </li>
  </xsl:template>

  <xsl:template match="d:userRoles" mode="short">
    <xsl:if test="count(child::*) > 0">
      <h3>UserRoles</h3>
      <table class="userRoles">
        <xsl:apply-templates select="child::*" mode="row"/>
      </table>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
