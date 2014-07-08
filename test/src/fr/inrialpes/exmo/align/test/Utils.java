package fr.inrialpes.exmo.align.test;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.parser.RDFParser;
import java.io.FileInputStream;
import java.io.InputStream;

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
        assert(aparser != null);
        aparser.initAlignment(null);
        RDFParser alignmentParser = new RDFParser();
        InputStream alignIn = new FileInputStream("test/input/" + fileName);
        EDOALAlignment loadedAlignment = alignmentParser.parse(alignIn);
        return loadedAlignment;
    }
}
