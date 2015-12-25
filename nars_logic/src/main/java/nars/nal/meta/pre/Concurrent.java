package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.task.Task;
import nars.task.Temporal;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends PreCondition {

    public static final Concurrent the = new Concurrent();

    protected Concurrent() {
    }

    @Override
    public final String toString() {
        return "concurrent"; //getClass().getSimpleName();
    }

    @Override
    public final boolean test(RuleMatch m) {
        Premise premise = m.premise;

        if (!premise.isEvent())
            return false;

        Task task = premise.getTask();
        Task belief = premise.getBelief();

        //return task.concurrent(belief, m.premise.duration());
        return Temporal.overlaps(task, belief);
    }

}
