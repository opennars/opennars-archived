package nars.nar;

import com.gs.collections.api.tuple.Pair;
import com.gs.collections.api.tuple.Twin;
import nars.NAR;
import nars.Premise;
import nars.bag.impl.CurveBag;
import nars.budget.BudgetFunctions;
import nars.nal.Deriver;
import nars.nal.nal8.Operation;
import nars.process.TaskBeliefProcess;
import nars.task.DefaultTask;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import nars.util.data.list.FasterList;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.event.Active;

import java.io.Serializable;
import java.util.*;

/**
 * Created by patrick.hammer on 18.02.2016.
 */
public class Minimi implements Serializable  {

    public Minimi(NAR nar, Deriver deriver) {
        super();

        final TreeMap<Float,Task> mem = new TreeMap<Float,Task>();
        final List<Task> derivedTasksBuffer = new FasterList();
        final Random rng = new XorShift1024StarRandom(1);

        new Active().add(
                nar.memory.eventCycleEnd.on((m) -> {
                    try {
                        while(mem.size()>20) {
                            Task ret = (Task) mem.pollFirstEntry();
                        }
                        if (mem.keySet().size() > 0) {

                            CurveBag bag = new CurveBag(mem.size(), nar.memory.random);
                            for (final Float ft : mem.keySet()) {
                                bag.mergeMax();
                                for (Task t : mem.values()) {
                                    t.getBudget().setPriority(ft);
                                    bag.put(t);
                                }
                            }
                            for(int i=0;i<10;i++) {
                                final Task task = (Task) bag.pop(); //mem.get(ft);
                                final Task belief = (Task) bag.pop();

                                //final Task belief = mem.get(f);
                                TaskBeliefProcess tbp = null;

                                if (task != belief && belief.isJudgment()) {
                                    tbp = new TaskBeliefProcess(nar, task, belief);
                                    deriver.run(tbp, derivedTasksBuffer::add);

                                    if (tbp != null && (task.isQuestion() || task.isGoal() || task.isQuest()) && task.getTerm().equals(belief.getTerm())) {
                                        if (belief != task.getBestSolution() && Premise.match(task, belief, tbp) != null) {
                                            task.setBestSolution(belief, nar.memory);
                                            nar.memory.eventAnswer.emit(new Twin<Task>() {
                                                @Override
                                                public Task getOne() {
                                                    return task;
                                                }

                                                @Override
                                                public Task getTwo() {
                                                    return belief;
                                                }

                                                //legacy shit:
                                                @Override public void put(Map<Task, Task> map) {}
                                                @Override public Map.Entry<Task, Task> toEntry() { return null;}
                                                @Override public int compareTo(Pair<Task, Task> taskTaskPair) { return 0;}
                                                @Override public Twin<Task> swap() {return null;}
                                            });
                                        }
                                    }
                                }
                            }
                            //recreate memory

                            TreeMap<Float,Task> newmem = new TreeMap<Float, Task>();
                            for(Float key : mem.keySet()) {
                                Task el = mem.get(key);
                                Float priority2 = key*0.95f; //global forgetting
                                newmem.put(priority2,el);
                            }
                            mem.clear(); //cant just replace instance since we are in lambda ^^
                            for(Float key: newmem.keySet()) {
                                mem.put(key,newmem.get(key));
                            }

                            derivedTasksBuffer.forEach(t -> Memput(t.getPriority(), ProcessTask(nar, t, mem), mem, rng));
                            derivedTasksBuffer.clear();
                        }
                    }catch(Exception ex) {} //recovery ^^
                }),
                nar.memory.eventInput.on((m) -> {
                    final DefaultTask task = (DefaultTask) m;
                    if(task.isInput()) {
                        task.setPriority(0.5f);
                        task.setDurability(0.5f); //<- not yet used but deriver kicks it out if zero!
                    }
                    try {
                        potentialExec(task, nar);
                        Memput(task.getPriority(), task, mem, rng);
                    }catch(Exception ex) {}
                })
        );
    }

    public void Memput(float priority, Task t, TreeMap<Float,Task> mem, Random rng) {
        ArrayList<Float> removeme = new ArrayList<Float>();
        Truth revise = null;
        TreeMap<Float, Task> toupdate = new TreeMap<>();
        for(Float key : mem.keySet()) {
            Task inside = mem.get(key);
            if(inside.getTerm().equals(t.getTerm()) && inside.getPunctuation() == t.getPunctuation()) {
                //its task of same content, revise!!
                removeme.add(key);
                if(inside.getTruth()!=null) {
                    revise = TruthFunctions.revision(t.getTruth(),inside.getTruth());
                }
            } else {
                Compound iside = inside.getTerm();
                for(Term elem : iside.cloneTerms()) {
                    if(elem.equals(iside.getTerm())) {
                        toupdate.put(key,inside);
                    }
                }
            }
        }
        for(Float f : removeme) {
            mem.remove(f);
        }
        if(revise!=null) {
            t.setTruth(revise);
        }
        float prionew = t.getPriority();
        if(t.getTruth()!=null) {
            prionew = t.getTruth().getExpectation();
        }
        mem.put(prionew+rng.nextFloat()*0.01f, t);

        for(Float f : toupdate.keySet()) {
            Task ret = mem.get(f);
            mem.remove(f);
            ret.getBudget().setPriority(BudgetFunctions.or(ret.getBudget().getPriority(),t.getPriority()));
            mem.put(ret.getBudget().getPriority(),ret);
        }
    }

    public Task ProcessTask(NAR nar, Task task, TreeMap<Float,Task> mem) {
        nar.input(task);
        potentialExec(task, nar);
        return task;
    }

    public void potentialExec(Task task, NAR nar) {
        if(task.isGoal() && task.getTerm() instanceof Operation) {
            nar.execute((DefaultTask) task);
        }
    }
}