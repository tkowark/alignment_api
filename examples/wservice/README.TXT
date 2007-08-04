Examples for AlignmentServer web service communication.

# $Id$

This directory contains an example of connecting to the
Alignment Server through web services.

-------------------------------------
CLIENT INVOKATION

java AlignmentClient keyword [genoptions] [options]
genoptions:
	-d [num]	 debug level
	-h 		 hostname
	-q		 quiet

java AlignmentClient wsdl			-> XML
     => Find the WSDL file from the path
java AlignmentClient find URI URI		-> AURI
java AlignmentClient match [method] URI URI	-> AURI
     -> CleanUp reception (err messages)
java AlignmentClient trim [method] AURI float	-> AURI
java AlignmentClient invert AURI		-> AURI
java AlignmentClient load URI | <stdin>		-> AURI
     => finir la partie stdin
java AlignmentClient store AURI 		-> AURI
java AlignmentClient retrieve AURI method	-> XML
       => embed a <?xml ...>
       => not always XML
//-java AlignmentClient metadata AURI key	-> XML

java AlignmentClient list alignments		-> XML
java AlignmentClient list method		-> XML
java AlignmentClient list renderers		-> XML
java AlignmentClient list services		-> XML

----------------------------------
USE

$ javac -cp .:../../lib/procalign.jar AlignmentClient.java

$ java -cp .:../../lib/procalign.jar AlignmentClient match file:///Java/alignapi/html/tutorial/myOnto.owl file:///Java/alignapi/html/tutorial/myOnto.owl
<SOAP-ENV:Envelope   xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'   xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance'   xmlns:xsd='http://www.w3.org/1999/XMLSchema'>  <SOAP-ENV:Body><id>1</id><sender>http://localhost:8089</sender><receiver>LocalHTMLInterface</receiver><in-reply-to>fr.inrialpes.exmo.align.service.Message@dd3d1b</in-reply-to><content>http://localhost:8089/alid/1186044903591/898</content>  </SOAP-ENV:Body></SOAP-ENV:Envelope>


