package nars.event.exe;

import nars.event.AbstractExecutive;
import nars.nal.Concept;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;


public class DesireThresholdExecutive extends AbstractExecutive {



    @Override
    protected boolean decide(Concept c, Task executableTask) {
        Term term = executableTask.getTerm();
        if ((term instanceof Operation) && (c.isDesired())) {

            Operation op = (Operation)term;
            op.setTask(executableTask);

            Operator oper = op.getOperator();

            return oper.execute(op, c.memory);
        }

        return false;

        //emit(Events.UnexecutableGoal.class, t, this, nal);
    }


}