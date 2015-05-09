package nars.tuprolog;

//import java.io.File;
//import java.io.IOException;

import com.gs.collections.api.map.primitive.MutableIntIntMap;
import com.gs.collections.api.map.primitive.MutableIntObjectMap;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import nars.tuprolog.event.QueryEvent;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
/** Prolog core with multithreaded concurrency */
public class DefaultProlog extends Prolog  {

    protected MutableIntObjectMap<EngineRunner> runners;    //key: id; obj: runner
    protected MutableIntIntMap threads;    //key: pid; obj: id
    protected EngineRunner er1;
    protected int id = 0;
    protected int rootID = 0;

    private Map<String, TermQueue> queues = new HashMap();
    private Map<String, ReentrantLock> locks = new HashMap();

    public DefaultProlog() throws InvalidLibraryException {
        this("nars.tuprolog.lib.BasicLibrary","nars.tuprolog.lib.ISOLibrary", "nars.tuprolog.lib.IOLibrary", "nars.tuprolog.lib.JavaLibrary");
    }

    protected DefaultProlog(String... libs) throws InvalidLibraryException {
        super(libs);

        setSpy(false);
        setWarning(true);
        runners = new IntObjectHashMap().asSynchronized();
        threads = new IntIntHashMap().asSynchronized();
    }

    @Override
    protected void init() {
        super.init();
        er1 = new EngineRunner(rootID, this);
    }

    public synchronized boolean threadCreate(Term threadID, Term goal) {
        id += 1;

        if (goal == null) return false;
        if (goal instanceof Var)
            goal = goal.getTerm();

        EngineRunner er = new EngineRunner(id, this);

        if (!unify(threadID, new Int(id))) return false;

        er.setGoal(goal);
        addRunner(er, id);
        Thread t = new Thread(er, threadID.toString() + goal.toString());
        addThread(t.getId(), id);

        t.start();
        return true;
    }

    public SolveInfo join(int id) {
        EngineRunner er = findRunner(id);
        if (er == null || er.isDetached()) return null;
        /*toSPY
		 * System.out.println("Thread id "+runnerId()+" - prelevo la soluzione (join)");*/
        SolveInfo solution = er.read();
		/*toSPY
		 * System.out.println("Soluzione: "+solution);*/
        removeRunner(id);
        return solution;
    }

    public SolveInfo read(int id) {
        EngineRunner er = findRunner(id);
        if (er == null || er.isDetached()) return null;
		/*toSPY
		 * System.out.println("Thread id "+runnerId()+" - prelevo la soluzione (read) del thread di id: "+er.getId());
		 */
        SolveInfo solution = er.read();
		/*toSPY
		 * System.out.println("Soluzione: "+solution);
		 */
        return solution;
    }

    public boolean hasNext(int id) {
        EngineRunner er = findRunner(id);
        if (er == null || er.isDetached()) return false;
        return er.hasOpenAlternatives();
    }

    public boolean nextSolution(int id) {
        EngineRunner er = findRunner(id);
        if (er == null || er.isDetached()) return false;
		/*toSPY
		 * System.out.println("Thread id "+runnerId()+" - next_solution: risveglio il thread di id: "+er.getId());
		 */
        boolean bool = er.nextSolution();
        return bool;
    }

    public void detach(int id) {
        EngineRunner er = findRunner(id);
        if (er == null) return;
        er.detach();
    }

    public boolean sendMsg(int dest, Term msg) {
        EngineRunner er = findRunner(dest);
        if (er == null) return false;
        Term msgcopy = msg.copy(new LinkedHashMap<>(), 0);
        er.sendMsg(msgcopy);
        return true;
    }

    public boolean sendMsg(String name, Term msg) {
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        Term msgcopy = msg.copy(new LinkedHashMap<>(), 0);
        queue.store(msgcopy);
        return true;
    }

    public boolean getMsg(int id, Term msg) {
        EngineRunner er = findRunner(id);
        if (er == null) return false;
        return er.getMsg(msg);
    }

    public boolean getMsg(String name, Term msg) {
        EngineRunner er = findRunner();
        if (er == null) return false;
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.get(msg, this, er);
    }

