package nars.entity;

import nars.core.Memory;
import nars.farg.slipnet.SlipNode;
import nars.language.Term;



public interface ConceptBuilder {
    public SlipNode newConcept(BudgetValue b, Term t, Memory m);    
}
