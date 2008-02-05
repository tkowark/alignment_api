#!/bin/sh
# Example of embeding alignment in a simple file
# $Id$

javac -cp .:../../lib/procalign.jar MyAlign.java
java -cp .:../../lib/procalign.jar MyAlign
