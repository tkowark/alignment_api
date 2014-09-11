package fr.inrialpes.exmo.align.test;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.parser.RDFParser;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nicolas Guillouet <nicolas@meaningengines.com>
 */
public class Utils {

    public static EDOALAlignment loadAlignement(String fileName) throws Exception {
        AlignmentParser aparser = new AlignmentParser(0);
        assert (aparser != null);
        aparser.initAlignment(null);
        RDFParser alignmentParser = new RDFParser();
        InputStream alignIn = new FileInputStream("test/input/" + fileName);
        EDOALAlignment loadedAlignment = alignmentParser.parse(alignIn);
        return loadedAlignment;
    }

    public static Model loadValues(String[] filesNames) throws Exception {
//        Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, null);
        Dataset dataset = DatasetFactory.createMem();
        Model model = dataset.getDefaultModel();
        for (String fileName : filesNames) {
            String fullPathName = "test/input/" + fileName;
            String source = "file:" + fullPathName;
            InputStream descIn = new FileInputStream(fullPathName);
            model.read(descIn, source);
            descIn.close();
        }
        return model;
    }

    /**
     * Return pair key => value with key = vars of resultSet value = uniq
     * results of each var.
     *
     * @param resultSet
     * @return
     */
    public static HashMap<String, Collection<String>> getResultValues(ResultSet resultSet) {
        HashMap<String, Collection<String>> toReturn = new HashMap();
        List<String> varsNames = resultSet.getResultVars();
        for (String varName : varsNames) {
            toReturn.put(varName, new TreeSet());
        }
        while (resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.nextSolution();
//            System.out.println("in getResultValues s1 : " + querySolution.get("s1").toString() + " s2 : " + querySolution.get("s2").toString());
            for (String varName : varsNames) {
                if (querySolution != null) {
                    if (querySolution.get(varName) != null) {
                        toReturn.get(varName).add(querySolution.get(varName).toString());
                    }
                }
            }
        }
        return toReturn;
    }

    public static void showResultValues(ResultSet resultSet) {
        List<String> varsNames = resultSet.getResultVars();
        StringBuilder stringBuilder = new StringBuilder();
        String separator = " ################### ";
        stringBuilder.append(separator);
        for (String varName : varsNames) {
            stringBuilder.append(varName);
        }
        stringBuilder.append(separator);
        stringBuilder.append("\n");

        while (resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.nextSolution();
            if (querySolution != null) {
                for (String varName : varsNames) {
                    stringBuilder.append(separator);
                    if (querySolution.get(varName) != null) {
                        stringBuilder.append(querySolution.get(varName).toString());
                    }
                }
            } else {
                stringBuilder.append("NO SOLUTION");
            }
            stringBuilder.append("\n");
        }
        System.out.println(stringBuilder.toString());
    }

}
