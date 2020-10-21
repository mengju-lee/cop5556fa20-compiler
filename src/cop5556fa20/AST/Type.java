/**
 * Code for the class project in COP5556 Programming Language Principles 
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

package cop5556fa20.AST;

import cop5556fa20.Scanner.Token;
import cop5556fa20.Parser;

public enum Type {
	Int, String, Image, Boolean, Void;
	
	public static Type getType(Token token) throws Parser.SyntaxException{
		switch (token.kind()){
		
			case KW_int -> {return Type.Int;} 
			case KW_boolean-> {return Type.Boolean;} 
			case KW_image-> {return Type.Image;} 
			case KW_string-> {return Type.String;} 
			case KW_void-> {return Type.Void;}
			
			default -> { 
				throw new Parser.SyntaxException(token,"illegal type");
			} 
		
		}
		
	}
}
