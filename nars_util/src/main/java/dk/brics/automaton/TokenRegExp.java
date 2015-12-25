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

import java.util.Map;

/**
 * A <tt>RegExp</tt> with <tt>toAutomaton(...)</tt> methods that produce
 * automatons in which the info field of the accepting states is set to the
 * info object specified in the <tt>TokenRegExp</tt> constructor.  This is
 * just a convenient shortcut for doing the same thing externally.
 * 
 * @author Kevin Krumwiede
 */
class TokenRegExp extends RegExp {
	
	private final Object info;

	public TokenRegExp(String s, int syntax_flags, Object info)
			throws IllegalArgumentException {
		super(s, syntax_flags);
		this.info = info;
	}

	TokenRegExp(String s, Object info)
			throws IllegalArgumentException {
		super(s);
		this.info = info;
	}

	@Override
	public AbstractAutomaton toAutomaton() {
		return toAutomaton(false);
	}

	@Override
	public AbstractAutomaton toAutomaton(boolean minimize) {
		AbstractAutomaton a = super.toAutomaton(minimize);
		setInfo(a);
		return a;
	}

	@Override
	public AbstractAutomaton toAutomaton(AutomatonProvider automaton_provider)
			throws IllegalArgumentException {
		AbstractAutomaton a = super.toAutomaton(automaton_provider);
		setInfo(a);
		return a;
	}

	@Override
	public AbstractAutomaton toAutomaton(AutomatonProvider automaton_provider,
										 boolean minimize) throws IllegalArgumentException {
		AbstractAutomaton a = super.toAutomaton(automaton_provider, minimize);
		setInfo(a);
		return a;
	}

	@Override
	public AbstractAutomaton toAutomaton(Map<String, AbstractAutomaton> automata)
			throws IllegalArgumentException {
		AbstractAutomaton a = super.toAutomaton(automata);
		setInfo(a);
		return a;
	}

	@Override
	public AbstractAutomaton toAutomaton(Map<String, AbstractAutomaton> automata,
										 boolean minimize) throws IllegalArgumentException {
		AbstractAutomaton a = super.toAutomaton(automata, minimize);
		setInfo(a);
		return a;
	}
	
	private void setInfo(AbstractAutomaton a) {
		if(info != null) {
			for(State s : a.getAcceptStates()) {
				s.setInfo(info);
			}
		}
	}
}
