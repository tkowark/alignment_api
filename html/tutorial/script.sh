#!/bin/csh

#####################
# Preparation

#mkdir alignapi
#cd alignapi
#unzip align*.zip
#java -jar lib/procalign.jar --help
#cd html/tutorial
setenv CWD `pwd`
setenv WNDIR ../../../WordNet-2.0/dict

/bin/rm results/*

#####################
# Matching

java -jar ../../lib/procalign.jar file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl

java -jar ../../lib/procalign.jar file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/equal.rdf

java -jar ../../lib/procalign.jar file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/equal.html

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/levenshtein.rdf

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/levenshtein.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/levenshtein.html

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/SMOA.rdf

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/SMOA.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/SMOA.html

java -jar ../../lib/alignwn.jar -Dwndict=$WNDIR -i fr.inrialpes.exmo.align.ling.JWNLAlignment file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/jwnl.rdf

java -jar ../../lib/alignwn.jar -Dwndict=$WNDIR -i fr.inrialpes.exmo.align.ling.JWNLAlignment file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/jwnl.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/jwnl.html

#####################
# Manipulating

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.33 file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/levenshtein33.rdf

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.33 file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/levenshtein33.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/levenshtein33.html

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance -t 0.5 file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/SMOA5.rdf

java -jar ../../lib/procalign.jar -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance -t 0.5 file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl -o results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/SMOA5.html

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -i -o results/AOMS5.rdf

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -i -o results/AOMS5.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/AOMS5.html

#####################
# Outputing

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter results/SMOA5.rdf -r fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor -o results/SMOA5.xsl

xsltproc results/SMOA5.xsl data.xml > results/data.xml

#####################
# Evaluating

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter refalign.rdf -r fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor -o results/refalign.html

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/refalign.rdf file://$CWD/results/equal.rdf

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/refalign.rdf file://$CWD/results/levenshtein33.rdf

# This is for printing distance matrix
#java -jar ../../lib/Procalign.jar file://$CWD/rdf/myOnto.owl file://$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > matrix.tex

cp refalign.rdf results

java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.util.GroupEval -r refalign.rdf -l "refalign,equal,SMOA,SMOA5,levenshtein,levenshtein33" -c prf -o results/eval.html

#####################
# Embedding

javac -classpath ../../lib/api.jar:../../lib/rdfparser.jar:../../lib/align.jar:../../lib/procalign.jar -d results Skeleton.java

java -cp ../../lib/Procalign.jar:results Skeleton file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl

javac -classpath ../../lib/api.jar:../../lib/rdfparser.jar:../../lib/align.jar:../../lib/procalign.jar -d results MyApp.java

#///////////// STRANGE RESULTS
java -cp ../../lib/Procalign.jar:results MyApp file://$CWD/myOnto.owl file://$CWD/edu.mit.visus.bibtex.owl > results/MyApp.owl