    public boolean waitMsg(int id, Term msg) {
        EngineRunner er = findRunner(id);
        if (er == null) return false;
        return er.waitMsg(msg);
    }

    public boolean waitMsg(String name, Term msg) {
        EngineRunner er = findRunner();
        if (er == null) return false;
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.wait(msg, this, er);
    }

    public boolean peekMsg(int id, Term msg) {
        EngineRunner er = findRunner(id);
        if (er == null) return false;
        return er.peekMsg(msg);
    }

    public boolean peekMsg(String name, Term msg) {
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.peek(msg, this);
    }

    public boolean removeMsg(int id, Term msg) {
        EngineRunner er = findRunner(id);
        if (er == null) return false;
        return er.removeMsg(msg);
    }

    public boolean removeMsg(String name, Term msg) {
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.remove(msg, this);
    }

    private void removeRunner(int id) {
        EngineRunner er = runners.get(id);

        if (er == null) return;
        runners.remove(id);

        int pid = er.getPid();

        threads.remove(pid);

    }

    protected void addRunner(EngineRunner er, int id) {
        runners.put(id, er);
    }

    protected void addThread(long pid, int id) {
        threads.put((int) pid, id);
    }

    public void cut() {
        findRunner().cut();
    }

    @Override public ExecutionContext getCurrentContext() {
        EngineRunner runner = findRunner();
        return runner.getCurrentContext();
    }

    public boolean hasOpenAlternatives() {
        EngineRunner runner = findRunner();
        return runner.hasOpenAlternatives();
    }

    public boolean isHalted() {
        EngineRunner runner = findRunner();
        return runner.isHalted();
    }

    public void pushSubGoal(SubGoalTree goals) {
        EngineRunner runner = findRunner();
        runner.pushSubGoal(goals);

    }


    /**
     * Solves a query
     *
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    public SolveInfo solve(Term query, double maxTimeSeconds) {
        //System.out.println("ENGINE SOLVE #0: "+g);
        if (query == null) return null;

        this.clearSinfoSetOf();
        er1.setGoal(query);

        SolveInfo sinfo = er1.solve(maxTimeSeconds);
        //System.out.println("ENGINE MAN solve(Term) risultato: "+s);

        //return er1.solve();

        notifyNewQueryResultAvailable(new QueryEvent(this, sinfo));

        return sinfo;

    }

    public SolveInfo solve(Term g) {
        return solve(g, 0);
    }

    @Override
    public void solveEnd() {
        er1.solveEnd();
        if (!runners.isEmpty()) {
            for (EngineRunner e : runners.values()) {
                e.solveEnd();
            }
            queues.clear();
            locks.clear();
            id = 0;
        }
    }

    @Override
    public void solveHalt() {
        er1.solveHalt();
        if (!runners.isEmpty()) {
            for (EngineRunner e : runners.values()) {
                e.solveHalt();
            }
        }
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
            SolveInfo sinfo = er1.solveNext(maxTimeSec);
            QueryEvent ev = new QueryEvent(this, sinfo);
            notifyNewQueryResultAvailable(ev);
            return sinfo;
        } else
            throw new NoMoreSolutionException();
    }



    /**
     * @return L'EngineRunner associato al thread di id specificato.
     */

    private EngineRunner findRunner(int id) {
        if (!runners.containsKey(id)) return null;
        return runners.get(id);
    }

    private EngineRunner findRunner() {
        int pid = (int) Thread.currentThread().getId();
        int id = threads.getIfAbsent(pid, -1);
        if (id == -1)
            return er1;

        return runners.get(id);
    }

    //Ritorna l'identificativo del thread corrente
    public int runnerId() {
        EngineRunner er = findRunner();
        return er.getId();
    }

    public boolean createQueue(String name) {
        synchronized (queues) {
            if (queues.containsKey(name)) return true;
            TermQueue newQ = new TermQueue();
            queues.put(name, newQ);
        }
        return true;
    }

    public void destroyQueue(String name) {
        synchronized (queues) {
            queues.remove(name);
        }
    }

    public int queueSize(int id) {
        EngineRunner er = findRunner(id);
        return er.msgQSize();
    }

    public int queueSize(String name) {
        TermQueue q = queues.get(name);
        if (q == null) return -1;
        return q.size();
    }

