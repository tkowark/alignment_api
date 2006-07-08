#######################################################################

$ setenv CWD `pwd`

$ java -jar ../lib/procalign.jar --help

$ java -jar ../lib/procalign.jar file://localhost$CWD/edu.umbc.ebiquity.publication.owl file://localhost$CWD/edu.mit.visus.bibtex.owl

$ java -jar ../lib/procalign.jar file://localhost$CWD/edu.umbc.ebiquity.publication.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance

$ java -jar ../lib/procalign.jar file://localhost$CWD/edu.umbc.ebiquity.publication.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -o raw.rdf

$ java -jar ../lib/procalign.jar file://localhost$CWD/edu.umbc.ebiquity.publication.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.5 -o dist.rdf

$ java -jar ../lib/procalign.jar file://localhost$CWD/edu.umbc.ebiquity.publication.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=smoaDistance -t 0.5 -o dist2.rdf

$ java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter -i dist.rdf -o tsid.rdf

$ java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter dist.rdf -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor

$ java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/refalign.rdf file://localhost$CWD/dist.rdf

$ java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/refalign.rdf file://localhost$CWD/dist2.rdf

$ java -jar ../lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > matrix.tex

$ java -jar ../lib/alignwn.jar file://localhost$CWD/edu.umbc.ebiquity.publication.owl file://localhost$CWD/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o jwnl.rdf

$ java -cp ../lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/refalign.rdf file://localhost$CWD/jwnl.rdf

#######################################################################
More info: http://alignapi.gforge.inria.fr

Planning:
- Alignment server (incl. DB storage, agents, WSDL service)
- Extensive composition operators (with comp. tables)
- Expressive alignment language (with SEKT/François Sharffe)
#######################################################################
