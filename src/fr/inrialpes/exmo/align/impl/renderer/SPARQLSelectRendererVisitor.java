/*
 * $Id$
 *
 * Copyright (C) INRIA, 2012-2014
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package fr.inrialpes.exmo.align.impl.renderer;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.Linkkey;
import fr.inrialpes.exmo.align.impl.edoal.LinkkeyBinding;
import fr.inrialpes.exmo.align.impl.edoal.LinkkeyEquals;
import fr.inrialpes.exmo.align.impl.edoal.LinkkeyIntersects;
import static fr.inrialpes.exmo.align.impl.renderer.GraphPatternRendererVisitor.blanks;

import java.io.PrintWriter;
import java.net.URI;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class SPARQLSelectRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String, String> nslist = null;

    boolean embedded = false;
    boolean oneway = false;
    boolean split = false;
    String splitdir = "";

    boolean edoal = false;

    boolean fromOnto1ToOnto2 = true;

    public SPARQLSelectRendererVisitor(PrintWriter writer) {
        super(writer);
    }

    public void init(Properties p) {
        if (p.getProperty("embedded") != null && !p.getProperty("embedded").equals("")) {
            embedded = true;
        }
        if (p.getProperty("oneway") != null && !p.getProperty("oneway").equals("")) {
            oneway = true;
        }
        if (p.getProperty("blanks") != null && !p.getProperty("blanks").equals("")) {
            blanks = true;
        }
        if (p.getProperty("weakens") != null && !p.getProperty("weakens").equals("")) {
            weakens = true;
        }
        if (p.getProperty("ignoreerrors") != null && !p.getProperty("ignoreerrors").equals("")) {
            ignoreerrors = true;
        }
        split = (p.getProperty("split") != null && !p.getProperty("split").equals(""));
        if (p.getProperty("dir") != null && !p.getProperty("dir").equals("")) {
            splitdir = p.getProperty("dir") + "/";
        }
        if (p.getProperty("indent") != null) {
            INDENT = p.getProperty("indent");
        }
        if (p.getProperty("newline") != null) {
            NL = p.getProperty("newline");
        }
    }

    public void visit(Alignment align) throws AlignmentException {
        if (subsumedInvocableMethod(this, align, Alignment.class)) {
            return;
        }
        if (align instanceof EDOALAlignment) {
            alignment = align;
        } else {
            try {
                alignment = EDOALAlignment.toEDOALAlignment((BasicAlignment) align);
            } catch (AlignmentException alex) {
                throw new AlignmentException("SPARQLSELECTRenderer: cannot render simple alignment. Need an EDOALAlignment", alex);
            }
        }
        edoal = alignment.getLevel().startsWith("2EDOAL");
        for (Cell c : alignment) {
            c.accept(this);
        };
    }

    public void visit(Cell cell) throws AlignmentException {
        if (subsumedInvocableMethod(this, cell, Cell.class)) {
            return;
        }
        // default behaviour
        this.cell = cell;
        URI u1 = cell.getObject1AsURI(alignment);
        URI u2 = cell.getObject2AsURI(alignment);
        if (edoal || (u1 != null && u2 != null)) {
            generateSelect(cell, (Expression) (cell.getObject1()), (Expression) (cell.getObject2()), false);
            if (!oneway) {
                generateSelect(cell, (Expression) (cell.getObject1()), (Expression) (cell.getObject2()), true);
            }
        }
    }

    public void visit(Relation rel) throws AlignmentException {
        if (subsumedInvocableMethod(this, rel, Relation.class)) {
            return;
        }
    }

    public void visit(final Linkkey linkkey) throws AlignmentException {
        for (LinkkeyBinding linkkeyBinding : linkkey.bindings()) {
            linkkeyBinding.accept(this);
        }
    }

    protected void resetS1(String obj) {
        if(fromOnto1ToOnto2){
            resetVariables("?s1", obj); 
        }else{
            resetVariables("?s2", obj);
        }
    }

    protected void resetS2(String obj) {
        if(fromOnto1ToOnto2){
            resetVariables("?s2", obj); 
        }else{
            resetVariables("?s1", obj);
        }
    }
    
    /**
     * Where each element must be equal
     *
     * @param linkkeyEquals
     * @throws AlignmentException
     */
    public void visit(final LinkkeyEquals linkkeyEquals) throws AlignmentException {
        //Main part for selection
        resetS1("?o1");
        Expression expr1 = linkkeyEquals.getExpression1();
        expr1.accept(this);
        resetS2("?o1");
        Expression expr2 = linkkeyEquals.getExpression2();
        expr2.accept(this);
        //First part 
        addToGP("MINUS { " + NL);
        resetS1("?o1");
        expr1.accept(this);
        resetS1("?o2");
        expr1.accept(this);
        resetS2("?o1");
        expr2.accept(this);
        addToGP("FILTER(?s1 != ?s2 && ?o2 != ?o1 && NOT EXISTS {" + NL);
        resetS2("?o2");
        expr2.accept(this);
        addToGP("}) " + NL);
        addToGP("} " + NL);
        //Second part
        addToGP("MINUS {" + NL);
        resetS1("?o1");
        expr1.accept(this);
        resetS2("?o1");
        expr2.accept(this);
        resetS2("?o2");
        expr2.accept(this);
        addToGP("FILTER(?s1 != ?s2 && ?o1 != ?o2 && NOT EXISTS {" + NL);
        resetS1("?o2");
        expr1.accept(this);
        addToGP("}) " + NL);
        addToGP("} " + NL);
    }

    /**
     * Where we must have at least one element equal between each source.
     *
     * @param linkkeyIntersects
     * @throws AlignmentException
     */
    public void visit(final LinkkeyIntersects linkkeyIntersects) throws AlignmentException {
        resetS1("?o1");
        Expression expr1 = linkkeyIntersects.getExpression1();
        expr1.accept(this);
        resetS2("?o1");
        Expression expr2 = linkkeyIntersects.getExpression2();
        expr2.accept(this);
    }

    protected void generateSelect(Cell cell, Expression expr1, Expression expr2, boolean from1To2) throws AlignmentException {
        // Here the generation is dependent on global variables
        List<String> listGP = new LinkedList<String>();
        blanks = false;
        fromOnto1ToOnto2 = from1To2;
//         :-( should find something better !!
        if (from1To2) {
            resetVariables(expr1, "s1", "o");
        } else {
            resetVariables(expr1, "s2", "o");
        }
        expr1.accept(this);
        listGP.add(getGP());
        if (from1To2) {
            resetVariables(expr2, "s2", "o");
        } else {
            resetVariables(expr2, "s1", "o");
        }
        expr2.accept(this);
        listGP.add(getGP());
        initStructure();
        String filter = "FILTER(?s1 != ?s2)";
        Set<Linkkey> linkkeys = ((EDOALCell) cell).linkkeys();
        if (linkkeys != null) {
            for (Linkkey linkkey : linkkeys) {
                linkkey.accept(this);
            }
            listGP.add(getGP());
        }      
        String query = createSelect(listGP, filter);
        if (corese) {
            throw new AlignmentException("corese case NOT IMPLEMENTED for SPARQLSelectRendererVisitor !!");
        }
        if (corese) {
            return;
        }
        saveQuery(cell, query);
    }

    protected String createSelect(List<String> listGP, String filter) {
        StringBuilder mainGPBuilder = new StringBuilder();
        for (String GP : listGP) {
            mainGPBuilder.append(GP);
        }
        return createPrefixList() + "SELECT DISTINCT ?s1 ?s2 " + NL + "WHERE {" + NL + mainGPBuilder + filter + NL + "}" + NL;
    }

}
