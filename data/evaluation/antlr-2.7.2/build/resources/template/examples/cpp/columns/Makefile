###############################################################################
# $Id: $
###############################################################################

G_FILES := column.g
column_g_FILES := L.cpp \
	L.hpp \
	P.cpp \
	P.hpp \
	PTokenTypes.hpp \
	PTokenTypes.txt

B_NAME := columns
B_OBJS := L.o  P.o 

C_TARGETS := $(column_g_FILES)

ifndef TOPDIR
TOPDIR  = $(shell pwd)/../../..
endif

include $(TOPDIR)/scripts/Config.make
include $(TOPDIR)/scripts/Rules.make

# Antlr deps.
EXTRA_LIBS := $(ANTLR_LIBS)
EXTRA_CXXFLAGS := $(ANTLR_INCS)

# DO NOT DELETE

L.o: L.hpp PTokenTypes.hpp
P.o: P.hpp PTokenTypes.hpp L.hpp
