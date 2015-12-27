package nars.term.transform;

import nars.term.Term;
import nars.term.compound.Compound;

import java.util.function.Predicate;

/** I = input term type, T = transformable subterm type */
public interface CompoundTransform extends Predicate<Term> {
    Term apply(Compound parent, Term subterm, int depth);

    /** enable predicate determined by the superterm, tested before processing any subterms */
    default boolean testSuperTerm(Compound terms) {
        return true;
    }

}
