<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Copyright 2018 Uppsala University Library
 
  This file is part of Cora.
 
      Cora is free software: you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation, either version 3 of the License, or
      (at your option) any later version.
 
      Cora is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.
 
      You should have received a copy of the GNU General Public License
      along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output indent="yes"/>
    <xsl:param name="domainFilter" />
    <xsl:key name="domain" match="identifiers/identifier[type = 'LOCAL'] | affiliations/affiliation[string-length(domain) &gt; 0]" use="domain"/>
    <xsl:template match="/">
        <xsl:apply-templates select="authorityPerson"/>
    </xsl:template>
    <xsl:template match="authorityPerson">
            <xsl:for-each select="affiliations/affiliation[string-length(domain) &gt; 0][generate-id(.)=generate-id(key('domain', domain))]/domain | identifiers/identifier[type = 'LOCAL'][generate-id(.)=generate-id(key('domain', domain))]/domain">
                <xsl:sort/>
                <xsl:variable name="domain">
                    <xsl:value-of select="."/>
                </xsl:variable>
                <xsl:if test=".=$domainFilter">
                
                <personDomainPart>
                    <recordInfo>
                        <id>
                            <xsl:value-of select="../../../pid"/>
                            <xsl:text>:</xsl:text>
                            <xsl:value-of select="."/>
                        </id>
                        <type>
                            <linkedRecordType>recordType</linkedRecordType>
                            <linkedRecordId>personDomainPart</linkedRecordId>
                        </type>
                        <xsl:for-each select="../../../recordInfo/events/event[type = 'CREATE']">
                            <createdBy>
                                <linkedRecordType>user</linkedRecordType>
                                <linkedRecordId>
                                    <xsl:choose>
                                        <xsl:when test="string-length(userId) &gt; 0">
                                            <xsl:value-of select="userId"></xsl:value-of>
                                        </xsl:when>
                                        <xsl:otherwise>                    
                                            <xsl:text>SYSTEM</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </linkedRecordId>
                            </createdBy>
                            <tsCreated>
                                <xsl:value-of select="timestamp"></xsl:value-of>
                            </tsCreated>
                        </xsl:for-each>
                        <dataDivider>
                            <linkedRecordType>system</linkedRecordType>
                            <linkedRecordId>diva</linkedRecordId>
                        </dataDivider>        
                        <xsl:for-each select="../../../recordInfo/events/event[type = 'UPDATE']">
                            <updated>
                                <xsl:attribute name="repeatId">
                                    <xsl:value-of select="position() - 1"></xsl:value-of>
                                </xsl:attribute>
                                <updatedBy>
                                    <linkedRecordType>user</linkedRecordType>
                                    <linkedRecordId>
                                        <xsl:choose>
                                            <xsl:when test="string-length(userId) &gt; 0">
                                                <xsl:value-of select="userId"></xsl:value-of>
                                            </xsl:when>
                                            <xsl:otherwise>                    
                                                <xsl:text>SYSTEM</xsl:text>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </linkedRecordId>
                                </updatedBy>
                                <tsUpdated>
                                    <xsl:value-of select="timestamp"></xsl:value-of>
                                </tsUpdated>
                            </updated>
                        </xsl:for-each> 
                        <domain>
                            <xsl:value-of select="."/>
                        </domain>
                        <xsl:for-each select="../publicRecord">
                <public>
                    <xsl:choose>
                        <xsl:when test=". = 'true'">
                            <xsl:text>yes</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>no</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </public>
            </xsl:for-each>
                    </recordInfo>                   
                    <xsl:for-each select="../../../affiliations/affiliation[domain = $domain]">
                        <affiliation>
                            <xsl:attribute name="repeatId">
                                <xsl:value-of select="position() - 1"></xsl:value-of>
                            </xsl:attribute>
                            <organisationLink>
                                <linkedRecordType>organisation</linkedRecordType>
                                <linkedRecordId>
                                    <xsl:value-of select="organisationId"></xsl:value-of>  
                                </linkedRecordId>     
                            </organisationLink>  
                            <xsl:if test="string-length(from) &gt; 0">
                                <affiliationFromYear>
                                    <xsl:value-of select="from"></xsl:value-of>
                                </affiliationFromYear>
                            </xsl:if>
                            <xsl:if test="string-length(until) &gt; 0">
                                <affiliationUntilYear>
                                    <xsl:value-of select="until"></xsl:value-of>
                                </affiliationUntilYear>
                            </xsl:if>   
                        </affiliation>
                    </xsl:for-each>
                    <xsl:for-each select="../../../identifiers/identifier[type = 'LOCAL'][domain = $domain]">
                        <identifier>
                            <xsl:value-of select="value"/>
                        </identifier>
                    </xsl:for-each>
                </personDomainPart>
                </xsl:if>
            </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>