/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.coderack;

import nars.core.Memory;
import nars.core.control.DefaultAttention;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.farg.slipnet.SlipNode;
import nars.farg.workspace.Workspace;
import nars.inference.TemporalRules;
import nars.language.Implication;

/**
 *
 * @author patrick.hammer
 */
public class Evaluater extends Codelet {

    public Evaluater(BudgetValue budget,Memory mem, Object args) {
        super(budget, mem, args);
    }
    
    @Override
    public void run(Workspace ws) {
        
        SlipNode c=this.mem.concepts.sampleNextConcept();
        if(c.term instanceof Implication && ((Implication)c.term).getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
            Concept d=ws.nar.memory.concept(((Implication)c.term).getPredicate());
            if(d!=null && !d.desires.isEmpty() && d.desires.get(0).truth.getExpectation()>0.6) {
                c.incPriority(0.5f); //it is desired so also increase the priority of the implication which has it as consequence!
            }
        }
        
        if(!c.term.toString().contains("^") && !(c.term instanceof Implication)) { //not about any influence and not implication? decrease priority..
            c.decPriority(0.5f);
        }
    }
    
}
