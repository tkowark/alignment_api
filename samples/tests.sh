#!/bin/csh
# This is a serie of tests made for the presentation of the API.
# All these tests can be automatically run

# Context
echo "Cleaning up."
setenv CWD `pwd`

# Clean up
/bin/rm aligns/*.owl
/bin/cp ../dtd/align.dtd aligns/align.dtd
/bin/cp ../file_properties.xml .

# Display parameters
echo "Basic..."
java -jar ../lib/procalign.jar

# Simple basic example
java -jar ../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.SubsDistNameAlignment file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -o aligns/sample.owl

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.SubsDistNameAlignment -t .6 -r fr.inrialpes.exmo.align.impl.OWLAxiomsRendererVisitor

# Test a number of methods
echo "Aligning..."
java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.NameEqAlignment -o aligns/NameEq.owl

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.EditDistNameAlignment -o aligns/EditDistName.owl

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.SubsDistNameAlignment -o aligns/SubsDistName.owl

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.StrucSubsDistAlignment -o aligns/StrucSubsDist.owl

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.StrucSubsDistAlignment -o aligns/StrucSubsDist4.owl -t .4

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.StrucSubsDistAlignment -o aligns/StrucSubsDist7.owl -t .7

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignmentTest -o aligns/JWNL.owl

# Evaluate their performances
echo "Comparing..."

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/NameEq.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/EditDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/SubsDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/StrucSubsDist.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/StrucSubsDist4.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/StrucSubsDist7.owl

# Other evaluations
echo "Comparing again..."

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.SymMeanEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/NameEq.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.SymMeanEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/EditDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.SymMeanEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/SubsDistName.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.SymMeanEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/StrucSubsDist.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.SymMeanEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/StrucSubsDist4.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.SymMeanEvaluator file://localhost$CWD/rdf/bibref.owl file://localhost$CWD/aligns/StrucSubsDist7.owl
# Pipelining
echo "Pipelining..."

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.PropSubsDistAlignment -o aligns/PropSubsDist.owl 

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.ClassStructAlignment -a aligns/PropSubsDist.owl -o aligns/Piped.owl

java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.PRecEvaluator file://localhost$CWD/aligns/StrucSubsDist.owl file://localhost$CWD/aligns/Piped.owl

# Rendering
echo "Rendering..."

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.OWLAxiomsRendererVisitor -t 0.4

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.XSLTRendererVisitor -t 0.4

java -jar ../lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.SWRLRendererVisitor -t 0.4

# Output to html
echo "HTML output..."

# This should be best done with JAVA XSLT if it exists than xsltproc
echo '<html><head></head><body>' > aligns/index.html
xsltproc ../html/form-align.xsl rdf/bibref.owl > aligns/bibref.html
echo '<a href="bibref.html">Reference</a>' >> aligns/index.html
foreach i (`ls aligns/*.owl | sed "s:aligns/::" | sed "s:\.owl::"`)
	echo '<a href="'$i'.html">'$i'</a>' >> aligns/index.html
	xsltproc ../html/form-align.xsl aligns/$i.owl > aligns/$i.html
end
echo '</body></html>' >> aligns/index.html
