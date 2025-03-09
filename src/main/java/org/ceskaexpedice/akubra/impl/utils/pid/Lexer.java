/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ceskaexpedice.akubra.impl.utils.pid;

import org.ceskaexpedice.akubra.impl.utils.pid.Token.TokenType;


/**
 * @author pavels
 *
 
 * Window - Preferences - Java - Code Style - Code Templates
 */
class Lexer {

	private static final int LOOK_AHEAD_DEPTH = 5;
	
	// char buffer with lookahead support
	private CharBuffer buffer = null;
	
	Lexer(String inputString) throws LexerException {
		this.buffer = new CharBuffer(inputString, LOOK_AHEAD_DEPTH);
	}
	
	Lexer(String inputString, int lookAhead) throws LexerException {
		this.buffer = new CharBuffer(inputString, lookAhead);
	}
	
	
	/**
	 * Returns char from given position
	 * @param pos position of char
	 * @return char 
	 * @throws SQLLexerException throws when the s
	 */	
	char charLookAhead(int charPosition) throws LexerException {
		char ch = (char)this.buffer.la(charPosition);
		return ch;
	}
	/**
	 * Returns real stream position of char
	 * @param position
	 * @return int
	 * @throws SQLLexerException
	 */
	int charPosition(int charPosition) throws LexerException {
		return this.buffer.position(charPosition);
	}
	
	/**
	 * Consume char and read new char into buffer
	 * @throws SQLLexerException
	 */
	void consumeChar() throws LexerException {
		this.buffer.consume();
	}
	
	/**
	 * Match char
	 * @param expectingChar
	 * @throws SQLLexerException
	 */
	void matchChar(char expectingChar) throws LexerException {
		if (charLookAhead(1) == expectingChar) {
			this.consumeChar();
		} else throw new LexerException("i am expecting '"+expectingChar+"' but got '"+charLookAhead(1)+"'");
	}

	Token matchALPHA() throws LexerException {
		int ch = this.buffer.la(1);
        this.consumeChar();
        return new Token(TokenType.ALPHA, ""+(char)ch);
	}
	
	boolean hexDigitPostfix(char ch) {
		switch(ch) {
			case 'A':
			case 'B': 
			case 'C': 
			case 'D':
			case 'E': 
			case 'F':
			case 'a':
			case 'b': 
			case 'c': 
			case 'd':
			case 'e': 
			case 'f':
				return true;
			default:
				return false;
		}			
	}
	
	Token matchHexDigit() throws LexerException {
		StringBuffer buffer = new StringBuffer();
		char ch = charLookAhead(1);
		if (!Character.isDigit(ch)) throw new LexerException("Expecting Digit !");
		buffer.append(ch);
		this.consumeChar();
		ch = charLookAhead(1);
		if (!hexDigitPostfix(ch)) throw new LexerException("Expecting 'A','B','C','D','E' or 'F' !");
		buffer.append(Character.toUpperCase(ch));
		this.consumeChar();
		return new Token(TokenType.HEXDIGIT, buffer.toString());
	}

	Token matchDigit() throws LexerException {
		StringBuffer buffer = new StringBuffer();
		char ch = charLookAhead(1);
		if (!Character.isDigit(ch)) throw new LexerException("Expecting Digit !");
		buffer.append(ch);
		this.consumeChar();
		return new Token(TokenType.DIGIT, buffer.toString());
	}
	
	void matchString(String str) throws LexerException {
		char[] chrs = str.toCharArray();
		for (int i = 0; i < chrs.length; i++) {
			matchChar(chrs[i]);
		}
	}
	
	Token readToken() throws LexerException {
		char ch = charLookAhead(1);
		if (ch == 65535) return new Token(TokenType.EOI, "eoi");
		switch(ch) {
			case ':': {
				this.matchChar(':');
				return new Token(TokenType.DOUBLEDOT,":");
			}
			case '-': {
				this.matchChar('-');
				return new Token(TokenType.MINUS,"-");
			}
			case '~': {
				this.matchChar('~');
				return new Token(TokenType.TILDA,"~");
			}
			case '.': {
				this.matchChar('.');
				return new Token(TokenType.DOT,".");
			}
            case ' ': {
                this.matchChar(' ');
                return new Token(TokenType.SPACE," ");
            }
            case '\t': {
                this.matchChar('\t');
                return new Token(TokenType.TAB,"\t");
            }
            case '\n': {
                this.matchChar('\n');
                return new Token(TokenType.NEWLINE,"\n");
            }
            case '@': {
                this.matchChar('@');
                return new Token(TokenType.AT,"@");
            }
            case '/': {
                this.matchChar('/');
                return new Token(TokenType.DIV,"/");
            }
			case '%': {
				this.matchChar('%');
				if (Character.isDigit(charLookAhead(2)) && hexDigitPostfix(charLookAhead(3))) {
					return matchHexDigit();
				} else 
				return new Token(TokenType.PERCENT,"%");
			}
			case '_': {
				this.matchChar('_');
				return new Token(TokenType.UNDERSCOPE,"_");
			}
			case '(': {
                this.matchChar('(');
			    return new Token(TokenType.LPAREN,"(");
			}
			case (')') :{
                this.matchChar(')');
			    return new Token(TokenType.RPAREN,")");
			}
			default: {
				if (Character.isDigit(ch)) {
					return matchDigit();
				}
				return matchALPHA();
			}
			
		}
	}
	

	
	
	
	
}