package nars.plugin.meta;

import nars.config.Parameters;
import nars.control.DerivationContext;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.Memory;

import java.util.HashSet;

import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import static nars.storage.Memory.isInputOrOperation;

public class MetaPlugin {
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
        HashSet<Task> already_attempted = new HashSet<>();
        for (int i = 0; i < Parameters.SEQUENCE_BAG_ATTEMPTS; i++) {
            Task takeout = usedSequenceTasks.takeNext();
            if (takeout == null) {
                break; //there were no elements in the bag to try
            }
            if (already_attempted.contains(takeout)) {
                usedSequenceTasks.putBack(takeout, memory.cycles(memory.param.sequenceForgetDurations), memory);
                continue;
            }
            already_attempted.add(takeout);
            memory.proceedWithTemporalInduction(newEvent.sentence, takeout.sentence, newEvent, nal, true);
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
