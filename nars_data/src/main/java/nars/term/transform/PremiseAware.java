package nars.term.transform;

import nars.term.Term;
import nars.term.compound.Compound;

public interface PremiseAware {
	Term function(Compound args, Subst r);
}
