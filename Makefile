FLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
        GrepProtocol.java \
        TestGrepProtocol.java \
		TestClientGrep.java \
		GrepServer.java \
		GrepClient.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
