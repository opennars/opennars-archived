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

import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import nars.tuprolog.event.*;
import nars.tuprolog.interfaces.IProlog;

import java.io.Serializable;
import java.util.*;


/**
 * The Prolog class represents a tuProlog engine.
 */
@SuppressWarnings("serial")
public class Prolog extends ConcurrentEngineManager implements /*Castagna 06/2011*/IProlog,/**/ Serializable {


    /*  manager of current theory */
    protected TheoryManager theoryManager;
    /*  component managing primitive  */
    private PrimitiveManager primitiveManager;
    /* component managing operators */
    private OperatorManager opManager;
    /* component managing flags */
    private FlagManager flagManager;
    /* component managing libraries */
    private LibraryManager libraryManager;






    /**
     * Builds a prolog engine with default libraries loaded.
     * <p>
     * The default libraries are BasicLibrary, ISOLibrary,
     * IOLibrary, and  JavaLibrary
     */
    public Prolog() {
        this(false, true);
        try {
            loadLibrary("nars.tuprolog.lib.BasicLibrary");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            loadLibrary("nars.tuprolog.lib.ISOLibrary");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            loadLibrary("nars.tuprolog.lib.IOLibrary");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (System.getProperty("java.vm.name").equals("IKVM.NET"))
                loadLibrary("OOLibrary.OOLibrary, OOLibrary");
            else
                loadLibrary("nars.tuprolog.lib.JavaLibrary");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Builds a tuProlog engine with loaded
     * the specified libraries
     *
     * @param libs the (class) name of the libraries to be loaded
     */
    public Prolog(String... libs) throws InvalidLibraryException {
        this(false, true);
        if (libs != null) {
            for (int i = 0; i < libs.length; i++) {
                loadLibrary(libs[i]);
            }
        }
    }


    /**
     * Initialize basic engine structures.
     *
     * @param spy     spying activated
     * @param warning warning activated
     */
    private Prolog(boolean spy, boolean warning) {
        super(spy, warning);
    }


    protected void initializeManagers() {
        flagManager = new FlagManager();
        libraryManager = new LibraryManager();
        opManager = new OperatorManager();
        theoryManager = new TheoryManager();
        primitiveManager = new PrimitiveManager();
        //config managers
        theoryManager.initialize(this);
        libraryManager.initialize(this);
        flagManager.initialize(this);
        primitiveManager.initialize(this);
        runners = new IntObjectHashMap().asSynchronized();
        threads = new IntIntHashMap().asSynchronized();

        er1 = new EngineRunner(rootID);
        er1.initialize(this);
    }



    /**
     * Gets the component managing flags
     */
    @Override public FlagManager getFlagManager() {
        return flagManager;
    }

    /**
     * Gets the component managing theory
     */
    @Override public TheoryManager getTheoryManager() {
        return theoryManager;
    }

    /**
     * Gets the component managing primitives
     */
    @Override public PrimitiveManager getPrimitiveManager() {
        return primitiveManager;
    }


    /**
     * Gets the component managing libraries
     */
    @Override public LibraryManager getLibraryManager() {
        return libraryManager;
    }

    /**
     * Gets the component managing operators
     */
    @Override public OperatorManager getOperatorManager() {
        return opManager;
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
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    public SolveInfo solve(Term g, double maxTimeSeconds) {
        //System.out.println("ENGINE SOLVE #0: "+g);
        if (g == null) return null;

        SolveInfo sinfo = super.solve(g, maxTimeSeconds);

        notifyNewQueryResultAvailable(new QueryEvent(this, sinfo));

        return sinfo;

    }

    public SolveInfo solve(Term g) {
        return solve(g, 0);
    }

    /**
     * Solves a query
     *
     * @param st the string representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    public SolveInfo solve(String st, double time) throws MalformedGoalException {
        try {
            Parser p = new Parser(opManager, st);
            Term t = p.nextTerm(true);
            return solve(t, time);
        } catch (Exception ex) {
            throw new MalformedGoalException(ex.toString());
        }
    }

    public SolveInfo solve(String st) throws MalformedGoalException {
        return solve(st, 0);
    }

    /**
     * Gets next solution
     *
     * @return the result of the demonstration
     * @throws NoMoreSolutionException if no more solutions are present
     * @see SolveInfo
     **/
    public SolveInfo solveNext(double maxTimeSec) throws NoMoreSolutionException {
        if (hasOpenAlternatives()) {
            SolveInfo sinfo = super.solveNext(maxTimeSec);
            QueryEvent ev = new QueryEvent(this, sinfo);
            notifyNewQueryResultAvailable(ev);
            return sinfo;
        } else
            throw new NoMoreSolutionException();
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
        primitiveManager.identifyFunctor(term);
    }



    /**
     * Gets the string representation of a term, using operators
     * currently defined by engine
     *
     * @param term the term to be represented as a string
     * @return the string representing the term
     */
    public String toString(Term term) {        //no syn
        return (term.toStringAsArgY(opManager, OperatorManager.OP_HIGH));
    }


    /**
     * Defines a new flag
     */
    public boolean defineFlag(String name, Struct valueList, Term defValue, boolean modifiable, String libName) {
        return flagManager.defineFlag(name, valueList, defValue, modifiable, libName);
    }



}