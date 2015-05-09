package nars.tuprolog;

import nars.tuprolog.event.*;
import nars.tuprolog.interfaces.IProlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by me on 5/9/15.
 */
abstract public class AbstractEngineManager implements IProlog {

    /*  spying activated ?  */
    private boolean spy;
    /*  warning activated ?  */
    private boolean warning;
    /* listeners registrated for virtual machine output events */
    /*Castagna 06/2011*/
	/* exception activated ? */
    private boolean exception;

    /**/
    private final ArrayList<OutputListener> outputListeners;
    /* listeners registrated for virtual machine internal events */
    private final ArrayList<SpyListener> spyListeners;
    /* listeners registrated for virtual machine state change events */
    private final ArrayList<WarningListener> warningListeners;
    /*Castagna 06/2011*/
	/* listeners registrated for virtual machine state exception events */
    private final ArrayList<ExceptionListener> exceptionListeners;
	/**/

    /* listeners to theory events */
    private final ArrayList<TheoryListener> theoryListeners;
    /* listeners to library events */
    private final ArrayList<LibraryListener> libraryListeners;
    /* listeners to query events */
    private final ArrayList<QueryListener> queryListeners;

    public AbstractEngineManager() {

        outputListeners = new ArrayList<>();
        spyListeners = new ArrayList<>();
        warningListeners = new ArrayList<>();
		/*Castagna 06/2011*/
        exceptionListeners = new ArrayList<>();
		/**/
        this.spy = false;
        this.warning = true;
        this.exception = true;
		/**/
        theoryListeners = new ArrayList<>();
        queryListeners = new ArrayList<>();
        libraryListeners = new ArrayList<>();
        absolutePathList = new ArrayList<>();
        init();


    }

    abstract protected void init();

    public synchronized SolveInfo solve(Term query) {
        return solve(query, 0);
    }

    public abstract SolveInfo solve(Term query, double maxTimeSeconds);

    abstract public void solveHalt();

    abstract public void solveEnd();

    public SolveInfo solveNext() throws NoMoreSolutionException {
        return solveNext(0);
    }

    abstract public SolveInfo solveNext(double maxTimeSec) throws NoMoreSolutionException;

    abstract public boolean unify(Term t0, Term t1);

    abstract boolean match(Term t0, Term t1, long now, ArrayList<Var> v1, ArrayList<Var> v2);

    abstract public void identifyFunctor(Term term);


    public abstract Primitives getPrimitives();

    public abstract Libraries getLibraries();




    abstract public Engine getEnv();

    abstract public boolean unify(Term t0, Term t1, ArrayList<Var> v1, ArrayList<Var> v2);


    /**
     * Gets a term from a string, using the operators currently
     * defined by the engine
     *
     * @param st the string representing a term
     * @return the term parsed from the string
     * @throws InvalidTermException if the string does not represent a valid term
     */
    public Term toTerm(String st) throws InvalidTermException {    //no syn

        return Parser.parseSingleTerm(st, getOperators());
    }

    /**
     * Gets the component managing flags
     */
    abstract public Flags getFlags();


    /**
     * Gets the component managing operators
     */
    abstract public Operators getOperators();

    /**
     * Gets the list of the operators currently defined
     *
     * @return the list of the operators
     */
    public Collection<Operator> getCurrentOperatorList() {    //no syn
        return getOperators().getOperators();
    }

    /**
     * Gets the reference to a loaded library
     *
     * @param name the name of the library already loaded
     * @return the reference to the library loaded, null if the library is
     * not found
     */
    public Library getLibrary(String name) {    //no syn
        return getLibraries().getLibrary(name);
    }


    protected Library getLibraryPredicate(String name, int nArgs) {        //no syn
        return getPrimitives().getLibraryPredicate(name, nArgs);
    }


    protected Library getLibraryFunctor(String name, int nArgs) {        //no syn
        return getPrimitives().getLibraryFunctor(name, nArgs);
    }

    // libraries management interface

    /**
     * Loads a library.
     * <p>
     * If a library with the same name is already present,
     * a warning event is notified and the request is ignored.
     *
     * @param className name of the Java class containing the library to be loaded
     * @return the reference to the Library just loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public Library loadLibrary(String className) throws InvalidLibraryException {    //no syn
        return getLibraries().load(className);
    }

    /**
     * Loads a library.
     * <p>
     * If a library with the same name is already present,
     * a warning event is notified and the request is ignored.
     *
     * @param className name of the Java class containing the library to be loaded
     * @param paths     The path where is contained the library.
     * @return the reference to the Library just loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public Library loadLibrary(String className, String[] paths) throws InvalidLibraryException {    //no syn
        return getLibraries().load(className, paths);
    }


    /**
     * Loads a specific instance of a library
     * <p>
     * If a library with the same name is already present,
     * a warning event is notified
     *
     * @param lib the (Java class) name of the library to be loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public void loadLibrary(Library lib) throws InvalidLibraryException {    //no syn
        getLibraries().load(lib);
    }


    /**
     * Gets the list of current libraries loaded
     *
     * @return the list of the library names
     */
    public String[] getCurrentLibraries() {        //no syn
        return getLibraries().getLibraries();
    }