    public boolean createLock(String name) {
        synchronized (locks) {
            if (locks.containsKey(name)) return true;
            ReentrantLock mutex = new ReentrantLock();
            locks.put(name, mutex);
        }
        return true;
    }

    public void destroyLock(String name) {
        synchronized (locks) {
            locks.remove(name);
        }
    }

    public boolean mutexLock(String name) {
        while (true) {
            ReentrantLock mutex = locks.get(name);
            if (mutex == null) {
                createLock(name);
                continue;
            }
            mutex.lock();
        /*toSPY
		 * System.out.println("Thread id "+runnerId()+ " - mi sono impossessato del lock");
		 */
            return true;
        }
    }


    public boolean mutexTryLock(String name) {
        ReentrantLock mutex = locks.get(name);
        if (mutex == null) return false;
		/*toSPY
		 * System.out.println("Thread id "+runnerId()+ " - provo ad impossessarmi del lock");
		 */
        return mutex.tryLock();
    }

    public boolean mutexUnlock(String name) {
        ReentrantLock mutex = locks.get(name);
        if (mutex == null) return false;
        try {
            mutex.unlock();
			/*toSPY
			 * System.out.println("Thread id "+runnerId()+ " - Ho liberato il lock");
			 */
            return true;
        } catch (IllegalMonitorStateException e) {
            return false;
        }
    }

    public boolean isLocked(String name) {
        ReentrantLock mutex = locks.get(name);
        if (mutex == null) return false;
        return mutex.isLocked();
    }

    public void unlockAll() {
        synchronized (locks) {
            Set<String> mutexList = locks.keySet();
            Iterator<String> it = mutexList.iterator();

            while (it.hasNext()) {
                ReentrantLock mutex = locks.get(it.next());
                boolean unlocked = false;
                while (!unlocked) {
                    try {
                        mutex.unlock();
                    } catch (IllegalMonitorStateException e) {
                        unlocked = true;
                    }
                }
            }
        }
    }

    @Override public Engine getEnv() {
        EngineRunner er = findRunner();
        return er.env;
    }

    public void identify(Term t) {
        EngineRunner er = findRunner();
        er.identify(t);
    }

    public boolean getRelinkVar() {
        EngineRunner r = this.findRunner();
        return r.getRelinkVar();
    }

    public void setRelinkVar(boolean b) {
        EngineRunner r = this.findRunner();
        r.setRelinkVar(b);
    }

    public ArrayList<Term> getBagOFres() {
        EngineRunner r = this.findRunner();
        return r.getBagOFres();
    }

    public void setBagOFres(ArrayList<Term> l) {
        EngineRunner r = this.findRunner();
        r.setBagOFres(l);
    }

    public ArrayList<String> getBagOFresString() {
        EngineRunner r = this.findRunner();
        return r.getBagOFresString();
    }

    public void setBagOFresString(ArrayList<String> l) {
        EngineRunner r = this.findRunner();
        r.setBagOFresString(l);
    }

    public Term getBagOFvarSet() {
        EngineRunner r = this.findRunner();
        return r.getBagOFvarSet();
    }

    public void setBagOFvarSet(Term l) {
        EngineRunner r = this.findRunner();
        r.setBagOFvarSet(l);
    }

    public Term getBagOFgoal() {
        EngineRunner r = this.findRunner();
        return r.getBagOFgoal();
    }

    public void setBagOFgoal(Term l) {
        EngineRunner r = this.findRunner();
        r.setBagOFgoal(l);
    }

    public Term getBagOFbag() {
        EngineRunner r = this.findRunner();
        return r.getBagOFBag();
    }

    public void setBagOFbag(Term l) {
        EngineRunner r = this.findRunner();
        r.setBagOFBag(l);
    }

    public String getSetOfSolution() {
        EngineRunner r = this.findRunner();
        return r.getSetOfSolution();
    }

    public void setSetOfSolution(String s) {
        EngineRunner r = this.findRunner();
        r.setSetOfSolution(s);
    }

    public void clearSinfoSetOf() {
        EngineRunner r = this.findRunner();
        r.clearSinfoSetOf();
    }


}

