/*
 * $Id$
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import org.semanticweb.owl.align.Parameters;

import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;

import fr.inrialpes.exmo.align.service.StoreRDFFormat;
import fr.inrialpes.exmo.align.service.DBService;

public class AServProtocolManager {

    Hashtable renderers = null; // language -> class
    Hashtable methods = null; // name -> class
    Hashtable aligned = null; // ontology -> surrogateList
    Hashtable alignments = null; // surrogate -> alignment
    Hashtable services = null; // name -> service

    public AServProtocolManager () {
	renderers = new Hashtable();
	methods = new Hashtable();
	aligned = new Hashtable();
	alignments = new Hashtable();
	services = new Hashtable();
    }

    public void init( Parameters p ) {
	// Read all these parameters from the database
	methods.put("Name equality","fr.inrialpes.exmo.align.impl.method.NameEqAlignment");
	methods.put("SMOA","fr.inrialpes.exmo.align.impl.method.SMOANameAlignment");
	methods.put("String distance","fr.inrialpes.exmo.align.impl.method.StringDistAlignment");
	renderers.put("COWL","fr.inrialpes.exmo.align.impl.renderer.COWLMappingRendererVisitor");
	renderers.put("HTML","fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor");
	renderers.put("OWL","fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor");
	renderers.put("RDF/XML","fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor");
	renderers.put("SEKT/OWMG","fr.inrialpes.exmo.align.impl.renderer.SEKTMappingRendererVisitor");
	renderers.put("SKOS","fr.inrialpes.exmo.align.impl.renderer.SKOSRendererVisitor");
	renderers.put("SWRL","fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor");
	renderers.put("XSLT","fr.inrialpes.exmo.align.impl.renderer.XSLTRendererVisitor");
    }

    public void close() {
    }

    // ==================================================
    // Protocol primitives
    // ==================================================

    public Set listmethods (){
	Set result = new HashSet();
	for (Enumeration e = methods.elements() ; e.hasMoreElements() ;) {
	    result.add(e.nextElement());
	}
	return result;
    }

    public Set listrenderers(){
	Set result = new HashSet();
	for (Enumeration e = renderers.elements() ; e.hasMoreElements() ;) {
	    result.add(e.nextElement());
	}
	return result;
    }

    public Message align(Message mess){

    //\prul{align-success}
    //{a - request ( align (O, O', n, P)) \rightarrow S}
	//{\langle O, O', A\rangle \Leftarrow Retrieve(n)\\A'\Leftarrow Align (O,O',A,P)\\ n'\Leftarrow Store(O, O', A')\\ S - inform (n') \rightarrow a}
	//{\begin{matrix}\neg Find(O,O',n,P) \wedge\\ reachable(O)\wedge reachable(O')\wedge\\ conform(P) \wedge Retrieve(n)\not=\emptyset\end{matrix}}

//\prul{align-library}
//{a - request ( align (O, O', n, P)) \rightarrow S}
    //{n' \Leftarrow Find(O,O',n,O)\\ S - inform (n') \rightarrow a}
    //{Find(O,O',n,O)\not= \emptyset}

    //\prul{align-unreachable}{a - request ( align (O, O', n, P)) \rightarrow S}{S - failure ( unreachable  ( O ) ) \rightarrow a}{\neg reachable(O)}

    //\prul{align-unreachable}{a - request ( align (O, O', n, P)) \rightarrow S}{S - failure ( unreachable  ( O' ) ) \rightarrow a}{\neg reachable(O')}

    //\prul{align-unknown}{a - request ( align (O, O', n, P)) \rightarrow S}{ S -  failure (unknown(n)) \rightarrow a }{\neg Retrieve(n)}

    //\prul{align-nonconform}{a - request ( align (O, O', n, P)) \rightarrow S}{S - failure ( nonconform  ( P ) ) \rightarrow a}{\neg conform(P)}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message find(Message mess){

    //\prul{search-success}{a - request ( find (O, T) ) \rightarrow S}{O' \Leftarrow Match(O,T)\\S - inform (O') \rightarrow a}{reachable(O)\wedge Match(O,T)\not=\emptyset}

    //\prul{search-void}{a - request ( find (O, T) ) \rightarrow S}{S - failure (nomatch) \rightarrow a}{reachable(O)\wedge Match(O,T)=\emptyset}

    //\prul{search-unreachable}{a - request ( find (O, T) ) \rightarrow S}{S - failure ( unreachable (O) ) \rightarrow a}{\neg reachable(O)}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message isAligned(Message mess){
	String o1 = null;
	String o2 = null;
	
	if ( !reachable( o1 ) ){
	    return new Message(mess.getId(),mess,mess.getSender(),"unreachable//"+o1,(Parameters)null);
	} else if ( !reachable( o2 ) ){
	    return new Message(mess.getId(),mess,mess.getSender(),"unreachable//"+o2,(Parameters)null);
	} else {
	    //List als = getAligned( o1, o2 );
	    //if ( als != null ){
	    //	return new Message(mess.getId(),mess,mess.getSender(),"true",(Parameters)null);
	    //} else {
		return new Message(mess.getId(),mess,mess.getSender(),"nomatch",(Parameters)null);
		//}
	}

    //\prul{query-align-unreachable}{a - query-if ( is-align ( O, O' )) \rightarrow S}{S -  failure ( unreachable(O') ) \rightarrow a}{\neg reachable(O')}
    }

    public Message store(Message mess){

    //\prul{store-alignment}
    //{a - request ( store (O, O', A)) \rightarrow S}
    //{n\Leftarrow Store(O, O', A)\\ S - inform (n) \rightarrow a}
    //{}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message replywith(Message mess){

    //\prul{redirect}{a - request ( q(x)~reply-with:~i) \rightarrow S}{
    //Q \Leftarrow Q\cup\{\langle a, i, !i', q(x), S'\rangle\}\		\
    //S - request( q( R(x) )~reply-with:~i')\rightarrow S'}{S'\in C(q)}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message replyto(Message mess){

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, \neg surr(y)}

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\	\
    //R \Leftarrow R\cup\{\langle a, !y', y, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, surr(y)}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message failure(Message mess){

    //\prul{failure-return}{S' - failure ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - failure( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message translate(Message mess){

//\prul{translate-success}{a - request ( translate ( M, n)) \rightarrow S}{\langle O, O', A\rangle \Leftarrow Retrieve(n)\\m'\Leftarrow Translate(m,A)\\S - inform ( m' ) \rightarrow a}{Retrieve(n)\not=\emptyset}

//\prul{translate-unknown}{a - request ( translate ( M, n)) \rightarrow S}{S - failure ( unknown (n) )  \rightarrow a}{Retrieve(n)=\emptyset}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public Message render(Message mess){

    //\prul{get-processor-success}{a - request ( render ( n, l )) \rightarrow S}{\langle O, O', A\rangle \Leftarrow Retrieve(n)\\P\Leftarrow Render(A,l)\\S - inform ( P~language:~l ) \rightarrow a}{l\in L\wedge Retrieve(n)\not=\emptyset}

    //\prul{get-processor-unknown}{a - request ( render ( n, l )) \rightarrow S}{S - failure ( unknown (n) ) \rightarrow a}{Retrieve(n)=\emptyset}

    //\prul{get-processor-failure}{a - request ( render ( n, l )) \rightarrow S}{S -  failure ( unsupported ( l )) \rightarrow a}{l\not\in L}
	return new Message(mess.getId(),mess,mess.getSender(),"dummy//",(Parameters)null);
    }

    public boolean reachable( String ontology ){
	return true;
    }
    /*
    public List getAligned( String o1, String o2 ){
	List a = (List)aligned.get( o1 );
	// for all the aligned of o1
	// if there are some in o2
	// then get it
	return a;
	}*/

}
