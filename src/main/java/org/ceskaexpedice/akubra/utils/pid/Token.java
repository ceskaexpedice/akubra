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
package org.ceskaexpedice.akubra.utils.pid;

class Token {

	private TokenType type;
	private String value;

	Token(TokenType type, String value) {
		super();
		this.type = type;
		this.value = value;
	}

	TokenType getType() {
		return type;
	}

	String getValue() {
		return value;
	}

	enum TokenType {
		ALPHA, DIGIT, HEXDIGIT, PERCENT, DOT, DOUBLEDOT, MINUS, TILDA, EOI, UNDERSCOPE,LPAREN,RPAREN, SPACE, TAB, NEWLINE, AT, DIV
	}
}
