Examples for AlignmentServer web service communication.

# $Id$

This directory contains an example of connecting to the Alignment Server through web services.
It is rudimentary and minimal... but works.

----------------------------------
USE

$ javac Client

$ java Client
Send to http://localhost:8089/aserv :: align
==>
<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance' xmlns:xsd='http://www.w3.org/1999/XMLSchema'> <SOAP-ENV:Body><url1>http://alignapi.gforge.inria.fr/tutorial/myOnto.owl</url1><url2>http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl</url2><method>fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment</method><force>false</force></SOAP-ENV:Body> </SOAP-ENV:Envelope>

Receive ==>
<SOAP-ENV:Envelope   xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'   xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance'   xmlns:xsd='http://www.w3.org/1999/XMLSchema'>  <SOAP-ENV:Body><id>0</id><sender>http://localhost:8089</sender><receiver>LocalHTMLInterface</receiver><in-reply-to>fr.inrialpes.exmo.align.service.Message@ce3388</in-reply-to><content>http://localhost:8089/alid/1181763377472/560</content>  </SOAP-ENV:Body></SOAP-ENV:Envelope>

------------------------------------
FILES

SOAPClient4XG.java
	-> a minimal SOAP client for sending messages
soap-example.xml
	-> a sample Soap message for the previous client
Client.java
	-> a more adapted Client to the AlignmentServer
Sympa.wsdl

