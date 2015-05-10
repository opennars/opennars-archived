package nars.prolog;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.nal.stamp.Stamp;
import nars.tuprolog.Clause;
import nars.tuprolog.ClauseIndex;
import nars.tuprolog.Clauses;
import nars.tuprolog.PTerm;

import java.util.Iterator;

/**
 * Created by me on 5/9/15.
 */
public class NARTheoryAdapter implements Clauses {

    private final NAR nar;

    public NARTheoryAdapter(NAR n) {
        this.nar = n;
    }

    @Override
    public void addFirst(String key, Clause d) {
        System.out.println(key + " " + d);
        if (d!=null && d.getClause()!=null)
            nar.believe(0.5f, 0.25f, d.getClause(), Stamp.ETERNAL, 1f, 0.9f);

    }

    @Override
    public void addLast(String key, Clause d) {
        addFirst(key, d);
        //System.err.println(this + " addLast(" + key + " " + d);
    }

    @Override
    public ClauseIndex remove(String key) {
        System.err.println(this + " abolish(" + key + " ");
        return null;
    }

    @Override
    public Iterator<Clause> getPredicates(PTerm head) {
        System.err.println(this + " getPredicates(Term " + head);
        return null;
    }

    @Override
    public Iterator<Clause> getPredicates(String key) {
        System.err.println(this + " getPredicates(String " + key);
        return null;
    }

    @Override
    public Iterator<Clause> iterator() {
        System.err.println(this + " iterator()");
        return Iterators.emptyIterator();
    }

    @Override
    public boolean containsKey(String ctxID) {
        System.err.println(this + " containsKey(" + ctxID);
        return false;
    }

    @Override
    public ClauseIndex put(String ctxID, ClauseIndex index) {
        System.err.println(this + " put(" + ctxID + " " + index);
        return null;
    }

    @Override
    public ClauseIndex get(String ctxID) {
        System.err.println(this + " get(" + ctxID);
        return null;
    }

    @Override
    public void clear() {

    }
}