    /**
     * Unloads a previously loaded library
     *
     * @param name of the library to be unloaded
     * @throws InvalidLibraryException if name is not a valid loaded library
     */
    public void unloadLibrary(String name) throws InvalidLibraryException {        //no syn
        getLibraries().unloadLibrary(name);
    }


    abstract public ExecutionContext getCurrentContext();

    /**
     * Gets the last Element of the path list
     */
    public String getCurrentDirectory() {
        String directory;
        if (absolutePathList.isEmpty()) {
            if (this.lastPath != null) {
                directory = this.lastPath;
            } else {
                directory = System.getProperty("user.dir");
            }
        } else {
            directory = absolutePathList.get(absolutePathList.size() - 1);
        }

        return directory;
    }

    /* path history for including documents */
    protected ArrayList<String> absolutePathList;
    protected String lastPath;

    /**
     * Append a new path to directory list
     */
    public void pushDirectoryToList(String path) {
        absolutePathList.add(path);
    }

    /**
     * Retract an element from directory list
     */
    public void popDirectoryFromList() {
        if (!absolutePathList.isEmpty()) {
            absolutePathList.remove(absolutePathList.size() - 1);
        }
    }

    /**
     * Reset directory list
     */
    public void resetDirectoryList(String path) {
        absolutePathList = new ArrayList<>();
        absolutePathList.add(path);
    }
    /**
     * Sets the last Element of the path list
     */
    public void setCurrentDirectory(String s) {
        this.lastPath = s;
    }

    abstract public Theories getTheories();


    /**
     * Sets a new theory
     *
     * @param th is the new theory
     * @throws InvalidTheoryException if the new theory is not valid
     * @see Theory
     */
    public void setTheory(Theory th) throws InvalidTheoryException {    //no syn
        getTheories().clear();
        addTheory(th);
    }


    /**
     * Adds (appends) a theory
     *
     * @param th is the theory to be added
     * @throws InvalidTheoryException if the new theory is not valid
     * @see Theory
     */

    public SolveInfo addTheory(final PrologTermIterator th) throws InvalidTheoryException {    //no syn
        return addTheory(th.iterator(this));
    }

    public SolveInfo addTheory(final Iterator<? extends Term> i) throws InvalidTheoryException {    //no syn
        Theory oldTh = getDynamicTheoryCopy();
        getTheories().consult(i, true, null);
        SolveInfo theoryGoal = getTheories().solveTheoryGoal();
        Theory newTh = getDynamicTheoryCopy();
        TheoryEvent ev = new TheoryEvent(this, oldTh, newTh);
        this.notifyChangedTheory(ev);
        return theoryGoal;
    }

    public SolveInfo addTheory(final Struct s) throws InvalidTheoryException {    //no syn
        Theory oldTh = getDynamicTheoryCopy();
        getTheories().consult(s, true, null);
        SolveInfo theoryGoal = getTheories().solveTheoryGoal();
        Theory newTh = getDynamicTheoryCopy();
        TheoryEvent ev = new TheoryEvent(this, oldTh, newTh);
        this.notifyChangedTheory(ev);
        return theoryGoal;
    }

    /**
     * Gets current theory
     *
     * @return current(dynamic) theory
     */
    public Theory getDynamicTheoryCopy() {    //no syn
        try {
            return new Theory(getTheories().getTheory(true));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
            //return null;
        }
    }

//
//	/**
//	 * Gets last consulted theory, with the original textual format
//	 *
//	 * @return theory
//	 */
//	public Theory getLastConsultedTheory() {	//no syn
//		return getTheoryManager().getLastConsultedTheory();
//	}


    /**
     * Clears current theory
     */
    public void clearTheory() {    //no syn
        try {
            setTheory(new Theory());
        } catch (InvalidTheoryException e) {
            // this should never happen
        }
    }

    // event listeners management

    /**
     * Adds a listener to ouput events
     *
     * @param l the listener
     */
    public synchronized void addOutputListener(OutputListener l) {
        outputListeners.add(l);
    }


    /**
     * Adds a listener to theory events
     *
     * @param l the listener
     */
    public synchronized void addTheoryListener(TheoryListener l) {
        theoryListeners.add(l);
    }

    /**
     * Adds a listener to library events
     *
     * @param l the listener
     */
    public synchronized void addLibraryListener(LibraryListener l) {
        libraryListeners.add(l);
    }

