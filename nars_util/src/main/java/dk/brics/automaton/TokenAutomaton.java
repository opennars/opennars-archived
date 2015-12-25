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
 * An automaton that can be used as a lexical analyzer.
 * 
 * @author Kevin Krumwiede
 */
class TokenAutomaton extends RunAutomaton {
	private static final long serialVersionUID = 1L;

	private static final int NO_ACCEPT = -1;
	private static final int NO_STEP = -1;
	
	/**
	 * Searches for the longest run of the specified character sequence,
	 * beginning at the specified index, that is accepted by this automaton.
	 * <p>
	 * This method returns true if an accepted run is found and a longer
	 * accepted run is not possible.  (A longer accepted run is possible if
	 * <tt>endOfInput</tt> is false and the automaton reaches the end of
	 * <tt>seq</tt> in a state that has transitions.)  The fields of
	 * <tt>details</tt> are filled with the token details.  The value of
	 * <tt>details.info</tt> is copied from the <tt>info</tt> field of the
	 * accepting state.
	 * <p>
	 * This method returns false if no accepted run is found or a longer
	 * accepted run is possible.  These conditions are distinguished by the
	 * value of <tt>details.info</tt>, which is set to
	 * {@link TokenDetails#NO_MATCH} if no accepted run is found or
	 * {@link TokenDetails#UNDERFLOW} if a longer accepted run is possible.
	 * 
	 * @param details the object in which to return the token details
	 * @return true if a match is found and no longer match is possible;
	 * otherwise false
	 * @see State#setInfo(Object)
	 * @see RunAutomaton#getInfo(int)
	 */
	public boolean find(CharSequence seq, int off, boolean endOfInput, TokenDetails details) {
		int maxAccept = NO_ACCEPT;
		Object info = null;
		int s = this.getInitialState();
		int l = seq.length();
		for(int i = off; i < l; ++i) {
			s = step(s, seq.charAt(i));
			if(s == NO_STEP) {
				if(maxAccept == NO_ACCEPT) {
					details.info = TokenDetails.NO_MATCH;
					return false;
				}
				else {
					details.seq = seq;
					details.off = off;
					details.len = maxAccept - details.off + 1;
					details.info = info;
					return true;
				}
			}
			else {
				if(this.isAccept(s)) {
					maxAccept = i;
					info = this.info[s];
				}
			}
		}
		// stepped to end of sequence
		if(!endOfInput && this.hasTransitions(s)) {
			details.info = TokenDetails.UNDERFLOW;
			return false;
		}
		else {
			if(maxAccept == NO_ACCEPT) {
				details.info = TokenDetails.NO_MATCH;
				return false;
			}
			else {
				details.seq = seq;
				details.off = off;
				details.len = maxAccept - details.off + 1;
				details.info = info;
				return true;
			}
		}
	}
		
	/**
	 * Returns true if the given state has any transitions.
	 */
	private boolean hasTransitions(int state) {
		if(!accept[state]) return true;
		int l = (state + 1) * points.length;
		for(int i = state * points.length; i < l; ++i) {
			if(transitions[i] != -1) return true;
		}
		return false;
	}
	
	public TokenAutomaton(Automaton a, boolean tableize, boolean ordered) {
		super(a, tableize, ordered);
	}	
		
	public TokenAutomaton(Automaton a, boolean tableize) {
		super(a, tableize);
	}
	
	TokenAutomaton(Automaton a) {
		super(a);
	}
		
//	public static TokenAutomaton load(InputStream stream) throws IOException, ClassNotFoundException {
//		ObjectInputStream s = new ObjectInputStream(stream);
//		return (TokenAutomaton) s.readObject();
//	}
}
