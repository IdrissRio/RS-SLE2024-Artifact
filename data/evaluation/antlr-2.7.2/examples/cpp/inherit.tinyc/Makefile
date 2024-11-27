###############################################################################
# $Id: $
###############################################################################

G_FILES := subc.g
subc_g_FILES := TinyCLexer.cpp TinyCLexer.hpp TinyCTokenTypes.hpp \
	TinyCTokenTypes.txt TinyCParser.hpp \
	MyCParser.cpp MyCParser.hpp \
	MyCParserTokenTypes.hpp MyCParserTokenTypes.txt \
	TinyCParserTokenTypes.txt
ANTLR_FLAGS := -glib ../tinyc/tinyc.g

B_NAME := tinyc2
B_OBJS := Main.o MyCParser.o

C_TARGETS := $(subc_g_FILES) TinyCParserTokenTypes.txt expandedsubc.g

ifndef TOPDIR
TOPDIR  = $(shell pwd)/../../..
endif

ifneq (TinyCParserTokenTypes.txt,$(wildcard TinyCParserTokenTypes.txt))
default: ;
	@echo "Verifying tinyc example is built"
	(cd ../tinyc && $(MAKE))
	cp ../tinyc/TinyCParserTokenTypes.txt .
	$(MAKE)
endif

include $(TOPDIR)/scripts/Config.make
include $(TOPDIR)/scripts/Rules.make

# Antlr deps.
EXTRA_LIBS := ../tinyc/TinyCLexer.o $(ANTLR_LIBS)
EXTRA_CXXFLAGS := $(ANTLR_INCS) -I../tinyc

Main.o: MyCParser.hpp MyCParserTokenTypes.hpp
MyCParser.o: MyCParser.hpp MyCParserTokenTypes.hpp

# DO NOT DELETE

Main.o: MyCParser.hpp MyCParserTokenTypes.hpp
MyCParser.o: MyCParser.hpp MyCParserTokenTypes.hpp
