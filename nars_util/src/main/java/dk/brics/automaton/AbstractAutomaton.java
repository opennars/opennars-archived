/*
 * Automaton (dk.brics.automaton)
 * 
 * Copyright (c) 2001-2011 Anders Moeller
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

import com.gs.collections.impl.set.mutable.primitive.IntHashSet;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Finite-state automaton with regular expression operations.
 * <p>
 * Class invariants:
 * <ul>
 * <li> An automaton is either represented explicitly (with {@link State} and {@link CharTransition} objects)
 *      or with a singleton string (see {@link #getSingleton()} and {@link #expandSingleton()}) in case the automaton is known to accept exactly one string.
 *      (Implicitly, all states and transitions of an automaton are reachable from its initial state.)
 * <li> Automata are always reduced (see {@link #reduce()}) 
 *      and have no transitions to dead states (see {@link #removeDeadTransitions()}).
 * <li> If an automaton is nondeterministic, then {@link #isDeterministic()} returns false (but
 *      the converse is not required).
 * <li> Automata provided as input to operations are generally assumed to be disjoint.
 * </ul>
 * <p>
 * If the states or transitions are manipulated manually, the {@link #restoreInvariant()}
 * and {@link #setDeterministic(boolean)} methods should be used afterwards to restore 
 * representation invariants that are assumed by the built-in automata operations.
 * 
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
abstract public class AbstractAutomaton<A extends AbstractTransition> implements Serializable, Cloneable {

	/**
	 * Minimize using Huffman's O(n<sup>2</sup>) algorithm. 
	 * This is the standard text-book algorithm.
	 * @see #setMinimization(int)
	 */
	static final int MINIMIZE_HUFFMAN = 0;
	
	/**
	 * Minimize using Brzozowski's O(2<sup>n</sup>) algorithm. 
	 * This algorithm uses the reverse-determinize-reverse-determinize trick, which has a bad
	 * worst-case behavior but often works very well in practice 
	 * (even better than Hopcroft's!).
	 * @see #setMinimization(int)
	 */
	static final int MINIMIZE_BRZOZOWSKI = 1;
	
	/**
	 * Minimize using Hopcroft's O(n log n) algorithm.
	 * This is regarded as one of the most generally efficient algorithms that exist.
	 * @see #setMinimization(int)
	 */
	private static final int MINIMIZE_HOPCROFT = 2;
	
	/** Selects minimization algorithm (default: <code>MINIMIZE_HOPCROFT</code>). */
	static int minimization = MINIMIZE_HOPCROFT;
	
	/** Initial state of this automaton. */
	State initial;
	
	/** If true, then this automaton is definitely deterministic 
	 (i.e., there are no choices for any run, but a run may crash). */
	boolean deterministic;
	
	/** Extra data associated with this automaton. */
	transient Object info;
	
	/** Hash code. Recomputed by {@link #minimize()}. */
	private int hash_code;
	
	/** Singleton string. Null if not applicable. */
	String singleton;
	
	/** Minimize always flag. */
	private static final boolean minimize_always = false;
	
//	/** Selects whether operations may modify the input automata (default: <code>false</code>). */
//	static final boolean allow_mutation = true;
	
//	/** Caches the <code>isDebug</code> state. */
//	static boolean is_debug = null;
	
	/** 
	 * Constructs a new automaton that accepts the empty language.
	 * Using this constructor, automata can be constructed manually from
	 * {@link State} and {@link CharTransition} objects.
	 * @see #setInitialState(State)
	 * @see State
	 * @see CharTransition
	 */
	public AbstractAutomaton() {
		this(null);
	}
	public AbstractAutomaton(String singleton) {
		this.singleton = singleton;
		initial = new State();
		deterministic = true;
	}
	
//	static boolean isDebug() {
//		if (is_debug == null)
//			is_debug = Boolean.valueOf(System.getProperty("dk.brics.automaton.debug") != null);
//		return is_debug.booleanValue();
//	}
//
	/** 
	 * Selects minimization algorithm (default: <code>MINIMIZE_HOPCROFT</code>). 
	 * @param algorithm minimization algorithm
	 */
	static void setMinimization(int algorithm) {
		minimization = algorithm;
	}
	
