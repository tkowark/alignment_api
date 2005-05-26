Ontology alignment API and implementation, version @VERS@
@DATE@

Copyright (C) 2003-2005 INRIA Rhône-Alpes.
Copyright (C) 2004 Université de Montréal.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public License
as published by the Free Software Foundation; either version 2.1
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

Read the LICENSE.TXT file for the terms of the GLPL license.

WHAT IS ONTOLOGY ALIGNMENT?
---------------------------

See http://co4.inrialpes.fr/align for the moment.

USING THE ONTOLOGY ALIGNMENT API IMPLEMENTATION
-----------------------------------------------

The Align API implementation requires that you fetch the OWL-API ()

$ setenv CWD `pwd`

Building:
---------

$ ant jar

Running:
--------

$ setenv CWD `pwd`

$ java -jar lib/procalign.jar --help

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor -t 0.4

$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter rdf/srcalign.owl

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -a rdf/srcalign.owl

$ java -jar lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl 

$ java -jar lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment

$ java -jar lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment -t .4

$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/aligns/bibref.owl file://localhost$CWD/aligns/BLA 

Using with JWNL (Wordnet)
-------------------------

- Wordnet 2.0 should be installed
- jwnl.jar commons-logging.jar must be in lib.
- file_properties.xml must be in current directory

$ java -jar lib/alignwn.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignmentTest -o aligns/JWNL.owl

LAST RELEASE
------------

The last release is available from:
	http://co4.inrialpes.fr/align
or
	http://www.inrialpes.fr/exmo/software/ontoalign.html

CVS ACCESS
----------

See http://co4.inrialpes.fr/align for CVS Access.

DOCUMENTATION
-------------

The best documentation can be found on the web site above.

FILES
-----

README.TXT	this file
LICENSE.TXT	the terms under which the software is licensed to you.
RELNOTES.TXT	release notes and to-do list
build.xml	the Ant build file for compiling and testing
classes/	empty directory for compiling the sources
distrib/	some files for generating a new jarfile
dtd/		contains the ontoalign DTDs (and schemas)
html/		contains some documentation in HTML format
javadoc/	javadoc API documentation in HTML (not very useful)
lib/		contains align.jar and other necessary jarfiles
rdf/		a few samples ontologies to align
samples/	two example applications
src/		Java sources of Transmorpher

