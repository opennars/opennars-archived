package ptrman.difficultyEnvironment.TestBed;

import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SyncOperator;
import nars.task.Task;

import java.util.List;

/**
 *
 */
public class Action extends SyncOperator {
    private final int id;
    private final String name;
    private final NarsInteractionContainer interactionContainer;

    public Action(NarsInteractionContainer interactionContainer, int id, String name) {
        super(name);

        this.name = name;
        this.id = id;
        this.interactionContainer = interactionContainer;
    }

    @Override
    public List<Task> apply(Task<Operation> operationTask) {
        interactionContainer.triggeredActionIds.add(id);
        System.out.println("NAR decide " + name);
        return null;
    }
}
