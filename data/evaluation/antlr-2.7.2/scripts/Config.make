###############################################################################
# $Id: //depot/code/org.antlr/dev/klaren.dev/Config.make.in $
###############################################################################

.EXPORT_ALL_VARIABLES:

# For package installers..
DESTDIR     =

# usual dribble
prefix      = /usr/local
exec_prefix = ${prefix}
datadir     = ${prefix}/share
libdir      = ${exec_prefix}/lib
includedir  = ${prefix}/include
bindir      = ${exec_prefix}/bin

# Version stuff...
VERSION         = 2
SUBVERSION      = 7
PATCHLEVEL      = 2
PACKAGE_NAME    = antlr
PACKAGE_VERSION = 2.7.2
versioneddir    = $(PACKAGE_NAME)-$(PACKAGE_VERSION)

# Programs we might use and their options...
ANTLR    = $(JAVA) -cp $(TOPDIR)/antlrall.jar antlr.Tool
# antlr is used to build antlr
ANTLR_BOOTSTRAP = $(JAVA) -cp /antlrall.jar antlr.Tool

CXX = g++
   CXXFLAGS = -g -O2 -felide-constructors -W -Wall -pipe $(ARCHFLAGS) $(INCLUDE) $(DEBUG) $(EXTRA_CXXFLAGS)
   EXTRA_CXXFLAGS =

CC = gcc
   CFLAGS       = -g -O2 -W -Wall -pipe
   DEBUG        = -g -D__DEBUG__
   EXTRA_CFLAGS =
   INCLUDE      =
   LDFLAGS      = 

CPP = gcc -E
   CPPFLAGS = 

CXXCPP = g++ -E
AR     = /usr/bin/ar
AS     = /usr/bin/as
LD     = /usr/bin/ld
RANLIB = ranlib
STRIP  = /usr/bin/strip

DOXYGEN  = doxygen
   DOXYGEN_FLAGS  =

JAVA     = java
   JAVA_FLAGS  = 
   JAVA_PREFIX    = 

JAVAC    = javac
   JAVAC_FLAGS = 

JAR      = jar

INSTALL  = /usr/local/bin/ginstall -c
MAKEDEP  = makedepend 
MKDIR    = /bin/mkdir
NM       = /usr/bin/nm
RM       = /bin/rm
TAR      = /usr/bin/tar
TCL      = /usr/bin/tclsh
TOUCH    = /usr/bin/touch

#
# From configure enabled features...
#
WITH_EXAMPLES  = no

#
# These are used for the examples
#
ANTLR_INCS = -I$(TOPDIR)/lib/cpp
ANTLR_LIBS = -L$(TOPDIR)/lib/cpp/src -lantlr

DMALLOC_LIBS = -L/local/klaren/lib -ldmalloc
DMALLOC_INC  = -I/local/klaren/include
