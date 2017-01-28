package nars.plugin.meta;

import nars.NAR;
import nars.config.Parameters;
import nars.control.DerivationContext;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Bag;
import nars.storage.Memory;
import nars.util.EventEmitter;
import nars.util.Events;
import nars.util.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MetaPlugin implements Plugin {
    InduceSucceedingEvent2Observer induceSucceedingEvent2Observer;
    MetaMaintainDisappointedAnticipationsObserver metaMaintainDisappointedAnticipationsObserver;
    CodeRefactoringImmigrants immigrants = new CodeRefactoringImmigrants();
    Interpreter interpreter;

    public MetaPlugin() {
        initLookupByTriggerType();
    }


    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        final Memory memory = n.memory;
        immigrants.memory = memory;
        interpreter = new Interpreter();
        interpreter.memory = memory;

        if( induceSucceedingEvent2Observer==null ) {
            induceSucceedingEvent2Observer = new InduceSucceedingEvent2Observer(immigrants);
        }

        if( metaMaintainDisappointedAnticipationsObserver == null ) {
            metaMaintainDisappointedAnticipationsObserver = new MetaMaintainDisappointedAnticipationsObserver(this);
        }

        memory.event.set(induceSucceedingEvent2Observer, enabled, Events.InduceSucceedingEvent2.class);
        memory.event.set(metaMaintainDisappointedAnticipationsObserver, enabled, Events.MetaMaintainDisappointedAnticipations.class);

        Operator metaUpdateOperator = memory.getOperator("^meta-update");
        if (metaUpdateOperator == null) {
            metaUpdateOperator = memory.addOperator(new MetaUpdateOperator(this));
        }

        return true;
    }

    // @concept the concept for which the maintainDisappointedAnticipations() method was triggered
    void handleEventMetaMaintainDisappointedAnticipations(Concept concept) {
        // trigger all which react to MaintainDisappointedAnticipations
        for( CompoundTerm iReaction : lookupByTriggerType.get(EnumTriggerType.MAINTAINDISAPPOINTEDANTICIPATIONS) ) {
            interpreter.entry(iReaction, new CallContext().setConcept(concept));
        }
    }

    // TODO< as dictionary >
    HashMap<EnumTriggerType, ArrayList<CompoundTerm>> lookupByTriggerType = new HashMap<>();

    private void initLookupByTriggerType() {
        lookupByTriggerType.put(EnumTriggerType.INDUCESUCCEEDINGEVENT2, new ArrayList<>());
        lookupByTriggerType.put(EnumTriggerType.MAINTAINDISAPPOINTEDANTICIPATIONS, new ArrayList<>());
    }

    enum EnumTriggerType {
        INDUCESUCCEEDINGEVENT2,
        MAINTAINDISAPPOINTEDANTICIPATIONS,
    }
}

class InduceSucceedingEvent2Observer implements EventEmitter.EventObserver {
    InduceSucceedingEvent2Observer(CodeRefactoringImmigrants immigrants) {
        this.immigrants = immigrants;
    }

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

class MetaMaintainDisappointedAnticipationsObserver implements EventEmitter.EventObserver {
    MetaPlugin metaPlugin;
    MetaMaintainDisappointedAnticipationsObserver(MetaPlugin metaPlugin) {
        this.metaPlugin = metaPlugin;
    }

    @Override
    public void event(Class event, Object[] args) {
        // check should be not neccesary
        // TODO< terrorize team and make this NOP go away >
        if (event != Events.MetaMaintainDisappointedAnticipations.class) {
            return;
        }

        Concept concept = (Concept)args[0];
        metaPlugin.handleEventMetaMaintainDisappointedAnticipations(concept);
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

class MetaUpdateOperator extends Operator {

    public MetaUpdateOperator(MetaPlugin metaPlugin) {
        super("^meta-update");
        this.metaPlugin = metaPlugin;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
        // TODO< add name to identify this >

        // HACK< for now we just set the MetaMaintainDisappointedAnticipations reaction >
        metaPlugin.lookupByTriggerType.get(MetaPlugin.EnumTriggerType.MAINTAINDISAPPOINTEDANTICIPATIONS).add((CompoundTerm)args[0]);

        return null;
    }

    private MetaPlugin metaPlugin;
}

