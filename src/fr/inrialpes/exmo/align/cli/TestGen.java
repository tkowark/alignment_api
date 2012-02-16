/*
 * $Id$
 *
 * Copyright (C) 2011-2012, INRIA
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.Properties;

import fr.inrialpes.exmo.align.gen.TestGenerator;
import fr.inrialpes.exmo.align.gen.BenchmarkGenerator;
import fr.inrialpes.exmo.align.gen.DiscriminantGenerator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

/** 
    An utility application for generating tests from command line.
    It can either generate a single test or a whole test suite from a single ontology.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.gen.TestGen [options]
    </pre>

    where the options are:
    <pre>
        --method=methodName         --> arbitraryTest
                                    --> generateBenchmark

        --fileName=file             --> the file name of the ontology

        --debug[=n] -d [n]          --> Report debug info at level n,
        --testNumber=number         --> the number of the generated test
   </pre>

*/

public class TestGen {
    private Properties params = null;
    private String methodName = null;                                           //the name of the method
    private String testNumber = null;                                           //the number of the generated test
    private String fileName   = null;                                           //the name of the input file
    private String dir        = null;                                           //the name of the input file
    private int debug         = 0;
    public static String ARBITRARY_TEST = "arbitraryTest";                      //generate an arbitrary test
    public static String GENERATE_BENCHMARK = "generateBenchmark";              //generate the Benchmark dataset
    public static String DISCRIMINANT_BENCHMARK = "disc";              //generate the Benchmark dataset

    public TestGen() {
	fileName = "onto.rdf";
    }

    public static void main(String[] args) {
        try { new TestGen().run( args ); }
        catch ( Exception ex ) { ex.printStackTrace(); };
    }

      public void run(String[] args) throws Exception {
          LongOpt[] longopts = new LongOpt[10];
          params = new Properties();

          longopts[0] = new LongOpt("method", LongOpt.REQUIRED_ARGUMENT, null, 'm');
          longopts[1] = new LongOpt("initonto", LongOpt.REQUIRED_ARGUMENT, null, 'i');
          longopts[2] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
          longopts[3] = new LongOpt("testNumber", LongOpt.REQUIRED_ARGUMENT, null, 't');
	  longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	  longopts[5] = new LongOpt("outdir", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	  longopts[6] = new LongOpt("urlprefix", LongOpt.REQUIRED_ARGUMENT, null, 'u');
	  longopts[7] = new LongOpt("ontoname", LongOpt.REQUIRED_ARGUMENT, null, 'n');
	  longopts[8] = new LongOpt("alignname", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	  longopts[9] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
          

          Getopt g = new Getopt("", args, "d::o:u:h:m:n:i:a:D:t", longopts);
          int c;
          String arg;

          while ((c = g.getopt()) != -1) {
              switch (c) {
	      case 'h':
		  printUsage();
		  return;
	      case 'm':
		  methodName = g.getOptarg();
		  break;
	      case 'i':
		  fileName = g.getOptarg();
		  params.setProperty( "filename", fileName );
		  break;
	      case 'n':
		  params.setProperty( "ontoname", g.getOptarg() );
		  break;
	      case 'a':
		  params.setProperty( "alignname", g.getOptarg() );
		  break;
	      case 't':
		  testNumber = g.getOptarg();
		  System.err.println("testNumber " + "[" + testNumber + "]");
		  break;
	      case 'o' : /* Use output directory */
		  params.setProperty( "outdir", g.getOptarg() );
		  break;
	      case 'u' : /* URLPrefix */
		  params.setProperty( "urlprefix", g.getOptarg() );
		  break;
	      case 'd' : /* Debug level  */
		  arg = g.getOptarg();
		  if ( arg != null ) params.setProperty( "debug", arg.trim() );
		  else 		  params.setProperty( "debug", "4" );
		  break;
	      case 'D' : /* Parameter definition: could be used for all parameters */
		  arg = g.getOptarg();
		  int index = arg.indexOf('=');
		  if ( index != -1 ) {
		      params.setProperty( arg.substring( 0, index), 
					  arg.substring(index+1));
		  } else {
		      System.err.println("Bad parameter syntax: "+g);
		      printUsage();
		      System.exit(0);
		  }
		  break;
              }
          }

	  if ( debug > 0 ) System.err.println( " >>>> "+methodName+" from "+fileName );

          if ( methodName.equals( ARBITRARY_TEST ) ) { //generate an arbitrary test
	      TestGenerator tg = new TestGenerator();
              tg.modifyOntology( fileName, (Properties)null, testNumber, params );
          } else if ( methodName.equals( GENERATE_BENCHMARK ) ) { //generate the benchmark
              BenchmarkGenerator gb = new BenchmarkGenerator();
              gb.generate( params );
	  } else if ( methodName.equals( DISCRIMINANT_BENCHMARK ) ) { //generate the benchmark
              DiscriminantGenerator gb = new DiscriminantGenerator();
              gb.generate( params );
          }
    }

     public void printUsage() {
         System.out.println("TestGen [options]");
         System.out.println("options are");
         System.out.println("--method=methodName, where methodName can be \""+ARBITRARY_TEST+"\" or \""+GENERATE_BENCHMARK+"\"");
         System.out.println("--initonto=filename (initial ontology)");
         System.out.println("--alignname=filename [default: refalign.rdf]");
         System.out.println("--ontoname=filename [default: onto.rdf]");
         System.out.println("--urlprefix=url");
         System.out.println("--outdir=directory [default: .]");
         System.out.println("--testNumber=number, if the arbitraryTest is chosen");
         System.out.println("--help");
         System.out.println("--debug=number [default: 0]");
         System.out.println("-Dparameter=value");
         System.out.println("where the parameters are");
         System.out.println( "[--------------------------------------------------------------------------]" );
         System.out.println( "[------------- The list of all modification is the following: --------------]" );
         System.out.println( "[1. Remove percentage subclasses       \""+ParametersIds.REMOVE_CLASSES+"\"    --------------]" );
         System.out.println( "[2. Remove percentage properties       \""+ParametersIds.REMOVE_PROPERTIES+"\"    --------------]" );
         System.out.println( "[3. Remove percentage comments         \""+ParametersIds.REMOVE_COMMENTS+"\"     --------------]" );
         System.out.println( "[4. Remove percentage restrictions     \""+ParametersIds.REMOVE_RESTRICTIONS+"\" --------------]" );
         System.out.println( "[5. Remove individuals                 \""+ParametersIds.REMOVE_INDIVIDUALS+"\"   ------------]" );
         System.out.println( "[6. Add percentage subclasses          \""+ParametersIds.ADD_CLASSES+"\"       --------------]" );
         System.out.println( "[7. Add percentage properties          \""+ParametersIds.ADD_PROPERTIES+"\"       --------------]" );
         System.out.println( "[8. Rename percentage classes          \""+ParametersIds.RENAME_CLASSES+"\"     --------------]" );
         System.out.println( "[9. Rename percentage properties       \""+ParametersIds.RENAME_PROPERTIES+"\"  --------------]" );
         System.out.println( "[10. noHierarchy                       \""+ParametersIds.NO_HIERARCHY+"\"    ---------------]" );
         System.out.println( "[11. Level flattened                   \""+ParametersIds.LEVEL_FLATTENED+"\"   ---------------]" );
         System.out.println( "[12. Add nbClasses to a specific level \""+ParametersIds.ADD_CLASSESLEVEL+"\"       ---------------]" );
         System.out.println( "[--------------------------------------------------------------------------]" );
    }
    
}
