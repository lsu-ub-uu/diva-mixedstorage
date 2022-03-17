<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
	<xsl:output indent="yes" method="xml" omit-xml-declaration="yes" />
	
	<xsl:variable name="organisations" select="personAccumulated/organisations"/>
	
    <xsl:template match="/">
    	<xsl:apply-templates select="personAccumulated"/>
    </xsl:template>
	
	<xsl:template match="personAccumulated">
		<authorityPerson>
			<xsl:apply-templates select="person"/>
			<!--
			<xsl:apply-templates select="personDomainParts"/>
			<xsl:apply-templates select="organisations"/>
			-->
		</authorityPerson>
	</xsl:template>
	
	<xsl:template match="person">
		<type>PERSON</type>
		<pid><xsl:value-of select="./recordInfo/id"/></pid>
		<recordInfo>
			<xsl:call-template name="events" />
			<recordDeleted>false</recordDeleted>
		</recordInfo>
		<xsl:call-template name="defaultName" />
		<xsl:if test="yearOfBirth">
			<birthYear><xsl:value-of select="yearOfBirth"/></birthYear>
		</xsl:if>
		<xsl:if test="yearOfDeath">
			<deathYear><xsl:value-of select="yearOfDeath"/></deathYear>
		</xsl:if>
		<xsl:if test="emailAddress">
			<email><xsl:value-of select="emailAddress"/></email>
		</xsl:if>
		<xsl:call-template name="alternativeNames" />
		<xsl:call-template name="identifiers" />
		<xsl:call-template name="affiliations" />
		<xsl:call-template name="urls" />
		<xsl:call-template name="biographies" />
		<publicRecord>
			<xsl:choose>
				<xsl:when test="recordInfo/public = 'yes'">
					<xsl:text>true</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>false</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</publicRecord>
	</xsl:template>

	<xsl:template name="events">
		<events>
			<xsl:if test="recordInfo/createdBy">
				<event>
					<type>CREATE</type>
					<timestamp><xsl:value-of select="concat(substring(recordInfo/tsCreated, 1, 23), 'Z')"/></timestamp>
					<userId><xsl:value-of select="recordInfo/createdBy/linkedRecordId"/></userId>
				</event>
			</xsl:if>
			<xsl:for-each select="recordInfo/updated">
				<event>
					<type>UPDATE</type>
					<timestamp><xsl:value-of select="concat(substring(tsUpdated, 1, 23), 'Z')"/></timestamp>
					<userId><xsl:value-of select="updatedBy/linkedRecordId"/></userId>
				</event>
			</xsl:for-each>
		</events>
		
	</xsl:template>
	
	<xsl:template name="defaultName">
		<defaultName>
			<lastname><xsl:value-of select="authorisedName/familyName"/></lastname>
			<firstname><xsl:value-of select="authorisedName/givenName"/></firstname>
			<xsl:choose>
				<xsl:when test="academicTitle">
					<addition><xsl:value-of select="academicTitle"/></addition>
				</xsl:when>
				<xsl:otherwise>
					<addition/>
				</xsl:otherwise>
			</xsl:choose>
			<number/>
		</defaultName>
	</xsl:template>
	
	<xsl:template name="alternativeNames">
		<xsl:if test="alternativeName">
			<alternativeNames>
				<xsl:for-each select="alternativeName">		
					<nameForm>
						<lastname><xsl:value-of select="familyName"/></lastname>
						<firstname><xsl:value-of select="givenName"/></firstname>
						<addition/>
						<number/>
					</nameForm>
				</xsl:for-each>
			</alternativeNames>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="identifiers">
		
		<xsl:if test="ORCID_ID or VIAF_ID or Libris_ID or ../personDomainParts/personDomainPart/identifier">
			<identifiers>
				<xsl:for-each select="Libris_ID">
					<identifier>
						<type>LIBRIS</type>
						<domain/>
						<value><xsl:value-of select="."/></value>
						<from/>
						<until/>
					</identifier>
				</xsl:for-each>
				<xsl:if test="../personDomainParts/personDomainPart/identifier">
					<xsl:for-each select="../personDomainParts/personDomainPart">
						<xsl:for-each select="identifier">
							<identifier>
								<type>LOCAL</type>
								<domain><xsl:value-of select="../recordInfo/domain"/></domain>
								<value><xsl:value-of select="."/></value>
								<from/>
								<until/>
							</identifier>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:if>
				<xsl:for-each select="ORCID_ID">
					<identifier>
						<type>ORCID</type>
						<domain/>
						<value><xsl:value-of select="."/></value>
						<from/>
						<until/>
					</identifier>
				</xsl:for-each>
				<xsl:for-each select="VIAF_ID">
					<identifier>
						<type>VIAF</type>
						<domain/>
						<value><xsl:value-of select="."/></value>
						<from/>
						<until/>
					</identifier>
				</xsl:for-each>
			</identifiers>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="affiliations">
		<xsl:if test="../personDomainParts/personDomainPart/affiliation or otherAffiliation">
			<affiliations>
				<xsl:for-each select="../personDomainParts/personDomainPart/affiliation">
					<xsl:variable name="recordId" select="organisationLink/linkedRecordId"/>
					<affiliation>
						
						<xsl:for-each select="$organisations/organisation[descendant::recordInfo/id[text() = $recordId]]">
							<organisationId><xsl:value-of select="recordInfo/id"/></organisationId>
							<domain><xsl:value-of select="recordInfo/domain"/></domain>
							<name><xsl:value-of select="organisationName/name"/></name>
							<alternativeName><xsl:value-of select="organisationAlternativeName/name"/></alternativeName>
						</xsl:for-each>
						
						<from>
							<xsl:if test="affiliationFromYear">
								<xsl:value-of select="affiliationFromYear"/>
							</xsl:if>
						</from>
						
						<until>
							<xsl:if test="affiliationUntilYear">
								<xsl:value-of select="affiliationUntilYear"/>
							</xsl:if>
						</until>
												
						<xsl:for-each select="$organisations/organisation[descendant::recordInfo/id = $recordId]">
							<xsl:if test=".//parentOrganisation">
								<xsl:call-template name="parents"/>
							</xsl:if>
						</xsl:for-each>
						
						<active>
							<xsl:choose>
								<xsl:when test="$organisations/organisation[descendant::recordInfo/id = $recordId]/recordInfo/selectable = 'yes'">
									<xsl:text>true</xsl:text>
								</xsl:when>
								<xsl:when test="$organisations/organisation[descendant::recordInfo/id = $recordId]/recordInfo/selectable = 'no'">
									<xsl:text>false</xsl:text>
								</xsl:when>
							</xsl:choose>
						</active>
												
						<organisationNumber>
							<xsl:if test="$organisations/organisation[descendant::recordInfo/id = $recordId]/organisationNumber">
								<xsl:value-of select="$organisations/organisation[descendant::recordInfo/id = $recordId]/organisationNumber"/>
							</xsl:if>
						</organisationNumber>
						
						
					</affiliation>
				</xsl:for-each>
				<xsl:call-template name="otherAffiliations" />
			</affiliations>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="parents">
	
		<xsl:variable name="parentId" select=".//parentOrganisation/organisationLink/linkedRecordId"/>
		<parents>
			<affiliation>
				<organisationId><xsl:value-of select="$parentId"/></organisationId>
				<domain><xsl:value-of select="$organisations/organisation[descendant::recordInfo/id = $parentId]//recordInfo/domain" /></domain>
				<name><xsl:value-of select="$organisations/organisation[descendant::recordInfo/id = $parentId]//organisationName/name" /></name>
				<alternativeName><xsl:value-of select="$organisations/organisation[descendant::recordInfo/id = $parentId]//organisationAlternativeName/name" /></alternativeName>
				<xsl:for-each select="$organisations/organisation[descendant::recordInfo/id = $parentId]">	
					<xsl:if test=".//parentOrganisation">
						<xsl:call-template name="parents" />
					</xsl:if>
				</xsl:for-each>
				
				<active>
					<xsl:choose>
						<xsl:when test="$organisations/organisation[descendant::recordInfo/id = $parentId]/recordInfo/selectable = 'yes'">
							<xsl:text>true</xsl:text>
						</xsl:when>
						<xsl:when test="$organisations/organisation[descendant::recordInfo/id = $parentId]/recordInfo/selectable = 'no'">
							<xsl:text>false</xsl:text>
						</xsl:when>
					</xsl:choose>
				</active>
						
				<organisationNumber>
					<xsl:if test="$organisations/organisation[descendant::recordInfo/id = $parentId]/organisationNumber">
						<xsl:value-of select="$organisations/organisation[descendant::recordInfo/id = $parentId]/organisationNumber"/>
					</xsl:if>
				</organisationNumber>

			</affiliation>
		</parents>
		
	</xsl:template>
	
	<xsl:template name="otherAffiliations">
		
		<xsl:for-each select="otherAffiliation">
		<affiliation>
			<name><xsl:value-of select="affiliation" /></name>
			<alternativeName/>
			<from>
				<xsl:if test="affiliationFromYear">
					<xsl:value-of select="affiliationFromYear" />
				</xsl:if>
			</from>
			<until>
				<xsl:if test="affiliationUntilYear">
					<xsl:value-of select="affiliationUntilYear" />
				</xsl:if>
			</until>
			<active>false</active>
			<organisationNumber/>
		</affiliation>
		</xsl:for-each>
		
	</xsl:template>
	
	<xsl:template name="urls">
		<xsl:if test="externalURL">
			<urls>
				<xsl:for-each select="externalURL">
					<url>
						<xsl:choose>
							<xsl:when test="linkTitle">
								<label><xsl:value-of select="linkTitle"/></label>
							</xsl:when>
							<xsl:otherwise>
								<label/>
							</xsl:otherwise>
						</xsl:choose>
						<url><xsl:value-of select="URL"/></url>
					</url>
				</xsl:for-each>
			</urls>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="biographies">
		<xsl:if test="biographySwedish or biographyEnglish">
			<biographies class="com.google.gson.internal.LinkedTreeMap" resolves-to="linked-hash-map">
			<xsl:for-each select="biographyEnglish">
				<entry>
					<string>eng</string>
					<string><xsl:value-of select="biography"/></string>
				</entry>
			</xsl:for-each>
			<xsl:for-each select="biographySwedish">
				<entry>
					<string>swe</string>
					<string><xsl:value-of select="biography"/></string>
				</entry>
			</xsl:for-each>
			</biographies>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>