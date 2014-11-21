/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.workspace;

import nars.core.EventEmitter.EventObserver;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.entity.BudgetValue;
import nars.farg.coderack.Breaker;
import nars.farg.coderack.Codelet;
import nars.farg.coderack.Coderack;

/**
 *
 * @author patrick.hammer
 */
public class Workspace {

    NAR nar;
    public Workspace(NAR nar) {
        this.nar=nar;
        codelets=new Coderack(FARGParameters.codelet_level,FARGParameters.max_codelets);
        codelets.putIn(new Breaker(new BudgetValue(0.9f,0.9f,0.5f),nar.memory,new int[]{1,2,3}));
        nar.on(CycleEnd.class, new EventObserver() { 

            @Override
            public void event(Class event, Object[] args) {
                Codelet cod=codelets.takeNext();
                cod.run();
                codelets.putIn(cod);
            }
            
        });
    }

    public double temperature() {
        return nar.memory.emotion.happy();
    }
        
    
    Coderack codelets;
}
