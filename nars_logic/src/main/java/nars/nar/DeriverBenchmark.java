package nars.nar;

import nars.NAR;
import nars.process.TaskBeliefProcess;
import nars.task.Task;
import nars.util.data.list.FasterList;

import java.util.List;

/**
 * Created by patrick.hammer on 23.02.2016.
 */
public class DeriverBenchmark {
    final static List<Task> derivedTasksBuffer = new FasterList();
    public static void main(String[] args) {
        final Default nar = new Default();
        final Task t1 = nar.inputTask("<chess --> competition>.");
        final Task t2 = nar.inputTask("<sport --> competition>.");

        TaskBeliefProcess tbp = new TaskBeliefProcess(nar, t1, t2);


        long startTime = System.nanoTime();
        for(int i=0;i<25000;i++) {
            nar.getDeriver().run(tbp, derivedTasksBuffer::add);
            derivedTasksBuffer.clear();
        }
        long endTime  = System.nanoTime();
        long ms = (endTime - startTime) / 1000000;
        System.out.println(ms);
    }
}
