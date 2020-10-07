/**
 * Class for  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2020.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2020 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2020
 *
 */

package cop5556fa20;

import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import cop5556fa20.Scanner.Kind;

public class SimpleParser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}


	final Scanner scanner;
	Token tok;


	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		tok = scanner.nextToken();
	}

	public void parse() throws SyntaxException, LexicalException {
		program();
		if (!consumedAll()) throw new SyntaxException(scanner.nextToken(), "tokens remain after parsing");
			//If consumedAll returns false, then there is at least one
		    //token left (the EOF token) so the call to nextToken is safe. 
	}
	

	public boolean consumedAll() {
		if (scanner.hasTokens()) { 
			Token t = scanner.nextToken();
			if (t.kind() != Scanner.Kind.EOF) return false;
		}
		return true;
	}


	private void program() throws SyntaxException, LexicalException {
		Kind kind = tok.kind();
		switch(kind) {
			case IDENT  -> {
				consume();
				statement();
			}
			
			case KW_int, KW_string ->{
				consume();
				
				match(Kind.IDENT, "no INDET was found after type");
				
				if(checkKind(Kind.SEMI)) {
					consume();
				}else if (checkKind(Kind.ASSIGN)) {
					expression();
					if(checkKind(Kind.SEMI)) {
						consume();
					}else {
						throw new SyntaxException(tok,"Syntax Error: should have an semi after assign and expression");
					}
				}
			}
			
			case KW_image  ->{
				consume();
			}
			
			default ->{
				if(!consumedAll()) {
					throw new SyntaxException(tok,"Syntax Error: could not begin with ");

				}
			}
		}
		if(!consumedAll()) {
			program();
		}
	}


	public void expression() throws SyntaxException, LexicalException {
		orExpression();
		
		if(checkKind(Kind.Q)) {
			consume();
			expression();
			match(Kind.COLON,"Syntax Error: should contain : between two expression");
			expression();
		}
	}
	
	public void orExpression() throws SyntaxException, LexicalException {
		andExpression();
		
		while(checkKind(Kind.OR)) {
			consume();
			andExpression();
		}
	}
	
	public void andExpression() throws SyntaxException, LexicalException {
		eqExpression();
		
		while (checkKind(Kind.AND)) {
			consume();
			eqExpression();
		}
	}
	
	public void eqExpression() throws SyntaxException, LexicalException {
		relExpression();
		
		while (checkKind(Kind.EQ) || checkKind(Kind.NEQ)) {
			consume();
			relExpression();
		}
	}
	
	public void relExpression() throws SyntaxException, LexicalException {
		addExpression();
		
		while (checkKind(Kind.LT) || checkKind(Kind.GT) || checkKind(Kind.LE) || checkKind(Kind.GE)) {
			consume();
			addExpression();
		}
	}
	
	public void addExpression() throws SyntaxException, LexicalException {
		multExpression();
		
		while (checkKind(Kind.PLUS) || checkKind(Kind.MINUS)) {
			consume();
			multExpression();
		}
	}
	
	public void multExpression() throws SyntaxException, LexicalException {
		unaryExpression();
		
		while (checkKind(Kind.STAR) || checkKind(Kind.DIV) || checkKind(Kind.MOD)) {
			consume();
			unaryExpression();
		}
	}
	
	public void unaryExpression() throws SyntaxException, LexicalException {
		if(checkKind(Kind.PLUS) || checkKind(Kind.MINUS)) {
			consume();
			unaryExpression();
		}else {
			unaryExpressionNotPlusMinus();
		}
	}
	
	public void unaryExpressionNotPlusMinus() throws SyntaxException, LexicalException {
		if(checkKind(Kind.EXCL)) {
			unaryExpression();
		}else {
			hashExpression();
		}
	}
	
	public void hashExpression() throws SyntaxException, LexicalException {
		primary();
		while(checkKind(Kind.HASH)) {
			consume();
			attribute();
			hashExpression();
		}
		
	}
	
	public void primary() throws SyntaxException, LexicalException {		
		switch(tok.kind()) {
			case INTLIT, IDENT, STRINGLIT, KW_X, KW_Y, CONST-> {
				consume();
			}

			case LPAREN  -> {
				consume();
				expression();
				match(Kind.RPAREN,"miss right paren after liftparen");
			}

			case LPIXEL  -> {
				consume();
				expression();
				match(Kind.COMMA,"miss comma in PixelConstructor ");
				expression();
				match(Kind.COMMA,"miss comma in PixelConstructor ");
				expression();
				match(Kind.RPIXEL,"miss right pixel in PixelConstructor ");
			}
			case LSQUARE   -> {
				consume();
				expression();
				match(Kind.COMMA,"miss comma in Pixelselector ");
				expression();
				match(Kind.RSQUARE,"miss right pixel in Pixelselector ");
			}
			case AT   -> {
				consume();
				primary();
			}
		}
		if(checkKind(Kind.LSQUARE)) {
			consume();
			expression();
			match(Kind.COMMA,"miss comma in Pixelselector ");
			expression();
			match(Kind.RSQUARE,"miss RPIXEL in PixelConstructor ");
		}
	}
	
	public void attribute() throws SyntaxException, LexicalException {
		if(tok.kind() == Kind.KW_WIDTH || tok.kind() == Kind.KW_HEIGHT || tok.kind() == Kind.KW_RED || tok.kind() == Kind.KW_GREEN || tok.kind() == Kind.KW_BLUE) {
			consume();
		}else {
			throw new SyntaxException(tok,"token doesn't match attribute");
		}
	}
	
	public void statement() throws SyntaxException, LexicalException {
		switch(tok.kind()) {
			case ASSIGN -> {
				consume();
				if(checkKind(Kind.STAR)) {
					constXYSelector();
					while(checkKind(Kind.COLON)) {
						consume();
						if(checkKind(Kind.COLON)) {
							consume();
						}else {
							expression();
						}
					}
				}else {
					expression();
				}
			}
			case LARROW ->{
				consume();
				expression();
			}
			case RARROW ->{
				consume();
				if(checkKind(Kind.KW_SCREEN)) {
					consume();
					if(checkKind(Kind.LSQUARE)) {
						consume();
						expression();
						match(Kind.COMMA,"miss comma in image out statement ");
						expression();
						match(Kind.RSQUARE,"miss right square in image out statement ");
					}
				}else {
					expression();
				}
			}
		}
	}
	
	public void constXYSelector() throws SyntaxException, LexicalException {
		if(tok.kind() == Kind.LSQUARE  || tok.kind() == Kind.KW_X || tok.kind() == Kind.COMMA || tok.kind() == Kind.KW_Y  || tok.kind() == Kind.RSQUARE) {
			consume();
		}else {
			throw new SyntaxException(tok,"token doesn't match attribute");
		}
	}
	
	private boolean checkKind(Kind kind) {
		return tok.kind() == kind;
	}
	
	
	private void match(Kind kind, String err) throws SyntaxException {
		if (checkKind(kind)) {
			tok = scanner.nextToken();
		}
		throw new SyntaxException(tok, err);
	}
	
	private void consume() throws SyntaxException {
        tok = scanner.nextToken();
    }

   //TODO--everything else.  Have fun!!
}
