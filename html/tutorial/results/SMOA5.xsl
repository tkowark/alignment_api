<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ns1="http://alignapi.gforge.inria.fr/tutorial/myOnto.owl#"
    xmlns:ns0="http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  >

  <xsl:template match="rdf:List">
    <xsl:element name="ns0:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Booklet">
    <xsl:element name="ns0:Booklet">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:InBook">
    <xsl:element name="ns0:Inbook">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="rdf:rest">
    <xsl:element name="ns0:hasAddress">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Collection">
    <xsl:element name="ns0:Incollection">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Part">
    <xsl:element name="ns0:Article">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:keywords">
    <xsl:element name="ns0:hasKeywords">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:number">
    <xsl:element name="ns0:hasNumber">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:lang">
    <xsl:element name="ns0:hasLanguage">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Misc">
    <xsl:element name="ns0:Misc">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:editor">
    <xsl:element name="ns0:hasEditor">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:InProceedings">
    <xsl:element name="ns0:Inproceedings">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:type">
    <xsl:element name="ns0:hasType">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:location">
    <xsl:element name="ns0:hasLocation">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:isbn">
    <xsl:element name="ns0:hasISBN">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:copyright">
    <xsl:element name="ns0:hasCopyright">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:TechReport">
    <xsl:element name="ns0:Techreport">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:contents">
    <xsl:element name="ns0:hasContents">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:series">
    <xsl:element name="ns0:hasSeries">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:annote">
    <xsl:element name="ns0:hasNote">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:key">
    <xsl:element name="ns0:hasKey">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:date">
    <xsl:element name="ns0:pageChapterData">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:lccn">
    <xsl:element name="ns0:hasLCCN">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Published">
    <xsl:element name="ns0:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Institution">
    <xsl:element name="ns0:Incollection">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:MastersThesis">
    <xsl:element name="ns0:Mastersthesis">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Entry">
    <xsl:element name="ns0:Entry">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:price">
    <xsl:element name="ns0:hasPrice">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Publisher">
    <xsl:element name="ns0:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:edition">
    <xsl:element name="ns0:hasEdition">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:size">
    <xsl:element name="ns0:hasSize">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:year">
    <xsl:element name="ns0:hasYear">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:month">
    <xsl:element name="ns0:hasMonth">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Unpublished">
    <xsl:element name="ns0:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Proceedings">
    <xsl:element name="ns0:Proceedings">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:institution">
    <xsl:element name="ns0:hasInstitution">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:humanCreator">
    <xsl:element name="ns0:humanCreator">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Techreport">
    <xsl:element name="ns0:Techreport">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:volume">
    <xsl:element name="ns0:hasVolume">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:affiliation">
    <xsl:element name="ns0:hasAffiliation">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:issn">
    <xsl:element name="ns0:hasISSN">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:short">
    <xsl:element name="ns0:hasAuthor">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:language">
    <xsl:element name="ns0:hasLanguage">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Article">
    <xsl:element name="ns0:Article">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:url">
    <xsl:element name="ns0:hasURL">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:author">
    <xsl:element name="ns0:hasAuthor">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:BookPart">
    <xsl:element name="ns0:Book">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:note">
    <xsl:element name="ns0:hasNote">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:howPublished">
    <xsl:element name="ns0:howPublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:publisher">
    <xsl:element name="ns0:hasPublisher">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:school">
    <xsl:element name="ns0:hasSchool">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Thesis">
    <xsl:element name="ns0:Phdthesis">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:InCollection">
    <xsl:element name="ns0:Incollection">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:endPage">
    <xsl:element name="ns0:hasPages">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:startPage">
    <xsl:element name="ns0:hasPages">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:organization">
    <xsl:element name="ns0:hasOrganization">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:inJournal">
    <xsl:element name="ns0:hasJournal">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:abstract">
    <xsl:element name="ns0:hasAbstract">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:PhdThesis">
    <xsl:element name="ns0:Phdthesis">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Book">
    <xsl:element name="ns0:Book">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:title">
    <xsl:element name="ns0:hasTitle">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:numberOrVolume">
    <xsl:element name="ns0:hasNumber">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:pages">
    <xsl:element name="ns0:hasPages">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:pagesOrChapter">
    <xsl:element name="ns0:pageChapterData">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Manual">
    <xsl:element name="ns0:Manual">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:mrNumber">
    <xsl:element name="ns0:hasMrnumber">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:chapter">
    <xsl:element name="ns0:hasChapter">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <!-- Copying the root -->
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Copying all elements and attributes -->
  <xsl:template match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
