package nars.nar;

import nars.Memory;
import nars.NAR;
import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.Deriver;
import nars.nal.nal8.OperatorReaction;
import nars.nal.nal8.operator.NullOperator;
import nars.op.mental.*;
import nars.task.flow.ImmediateTaskPerception;
import nars.task.flow.TaskPerception;
import nars.term.Term;
import nars.time.FrameClock;
import nars.util.data.random.XorShift1024StarRandom;
import java.util.Random;
import java.util.function.Consumer;

public class Simple extends NAR {

    public final Minimi core;
    public final TaskPerception input;

    //legacy shit to make Memory and the NAR happy ^^
    public Simple() { this(new Memory(new FrameClock(), CacheBag.memory(0)), 0, 0, 0,0); }
    public NAR forEachConcept(Consumer<Concept> recip) {return this;} //this NAR doesn't support it
    public Simple nal(int maxNALlevel) { memory.nal(maxNALlevel); return this;}
    public String toString() {return "Minimi";}
    protected Concept doConceptualize(Term term, Budget b) {
        return null; //there are no concepts, so ^^
    }

    //we use the standard deriver
    public Deriver getDeriver() {
        return Deriver.standardDeriver;
    }

    public void initDefaults(Memory m) {
        //parameter defaults
        m.duration.set(5);
        m.derivationThreshold.set(0);
        m.taskProcessThreshold.set(0); //warning: if this is not zero, it could remove un-TaskProcess-able tasks even if they are stored by a Concept
        //budget propagation thresholds
        m.executionExpectationThreshold.set(0.5);
        m.shortTermMemoryHistory.set(1);
    }

    public Simple(Memory memory, int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        super(memory);
        getDeriver().load(memory);
        initDefaults(memory);
        the("input", input = initInput()); //legacy shit:
        the("core", core = new Minimi(this, getDeriver()));
        beforeNextFrame(() -> { //register mechanisms
            memory.the(new STMTemporalLinkage(this, getDeriver()));
            memory.the(new Anticipate(this)); //and ops
            OperatorReaction[] exampleOperators = new OperatorReaction[]{ new NullOperator("break"),
                    new NullOperator("drop"), new NullOperator("goto"), new NullOperator("open"), new NullOperator("pick"),
                    new NullOperator("strike"), new NullOperator("throw"), new NullOperator("activate"), new NullOperator("deactivate")
            };
            for (OperatorReaction o : exampleOperators)
                onExec(o); });
    }

    @Override
    public Concept apply(Term term) {return null; }

    public ImmediateTaskPerception initInput() {
        ImmediateTaskPerception input = new ImmediateTaskPerception( false, this, task -> true, task -> process(task));
        return input;
    }
}