//	/**
//	 * Sets or resets minimize always flag.
//	 * If this flag is set, then {@link #minimize()} will automatically
//	 * be invoked after all operations that otherwise may produce non-minimal automata.
//	 * By default, the flag is not set.
//	 * @param flag if true, the flag is set
//	 */
//	static public void setMinimizeAlways(boolean flag) {
//		minimize_always = flag;
//	}
	
//	/**
//	 * Sets or resets allow mutate flag.
//	 * If this flag is set, then all automata operations may modify automata given as input;
//	 * otherwise, operations will always leave input automata languages unmodified.
//	 * By default, the flag is not set.
//	 * @param flag if true, the flag is set
//	 * @return previous value of the flag
//	 */
//	static public boolean setAllowMutate(boolean flag) {
//		boolean b = allow_mutation;
//		allow_mutation = flag;
//		return b;
//	}
//
//	/**
//	 * Returns the state of the allow mutate flag.
//	 * If this flag is set, then all automata operations may modify automata given as input;
//	 * otherwise, operations will always leave input automata languages unmodified.
//	 * By default, the flag is not set.
//	 * @return current value of the flag
//	 */
//	static boolean getAllowMutate() {
//		return allow_mutation;
//	}
	
	void checkMinimizeAlways() {
		if (minimize_always)
			minimize();
	}
	
	boolean isSingleton() {
		return singleton!=null;
	}

	/**
	 * Returns the singleton string for this automaton.
	 * An automaton that accepts exactly one string <i>may</i> be represented
	 * in singleton mode. In that case, this method may be used to obtain the string.
	 * @return string, null if this automaton is not in singleton mode.
	 */
	public String getSingleton() {
		return singleton;
	}
	
	/** 
	 * Sets initial state. 
	 * @param s state
	 */
	void setInitialState(State s) {
		initial = s;
		singleton = null;
	}
	
	/** 
	 * Gets initial state. 
	 * @return state
	 */
	State getInitialState() {
		expandSingleton();
		return initial;
	}
	
	/**
	 * Returns deterministic flag for this automaton.
	 * @return true if the automaton is definitely deterministic, false if the automaton
	 *         may be nondeterministic
	 */
	public boolean isDeterministic() {
		return deterministic;
	}
	
	/**
	 * Sets deterministic flag for this automaton.
	 * This method should (only) be used if automata are constructed manually.
	 * @param deterministic true if the automaton is definitely deterministic, false if the automaton
	 *                      may be nondeterministic
	 */
	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}
	
	/**
	 * Associates extra information with this automaton. 
	 * @param info extra information
	 */
	public void setInfo(Object info) {
		this.info = info;
	}
	
	/**
	 * Returns extra information associated with this automaton. 
	 * @return extra information
	 * @see #setInfo(Object)
	 */
	public Object getInfo()	{
		return info;
	}
	
	/** 
	 * Returns the set of states that are reachable from the initial state.
	 * @param ordered if true, states will be returned in a deterministically
	 * ordered collection
	 * @return set of {@link State} objects
	 */
	Set<State<A>> getStates(boolean ordered) {
		expandSingleton();
		Set<State<A>> visited;
		if (ordered)
			visited = new LinkedHashSet<>();
		else
			visited = new HashSet<>();
		Deque<State> worklist = new LinkedList<>();
		worklist.add(initial);
		visited.add(initial);
		while (worklist.size() > 0) {
			State<?> s = worklist.removeFirst();

			if (ordered) {
				AbstractTransition[] tr = s.getSortedTransitions(false);
				for (AbstractTransition t : tr) {
					State tt = t.to;
					if (visited.add(tt))
						worklist.add(tt);
				}
			} else {
				s.transitions.forEach( t -> {
					State tt = t.to;
					if (visited.add(tt))
						worklist.add(tt);
				});
			}
		}
		return visited;
	}
	
	/** 
	 * Returns the set of states that are reachable from the initial state.
	 * @return set of {@link State} objects
	 */
	public Set<State<A>> getStates() {
		return getStates(false);//isDebug());
	}
	
	/** 
	 * Returns the set of reachable accept states. 
	 * @return set of {@link State} objects
	 */
	Set<State<A>> getAcceptStates() {
		expandSingleton();
		Set<State<A>> accepts = new HashSet<>();
		Set<State<A>> visited = new HashSet<>();
		Deque<State<A>> worklist = new LinkedList<>();
		worklist.add(initial);
		visited.add(initial);
		while (worklist.size() > 0) {
			State<A> s = worklist.removeFirst();
			if (s.accept)
				accepts.add(s);
			for (AbstractTransition t : s.transitions) {
				State tt = t.to;
				if (visited.add(tt)) {
					worklist.add(tt);
				}
			}
		}
		return accepts;
	}
	
	/** 
	 * Assigns consecutive numbers to the given states. 
	 */
	static void setStateNumbers(Iterable<? extends State> states) {
		int number = 0;
		for (State s : states)
			s.number = number++;
	}

	abstract protected A newFullrangeTransition(State<A> s);

	public static class CharAutomaton extends AbstractAutomaton<CharTransition> {

		public CharAutomaton() {
			super();
		}

		public CharAutomaton(String s) {
			super(s);
		}

		@Override
		protected CharTransition newFullrangeTransition(State<CharTransition> s) {
			return new CharTransition(Character.MIN_VALUE, Character.MAX_VALUE, s);
		}

		@Override public CharTransition newTransition(State<CharTransition> s, int min, int max) {
			return new CharTransition((char)min, (char)max, s);
		}

	}
	public static class ByteAutomaton extends AbstractAutomaton<ByteTransition> {

		@Override
		protected ByteTransition newFullrangeTransition(State<ByteTransition> s) {
			return new ByteTransition(Byte.MIN_VALUE, Byte.MAX_VALUE, s);
		}

		@Override public ByteTransition newTransition(State<ByteTransition> s, int min, int max) {
			return new ByteTransition((byte)min, (byte)max, s);
		}
	}

	/** 
	 * Adds transitions to explicit crash state to ensure that transition function is total. 
	 */
	void totalize() {
		State<A> s = new State();
		s.transitions.add(newFullrangeTransition(s));
		for (State p : getStates()) {
			int maxi = Character.MIN_VALUE;
			for (AbstractTransition t : p.getSortedTransitions(false)) {
				if (t.min() > maxi)
					p.transitions.add(newTransition(s, (char) maxi, (char) (t.min() - 1)));
				int tmax = t.max();
				if (tmax + 1 > maxi)
					maxi = tmax + 1;
			}
			if (maxi <= Character.MAX_VALUE)
				p.transitions.add(newTransition(s, (char)maxi, Character.MAX_VALUE));
		}
	}

	abstract public A newTransition(State<A> s, int min, int max);

	public A newTransition(State<A> s, int minmax) {
		return newTransition(s, minmax, minmax);
	}


	/**
	 * Restores representation invariant.
	 * This method must be invoked before any built-in automata operation is performed 
	 * if automaton states or transitions are manipulated manually.
	 * @see #setDeterministic(boolean)
	 */
	private void restoreInvariant() {
		removeDeadTransitions();
	}
	
	/** 
	 * Reduces this automaton.
	 * An automaton is "reduced" by combining overlapping and adjacent edge intervals with same destination. 
	 */
	public void reduce() {
		if (isSingleton())
			return;
		Set<State<A>> states = getStates();
		setStateNumbers(states);
		for (State s : states) {
			AbstractTransition[] st = s.getSortedTransitions(true);
			s.resetTransitions();
			State<A> p = null;
			int min = -1, max = -1;
			Set<A> strans = s.transitions;
			for (AbstractTransition t : st) {
				if (p == t.to) {
					if (t.min() <= max + 1) {
						if (t.max() > max)
							max = t.max();
					} else {
						if (p != null)
							strans.add(newTransition(p, min, max));
						min = t.min();
						max = t.max();
					}
				} else {
					if (p != null)
						strans.add(newTransition(p, min, max));
					p = t.to;
					min = t.min();
					max = t.max();
				}
			}
			if (p != null)
				strans.add(newTransition(p, min, max));
		}
		clearHashCode();
	}
	
	/** 
	 * Returns sorted array of all interval start points. 
	 */
	int[] getStartPoints() {
		IntHashSet pointset = new IntHashSet();
		for (State<A> s : getStates()) {
			pointset.add(Character.MIN_VALUE);
			for (AbstractTransition t : s.transitions) {
				pointset.add(t.min());
				if (t.max() < Character.MAX_VALUE)
					pointset.add((char)(t.max() + 1));
			}
		}
		return pointset.toSortedArray();
//		char[] points = new char[pointset.size()];
//		int n = 0;
//		for (Character m : pointset)
//			points[n++] = m;
//		Arrays.sort(points);
//		return points;
	}
	
	/** 
	 * Returns the set of live states. A state is "live" if an accept state is reachable from it. 
	 * @return set of {@link State} objects
	 */
	public Set<State<A>> getLiveStates() {
		expandSingleton();
		return getLiveStates(getStates());
	}
	
	private Set<State<A>> getLiveStates(Iterable<State<A>> states) {
		Map<State, Set<State<A>>> map = new HashMap<>();
		for (State<A> s : states)
			map.put(s, new HashSet<State<A>>());
		for (State<A> s : states)
			for (A t : s.transitions)
				map.get(t.to).add(s);
		Set<State<A>> live = new HashSet<>(getAcceptStates());
		Deque<State<A>> worklist = new LinkedList<>(live);
		while (worklist.size() > 0) {
			State<A> s = worklist.removeFirst();
			for (State<A> p : map.get(s))
				if (!live.contains(p)) {
					live.add(p);
					worklist.add(p);
				}
		}
		return live;
	}
	
	/** 
	 * Removes transitions to dead states and calls {@link #reduce()} and {@link #clearHashCode()}.
	 * (A state is "dead" if no accept state is reachable from it.)
	 */
	void removeDeadTransitions() {
		clearHashCode();
		if (isSingleton())
			return;
		Set<State<A>> states = getStates();
		Set<State<A>> live = getLiveStates(states);
		for (State s : states) {
			Set<A> st = s.transitions;
			s.resetTransitions();
			for (A t : st)
				if (live.contains(t.to))
					s.transitions.add(t);
		}
		reduce();
	}
	
	/** 
	 * Returns a sorted array of transitions for each state (and sets state numbers). 
	 */
	static AbstractTransition[][] getSortedTransitions(Set<State<?>> states) {
		setStateNumbers(states);
		AbstractTransition[][] transitions = new AbstractTransition[states.size()][];
		for (State s : states)
			transitions[s.number] = s.getSortedTransitionArray(false);
		return transitions;
	}
	
	/** 
	 * Expands singleton representation to normal representation.
	 * Does nothing if not in singleton representation. 
	 */
	void expandSingleton() {
		if (isSingleton()) {
			State<A> p = new State();
			String s = this.singleton;
			initial = p;
			for (int i = 0; i < s.length(); i++) {
				p.transitions.add(
					newTransition(p = new State(), s.charAt(i))
				);
			}
			p.accept = true;
			deterministic = true;
			singleton = null;
		}
	}
	
	/**
	 * Returns the number of states in this automaton.
	 */
	int getNumberOfStates() {
		if (isSingleton())
			return singleton.length() + 1;
		return getStates().size();
	}
	
	/**
	 * Returns the number of transitions in this automaton. This number is counted
	 * as the total number of edges, where one edge may be a character interval.
	 */
	int getNumberOfTransitions() {
		if (isSingleton())
			return singleton.length();
		int c = 0;
		for (State s : getStates())
			c += s.transitions.size();
		return c;
	}
	
	/**
	 * Returns true if the language of this automaton is equal to the language
	 * of the given automaton. Implemented using <code>hashCode</code> and
	 * <code>subsetOf</code>.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AbstractAutomaton))
			return false;
		AbstractAutomaton a = (AbstractAutomaton)obj;
		if (isSingleton() && a.isSingleton())
			return singleton.equals(a.singleton);
		return hashCode() == a.hashCode() && subsetOf(a) && a.subsetOf(this);
	}
	
	/**
	 * Returns hash code for this automaton. The hash code is based on the
	 * number of states and transitions in the minimized automaton.
	 * Invoking this method may involve minimizing the automaton.
	 */
	@Override
	public int hashCode() {
		if (hash_code == 0)
			minimize();
		return hash_code;
	}
	
	/**
	 * Recomputes the hash code.
	 * The automaton must be minimal when this operation is performed.
	 */
	void recomputeHashCode() {
		hash_code = getNumberOfStates() * 3 + getNumberOfTransitions() * 2;
		if (hash_code == 0)
			hash_code = 1;
	}
	
	/**
	 * Must be invoked when the stored hash code may no longer be valid.
	 */
	void clearHashCode() {
		hash_code = 0;
	}
	
	/**
	 * Returns a string representation of this automaton.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (isSingleton()) {
			b.append("singleton: ").append(singleton);
//			for (char c : singleton.toCharArray())
//				AbstractTransition.appendCharString(c, b);
			b.append('\n');
		} else {
			Set<State<A>> states = getStates();
			setStateNumbers(states);
			b.append("initial state: ").append(initial.number).append('\n');
			for (State s : states)
				b.append(s);
		}
		return b.toString();
	}

//	/**
//	 * Returns <a href="http://www.research.att.com/sw/tools/graphviz/" target="_top">Graphviz Dot</a>
//	 * representation of this automaton.
//	 */
//	public String toDot() {
//		StringBuilder b = new StringBuilder("digraph Automaton {\n");
//		b.append("  rankdir = LR;\n");
//		Set<State> states = getStates();
//		setStateNumbers(states);
//		for (State s : states) {
//			b.append("  ").append(s.number);
//			if (s.accept)
//				b.append(" [shape=doublecircle,label=\"\"];\n");
//			else
//				b.append(" [shape=circle,label=\"\"];\n");
//			if (s == initial) {
//				b.append("  initial [shape=plaintext,label=\"\"];\n");
//				b.append("  initial -> ").append(s.number).append('\n');
//			}
//			for (CharTransition t : s.transitions) {
//				b.append("  ").append(s.number);
//				t.appendDot(b);
//			}
//		}
//		return b.append("}\n").toString();
//	}
	
	/**
	 * Returns a clone of this automaton, expands if singleton.
	 */
	AbstractAutomaton cloneExpanded() {
		AbstractAutomaton a = clone();
		a.expandSingleton();
		return a;
	}

	/**
	 * Returns a clone of this automaton unless <code>allow_mutation</code> is set, expands if singleton.
	 */
	AbstractAutomaton cloneExpandedIfRequired() {
//		if (allow_mutation) {
//			expandSingleton();
//			return this;
//		} else
			return cloneExpanded();
	}

	/**
	 * Returns a clone of this automaton.
	 */
	@Override
	public AbstractAutomaton clone() {
		try {
			AbstractAutomaton a = (AbstractAutomaton)super.clone();
			if (!isSingleton()) {
				Map<State<A>, State<A>> m = new HashMap<>();
				Set<State<A>> states = getStates();
				for (State<A> s : states)
					m.put(s, new State());
				for (State<A> s : states) {
					State<A> p = m.get(s);
					p.accept = s.accept;
					p.info = s.info;
					if (s == initial)
						a.initial = p;
					for (A t : s.transitions)
						p.transitions.add(newTransition(m.get(t.to), t.min(), t.max()));
				}
			}
			a.info = info;
			return a;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves a serialized <code>Automaton</code> located by a URL.
	 * @param url URL of serialized automaton
	 * @exception IOException if input/output related exception occurs
	 * @exception OptionalDataException if the data is not a serialized object
	 * @exception InvalidClassException if the class serial number does not match
	 * @exception ClassCastException if the data is not a serialized <code>Automaton</code>
	 * @exception ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static AbstractAutomaton load(URL url) throws IOException, ClassCastException,
	                                             ClassNotFoundException {
		return load(url.openStream());
	}
	
	/**
	 * Retrieves a serialized <code>Automaton</code> from a stream.
	 * @param stream input stream with serialized automaton
	 * @exception IOException if input/output related exception occurs
	 * @exception OptionalDataException if the data is not a serialized object
	 * @exception InvalidClassException if the class serial number does not match
	 * @exception ClassCastException if the data is not a serialized <code>Automaton</code>
	 * @exception ClassNotFoundException if the class of the serialized object cannot be found
	 */
	public static AbstractAutomaton load(InputStream stream) throws IOException, ClassCastException,
	                                                        ClassNotFoundException {
		ObjectInputStream s = new ObjectInputStream(stream);
		return (AbstractAutomaton)s.readObject();
	}
	
	/**
	 * Writes this <code>Automaton</code> to the given stream.
	 * @param stream output stream for serialized automaton
	 * @exception IOException if input/output related exception occurs
	 */
	public void store(OutputStream stream) throws IOException {
		ObjectOutput s = new ObjectOutputStream(stream);
		s.writeObject(this);
		s.flush();
	}

	/** 
	 * See {@link BasicAutomata#makeEmpty()}.
	 */
	public static AbstractAutomaton makeEmpty()	{
		return BasicAutomata.makeEmpty();
	}

	/** 
	 * See {@link BasicAutomata#makeEmptyString()}.
	 */
	public static AbstractAutomaton makeEmptyString() {
		return BasicAutomata.makeEmptyString();
	}
	
	/** 
	 * See {@link BasicAutomata#makeAnyString()}.
	 */
	public static AbstractAutomaton makeAnyString()	{
		return BasicAutomata.makeAnyString();
	}
	
	/** 
	 * See {@link BasicAutomata#makeAnyChar()}.
	 */
	public static AbstractAutomaton makeAnyChar() {
		return BasicAutomata.makeAnyChar();
	}
	
	/** 
	 * See {@link BasicAutomata#makeChar(char)}.
	 */
	static AbstractAutomaton makeChar(char c) {
		return BasicAutomata.makeChar(c);
	}
	
	/** 
	 * See {@link BasicAutomata#makeCharRange(char, char)}.
	 */
	static AbstractAutomaton makeCharRange(char min, char max) {
		return BasicAutomata.makeCharRange(min, max);
	}
	
	/** 
	 * See {@link BasicAutomata#makeCharSet(String)}.
	 */
	static AbstractAutomaton makeCharSet(String set) {
		return BasicAutomata.makeCharSet(set);
	}
	
	/** 
	 * See {@link BasicAutomata#makeInterval(int, int, int)}.
	 */
	public static AbstractAutomaton makeInterval(int min, int max, int digits) throws IllegalArgumentException {
		return BasicAutomata.makeInterval(min, max, digits);
	}
	
	/** 
	 * See {@link BasicAutomata#makeString(String)}.
	 */
	static AbstractAutomaton makeString(String s) {
		return BasicAutomata.makeString(s);
	}
	
    /** 
     * See {@link BasicAutomata#makeStringUnion(CharSequence...)}.
     */
    public static AbstractAutomaton makeStringUnion(CharSequence... strings) {
        return BasicAutomata.makeStringUnion(strings);
    }

	/**
	 * See {@link BasicAutomata#makeMaxInteger(String)}.
	 */
	static AbstractAutomaton makeMaxInteger(String n) {
		return BasicAutomata.makeMaxInteger(n);
	}
	
	/**
	 * See {@link BasicAutomata#makeMinInteger(String)}.
	 */
	public static AbstractAutomaton makeMinInteger(String n) {
		return BasicAutomata.makeMinInteger(n);
	}

	/**
	 * See {@link BasicAutomata#makeTotalDigits(int)}.
	 */
	public static AbstractAutomaton makeTotalDigits(int i) {
		return BasicAutomata.makeTotalDigits(i);
	}

	/**
	 * See {@link BasicAutomata#makeFractionDigits(int)}.
	 */
	public static AbstractAutomaton makeFractionDigits(int i) {
		return BasicAutomata.makeFractionDigits(i);
	}
	
	/**
	 * See {@link BasicAutomata#makeIntegerValue(String)}.
	 */
	public static AbstractAutomaton makeIntegerValue(String value) {
		return BasicAutomata.makeIntegerValue(value);
	}
	
	/**
	 * See {@link BasicAutomata#makeDecimalValue(String)}.
	 */
	public static AbstractAutomaton makeDecimalValue(String value) {
		return BasicAutomata.makeDecimalValue(value);
	}
	
	/**
	 * See {@link BasicAutomata#makeStringMatcher(String)}.
	 */
	public static AbstractAutomaton makeStringMatcher(String s) {
		return BasicAutomata.makeStringMatcher(s);
	}
	
	/** 
	 * See {@link BasicOperations#concatenate(AbstractAutomaton, AbstractAutomaton)}.
	 */
	public AbstractAutomaton concatenate(AbstractAutomaton a) {
		return BasicOperations.concatenate(this, a);
	}
	
	/**
	 * See {@link BasicOperations#concatenate(List)}.
	 */
	public static AbstractAutomaton concatenate(List<AbstractAutomaton> l) {
		return BasicOperations.concatenate(l);
	}

	/**
	 * See {@link BasicOperations#optional(AbstractAutomaton)}.
	 */
	public AbstractAutomaton optional() {
		return BasicOperations.optional(this);
	}
	
	/**
	 * See {@link BasicOperations#repeat(AbstractAutomaton)}.
	 */
	public AbstractAutomaton repeat() {
		return BasicOperations.repeat(this);
	}

	/**
	 * See {@link BasicOperations#repeat(AbstractAutomaton, int)}.
	 */
	public AbstractAutomaton repeat(int min) {
		return BasicOperations.repeat(this, min);
	}
	
	/**
	 * See {@link BasicOperations#repeat(AbstractAutomaton, int, int)}.
	 */
	public AbstractAutomaton repeat(int min, int max) {
		return BasicOperations.repeat(this, min, max);
	}

	/**
	 * See {@link BasicOperations#complement(AbstractAutomaton)}.
	 */
	AbstractAutomaton complement() {
		return BasicOperations.complement(this);
	}

	/**
	 * See {@link BasicOperations#minus(AbstractAutomaton, AbstractAutomaton)}.
	 */
	public AbstractAutomaton minus(AbstractAutomaton a) {
		return BasicOperations.minus(this, a);
	}

	/**
	 * See {@link BasicOperations#intersection(AbstractAutomaton, AbstractAutomaton)}.
	 */
	public AbstractAutomaton intersection(AbstractAutomaton a) {
		return BasicOperations.intersection(this, a);
	}
	
	/**
	 * See {@link BasicOperations#subsetOf(AbstractAutomaton, AbstractAutomaton)}.
	 */
	private boolean subsetOf(AbstractAutomaton a) {
		return BasicOperations.subsetOf(this, a);
	}
	
	/**
	 * See {@link BasicOperations#union(AbstractAutomaton, AbstractAutomaton)}.
	 */
	public AbstractAutomaton union(AbstractAutomaton a) {
		return BasicOperations.union(this, a);
	}
	
	/**
	 * See {@link BasicOperations#union(Collection)}.
	 */
	public static AbstractAutomaton union(Collection<AbstractAutomaton> l) {
		return BasicOperations.union(l);
	}

	/**
	 * See {@link BasicOperations#determinize(AbstractAutomaton)}.
	 */
	void determinize() {
		BasicOperations.determinize(this);
	}

	/** 
	 * See {@link BasicOperations#addEpsilons(AbstractAutomaton, Collection)}.
	 */
	void addEpsilons(Collection<StatePair> pairs) {
		BasicOperations.addEpsilons(this, pairs);
	}
	
	/**
	 * See {@link BasicOperations#isEmptyString(AbstractAutomaton)}.
	 */
	boolean isEmptyString() {
		return BasicOperations.isEmptyString(this);
	}

	/**
	 * See {@link BasicOperations#isEmpty(AbstractAutomaton)}.
	 */
	public boolean isEmpty() {
		return BasicOperations.isEmpty(this);
	}
	
	/**
	 * See {@link BasicOperations#isTotal(AbstractAutomaton)}.
	 */
	public boolean isTotal() {
		return BasicOperations.isTotal(this);
	}
	
	/**
	 * See {@link BasicOperations#getShortestExample(AbstractAutomaton, boolean)}.
	 */
	public String getShortestExample(boolean accepted) {
		return BasicOperations.getShortestExample(this, accepted);
	}
	
	/**
	 * See {@link BasicOperations#run(AbstractAutomaton, String)}.
	 */
	public boolean run(String s) {
		return BasicOperations.run(this, s);
	}
	
	/**
	 * See {@link MinimizationOperations#minimize(AbstractAutomaton)}.
	 */
	public void minimize() {
		MinimizationOperations.minimize(this);
	}
	
	/**
	 * See {@link MinimizationOperations#minimize(AbstractAutomaton)}.
	 * Returns the automaton being given as argument.
	 */
	public static AbstractAutomaton minimize(AbstractAutomaton a) {
		a.minimize();
		return a;
	}
	
	/**
	 * See {@link SpecialOperations#overlap(AbstractAutomaton, AbstractAutomaton)}.
	 */
	public AbstractAutomaton overlap(AbstractAutomaton a) {
		return SpecialOperations.overlap(this, a);
	}
	
//	/**
//	 * See {@link SpecialOperations#singleChars(AbstractAutomaton)}.
//	 */
//	public AbstractAutomaton singleChars() {
//		return SpecialOperations.singleChars(this);
//	}
	
	/**
	 * See {@link SpecialOperations#trim(AbstractAutomaton, String, char)}.
	 */
	public AbstractAutomaton trim(String set, char c) {
		return SpecialOperations.trim(this, set, c);
	}
//
//	/**
//	 * See {@link SpecialOperations#compress(AbstractAutomaton, String, char)}.
//	 */
//	public AbstractAutomaton compress(String set, char c) {
//		return SpecialOperations.compress(this, set, c);
//	}
	
	/**
	 * See {@link SpecialOperations#subst(AbstractAutomaton, Map)}.
	 */
	public AbstractAutomaton subst(Map<Character,Set<Character>> map) {
		return SpecialOperations.subst(this, map);
	}

	/**
	 * See {@link SpecialOperations#subst(AbstractAutomaton, char, String)}.
	 */
	public AbstractAutomaton subst(char c, String s) {
		return SpecialOperations.subst(this, c, s);
	}
	
	/**
	 * See {@link SpecialOperations#homomorph(AbstractAutomaton, char[], char[])}.
	 */
	public AbstractAutomaton homomorph(char[] source, char[] dest) {
		return SpecialOperations.homomorph(this, source, dest);
	}
	
	/**
	 * See {@link SpecialOperations#projectChars(AbstractAutomaton, Set)}.
	 */
	public AbstractAutomaton projectChars(Set<Character> chars) {
		return SpecialOperations.projectChars(this, chars);
	}
	
	/**
	 * See {@link SpecialOperations#isFinite(AbstractAutomaton)}.
	 */
	public boolean isFinite() {
		return SpecialOperations.isFinite(this);
	}
	
	/**
	 * See {@link SpecialOperations#getStrings(AbstractAutomaton, int)}.
	 */
	public Set<String> getStrings(int length) {
		return SpecialOperations.getStrings(this, length);
	}
	
	/**
	 * See {@link SpecialOperations#getFiniteStrings(AbstractAutomaton)}.
	 */
	public Set<String> getFiniteStrings() {
		return SpecialOperations.getFiniteStrings(this);
	}
	
	/**
	 * See {@link SpecialOperations#getFiniteStrings(AbstractAutomaton, int)}.
	 */
	public Set<String> getFiniteStrings(int limit) {
		return SpecialOperations.getFiniteStrings(this, limit);
	}

	/**
	 * See {@link SpecialOperations#getCommonPrefix(AbstractAutomaton)}.
	 */
	public String getCommonPrefix() {
		return SpecialOperations.getCommonPrefix(this);
	}
	
	/**
	 * See {@link SpecialOperations#prefixClose(AbstractAutomaton)}.
	 */
	public void prefixClose() {
		SpecialOperations.prefixClose(this);
	}

	/**
	 * See {@link SpecialOperations#hexCases(AbstractAutomaton)}.
	 */
	public static AbstractAutomaton hexCases(AbstractAutomaton a) {
		return SpecialOperations.hexCases(a);
	}
	
	/**
	 * See {@link SpecialOperations#replaceWhitespace(AbstractAutomaton)}.
	 */
	public static AbstractAutomaton replaceWhitespace(AbstractAutomaton a) {
		return SpecialOperations.replaceWhitespace(a);
	}
	
	/**
	 * See {@link ShuffleOperations#shuffleSubsetOf(Collection, AbstractAutomaton, Character, Character)}.
	 */ 
	public static String shuffleSubsetOf(Collection<AbstractAutomaton> ca, AbstractAutomaton a, Character suspend_shuffle, Character resume_shuffle) {
		return ShuffleOperations.shuffleSubsetOf(ca, a, suspend_shuffle, resume_shuffle);
	}

	/** 
	 * See {@link ShuffleOperations#shuffle(AbstractAutomaton, AbstractAutomaton)}.
	 */
	public AbstractAutomaton shuffle(AbstractAutomaton a) {
		return ShuffleOperations.shuffle(this, a);
	}
}
