#!/bin/csh

#####################
# Preparation

#mkdir alignapi
#cd alignapi
#unzip align*.zip
#java -jar lib/procalign.jar --help
#cd html/tutorial
setenv CWD `pwd`

/bin/rm results/*

#####################
# Matching

java -jar ../../lib/procalign.jar file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl
java -jar ../../lib/procalign.jar file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -o results/equal.rdf
xsltproc ../form-align.xsl results/equal.rdf > results/equal.html
java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -o results/levenshtein.rdf
xsltproc ../form-align.xsl results/levenshtein.rdf > results/levenshtein.html
java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -o results/SMOA.rdf
xsltproc ../form-align.xsl results/SMOA.rdf > results/SMOA.html
#java -jar ../../lib/alignwn.jar -i fr.inrialpes.exmo.align.ling.JWNLAlignment file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -o results/jwnl.rdf
#xsltproc ../form-align.xsl results/jwnl.rdf > results/jwnl.html

#####################
# Manipulating

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.33 file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -o results/levenshtein33.rdf
xsltproc ../form-align.xsl results/levenshtein33.rdf > results/levenshtein33.html
java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance -t 0.5 file://localhost$CWD/myOnto.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -o results/SMOA5.rdf
xsltproc ../form-align.xsl results/SMOA5.rdf > results/SMOA5.html
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter -i results/SMOA5.rdf -o results/AOMS5.rdf
xsltproc ../form-align.xsl results/AOMS5.rdf > results/AOMS5.html

#####################
# Outputing

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor -o results/SMOA5.xsl
xsltproc results/SMOA5.xsl data.xml > results/data.xml

#####################
# Evaluating

xsltproc ../form-align.xsl refalign.rdf > results/refalign.html
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/refalign.rdf file://localhost$CWD/results/equal.rdf
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/refalign.rdf file://localhost$CWD/results/levenshtein33.rdf
#java -jar ../../lib/Procalign.jar file://localhost$CWD/rdf/myOnto.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > matrix.tex
cp refalign.rdf results
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.GroupEval -r refalign.rdf -l "refalign,equal,SMOA,SMOA5,levenshtein,levenshtein33" -c prf -o results/eval.html

#####################
# Embedding

javac -classpath ../../lib/api.jar:../../lib/rdfparser.jar:../../lib/align.jar:../../lib/procalign.jar -d results Skeleton.java
java -cp ../../lib/Procalign.jar:results Skeleton myOnto.owl edu.mit.visus.bibtex.owl
javac -classpath ../../lib/api.jar:../../lib/rdfparser.jar:../../lib/align.jar:../../lib/procalign.jar -d results MyApp.java
java -cp ../../lib/Procalign.jar:results MyApp myOnto.owl edu.mit.visus.bibtex.owl > results/MyApp.owl
