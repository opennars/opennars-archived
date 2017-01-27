package nars.plugin.meta;

import nars.NAR;
import nars.config.Parameters;
import nars.control.DerivationContext;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.Memory;
import nars.util.EventEmitter;
import nars.util.Events;
import nars.util.Plugin;

import java.util.HashSet;

public class MetaPlugin implements Plugin {
    InduceSucceedingEvent2Observer induceSucceedingEvent2Observer;
    CodeRefactoringImmigrants immigrants = new CodeRefactoringImmigrants();

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        final Memory memory = n.memory;
        immigrants.memory = memory;

        if( induceSucceedingEvent2Observer==null ) {
            induceSucceedingEvent2Observer = new InduceSucceedingEvent2Observer();
        }

        memory.event.set(induceSucceedingEvent2Observer, enabled, Events.InduceSucceedingEvent2.class);

        return true;
    }
}

class InduceSucceedingEvent2Observer implements EventEmitter.EventObserver {
    CodeRefactoringImmigrants immigrants;

    @Override
    public void event(Class event, Object[] args) {
        // check should be not neccesary
        // TODO< terrorize team and make this NOP go away >
        if (event != Events.InduceSucceedingEvent2.class) {
            return;
        }

        int debug0 = 0;

        Task newEvent = (Task)args[0];
        DerivationContext nal = (DerivationContext)args[1];
        immigrants.eventInference_NotifedBy_InduceSucceedingEvent2(newEvent, nal, immigrants.memory.sequenceTasks);
    }
}

class CodeRefactoringImmigrants {
    // wrapper for not jet alienated variables
    //Bag<Task<Term>,Sentence<Term>> getSequenceTasks() {
    //    return memory.sequenceTasks;
    //}

    public Memory memory;

    // from Concept.java : public boolean eventInference(final Task newEvent, DerivationContext nal)
    void eventInference_NotifedBy_InduceSucceedingEvent2(final Task newEvent, DerivationContext nal,  Bag<Task<Term>,Sentence<Term>> usedSequenceTasks) {
        /*for (Task stmLast : stm) {
            Concept OldConc = this.concept(stmLast.getTerm());
            if(OldConc != null)
            {
                TermLink template = new TermLink(newEvent.getTerm(), TermLink.TEMPORAL);
                if(OldConc.termLinkTemplates == null)
                    OldConc.termLinkTemplates = new ArrayList<>();
                OldConc.termLinkTemplates.add(template);
                OldConc.buildTermLinks(newEvent.getBudget()); //will be built bidirectionally anyway
            }
        }*/

        //also attempt direct
        HashSet<Task> already_attempted = new HashSet<Task>();
        for(int i =0 ;i<Parameters.SEQUENCE_BAG_ATTEMPTS;i++) {
            Task takeout = usedSequenceTasks.takeNext();
            if(takeout == null) {
                break; //there were no elements in the bag to try
            }
            if(already_attempted.contains(takeout)) {
                usedSequenceTasks.putBack(takeout, memory.cycles(memory.param.sequenceForgetDurations), memory);
                continue;
            }
            already_attempted.add(takeout);
            try {
                memory.proceedWithTemporalInduction(newEvent.sentence, takeout.sentence, newEvent, nal, true);
            } catch (Exception ex) {
                if(Parameters.DEBUG) {
                    System.out.println("issue in temporal induction");
                }
            }
            usedSequenceTasks.putBack(takeout, memory.cycles(memory.param.sequenceForgetDurations), memory);
        }
        //for (Task stmLast : stm) {
        // proceedWithTemporalInduction(newEvent.sentence, stmLast.sentence, newEvent, nal, true);
        //}

        memory.addToSequenceTasks(newEvent);

        /*System.out.println("----------");
        for(Task t : this.sequenceTasks) {
            System.out.println(t.sentence.getTerm().toString()+ " " +String.valueOf(t.getPriority()));
        }
        System.out.println("^^^^^^^^^");*/
    }
}
