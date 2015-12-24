package nars.term.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

public class intersect extends BinaryTermOperator {

    @Override public Term apply(Term a, Term b) {
        return TermContainer.intersect(
                a.op(), (Compound) a, (Compound) b
        );
    }

}