    /**
     * Adds a listener to theory events
     *
     * @param l the listener
     */
    public synchronized void addQueryListener(QueryListener l) {
        queryListeners.add(l);
    }

    /**
     * Adds a listener to spy events
     *
     * @param l the listener
     */
    public synchronized void addSpyListener(SpyListener l) {
        spyListeners.add(l);
    }

    /**
     * Adds a listener to warning events
     *
     * @param l the listener
     */
    public synchronized void addWarningListener(WarningListener l) {
        warningListeners.add(l);
    }

	/*Castagna 06/2011*/

    /**
     * Adds a listener to exception events
     *
     * @param l the listener
     */
    public synchronized void addExceptionListener(ExceptionListener l) {
        exceptionListeners.add(l);
    }
	/**/

    /**
     * Removes a listener to ouput events
     *
     * @param l the listener
     */
    public synchronized void removeOutputListener(OutputListener l) {
        outputListeners.remove(l);
    }

    /**
     * Removes all output event listeners
     */
    public synchronized void removeAllOutputListeners() {
        outputListeners.clear();
    }

    /**
     * Removes a listener to theory events
     *
     * @param l the listener
     */
    public synchronized void removeTheoryListener(TheoryListener l) {
        theoryListeners.remove(l);
    }

    /**
     * Removes a listener to library events
     *
     * @param l the listener
     */
    public synchronized void removeLibraryListener(LibraryListener l) {
        libraryListeners.remove(l);
    }

    /**
     * Removes a listener to query events
     *
     * @param l the listener
     */
    public synchronized void removeQueryListener(QueryListener l) {
        queryListeners.remove(l);
    }


    /**
     * Removes a listener to spy events
     *
     * @param l the listener
     */
    public synchronized void removeSpyListener(SpyListener l) {
        spyListeners.remove(l);
    }

    /**
     * Removes all spy event listeners
     */
    public synchronized void removeAllSpyListeners() {
        spyListeners.clear();
    }

    /**
     * Removes a listener to warning events
     *
     * @param l the listener
     */
    public synchronized void removeWarningListener(WarningListener l) {
        warningListeners.remove(l);
    }

    /**
     * Removes all warning event listeners
     */
    public synchronized void removeAllWarningListeners() {
        warningListeners.clear();
    }

	/* Castagna 06/2011*/

    /**
     * Removes a listener to exception events
     *
     * @param l the listener
     */
    public synchronized void removeExceptionListener(ExceptionListener l) {
        exceptionListeners.remove(l);
    }
	/**/

	/*Castagna 06/2011*/

    /**
     * Removes all exception event listeners
     */
    public synchronized void removeAllExceptionListeners() {
        exceptionListeners.clear();
    }
	/**/

    /**
     * Gets a copy of current listener list to output events
     */
    public synchronized List<OutputListener> getOutputListenerList() {
        return new ArrayList<>(outputListeners);
    }

    /**
     * Gets a copy of current listener list to warning events
     */
    public synchronized List<WarningListener> getWarningListenerList() {
        return new ArrayList<>(warningListeners);
    }

	/*Castagna 06/2011*/

    /**
     * Gets a copy of current listener list to exception events
     */
    public synchronized List<ExceptionListener> getExceptionListenerList() {
        return new ArrayList<>(exceptionListeners);
    }
	/**/

    /**
     * Gets a copy of current listener list to spy events
     */
    public synchronized List<SpyListener> getSpyListenerList() {
        return new ArrayList<>(spyListeners);
    }

    /**
     * Gets a copy of current listener list to theory events
     */
    public synchronized List<TheoryListener> getTheoryListenersList() {
        return new ArrayList<>(theoryListeners);
    }

    /**
     * Gets a copy of current listener list to library events
     */
    public synchronized List<LibraryListener> getLibraryListenerList() {
        return new ArrayList<>(libraryListeners);
    }

    /**
     * Gets a copy of current listener list to query events
     */
    public synchronized List<QueryListener> getQueryListenerList() {
        return new ArrayList<>(queryListeners);
    }

    // notification

    /**
     * Notifies an ouput information event
     *
     * @param e the event
     */
    protected void notifyOutput(OutputEvent e) {
        for (OutputListener ol : outputListeners) {
            ol.onOutput(e);
        }
    }

    /**
     * Notifies a spy information event
     *
     * @param e the event
     */
    protected void notifySpy(SpyEvent e) {
        for (SpyListener sl : spyListeners) {
            sl.onSpy(e);
        }
    }

    /**
     * Notifies a warning information event
     *
     * @param e the event
     */
    protected void notifyWarning(WarningEvent e) {
        for (WarningListener wl : warningListeners) {
            wl.onWarning(e);
        }
    }

	/*Castagna 06/2011*/

    /**
     * Notifies a exception information event
     *
     * @param e the event
     */
    protected void notifyException(ExceptionEvent e) {
        for (ExceptionListener el : exceptionListeners) {
            el.onException(e);
        }
    }
	/**/

