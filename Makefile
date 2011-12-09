#***************************************************************************
#
# This section describes the current package.
#
# o PACKAGE     - The complete package name. 
# o PACKAGE_LOC - Same as PACKAGE but with "/"s instead of "."s.
# o SOURCE      - List of the source files. Remember extension.
# o JNI_SOURCE  - Files from SOURCE that are to be built with the JAVAH 
#                 compiler.
# o JAR_EXTRAS  - None-class files and directories that are to be bundled
#                 into the jar archive.
#
#***************************************************************************

PACKAGE     = xtc.oop
PACKAGE_LOC = xtc/oop

SOURCE = \
	Translator.java \
	ClassLayoutParser.java \
	LeafTransplant.java \
	CPPPrinter.java \
	DependencyResolver.java \
        TranslatorSymbolTable.java \
	MavisBeacon.java


JNI_SOURCE =

JAR_EXTRAS = 

#***************************************************************************
#
# Include common part of makefile
#
#***************************************************************************

ifdef JAVA_DEV_ROOT
include $(JAVA_DEV_ROOT)/Makerules
endif