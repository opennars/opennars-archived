/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
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

import nars.tuprolog.event.*;
import nars.tuprolog.interfaces.IProlog;

import java.io.Serializable;
import java.util.*;


/**
 * The Prolog class represents a tuProlog engine.
 */
@SuppressWarnings("serial")
abstract public class Prolog extends AbstractEngineManager implements /*Castagna 06/2011*/IProlog,/**/ Serializable {


    /*  manager of current theory */
    protected Theories theories;
    /*  component managing primitive  */
    private Primitives primitives;
    /* component managing operators */
    private Operators operators;
    /* component managing flags */
    private Flags flags;
    /* component managing libraries */
    private Libraries libraries;




    /**
     * Builds a tuProlog engine with loaded
     * the specified libraries
     *
     * @param libs the (class) name of the libraries to be loaded
     */
    public Prolog(String... libs) throws InvalidLibraryException {
        super();
        if (libs != null) {
            for (int i = 0; i < libs.length; i++) {
                loadLibrary(libs[i]);
            }
        }
    }


    protected void init() {
        flags = new Flags(this);
        operators = new Operators();
        libraries = new Libraries(this);
        primitives = new Primitives(this);
        theories = new Theories(this);

    }



    /**
     * Gets the component managing flags
     */
    @Override public Flags getFlags() {
        return flags;
    }

    /**
     * Gets the component managing theory
     */
    @Override public Theories getTheories() {
        return theories;
    }

    /**
     * Gets the component managing primitives
     */
    @Override public Primitives getPrimitives() {
        return primitives;
    }


    /**
     * Gets the component managing libraries
     */
    @Override public Libraries getLibraries() {
        return libraries;
    }

    /**
     * Gets the component managing operators
     */
    @Override public Operators getOperators() {
        return operators;
    }


    /**
     * Gets the current version of the tuProlog system
     */
    public static String getVersion() {
        return nars.tuprolog.util.VersionInfo.getEngineVersion();
    }





    // theory management interface




    // operators management



    // solve interface



    /**
     * Solves a query
     *
     * @param st the string representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    public SolveInfo solve(String st, double time) throws MalformedGoalException {
        try {
            Parser p = new Parser(operators, st);
            Term t = p.nextTerm(true);
            return solve(t, time);
        } catch (Exception ex) {
            throw new MalformedGoalException(ex.toString());
        }
    }

    public SolveInfo solve(String st) throws MalformedGoalException {
        return solve(st, 0);
    }


    public SolveInfo solveNext() throws NoMoreSolutionException {
        return solveNext(0);
    }


    /**
     * Unifies two terms using current demonstration context.
     *
     * @param t0 first term to be unified
     * @param t1 second term to be unified
     * @return true if the unification was successful
     */
    @Override public boolean match(Term t0, Term t1, long now, ArrayList<Var> v1, ArrayList<Var> v2) {    //no syn
        return t0.match(t1, now, v1, v2);
    }

    /**
     * Unifies two terms using current demonstration context.
     *
     * @param t0 first term to be unified
     * @param t1 second term to be unified
     * @return true if the unification was successful
     */
    @Deprecated public boolean unify(Term t0, Term t1) {    //no syn
        return unify(t0, t1, new ArrayList(), new ArrayList());
    }

    @Override
    public boolean unify(Term t0, Term t1, ArrayList<Var> v1, ArrayList<Var> v2) {    //no syn
        return t0.unify(this, t1, v1, v2);
    }

    /**
     * Identify functors
     *
     * @param term term to identify
     */
    @Override public void identifyFunctor(Term term) {    //no syn
        primitives.identifyFunctor(term);
    }



    /**
     * Gets the string representation of a term, using operators
     * currently defined by engine
     *
     * @param term the term to be represented as a string
     * @return the string representing the term
     */
    public String toString(Term term) {        //no syn
        return (term.toStringAsArgY(operators, Operators.OP_HIGH));
    }


    /**
     * Defines a new flag
     */
    public boolean defineFlag(String name, Struct valueList, Term defValue, boolean modifiable, String libName) {
        return flags.defineFlag(name, valueList, defValue, modifiable, libName);
    }



}