    //

    /**
     * Notifies a new theory set or updated event
     *
     * @param e the event
     */
    protected void notifyChangedTheory(TheoryEvent e) {
        for (TheoryListener tl : theoryListeners) {
            tl.theoryChanged(e);
        }
    }

    /**
     * Notifies a library loaded event
     *
     * @param e the event
     */
    protected void notifyLoadedLibrary(LibraryEvent e) {
        for (LibraryListener ll : libraryListeners) {
            ll.libraryLoaded(e);
        }
    }

    /**
     * Notifies a library unloaded event
     *
     * @param e the event
     */
    protected void notifyUnloadedLibrary(LibraryEvent e) {
        for (LibraryListener ll : libraryListeners) {
            ll.libraryUnloaded(e);
        }
    }

    /**
     * Notifies a library loaded event
     *
     * @param e the event
     */
    protected void notifyNewQueryResultAvailable(QueryEvent e) {
        for (QueryListener ql : queryListeners) {
            ql.newQueryResultAvailable(e);
        }
    }



    /**
     * Switches on/off the notification of warning information events
     *
     * @param state - true for enabling warning information notification
     */
    public synchronized void setWarning(boolean state) {
        warning = state;
    }

    /**
     * Checks if warning information are notified
     *
     * @return true if the engine emits warning information
     */
    public synchronized boolean isWarning() {
        return warning;
    }

    /**
     * Notifies a warn information event
     *
     * @param m the warning message
     */
    public void warn(String m) {
        if (warning) {
            notifyWarning(new WarningEvent(this, m));
            //log.warn(m);
        }
    }

	/*Castagna 06/2011*/

    /**
     * Notifies a exception information event
     *
     * @param m the exception message
     */
    public void exception(String m) {
        if (exception) {
            notifyException(new ExceptionEvent(this, m));
        }
    }
	/**/

	/*Castagna 06/2011*/

    /**
     * Checks if exception information are notified
     *
     * @return true if the engine emits exception information
     */
    public synchronized boolean isException() {
        return exception;
    }
	/**/

	/*Castagna 06/2011*/

    /**
     * Switches on/off the notification of exception information events
     *
     * @param state - true for enabling exception information notification
     */
    public synchronized void setException(boolean state) {
        exception = state;
    }
	/**/

    /**
     * Produces an output information event
     *
     * @param m the output string
     */
    public synchronized void stdOutput(String m) {
        notifyOutput(new OutputEvent(this, m));
    }


    // spy interface ----------------------------------------------------------

    /**
     * Switches on/off the notification of spy information events
     *
     * @param state - true for enabling the notification of spy event
     */
    public synchronized void setSpy(boolean state) {
        spy = state;
    }

    /**
     * Checks the spy state of the engine
     *
     * @return true if the engine emits spy information
     */
    public synchronized boolean isSpy() {
        return spy;
    }


    /**
     * Notifies a spy information event
     */
    protected synchronized void spy(final String s) {
        if (spy) {
            notifySpy(new SpyEvent(this, s));
        }
    }

    /**
     * Notifies a spy information event
     *
     * @param s TODO
     */
    protected synchronized void spy(String s, Engine e) {
        //System.out.println("spy: "+i+"  "+s+"  "+g);
        if (spy) {
            ExecutionContext ctx = e.currentContext;
            int i = 0;
            String g = "-";
            if (ctx.fatherCtx != null) {
                i = ctx.depth - 1;
                g = ctx.fatherCtx.currentGoal.toString();
            }
            notifySpy(new SpyEvent(this, e, "spy: " + i + "  " + s + "  " + g));
        }
    }


    public Term termSolve(String st) {
        try {
            Parser p = new Parser(getOperators(), st);
            Term t = p.nextTerm(true);
            return t;
        } catch (InvalidTermException e) {
            String s = "null";
            Term t = Term.createTerm(s);
            return t;
        }
    }

    public abstract ArrayList<String> getBagOFresString();

    public abstract ArrayList<Term> getBagOFres();

    public abstract void setRelinkVar(boolean b);

    public abstract void setBagOFgoal(Term goal);

    public abstract void setBagOFvarSet(Term varSet);

    public abstract void setBagOFbag(Term tList);



    public abstract void setBagOFres(ArrayList<Term> l);

    public abstract void setBagOFresString(ArrayList<String> lString);

    public abstract boolean getRelinkVar();

    public abstract Term getBagOFgoal();

    public abstract Term getBagOFbag();

    public abstract Term getBagOFvarSet();

    public abstract String getSetOfSolution();

    public abstract void setSetOfSolution(String s);


    public abstract void cut();

    public abstract void pushSubGoal(SubGoalTree abstractSubGoalTrees);

    public abstract void identify(Term goal);

}
