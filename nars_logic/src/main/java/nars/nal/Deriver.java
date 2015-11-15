package nars.nal;

import nars.Memory;
import nars.Premise;
import nars.process.ConceptProcess;
import nars.task.Task;

import java.util.function.Consumer;

/**
 *
 * Implements a strategy for managing submitted derivation processes
 * ( via the run() methods )
 *
 * Created by patrick.hammer on 30.07.2015.
 */
abstract public class Deriver  {

    public final DerivationRules rules;


    public Deriver(DerivationRules rules) {
        this.rules = rules;
    }

    abstract protected void forEachRule(final RuleMatch match, Consumer<Task> receiver);

    /** runs a ConceptProcess (premise) and supplies
     *  a consumer with all resulting derived tasks.
     *  this method does not provide a way to stop or interrupt
     *  the process once it begins.
     */
    public final void run(Premise premise, Consumer<Task> t) {
        premise.memory().eventConceptProcess.emit((ConceptProcess)premise);

        RuleMatch m = RuleMatch.matchers.get();
        m.start(premise);

        forEachRule(m, t);
    }


    public synchronized void load(Memory memory) {
        for (int i = 0; i < rules.size(); i++) {
            rules.get(i).normalized(memory.terms);
        }
    }
}
