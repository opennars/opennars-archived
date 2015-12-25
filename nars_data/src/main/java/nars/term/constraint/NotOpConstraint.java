package nars.term.constraint;

import nars.Op;
import nars.term.Term;
import nars.term.transform.Subst;

/**
 * Created by me on 12/13/15.
 */
public final class NotOpConstraint implements MatchConstraint {

    private final int op;

    public NotOpConstraint(Op o) {
        this(o.bit());
    }

    public NotOpConstraint(int opVector) {
        this.op = opVector;
    }

    @Override
    public boolean invalid(Term assignee, Term value, Subst f) {
        return value.op().isA(op);
    }
    @Override
    public String toString() {
        return "op!=" + Integer.toHexString(op);
    }
}

