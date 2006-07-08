<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
    xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
xmlns:ns1="http://alignapi.gforge.inria.fr/tutorial/myOnto.owl#"
xmlns:ns2="http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl#"
>

  <xsl:template match="rdf:List">
    <xsl:element name="ns2:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Booklet">
    <xsl:element name="ns2:Booklet">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:InBook">
    <xsl:element name="ns2:Inbook">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="rdf:rest">
    <xsl:element name="ns2:hasAddress">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Collection">
    <xsl:element name="ns2:Incollection">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Part">
    <xsl:element name="ns2:Article">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:keywords">
    <xsl:element name="ns2:hasKeywords">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:number">
    <xsl:element name="ns2:hasNumber">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:lang">
    <xsl:element name="ns2:hasLanguage">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Misc">
    <xsl:element name="ns2:Misc">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:editor">
    <xsl:element name="ns2:hasEditor">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:InProceedings">
    <xsl:element name="ns2:Inproceedings">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:type">
    <xsl:element name="ns2:hasType">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:location">
    <xsl:element name="ns2:hasLocation">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:isbn">
    <xsl:element name="ns2:hasISBN">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:copyright">
    <xsl:element name="ns2:hasCopyright">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:TechReport">
    <xsl:element name="ns2:Techreport">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:contents">
    <xsl:element name="ns2:hasContents">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:series">
    <xsl:element name="ns2:hasSeries">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:annote">
    <xsl:element name="ns2:hasNote">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:key">
    <xsl:element name="ns2:hasKey">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:date">
    <xsl:element name="ns2:pageChapterData">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:lccn">
    <xsl:element name="ns2:hasLCCN">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Published">
    <xsl:element name="ns2:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Institution">
    <xsl:element name="ns2:Incollection">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:MastersThesis">
    <xsl:element name="ns2:Mastersthesis">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Entry">
    <xsl:element name="ns2:Entry">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:price">
    <xsl:element name="ns2:hasPrice">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Publisher">
    <xsl:element name="ns2:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:edition">
    <xsl:element name="ns2:hasEdition">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:size">
    <xsl:element name="ns2:hasSize">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:year">
    <xsl:element name="ns2:hasYear">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:month">
    <xsl:element name="ns2:hasMonth">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Unpublished">
    <xsl:element name="ns2:Unpublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Proceedings">
    <xsl:element name="ns2:Proceedings">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:institution">
    <xsl:element name="ns2:hasInstitution">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:humanCreator">
    <xsl:element name="ns2:humanCreator">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Techreport">
    <xsl:element name="ns2:Techreport">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:volume">
    <xsl:element name="ns2:hasVolume">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:affiliation">
    <xsl:element name="ns2:hasAffiliation">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:issn">
    <xsl:element name="ns2:hasISSN">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:short">
    <xsl:element name="ns2:hasAuthor">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:language">
    <xsl:element name="ns2:hasLanguage">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Article">
    <xsl:element name="ns2:Article">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:url">
    <xsl:element name="ns2:hasURL">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:author">
    <xsl:element name="ns2:hasAuthor">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:BookPart">
    <xsl:element name="ns2:Book">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:note">
    <xsl:element name="ns2:hasNote">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:howPublished">
    <xsl:element name="ns2:howPublished">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:publisher">
    <xsl:element name="ns2:hasPublisher">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:school">
    <xsl:element name="ns2:hasSchool">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Thesis">
    <xsl:element name="ns2:Phdthesis">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:InCollection">
    <xsl:element name="ns2:Incollection">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:endPage">
    <xsl:element name="ns2:hasPages">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:startPage">
    <xsl:element name="ns2:hasPages">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:organization">
    <xsl:element name="ns2:hasOrganization">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:inJournal">
    <xsl:element name="ns2:hasJournal">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:abstract">
    <xsl:element name="ns2:hasAbstract">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:PhdThesis">
    <xsl:element name="ns2:Phdthesis">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Book">
    <xsl:element name="ns2:Book">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:title">
    <xsl:element name="ns2:hasTitle">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:numberOrVolume">
    <xsl:element name="ns2:hasNumber">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:pages">
    <xsl:element name="ns2:hasPages">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:pagesOrChapter">
    <xsl:element name="ns2:pageChapterData">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:Manual">
    <xsl:element name="ns2:Manual">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:mrNumber">
    <xsl:element name="ns2:hasMrnumber">
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ns1:chapter">
    <xsl:element name="ns2:hasChapter">
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
