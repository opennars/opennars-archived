/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

import nars.tuprolog.util.OneWayList;

import java.io.Serializable;
import java.util.*;

/**
 * Term class is the root abstract class for prolog data type
 * @see Struct
 * @see Var
 * @see  Number
 */
public abstract class Term extends SubGoalElement implements Comparable<Term>, Serializable {
	private static final long serialVersionUID = 1L;

    // true and false constants
    public static final Term TRUE  = new Struct("true");
    public static final Term FALSE = new Struct("false");    
    
    // checking type and properties of the Term
    
    
    /** is this term a null term?*/
    public abstract boolean isEmptyList();
    
    //
    
    /** is this term a constant prolog term? */
    public abstract boolean isAtomic();
    
    /** is this term a prolog compound term? */
    public abstract boolean isCompound();
    
    /** is this term a prolog (alphanumeric) atom? */
    public abstract boolean isAtom();
    
    /** is this term a prolog list? */
    public abstract boolean isList();
    
    /** is this term a ground term? */
    public abstract boolean isGround();



    /**
     * Tests for the equality of two object terms
     *
     * The comparison follows the same semantic of
     * the isEqual method.
     *
     */
    public boolean equals(final Object t) {
        if (!(t instanceof Term))
            return false;
        return isEqual((Term) t);
    }


    
    
    
    /**
     * is term greater than term t?
     */
    public abstract boolean isGreater(Term t);
    public abstract boolean isGreaterRelink(Term t, ArrayList<String> vorder);
    
    /**
     * Tests if this term is (logically) equal to another
     */
    public abstract boolean isEqual(Term t);
    
    /**
	 * Gets the actual term referred by this Term. if the Term is a bound variable, the method gets the Term linked to the variable
	 */
    public abstract Term getTerm();
    
    
    /**
     * Unlink variables inside the term
     */
    abstract public void free();
    
    
    /**
     * Resolves variables inside the term, starting from a specific time count.
     *
     * If the variables has been already resolved, no renaming is done.
     * @param count new starting time count for resolving process
     * @return the new time count, after resolving process
     */
    abstract public long resolveTerm(long count);
    
    
    /**
     * Resolves variables inside the term
     * 
     * If the variables has been already resolved, no renaming is done.
     */
    @Deprecated public Term resolveTerm() {
        resolveTerm(System.currentTimeMillis());
        return this;
    }

    
    /**
     * gets a engine's copy of this term.
     * @param idExecCtx Execution Context identified
     */
    public Term copyGoal(Map<Var,Var> vars, int idExecCtx) {
        return copy(vars,idExecCtx);
    }
    
    
    /**
     * gets a copy of this term for the output
     */
    public Term copyResult(Collection<Var> goalVars, List<Var> resultVars) {
        IdentityHashMap<Var,Var> originals = new IdentityHashMap<>();
        for (Var key: goalVars) {
            Var clone = new Var();
            if (!key.isAnonymous())
                clone = new Var(key.getOriginalName());
            originals.put(key,clone);
            resultVars.add(clone);
        }
        return copy(originals,new IdentityHashMap<>());
    }


    @Override
    public Term clone() {
        /** shouldnt need cloned */
        return this;
    }



    @Override
    abstract public String toString();

    @Override
    abstract public int hashCode();




    /**
     * gets a copy (with renamed variables) of the term.
     *
     * The list argument passed contains the list of variables to be renamed
     * (if empty list then no renaming)
     * @param idExecCtx Execution Context identifier
     */
    abstract public Term copy(Map<Var,Var> vMap, int idExecCtx);
    
    /**
     * gets a copy for result.
     */
    abstract public Term copy(Map<Var,Var> vMap, Map<Term,Var> substMap);


