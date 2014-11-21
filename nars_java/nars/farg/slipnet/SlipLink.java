/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.slipnet;

import nars.entity.BudgetValue;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;

/**
 *
 * @author patrick.hammer
 */
public class SlipLink extends TaskLink {

    public SlipLink(Task t, TermLink template, BudgetValue v, int recordLength) {
        super(t, template, v, recordLength);
    }
    
    public double DegreeOfAssociation() {
        return this.budget.summary();
    }
}
