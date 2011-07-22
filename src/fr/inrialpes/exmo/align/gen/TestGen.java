/*
 * $Id: TestGen.java
 *
 * Copyright (C) 2003-2010, INRIA
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

package fr.inrialpes.exmo.align.gen;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/** Generates tests.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.gen.TestGen [options]
    </pre>

    where the options are:
    <pre>
        --method=methodName         --> arbitraryTest
                                    --> generateBenchmark

        --fileName=file             --> the file name of the ontology

        --testNumber=number         --> the number of the generated test
   </pre>

*/

public class TestGen {
    private BasicParameters params = null;
    private String methodName = null;                                           //the name of the method
    private String testNumber = null;                                           //the number of the generated test
    private String fileName   = null;                                           //the name of the input file
    public static String ARBITRARY_TEST = "arbitraryTest";                      //generate an arbitrary test
    public static String GENERATE_BENCHMARK = "generateBenchmark";              //generate the Benchmark dataset

    public static void main(String[] args) {
        try { new TestGen().run( args ); }
        catch (Exception ex) { ex.printStackTrace(); };
    }

      public void run(String[] args) throws Exception {
          LongOpt[] longopts = new LongOpt[4];
          params = new BasicParameters();

          longopts[0] = new LongOpt("method", LongOpt.REQUIRED_ARGUMENT, null, 'm');
          longopts[1] = new LongOpt("fileName", LongOpt.REQUIRED_ARGUMENT, null, 'p');
          longopts[2] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
          longopts[3] = new LongOpt("testNumber", LongOpt.REQUIRED_ARGUMENT, null, 't');
          

          Getopt g = new Getopt("", args, "h:m:p:t", longopts);
          int c;
          
          while ((c = g.getopt()) != -1) {
              switch (c) {
                  case 'h':
                      printUsage();
                      return;
                  case 'm':
                      methodName = g.getOptarg();
                      System.out.println("method " + "[" + methodName + "]");
                      break;
                  case 'p':
                      fileName = g.getOptarg();
                      System.out.println("fileName " + "[" + fileName + "]");
                      break;
                  case 't':
                      testNumber = g.getOptarg();
                      System.out.println("testNumber " + "[" + testNumber + "]");
                      break;
              }
          }

          //generate an arbitrary test
          if (methodName.equals(this.ARBITRARY_TEST)) {
              int currentNb = g.getOptind();
              int totalNb = args.length;

              //copy the vector of parameters
              String[] parameters = new String[totalNb-currentNb];
              System.arraycopy(args, currentNb, parameters, 0, totalNb-currentNb);

              //build an ArbitraryTest object and modify the ontology according to it
              ArbitraryTest at = new ArbitraryTest(this.fileName, this.testNumber, parameters);
              at.modifyOntology();
          }

          //generate the benchmark
          if (methodName.equals(this.GENERATE_BENCHMARK)) {
              GenerateBenchmark gb = new GenerateBenchmark(this.fileName);
              gb.generate();
          }
    }


     public void printUsage() {
         System.out.println("TestGen [options]");
         System.out.println("options are");
         System.out.println("--method=methodName, where methodName can be \"arbitraryTest\" or \"generateBenchmark\"");
         System.out.println("--fileName=file");
         System.out.println("--testNumber=number, if the arbitraryTest is chosen");
         System.out.println("parameter value");
         System.out.println("where the parameters are");
         System.out.println( "[--------------------------------------------------------------------------]" );
         System.out.println( "[------------- The list of all modification is the following: --------------]" );
         System.out.println( "[1. Remove percentage subclasses       \"removeClasses\"    --------------]" );
         System.out.println( "[2. Remove percentage properties       \"removeProperties\"    --------------]" );
         System.out.println( "[3. Remove percentage comments         \"removeComments\"     --------------]" );
         System.out.println( "[4. Remove percentage restrictions     \"removeRestrictions\" --------------]" );
         System.out.println( "[5. Remove individuals                 \"removeIndividuals\"   ------------]" );
         System.out.println( "[6. Add percentage subclasses          \"addClasses\"       --------------]" );
         System.out.println( "[7. Add percentage properties          \"addProperties\"       --------------]" );
         System.out.println( "[8. Rename percentage classes          \"renameClasses\"     --------------]" );
         System.out.println( "[9. Rename percentage properties       \"renameProperties\"  --------------]" );
         System.out.println( "[10. noHierarchy                       \"noHierarchy\"    ---------------]" );
         System.out.println( "[11. Level flattened                   \"levelFlattened\"   ---------------]" );
         System.out.println( "[12. Add nbClasses to a specific level \"addClassesLevel\"       ---------------]" );
         System.out.println( "[--------------------------------------------------------------------------]" );
    }
    
}
