#include <iostream>
#include "LangLexer.hpp"
#include "LangParser.hpp"
#include "LangWalker.hpp"

int main()
{
	ANTLR_USING_NAMESPACE(std)
	ANTLR_USING_NAMESPACE(antlr)
	try {
		LangLexer lexer(cin);
		LangParser parser(lexer);

		ASTFactory ast_factory;
		parser.initializeASTFactory(ast_factory);
		parser.setASTFactory(&ast_factory);

		parser.block();
//		antlr.CommonAST a = (antlr.CommonAST)parser.getAST();
		RefAST a = parser.getAST();
		cout << a->toStringList() << endl;
		LangWalker walker;
		
		walker.initializeASTFactory(ast_factory);
		walker.setASTFactory(&ast_factory);

		walker.block(a);	// walk tree
		cout << "done walking" << endl;
	} catch(exception& e) {
		cout << "exception: "<< e.what() << endl;
	}
}

