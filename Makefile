JFLAGS=-g	#generate all debugging informations including variables
JC=javac
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES=ChessMain.java

default: build

build: classes permissions

classes: $(CLASSES:.java=.class)

permissions:
	chmod 777 *.class

clean:
	$(RM) *.class
