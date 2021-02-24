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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output indent="yes"/>
    <xsl:template match="/">
        <xsl:apply-templates select="authorityPerson"/>
    </xsl:template>
    <xsl:template match="authorityPerson">
        <person>
            <xsl:apply-templates select="recordInfo"/>
            <xsl:apply-templates select="defaultName"/>
            <xsl:if test="string-length(birthYear) &gt; 0">
                <yearOfBirth>
                    <xsl:value-of select="birthYear"></xsl:value-of>
                </yearOfBirth>
            </xsl:if>
            <xsl:if test="string-length(deathYear) &gt; 0">
                <yearOfDeath>
                    <xsl:value-of select="deathYear"></xsl:value-of>
                </yearOfDeath>
            </xsl:if>
            <xsl:if test="string-length(email) &gt; 0">
                <emailAddress>
                    <xsl:value-of select="email"></xsl:value-of>
                </emailAddress>
            </xsl:if>
            <xsl:apply-templates select="alternativeNames"/>
            <xsl:apply-templates select="urls"/>
            <xsl:apply-templates select="affiliations"/>
            <xsl:apply-templates select="identifiers"/>
            <xsl:apply-templates select="biographies"/>
        </person>
    </xsl:template>
    <xsl:template match="recordInfo">
        <recordinfo>
            <id>
                <xsl:value-of select="../pid"></xsl:value-of>
            </id>
            <type>
                <linkedRecordType>recordType</linkedRecordType>
                <linkRecordId>
                    <xsl:text>person</xsl:text>
                </linkRecordId>
            </type>
            <xsl:for-each select="events/event[type = 'CREATE']">
                <createdBy>
                    <linkedRecordType>user</linkedRecordType>
                    <linkRecordId>
                        <xsl:choose>
                            <xsl:when test="string-length(userId) &gt; 0">
                                <xsl:value-of select="userId"></xsl:value-of>
                            </xsl:when>
                            <xsl:otherwise>                    
                                <xsl:text>SYSTEM</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </linkRecordId>
                </createdBy>
                <tsCreated>
                    <xsl:value-of select="timestamp"></xsl:value-of>
                </tsCreated>
            </xsl:for-each>
            <dataDivider>
                <linkedRecordType>system</linkedRecordType>
                <linkRecordId>diva</linkRecordId>
            </dataDivider>
            <xsl:for-each select="events/event[type = 'UPDATE']">
                <updated>
                    <xsl:attribute name="repeatId">
                        <xsl:value-of select="position() - 1"></xsl:value-of>
                    </xsl:attribute>
                    <updatedBy>
                        <linkedRecordType>user</linkedRecordType>
                        <linkRecordId>
                            <xsl:choose>
                                <xsl:when test="string-length(userId) &gt; 0">
                                    <xsl:value-of select="userId"></xsl:value-of>
                                </xsl:when>
                                <xsl:otherwise>                    
                                    <xsl:text>SYSTEM</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </linkRecordId>
                    </updatedBy>
                    <tsUpdated>
                        <xsl:value-of select="timestamp"></xsl:value-of>
                    </tsUpdated>
                </updated>
            </xsl:for-each> 
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
            <xsl:for-each-group select="../affiliations/affiliation | ../identifiers/identifier[type = 'LOCAL']" group-by="domain">
                <xsl:variable name="repeat">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:variable>
                    <xsl:for-each select="distinct-values(current-group()/domain)">
                        <domain>
                            <xsl:attribute name="repeatId">
                                <xsl:value-of select="$repeat"></xsl:value-of>
                            </xsl:attribute>
                            <xsl:value-of select="." />
                        </domain>
                    </xsl:for-each>
            </xsl:for-each-group>
        </recordinfo>
    </xsl:template>
    <xsl:template match="defaultName">
        <authorisedName>
            <xsl:if test="string-length(lastname) &gt; 0">
                <familyName>
                    <xsl:value-of select="lastname"></xsl:value-of>
                </familyName>
            </xsl:if>
            <xsl:if test="string-length(firstname) &gt; 0">
                <givenName>
                    <xsl:value-of select="firstname"></xsl:value-of>
                </givenName>
            </xsl:if>
        </authorisedName>
        <xsl:if test="string-length(addition) &gt; 0">
            <academicTitle>
                <xsl:value-of select="addition"></xsl:value-of>
            </academicTitle>
        </xsl:if>
    </xsl:template>
    <xsl:template match="alternativeNames">
        <xsl:if test="string-length(nameForm/lastname) &gt; 0 or string-length(nameForm/firstname) &gt; 0">
           <xsl:for-each select="nameForm">
               <alternativeName>
                   <xsl:attribute name="repeatId">
                       <xsl:value-of select="position() - 1"></xsl:value-of>
                   </xsl:attribute>
                   <xsl:if test="string-length(lastname) &gt; 0">
                       <familyName>
                           <xsl:value-of select="lastname"></xsl:value-of>
                       </familyName>
                   </xsl:if>
                   <xsl:if test="string-length(firstname) &gt; 0">
                       <givenName>
                           <xsl:value-of select="firstname"></xsl:value-of>
                       </givenName>
                   </xsl:if>
               </alternativeName>              
           </xsl:for-each>
        </xsl:if>
    </xsl:template>
    <xsl:template match="urls">
        <xsl:for-each select="url">
            <externalURL>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <xsl:if test="string-length(label) &gt; 0">
                    <linkTitle>
                        <xsl:value-of select="label"></xsl:value-of>
                    </linkTitle>
                </xsl:if>
                <xsl:if test="string-length(url) &gt; 0">
                    <URL>
                        <xsl:value-of select="url"></xsl:value-of>
                    </URL>
                </xsl:if>
            </externalURL>              
        </xsl:for-each>      
    </xsl:template>
    <xsl:template match="affiliations">
        <xsl:for-each select="affiliation[string-length(domain) &gt; 0]">
            <affiliation>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <organisationLink>
                    <linkedRecordType>organisation</linkedRecordType>
                    <linkRecordId>
                        <xsl:value-of select="organisationId"></xsl:value-of>  
                    </linkRecordId>     
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
        <xsl:for-each select="affiliation[string-length(domain) = 0]">
            <otherAffiliation>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <xsl:if test="string-length(name) &gt; 0">
                    <affiliation>
                        <xsl:value-of select="name"></xsl:value-of> 
                    </affiliation>                   
                </xsl:if>
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
            </otherAffiliation>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="identifiers">
        <xsl:for-each select="identifier[type = 'LOCAL']">
            <localIdentifier>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <domain>
                    <xsl:value-of select="domain"></xsl:value-of>
                </domain>
                <identifier>
                    <xsl:value-of select="value"></xsl:value-of>
                </identifier>
            </localIdentifier>
        </xsl:for-each>  
        <xsl:for-each select="identifier[type = 'ORCID']">
            <ORCID_ID>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <xsl:value-of select="value"></xsl:value-of>
            </ORCID_ID>
        </xsl:for-each>  
        <xsl:for-each select="identifier[type = 'VIAF']">
            <VIAF_ID>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <xsl:value-of select="value"></xsl:value-of>
            </VIAF_ID>
        </xsl:for-each>
        <xsl:for-each select="identifier[type = 'LIBRIS']">
            <Libris_ID>
                <xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
                <xsl:value-of select="value"></xsl:value-of>
            </Libris_ID>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="biographies">
        <xsl:for-each select="entry[string[1] = 'eng']">
            <biographyEnglish>
                <biography>
                    <xsl:value-of select="string[2]"></xsl:value-of>
                </biography>              
                <language>en</language>                
            </biographyEnglish>
        </xsl:for-each>
        <xsl:for-each select="entry[string[1] = 'swe']">
            <biographySwedish>
                <biography>
                    <xsl:value-of select="string[2]"></xsl:value-of>
                </biography>              
                <language>sv</language>                
            </biographySwedish>
        </xsl:for-each>        
    </xsl:template>
</xsl:stylesheet>