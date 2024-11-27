#include <iostream>
#include "T.hpp"

int main()
{
	ANTLR_USING_NAMESPACE(std)
	ANTLR_USING_NAMESPACE(antlr)

	try
	{
		T lexer(cin);

		for(;;)
		{
			RefToken t = lexer.nextToken();

			if ( t->getType() == Token::EOF_TYPE )
				break;

			cout << "Token: " << t->toString() << endl;
		}
		cout << "done lexing..." << endl;
	}
	catch(exception& e)
	{
		cerr << "exception: " << e.what() << endl;
	}
}
