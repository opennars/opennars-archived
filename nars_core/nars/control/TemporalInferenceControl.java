/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.control;

import java.util.HashSet;
import nars.config.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.operator.Operation;
import nars.util.Events;

/**
 *
 * @author patrick.hammer
 */
public class TemporalInferenceControl {
    public static boolean proceedWithTemporalInduction(final Sentence newEvent, final Sentence stmLast, Task controllerTask, DerivationContext nal, boolean SucceedingEventsInduction) {
        
        if(SucceedingEventsInduction && !controllerTask.isElemOfSequenceBuffer()) { //todo refine, add directbool in task
            return false;
        }
        if (newEvent.isEternal() || !controllerTask.isInputOrOperation()) {
            return false;
        }
        /*if (equalSubTermsInRespectToImageAndProduct(newEvent.term, stmLast.term)) {
            return false;
        }*/
        
        if(newEvent.punctuation!=Symbols.JUDGMENT_MARK || stmLast.punctuation!=Symbols.JUDGMENT_MARK)
            return false; //temporal inductions for judgements only
        
        nal.setTheNewStamp(newEvent.stamp, stmLast.stamp, nal.memory.time());
        nal.setCurrentTask(controllerTask);

        Sentence previousBelief = stmLast;
        nal.setCurrentBelief(previousBelief);

        Sentence currentBelief = newEvent;

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        TemporalRules.temporalInduction(currentBelief, previousBelief, nal, SucceedingEventsInduction);
        return false;
    }

    public static void eventInference(DerivationContext nal) {
         //also attempt direct
        double SEQUENCE_BAG_PRIORITY_TRESHOLD = 0.0; //no need to keep them for now for easier visualization
        HashSet<Task> already_attempted = new HashSet<Task>();
        for(int i = 0 ;i<Parameters.SEQUENCE_BAG_ATTEMPTS;i++) {
            Task takeout = nal.memory.sequenceTasks.takeNext();
            if(takeout == null) {
                break; //there were no elements in the bag to try
            }
            takeout.setElemOfSequenceBuffer(true);
            if(already_attempted.contains(takeout)) {
                nal.memory.sequenceTasks.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
                continue;
            }
            already_attempted.add(takeout);

            Task takeout2 = nal.memory.sequenceTasks.takeNext();
            if(takeout2 == null) {
                nal.memory.sequenceTasks.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
                break; //there were no elements in the bag to try
            }
            takeout2.setElemOfSequenceBuffer(true);

            try {
                proceedWithTemporalInduction(takeout2.sentence, takeout.sentence, takeout2, nal, true);
            } catch (Exception ex) {
                if(Parameters.DEBUG) {
                    System.out.println("issue in temporal induction");
                }
            }
            if(takeout.getPriority() > SEQUENCE_BAG_PRIORITY_TRESHOLD) {
                nal.memory.sequenceTasks.putBack(takeout, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
            }
            if(takeout2.getPriority() > SEQUENCE_BAG_PRIORITY_TRESHOLD) {
                nal.memory.sequenceTasks.putBack(takeout2, nal.memory.cycles(nal.memory.param.sequenceForgetDurations), nal.memory);
            }
        }

        /*for (int i = 0; i < 10; ++i) System.out.println();
        System.out.println("----------");
        for(Task t : nal.memory.sequenceTasks) {
            System.out.println(t.sentence.getTerm().toString()+ " " +String.valueOf(t.getPriority()));
        }
        System.out.println("^^^^^^^^^");*/
    }
    
    public static void addToSequenceTasks(DerivationContext nal, final Task newEvent) {

        float periority_penalty = 1.0f;
        if(newEvent.getTerm() instanceof Conjunction) {
            Conjunction term = ((Conjunction)newEvent.getTerm());
            if(!(term.term[term.term.length-1] instanceof Operation)) {
                periority_penalty *= Parameters.NOT_OPERATION_SEQUENCE_END_PENALTY;
            }
            if(term.term[0] instanceof Operation) { //also try to start with a condition
                periority_penalty *= Parameters.OPERATION_SEQUENCE_START_PENALTY;
            }
        }
        if(newEvent.getTerm() instanceof Implication  ||
                newEvent.getTerm() instanceof Equivalence) { //allow everything except this in event bag
            return;
        }

        //multiple versions are necessary, but we do not allow duplicates
        Task removal = null;
        do
        {
            removal = null;
            for(Task s : nal.memory.sequenceTasks) {
                if(CompoundTerm.cloneDeepReplaceIntervals(s.getTerm()).equals(
                        CompoundTerm.cloneDeepReplaceIntervals(newEvent.getTerm()))) {
                        // && //-- new outcommented
                        //s.sentence.stamp.equals(newEvent.sentence.stamp,false,true,true,false) ) {
                    //&& newEvent.sentence.getOccurenceTime()>s.sentence.getOccurenceTime() ) { 
                    removal = s;
                    break;
                }
            }
            if(removal != null) {
                nal.memory.sequenceTasks.take(removal);
            }
        }
        while(removal != null);
        //ok now add the new one:
        //making sure we do not mess with budget of the task:
        Task t2 = new Task(newEvent.sentence, new BudgetValue(1.0f*periority_penalty,1.0f/(float)newEvent.sentence.term.getComplexity(),0.1f), newEvent.getParentTask(), newEvent.getParentBelief(), newEvent.getBestSolution());
        //we use a event default budget here so the time it appeared and whether it was selected is key criteria currently divided by complexity
        if(newEvent.isInput()) {
            t2.parentTask = null;
        }
        nal.memory.sequenceTasks.putIn(t2);

        //debug:
        /*System.out.println("---------");
        for(Task t : this.sequenceTasks) {
            System.out.println(t.getTerm().toString());
        }
        System.out.println("^^^^^^");*/
    }
}
