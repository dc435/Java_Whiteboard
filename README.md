# COMP90015-Whiteboard
COMP90015, Assignment 2, Sem 2, 2022. D Curran, Y Peng.

The code can compiled by running the pom.xml file with Maven. It produces 2 jar files with all dependencies, called:
client-jar-with-dependencies.jar
server-jar-with-dependencies.jar

The client GUI can be run using the following command:
>> java -cp client-jar-with-dependencies.jar WBClient [portNo.]
The [PortNo] is an optional command line option for the local port number on which this client will be listening. Default client port is 3210.

The server can be run using the following command:
>> java -cp server-jar-with-dependencies.jar WBServer [portNo.]
The [PortNo] is an optional command line option for the local port number on which the server will be listening. Default server port is 3200.
