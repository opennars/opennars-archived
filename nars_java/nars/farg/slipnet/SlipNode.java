/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.farg.slipnet;

import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.Bag;

/**
 *
 * @author patrick.hammer
 */
public class SlipNode extends Concept {

    public SlipNode(BudgetValue b, Term tm, Bag<SlipLink, Task> taskLinks, Bag<TermLink, TermLink> termLinks, Memory memory) {
        super(b, tm, taskLinks, termLinks, memory);
    }
    
    public boolean AreRelated(SlipNode other) {
        return this==other || AreLinked(other);
    }
    
    public boolean AreLinked(SlipNode other) {
        for(TaskLink outgoing : this.taskLinks) {
            if(outgoing.targetTask.sentence.content==other.term) {
                return true;
            }
        }
        for(TermLink outgoing : this.termLinks) {
            if(outgoing.target==other.term) {
                return true;
            }
        }
        return false;
    }
    
    public boolean IsActive() {
        return this.budget.summary()>0.6;
    }
    
}
