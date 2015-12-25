package nars.term.transform;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.match.PatternCompound;
import nars.term.variable.Variable;
import org.jetbrains.annotations.NotNull;

public interface Subst {

	boolean isEmpty();

	Term getXY(Object t);

	void clear();

	/** match a range of subterms of Y */
	static Term[] collect(Compound y, int from, int to) {
        int s = to-from;
        Term[] m = new Term[s];
        for (int i = 0; i < s; i++) {
            int k = i+from;
            m[i] = y.term(k);
        }

        return m;
    }

	static boolean isSubstitutionComplete(Term a, Op o) {
        return o == Op.VAR_PATTERN ? !Variable.hasPatternVariable(a) : !a.hasAny(o);
    }

    @Deprecated default boolean matchPermute(PatternCompound patternCompound, @NotNull Compound<Term> y) {
        throw new RuntimeException("unsupported but this is depr anyway");
    }
    @Deprecated default boolean matchCompoundWithEllipsis(PatternCompound patternCompound, @NotNull Compound<Term> y) {
        throw new RuntimeException("unsupported but this is depr anyway");
    }


    // default ImmediateTermTransform getTransform(Operator t) {
	// return null;
	// }

	//
	// boolean match(final Term X, final Term Y);
	//
	// /** matches when x is of target variable type */
	// boolean matchXvar(Variable x, Term y);
	//
	// /** standard matching */
	// boolean next(Term x, Term y, int power);
	//
	// /** compiled matching */
	// boolean next(TermPattern x, Term y, int power);
	//
	// void putXY(Term x, Term y);
	// void putYX(Term x, Term y);
	//

}
