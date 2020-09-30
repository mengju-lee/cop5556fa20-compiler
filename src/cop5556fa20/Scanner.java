/**
 * Scanner for the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;

public class Scanner {
	
	@SuppressWarnings("preview")
	public record Token(
		Kind kind,
		int pos, //position in char array.  Starts at zero
		int length, //number of chars in token
		int line, //line number of token in source.  Starts at 1
		int posInLine //position in line of source.  Starts at 1
		) {
	}
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		int pos;
		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		public int pos() { return pos; }
	}
	
	
	public static enum Kind {
		IDENT, INTLIT, STRINGLIT, CONST,
		KW_X/* X */,  KW_Y/* Y */, KW_WIDTH/* width */,KW_HEIGHT/* height */, 
		KW_SCREEN/* screen */, KW_SCREEN_WIDTH /* screen_width */, KW_SCREEN_HEIGHT /*screen_height */,
		KW_image/* image */, KW_int/* int */, KW_string /* string */,
		KW_RED /* red */,  KW_GREEN /* green */, KW_BLUE /* blue */,
		ASSIGN/* = */, GT/* > */, LT/* < */, 
		EXCL/* ! */, Q/* ? */, COLON/* : */, EQ/* == */, NEQ/* != */, GE/* >= */, LE/* <= */, 
		AND/* & */, OR/* | */, PLUS/* + */, MINUS/* - */, STAR/* * */, DIV/* / */, MOD/* % */, 
	    AT/* @ */, HASH /* # */, RARROW/* -> */, LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, LPIXEL /* << */, RPIXEL /* >> */,  SEMI/* ; */, COMMA/* , */,  EOF
	}


	

	/**
	 * Returns the text of the token.  If the token represents a String literal, then
	 * the returned text omits the delimiting double quotes and replaces escape sequences with
	 * the represented character.
	 * 
	 * @param token
	 * @return
	 */
	public String getText(Token token) {
		/* IMPLEMENT THIS */
		String tok = new String(chars, token.pos, token.length);
		/*
		tok = tok.replace('"' , ' ');
		tok = tok.replace('\b', 'b');
		tok = tok.replace('\t', 't');
		tok = tok.replace('\n', 'n');
		tok = tok.replace('\f', 'f');
		tok = tok.replace('\"', '"');
		tok = tok.replace('\'', '\'');
		tok = tok.replace('\\', '\\');
		tok = tok.replace('\r', 'r');
		*/

		return tok;
	}
	
	
	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}
	
	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	

	/**
	 * The list of tokens created by the scan method.
	 */
	private final ArrayList<Token> tokens = new ArrayList<Token>();
	

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	public static enum State { START, AFTER_LT, AFTER_GT, AFTER_EQ, AFTER_EXCL, AFTER_MINUS, AFTER_DIV, AFTER_QUOTA, DIGITS, IDENT_PART, COMMENT, String }

	final char[] chars;

	static final char EOFchar = 0;

	Scanner(String inputString) {
		/* IMPLEMENT THIS */
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1);
		chars[numChars] = EOFchar;

	}
	

	
	public Scanner scan() throws LexicalException {
		/* IMPLEMENT THIS */
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		int startPos = 0;
		int startPosInLine = 0;
		State state = State.START;
		while( pos < chars.length ){
			char ch = chars[pos];
			switch(state){
				case START -> {
					startPos = pos;
					startPosInLine = posInLine;
					switch (ch) {
						case '\n' -> {
							pos++;
							line++;
							posInLine = 1;
						}
						case ' ', '\t', '\f' -> {
							pos++;
							posInLine++;
						}
						case '\r' -> {
							if( pos + 1 < chars.length ){
								if( chars[pos+1] == '\n'){
									pos++;
									posInLine = 1;
								}else {
									pos++;
									line++;
									posInLine = 1;
								}
							}else{
								pos++;
								line++;
								posInLine = 1;
							}
						}

						case  0  -> {
							pos++;
							tokens.add(new Token(Kind.EOF, startPos, 0, line, startPosInLine));
						}
						case '(' -> {
							tokens.add(new Token(Kind.LPAREN, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case ')' -> {
							tokens.add(new Token(Kind.RPAREN, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '[' -> {
							tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case ']' -> {
							tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case ';' -> {
							tokens.add(new Token(Kind.SEMI, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case ',' -> {
							tokens.add(new Token(Kind.COMMA, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '?' -> {
							tokens.add(new Token(Kind.Q, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '+' -> {
							tokens.add(new Token(Kind.PLUS, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '*' -> {
							tokens.add(new Token(Kind.STAR, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '%' -> {
							tokens.add(new Token(Kind.MOD, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '@' -> {
							tokens.add(new Token(Kind.AT, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '#' -> {
							tokens.add(new Token(Kind.HASH, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}

						case '0' -> {
							tokens.add(new Token(Kind.INTLIT, startPos, 1, line, startPosInLine));
							pos++;
							posInLine++;
						}
						case '<' -> {
							state = State.AFTER_LT;
							pos++;
							posInLine++;
						}
						case '>' -> {
							state = State.AFTER_GT;
							pos++;
							posInLine++;
						}
						case '=' -> {
							state = State.AFTER_EQ;
							pos++;
							posInLine++;
						}
						case '!' -> {
							state = State.AFTER_EXCL;
							pos++;
							posInLine++;
						}
						case '-' -> {
							state = State.AFTER_MINUS;
							pos++;
							posInLine++;
						}
						case '/' -> {
							state = State.AFTER_DIV;
							pos++;
							posInLine++;
						}
						case '"' -> {
							state = State.AFTER_QUOTA;
							pos++;
							posInLine++;
						}


						default -> {
							if(Character.isDigit(ch)) {
								state = State.DIGITS ;
								pos++;
								posInLine++;
							} else if ( Character.isJavaIdentifierStart(ch) ) {
								state = State.IDENT_PART;
								pos++;
								posInLine++;
							}
							else { throw new LexicalException(line + ":" + posInLine + " Illegal character " + (int)ch, pos); }
						}
					}
				}
				case DIGITS -> {
					if (Character.isDigit(ch)) {
						pos++;
						posInLine++;
					}else {
						try {
							Integer.parseInt(new String(chars, startPos, pos - startPos));
						} catch(NumberFormatException e) {
							throw new LexicalException( line + ":" + posInLine + " number out of range " + (int)ch, pos);
						}
						state = State.START;
						tokens.add(new Token(Kind.INTLIT, startPos, pos - startPos, line, startPosInLine));
					}
				}
				case IDENT_PART -> {
					if (Character.isJavaIdentifierStart(ch) || Character.isDigit(ch)) {
						pos++;
						posInLine++;
					}else {
						Kind reserve = reserves.get(new String(chars, startPos, pos - startPos));
						int con = 0;
						if( reserve != null) {
							tokens.add(new Token(reserve, startPos, pos - startPos, line, startPosInLine));
						}
						else{
							try {
								con = constants.get(new String(chars, startPos, pos - startPos));
								tokens.add(new Token(Kind.CONST, startPos, pos - startPos, line, startPosInLine));
							}catch(NullPointerException e){
								tokens.add(new Token(Kind.IDENT, startPos, pos - startPos, line, startPosInLine));
							}
						}
						state = State.START;
					}
				}
				case AFTER_LT -> {
					if( ch =='=' ){
						state = State.START;
						tokens.add(new Token(Kind.LE, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}else if(ch=='-') {
						state = State.START;
						tokens.add(new Token(Kind.LARROW, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}else if(ch=='<') {
						state = State.START;
						tokens.add(new Token(Kind.LPIXEL, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}else {
						state = State.START;
						tokens.add(new Token(Kind.LT, startPos, 1, line, startPosInLine));
					}
				}
				case AFTER_GT -> {
					if( ch=='=' ){
						state = State.START;
						tokens.add(new Token(Kind.GE, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}else if(ch=='>') {
						state = State.START;
						tokens.add(new Token(Kind.RPIXEL, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}
					else{
						state = State.START;
						tokens.add(new Token(Kind.GT, startPos, 1, line, startPosInLine));
					}
				}
				case AFTER_EQ -> {
					if( ch=='=') {
						state = State.START;
						tokens.add(new Token(Kind.EQ, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}
					else {
						state = State.START;
						tokens.add(new Token(Kind.ASSIGN, startPos, 1, line, startPosInLine));
					}
				}
				case AFTER_EXCL -> {
					if( ch=='=' ) {
						state = State.START;
						tokens.add(new Token(Kind.NEQ, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}
					else {
						state = State.START;
						tokens.add(new Token(Kind.EXCL, startPos, 1, line, startPosInLine));
					}
				}
				case AFTER_MINUS -> {
					if( ch=='>' ) {
						state = State.START;
						tokens.add(new Token(Kind.RARROW, startPos, 2, line, startPosInLine));
						pos++;
						posInLine++;
					}
					else {
						state = State.START;
						tokens.add(new Token(Kind.MINUS, startPos, 1, line, startPosInLine));
					}
				}
				case AFTER_DIV -> {
					if( ch=='/' ){
						state = State.COMMENT;
						pos++;
						posInLine++;
					}
					else {
						tokens.add(new Token(Kind.DIV, startPos, 1, line, startPosInLine));
						state = State.START;
					}
				}
				case AFTER_QUOTA -> {
					if( ch != '"' && ch != '\b' && ch != '\t' && ch != '\n' && ch != '\f' && ch != '\r' && ch != '\"' && ch != '\'' && ch != '\\'){
						pos++;
						posInLine++;
					}
					else if(ch == '"' || ch == '\"'){
						state = State.START;
						pos++;
						posInLine++;
						tokens.add(new Token(Kind.STRINGLIT, startPos, pos - startPos, line, startPosInLine));								
					}else if(ch == '\b' || ch == '\t' || ch == '\n' || ch == '\f' || ch == '\r' || ch == '\'' || ch == '\\'){
						throw new LexicalException(line + ":" + posInLine + " EscapeSquence " + (int)ch, pos);
					}
				}
				case COMMENT -> {
					if( ch== '\n' || ch== '\r' || ch == 0 ){
						state = State.START;
						pos++;
						line++;
						posInLine = 1;
					}
					else {
						pos++;
						posInLine++;
					}
				}
			}
		}
		return this;
	}
	

	/**
	 * precondition:  This Token is an INTLIT or CONST
	 * @throws LexicalException 
	 * 
	 * @returns the integer value represented by the token
	 */
	public int intVal(Token t) throws LexicalException {
		/* IMPLEMENT THIS */
		int num = 0;
		if(t.kind == Kind.CONST) {
			num = constants.get(new String(chars, t.pos, t.length));
		}else if(t.kind == Kind.INTLIT){
			try {
				num = Integer.parseInt(new String(chars, t.pos, t.length));
			} catch(NumberFormatException e) {
				throw new LexicalException( t.line + ":" + t.posInLine + " number out of range ", t.pos);
			}
		}else {
			throw new LexicalException( t.line + ":" + t.posInLine + " input illegal ", t.pos);
		}
		
		return num;
	}
	
	/**
	 * Hashmap containing the values of the predefined colors.
	 * Included for your convenience.  
	 * 
	 */
	private static HashMap<String, Integer> constants;
	static {
		constants = new HashMap<String, Integer>();	
		constants.put("Z", 255);
		constants.put("WHITE", 0xffffffff);
		constants.put("SILVER", 0xffc0c0c0);
		constants.put("GRAY", 0xff808080);
		constants.put("BLACK", 0xff000000);
		constants.put("RED", 0xffff0000);
		constants.put("MAROON", 0xff800000);
		constants.put("YELLOW", 0xffffff00);
		constants.put("OLIVE", 0xff808000);
		constants.put("LIME", 0xff00ff00);
		constants.put("GREEN", 0xff008000);
		constants.put("AQUA", 0xff00ffff);
		constants.put("TEAL", 0xff008080);
		constants.put("BLUE", 0xff0000ff);
		constants.put("NAVY", 0xff000080);
		constants.put("FUCHSIA", 0xffff00ff);
		constants.put("PURPLE", 0xff800080);
	}

	private static HashMap<String,Kind> reserves = new HashMap<String,Kind>();
	static {
		reserves = new HashMap<String, Kind>();
		reserves.put("X", Kind.KW_X);
		reserves.put("Y", Kind.KW_Y);
		reserves.put("width", Kind.KW_WIDTH);
		reserves.put("height", Kind.KW_HEIGHT);
		reserves.put("screen", Kind.KW_SCREEN);
		reserves.put("screen_width", Kind.KW_SCREEN_WIDTH);
		reserves.put("screen_height", Kind.KW_SCREEN_HEIGHT);
		reserves.put("image", Kind.KW_image);
		reserves.put("int", Kind.KW_int);
		reserves.put("string", Kind.KW_string);
		reserves.put("red", Kind.KW_RED);
		reserves.put("green", Kind.KW_GREEN);
		reserves.put("blue", Kind.KW_BLUE);
	}
	/**
	 * Returns a String representation of the list of Tokens.
	 * You may modify this as desired. 
	 */
	public String toString() {
		return tokens.toString();
	}

}
