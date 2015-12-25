/*
 * Automaton (dk.brics.automaton)
 * 
 * Copyright (c) 2012-2013 Kevin Krumwiede
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package dk.brics.automaton;

/**
 * Data structure for returning token info from a <tt>TokenAutomaton</tt>.
 *
 * @author Kevin Krumwiede
 * @see TokenAutomaton#find(CharSequence, int, boolean, TokenDetails)
 */
public class TokenDetails {
	/**
	 * Indicates that no match is possible.
	 */
	public static final Object NO_MATCH = "NO_MATCH";
	
	/**
	 * Indicates that a match extends to the end of the character sequence and
	 * could be longer.
	 */
	public static final Object UNDERFLOW = "UNDERFLOW";
	
	/**
	 * The character sequence containing the token.
	 */
	public CharSequence seq;
	
	/**
	 * Offset of the first character of the token.
	 */
	public int off;
	
	/**
	 * Length of the token.
	 */
	public int len;
		
	/**
	 * Object associated with the token, or one of the constants
	 * <tt>NO_MATCH</tt> or <tt>UNDERFLOW</tt>.
	 */
	public Object info;

	@Override
	public String toString() {
		return "TokenDetails{" +
				"seq=" + seq +
				", off=" + off +
				", len=" + len +
				", info=" + info +
				'}';
	}
}
