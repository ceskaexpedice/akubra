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
package org.ceskaexpedice.akubra.pid;

import java.io.IOException;
import java.io.StringReader;


/**
 * @author pavels
 *
 */
class CharBuffer {

	private int[] buffer = null;	
	private int[] positions = null;
	private int depth = 0;
	private StringReader input = null;
	private int counter = 0;
	
	CharBuffer(String inputString, int depth) throws LexerException {
		try {
			this.input = new StringReader(inputString);
			this.depth = depth;
			this.buffer = new int[this.depth];
			this.positions = new int[this.depth];
			
			for (int i = 0; i < this.buffer.length; i++) {
				this.buffer[i] = this.input.read();
				this.positions[i] = counter ++;
			}
		} catch (IOException e) {
			throw new LexerException(e.getMessage());
		}
	}
	
	int la(int pos) throws LexerException {
		if ((pos >=1) && (pos <= this.depth)) {
			return this.buffer[pos - 1];
		} else throw new LexerException("cannot look ahead to '"+pos+"' position");
	}
	
	int position(int pos) throws LexerException {
		if ((pos >=1) && (pos <= this.depth)) {
			return this.positions[pos - 1];
		} else throw new LexerException("cannot look ahead to '"+pos+"' position");
	}
	
	void consume() throws LexerException {
		try {
			for (int i = 0; i < this.depth-1; i++) {
				this.buffer[i] = this.buffer[i+1];
				this.positions[i] = this.positions[i+1];
			}
			
			this.buffer[this.depth - 1] = this.input.read();
			this.positions[this.depth - 1] = this.counter ++;
		} catch (IOException e) {
			throw new LexerException(e.getMessage());
		}
	}

}