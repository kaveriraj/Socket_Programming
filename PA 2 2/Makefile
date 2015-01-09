JAVAC =  javac
all : server.class client.class packet.class
.PHONY : all
server.class : server.java
		$(JAVAC) server.java
client.class : client.java
		$(JAVAC) client.java 
packet.class : packet.java
		$(JAVAC) packet.java
clean :
	-\rm *.class