    public boolean unify(final Prolog mediator, final Term t1) {
        return unify(mediator, t1, new ArrayList(), new ArrayList());
    }
    /**
     * Try to unify two terms
     * @param mediator have the reference of EngineManager
     * @param t1 the term to unify
     * @return true if the term is unifiable with this one
     */
    public boolean unify(final Prolog engine, final Term t1, ArrayList<Var> v1, ArrayList<Var> v2) {
        resolveTerm();
        t1.resolveTerm();

        v1.clear(); v2.clear();
        boolean ok = unify(v1,v2,t1);
        if (ok) {
            ExecutionContext ec = engine.getCurrentContext();
            if (ec != null) {
                Engine.State env = engine.getEnv();
                int id = (env==null)? Var.PROGRESSIVE : env.nDemoSteps;
                // Update trailingVars
                ec.trailingVars = new OneWayList<>(v1,ec.trailingVars);
                // Renaming after unify because its utility regards not the engine but the user
                int count = 0;
                for(final Var v:v1){
                    v.rename(id,count);
                    if(id>=0){
                        id++;
                    }else{
                        count++;
                    }
                }
                for(final Var v:v2){
                    v.rename(id,count);
                    if(id>=0){
                        id++;
                    }else{
                        count++;
                    }
                }
            }
            return true;
        }
        Var.free(v1);
        Var.free(v2);
    	return false;
    }

    @Override
    public Term getValue() {
        return this;
    }
    
    
    /**
     * Tests if this term is unifiable with an other term.
     * No unification is done.
     *
     * The test is done outside any demonstration context
     * @param t the term to checked
     *
     * @return true if the term is unifiable with this one
     */
    public boolean match(Term t, long time, ArrayList<Var> v1, ArrayList<Var> v2) {
        v1.clear(); v2.clear();

        resolveTerm(time);
        t.resolveTerm(time);

        boolean ok = unify(v1,v2,t);
        Var.free(v1);
        Var.free(v2);
        return ok;
    }

    @Deprecated public boolean match(Term t) {
        return match(t, System.currentTimeMillis(), new ArrayList(), new ArrayList());
    }

    /**
     * Tries to unify two terms, given a demonstration context
     * identified by the mark integer.
     *
     * Try the unification among the term and the term specified
     * @param varsUnifiedArg1 Vars unified in myself
     * @param varsUnifiedArg2 Vars unified in term t
     */
    abstract public boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t);
    
    
    /**
     * Static service to create a Term from a string.
     * @param st the string representation of the term
     * @return the term represented by the string
     * @throws InvalidTermException if the string does not represent a valid term
     */
    public static Term createTerm(final String st) {
        return Parser.parseSingleTerm(st);
    }

    /**
     * Static service to create a Term from a string, providing an
     * external operate manager.
     * @param st the string representation of the term
     * @param op the operate manager used to build the term
     * @return the term represented by the string
     * @throws InvalidTermException if the string does not represent a valid term
     */
    public static Term createTerm(String st, Operators op) {
        return Parser.parseSingleTerm(st, op);
    }
    

    
    /**
     * Gets an iterator providing
     * a term stream from a source text
     */
    public static java.util.Iterator<Term> getIterator(String text) {
        return new Parser(text).iterator();
    }
    
    // term representation
    
    /**
     * Gets the string representation of this term
     * as an X argument of an operate, considering the associative property.
     */
    String toStringAsArgX(Operators op,int prio) {
        return toStringAsArg(op,prio,true);
    }
    
    /**
     * Gets the string representation of this term
     * as an Y argument of an operate, considering the associative property.
     */
    String toStringAsArgY(Operators op,int prio) {
        return toStringAsArg(op,prio,false);
    }
    
    /**
     * Gets the string representation of this term
     * as an argument of an operate, considering the associative property.
     *
     *  If the boolean argument is true, then the term must be considered
     *  as X arg, otherwise as Y arg (referring to prolog associative rules)
     */
    String toStringAsArg(Operators op,int prio,boolean x) {
        return toString();
    }
    
    //
    
    /**
     * The iterated-goal term G of a term T is a term defined
     * recursively as follows:
     * <ul>
     * <li>if T unifies with ^(_, Goal) then G is the iterated-goal
     * term of Goal</li>
     * <li>else G is T</li>
     * </ul>
     */
    public Term iteratedGoalTerm() {
        return this;
    }
    
    /*Castagna 06/2011*/
    /**
	 * Visitor pattern
	 * @param tv - Visitor
	 */
	public abstract void accept(TermVisitor tv);
    /**/

    @Override
    public int compareTo(Term o) {
        return toString().compareTo(o.toString());
    }
}