Ontology alignment API and implementation, version 2.1+
22/02/2006

Copyright (C) 2003 The University of Manchester.
Copyright (C) 2003 The University of Karlsruhe.
Copyright (C) 2003-2005 INRIA Rhône-Alpes.
Copyright (C) 2004-2005 Université de Montréal.
Copyright (C) 2005 CNR Pisa.
Copyright (C) 2005 Konstantinos A. Nedas.
Copyright (C) 2006 CERT

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public License
as published by the Free Software Foundation; either version 2.1
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

Read the LICENSE.TXT file for the terms of the LGPL license.

WHAT IS ONTOLOGY ALIGNMENT?
---------------------------

See http://alignapi.gforge.inria.fr for the moment.
See also http://www.ontologymatching.org

USING THE ONTOLOGY ALIGNMENT API IMPLEMENTATION
-----------------------------------------------

The Align API _implementation_ requires that you fetch the OWL-API ()

$ setenv CWD `pwd`

Building:
---------

$ ant jar

Running:
--------

$ setenv CWD `pwd`

$ java -jar lib/procalign.jar --help

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment -t 0.4 -o aligns/bibref.owl

$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.ParserPrinter rdf/srcalign.owl

$ java -jar lib/procalign.jar file://localhost$CWD/rdf/onto1.owl file://localhost$CWD/rdf/onto2.owl -a rdf/srcalign.owl

$ java -jar lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl 

$ java -jar lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment

$ java -jar lib/Procalign.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment -t .4

$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.util.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://localhost$CWD/aligns/bibref.owl file://localhost$CWD/rdf/srcalign.owl

Using with JWNL (Wordnet)
-------------------------

- Wordnet 2.0 should be installed
- jwnl.jar commons-logging.jar must be in lib.
- file_properties.xml must be in current directory

$ java -jar lib/alignwn.jar file://localhost$CWD/rdf/edu.umbc.ebiquity.publication.owl file://localhost$CWD/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.ling.JWNLAlignment -o aligns/JWNL.owl

LAST RELEASE
------------

The last release is available from:
    http://gforge.inria.fr/frs/?group_id=117

SOURCE REPOSITORY
-----------------

See http://gforge.inria.fr/scm/?group_id=117 for SVN Access.

DOCUMENTATION
-------------

The documentation can be found on http://gforge.inria.fr/docman/?group_id=117

FILES
-----

README.TXT	this file
LICENSE.TXT	the terms under which the software is licensed to you.
build.xml	the Ant build file for compiling and testing
classes/	empty directory for compiling the sources
distrib/	some files for generating a new jarfile
dtd/		contains the ontoalign DTDs (and schemas)
html/		contains some documentation in HTML format
			 (includes relnotes.html)
javadoc/	javadoc API documentation in HTML (not very useful)
lib/		contains align.jar, alignwn.jar, procalign.jar
			 and other necessary jarfiles
rdf/		a few samples ontologies to align
samples/	two example applications
src/		Java sources of ontoalign
		org.semanticweb.owl.align: the API
		fr.inrialpes.exmo.align.impl: basic implementation
		fr.inrialpes.exmo.align.util: utility wrapper functions
		fr.inrialpes.exmo.align.ling: WordNet-based implementation

