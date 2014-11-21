/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.coderack;

import nars.core.Memory;
import nars.core.control.DefaultAttention;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.farg.slipnet.SlipNode;

/**
 *
 * @author patrick.hammer
 */
public class Breaker extends Codelet {

    public Breaker(BudgetValue budget,Memory mem, Object args) {
        super(budget, mem, args);
    }
    
    @Override
    public void run() {
        
        if(Memory.randomNumber.nextDouble()<mem.emotion.happy()) {
            return;
        }
        
        SlipNode c=this.mem.concepts.sampleNextConcept();
        
        if(c!=null) {
            if(c.desires.isEmpty() || c.desires.get(0).truth.getExpectation()<0.5) {
                ((DefaultAttention)mem.concepts).takeOut(c.term);
            }
        }
    }
    
}
