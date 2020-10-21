/**
 * Parser for the class project in COP5556 Programming Language Principles 
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


import static cop5556fa20.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.ExprBinary;
import cop5556fa20.AST.Dec;
import cop5556fa20.AST.DecImage;
import cop5556fa20.AST.DecVar;
import cop5556fa20.AST.ExprArg;
import cop5556fa20.AST.ExprConditional;
import cop5556fa20.AST.ExprConst;
import cop5556fa20.AST.ExprEmpty;
import cop5556fa20.AST.ExprHash;
import cop5556fa20.AST.ExprIntLit;
import cop5556fa20.AST.ExprPixelConstructor;
import cop5556fa20.AST.ExprPixelSelector;
import cop5556fa20.AST.ExprStringLit;
import cop5556fa20.AST.ExprVar;
import cop5556fa20.AST.Expression;
import cop5556fa20.AST.ExprUnary;
import cop5556fa20.AST.Program;
import cop5556fa20.AST.Statement;
import cop5556fa20.AST.StatementAssign;
import cop5556fa20.AST.StatementImageIn;
import cop5556fa20.AST.StatementLoop;
import cop5556fa20.AST.StatementOutFile;
import cop5556fa20.AST.StatementOutScreen;
import cop5556fa20.AST.Type;
import cop5556fa20.Scanner.Kind;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import cop5556fa20.SimpleParser.SyntaxException;

public class Parser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;  //the token that caused an error to be discovered.

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken(); // establish invariant that t is always the next token to be processed
	}

	public Program parse() throws SyntaxException, LexicalException {
		Program p = program();
		matchEOF();
		return p;
	}

	private static final Kind[] firstProgram = { KW_int, KW_string, KW_image, KW_boolean, KW_void, IDENT}; //this is not the correct FIRST(Program...), but illustrates a handy programming technique
	
	private Program program() throws SyntaxException, LexicalException {
		
		Token first = t; //always save the current token.  
		List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		while (isKind(firstProgram)) {
			switch (t.kind()) {
				case KW_int, KW_string, KW_image, KW_boolean, KW_void -> {
					decsAndStatements.add(declaration());
				}
				
				case IDENT -> {
					decsAndStatements.add(statement());
				}
				//Your finished parser should NEVER throw UnsupportedOperationException, but it is convenient as a placeholder for unimplemented features.
				default -> throw new UnsupportedOperationException("unimplemented feature in program"); 
			}
			
			match(SEMI);

		}
		return new Program(first, decsAndStatements);  //return a Program object
	}
	
	Dec declaration() throws SyntaxException, LexicalException {
		Dec dec = null;
		switch(t.kind()){
		
			case KW_int, KW_string, KW_boolean, KW_void -> { 
				dec = variableDecl();
			}

			case KW_image -> {
				dec = imageDecl();
			}
			
			default ->{
				throw new SyntaxException(t,"Encountered unexpected token");

			}
		}
		
		return dec;
	}
	
	DecVar variableDecl() throws SyntaxException, LexicalException {
		Token first = t;
		Type type = Type.getType(t);
		consume();
		String name = scanner.getText(t);
		match(IDENT);
		Expression e = ExprEmpty.empty;
		if(isKind(ASSIGN)){
			consume();
			e = expression();
		}else if(isKind(SEMI)){
		}else {
			throw new SyntaxException(t,"Syntax Error: should have an assign or semi after type");
		}
		return new DecVar(first, type, name, e);
	}
	
	DecImage imageDecl() throws SyntaxException, LexicalException {
		Token first = t;
		Type type = Type.getType(t);
		Kind op = Kind.NOP;
		Expression e = ExprEmpty.empty;
		consume();
		Expression xSize = null, ySize = null;
		if(isKind(LSQUARE)){
			consume();
			xSize = expression();
			if(isKind(COMMA)){
				consume();
				ySize = expression();
				match(RSQUARE);
			}else{
				throw new SyntaxException(t,"Encountered unexpected token");
			}
		}
		String name = scanner.getText(t);
		match(IDENT);
		if(isKind(Kind.LARROW) || isKind(Kind.ASSIGN)){
			op = t.kind();
			consume();
			e = expression();
		}
		return new DecImage(first, type, name, xSize, ySize, op, e);
	}
	
	Expression expression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = orExpression();
		if(isKind(Kind.Q)) {
			consume();
			Expression e1 = expression();
			Expression e2 = ExprEmpty.empty;
			match(Kind.COLON);
			e2 = expression();
			e0 = new ExprConditional(first, e0, e1, e2);
		}
		
		return e0;
	}
	
	Expression orExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Kind op = Kind.NOP;
		Expression e0 = andExpression(), e1;
		while(isKind(Kind.OR)){
			op = t.kind();
			consume();
			e1 = andExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Expression andExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Kind op = Kind.NOP;
		Expression e0 = eqExpression(), e1;
		while(isKind(Kind.AND)){
			op = t.kind();
			consume();
			e1 = eqExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Expression eqExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Kind op = Kind.NOP;
		Expression e0 = relExpression(), e1;
		while(isKind(Kind.EQ) || isKind(Kind.NEQ)){
			op = t.kind();
			consume();
			e1 = relExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Expression relExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Kind op = Kind.NOP;
		Expression e0 = addExpression(), e1;
		while(isKind(Kind.LT) || isKind(Kind.GT) || isKind(Kind.LE) || isKind(Kind.GE)){
			op = t.kind();
			consume();
			e1 = addExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	Expression addExpression() throws SyntaxException, LexicalException {
		Token firstToken = t;
		Kind op = Kind.NOP;
		Expression e0 = multExpression(), e1;
		while(isKind(Kind.PLUS) || isKind(Kind.MINUS)){
			op = t.kind();
			consume();
			e1 = multExpression();
			e0 = new ExprBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression multExpression() throws SyntaxException, LexicalException {
		Token firstToken = t;
		Kind op = Kind.NOP;
		Expression e0 = unaryExpression(), e1;
		while(isKind(Kind.STAR) || isKind(Kind.DIV) || isKind(Kind.MOD)){
			op = t.kind();
			consume();
			e1 = unaryExpression();
			e0 = new ExprBinary(firstToken, e0, op, e1);
		}
		return e0;
	}
	
	Expression unaryExpression() throws SyntaxException, LexicalException {
		Token firstToken = t;
		Kind op = Kind.NOP;
		Expression e0 = null;
		if(isKind(Kind.PLUS) || isKind(Kind.MINUS)) {
			op = t.kind();
			consume();
			Expression e = unaryExpression();
			e0 = new ExprUnary(firstToken, op, e);
		}else {
			e0 = unaryExpressionNotPlusMinus();
		}
		return e0;
	}
	
	Expression unaryExpressionNotPlusMinus() throws SyntaxException, LexicalException {
		Token firstToken = t;
		Kind op = Kind.NOP;
		Expression e0 = ExprEmpty.empty;
		if(isKind(Kind.EXCL)) {
			op = t.kind();
			consume();
			Expression e = unaryExpression();
			e0 = new ExprUnary(firstToken, op, e);
		}else {
			e0 = hashExpression();
		}
		return e0;
	}
	
	Expression hashExpression() throws SyntaxException, LexicalException {
		Token firstToken = t;
		String attr = null;
		Expression e0 = primary();
		while(isKind(Kind.HASH)) {
			consume();
			attr = scanner.getText(t);
			e0 = new ExprHash(firstToken, e0, attr);;
		}
		return e0;
	}
	
	
	
	Expression primary() throws SyntaxException, LexicalException {
		Expression primExpr = null;
		Token firstToken = t;
		switch(t.kind()) {

			case INTLIT-> {
				primExpr = new ExprIntLit(firstToken, scanner.intVal(t));
				consume();
			}
			
			case IDENT-> {
				primExpr = new ExprVar(firstToken, scanner.getText(t));
				consume();
			}
			
			case STRINGLIT-> {
				primExpr = new ExprStringLit(firstToken, scanner.getText(t));
				consume();
			}
			
			case KW_X, KW_Y -> {
				primExpr = new ExprVar(firstToken, scanner.getText(t));
				consume();
			}
			
			case CONST-> {
				primExpr = new ExprConst(firstToken, scanner.getText(t), scanner.intVal(t));
				consume();
			}
			
			case LPAREN  -> {
				consume();
				primExpr = expression();
				match(Kind.RPAREN);
			}

			case LPIXEL  -> {
				Expression e2 = ExprEmpty.empty;
				Expression e0 = ExprEmpty.empty;
				Expression e1 = ExprEmpty.empty;
				consume();
				e0 = expression();
				match(Kind.COMMA);
				e1 =expression();
				match(Kind.COMMA);
				e2 =expression();
				match(Kind.RPIXEL);
				primExpr = new ExprPixelConstructor(firstToken, e0, e1, e2);
			}

			case AT -> {
				Expression e0 = ExprEmpty.empty;
				consume();
				e0 = primary();
				primExpr = new ExprArg(firstToken, e0);
				
			}default->{
				throw new SyntaxException(t,"Syntax Error: input for primary is not illegl ");
			}
		}
		
		if(isKind(Kind.LSQUARE)) {
			Expression e0 = ExprEmpty.empty;
			Expression e1 = ExprEmpty.empty;
			consume();
			e0 = expression();
			match(Kind.COMMA);
			e1 = expression();
			match(Kind.RSQUARE);
			primExpr = new ExprPixelSelector(firstToken, primExpr, e0, e1);
		}
		
		return primExpr;
	}

	Statement statement() throws SyntaxException, LexicalException {
		Statement stmt = null;
		Token firstToken = t;
		String name = scanner.getText(t);
		match(IDENT);
		switch(t.kind()) {
			case ASSIGN -> {
				Expression e = ExprEmpty.empty;
				Expression e0 = ExprEmpty.empty;
				Expression e1 = ExprEmpty.empty;
				consume();
				if(isKind(Kind.STAR)) {
					consume();
					constXYSelector();
					match(Kind.COLON);
					if(isKind(Kind.COLON)) {
						consume();
						e1 = expression();
					}else {
						e0 = expression();
						match(Kind.COLON);
						e1 = expression();
					}
					stmt = new StatementLoop(firstToken, name, e0, e1);
				}else {
					e = expression();
					stmt = new StatementAssign(firstToken, name, e);
				}
			}
			case LARROW ->{
				Expression e = ExprEmpty.empty;
				consume();
				e = expression();
				stmt = new StatementImageIn(firstToken, name, e);
			}
			case RARROW ->{
				Expression e = ExprEmpty.empty;
				Expression e0 = ExprEmpty.empty;
				Expression e1 = ExprEmpty.empty;
				consume();
				if(isKind(Kind.KW_SCREEN)) {
					consume();
					if(isKind(Kind.LSQUARE)) {
						consume();
						e0 = expression();
						match(Kind.COMMA);
						e1 = expression();
						match(Kind.RSQUARE);
					}
					stmt = new StatementOutScreen(firstToken, name, e0, e1);
				}else {
					e = expression();
					stmt = new StatementOutFile(firstToken, name, e);
				}
			}
			default ->{
				throw new SyntaxException(t,"errr");
			}
		}
		return stmt;
	}
	
	public void constXYSelector() throws SyntaxException, LexicalException {
			match(Kind.LSQUARE);
			match(Kind.KW_X);
			match(Kind.COMMA);
			match(Kind.KW_Y);
			match(Kind.RSQUARE);
	}



	protected boolean isKind(Kind kind) {
		return t.kind() == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind())
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(t, kind.toString());
		return null; // unreachable
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		error(t, "expected one of " + kinds);
		return null; // unreachable
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			error(t, "attempting to consume EOF");
		}
		t = scanner.nextToken();
		return tmp;
	}

	private void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t, message);
	}
	
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so there is no
	 * attempt to get the nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		error(t, EOF.toString());
		return null; // unreachable
	}
}
