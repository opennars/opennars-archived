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
import nars.farg.workspace.Workspace;

/**
 *
 * @author patrick.hammer
 */
public class Breaker extends Codelet {

    public Breaker(BudgetValue budget,Memory mem, Object args) {
        super(budget, mem, args);
    }
    
    @Override
    public void run(Workspace ws) {
        
        if(Memory.randomNumber.nextDouble()*0.2f>ws.tenperature) { //temperature too low
            return;
        }
        
        
        
        SlipNode c=this.mem.concepts.sampleNextConcept();
        
        if(c==null || Memory.randomNumber.nextDouble()*5.0<c.getPriority()) //priority too high
            return;
        
        double n=ws.n_concepts;
        
        if(n<100) {
            return; //really too less concepts we dont want to forget all..
        }
        
        double AMOUNT_OF_CONCEPTS_MAX=1000; //TODO create parameter or get somehow
        
        if(Memory.randomNumber.nextDouble()*AMOUNT_OF_CONCEPTS_MAX<n)
            return; //too less concepts
  

          // if(c.desires.isEmpty() || c.desires.get(0).truth.getExpectation()<0.5) {
                ((DefaultAttention)mem.concepts).takeOut(c.term);
            //}
    }
    
}
