/*
 * Memory.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars;


import com.github.fge.grappa.run.ParseRunner;
import com.github.fge.grappa.run.ParsingResult;
import com.gs.collections.api.tuple.Twin;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.TermIndex;
import nars.nal.nal8.Execution;
import nars.process.ConceptProcess;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.nal7.Tense;
import nars.time.Clock;
import nars.truth.Truth;
import nars.util.data.random.XorShift128PlusRandom;
import nars.util.event.DefaultTopic;
import nars.util.event.EventEmitter;
import nars.util.event.Topic;
import nars.util.meter.EmotionMeter;
import nars.util.meter.LogicMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Memory consists of the run-time state of a NAR, including: * term and concept
 * memory * clock * reasoner state * etc.
 * <p>
 * Excluding input/output channels which are managed by a NAR.
 * <p>
 * A memory is controlled by zero or one NAR's at a given time.
 * <p>
 * Memory is serializable so it can be persisted and transported.
 */
public class Memory extends Param {

    protected Atom self;

    public final Random random;

    @Deprecated
    public final transient EventEmitter<Class, Object[]> event;

    public final transient Topic<Task> eventTaskRemoved = new DefaultTopic<>();
    public final transient Topic<ConceptProcess> eventConceptProcess = new DefaultTopic<>();
    public final transient Topic<Task> eventRevision = new DefaultTopic<>();

    public final transient Topic<Memory> eventReset = new DefaultTopic<>();

    public final transient Topic<NAR> eventFrameStart = new DefaultTopic<>();

    /**
     * fired at the end of each memory cycle
     */
    public final transient Topic<Memory> eventCycleEnd = new DefaultTopic<>(); //eventCycleStart; //new DefaultObserved();

    public final transient Topic<Task> eventTaskProcess = new DefaultTopic<>();

    public static final Logger logger = LoggerFactory.getLogger(Memory.class);

    /**
     * used for reporting or informing outside. consists of additional notes
     * or data which could annotate a log or summary of system activity
     */
    public final transient Topic<Serializable> eventSpeak = new DefaultTopic<>();



    public final transient Topic<Task> eventInput = new DefaultTopic<>();
    public final transient Topic<Serializable> eventError = new DefaultTopic<>();

    /** all derivations, even if they do not eventually reach the memory via input;
     *  this generates many events, use with caution
     */
    public final transient Topic<Task> eventDerived = new DefaultTopic<>();

    public final transient Topic<Twin<Task>> eventAnswer = new DefaultTopic<>();
    public final transient Topic<Concept> eventConceptChange = new DefaultTopic();

    /** executables (incl. operators) */
    public final transient Map<Term, Topic<Execution>> exe = new HashMap();



    //TODO move these to separate components, not part of Memory:
    public final transient EmotionMeter emotion;
    public final transient LogicMeter logic;

    public final Clock clock;

    /** holds known Term's and Concept's */
    public final TermIndex index;


    /** maximum NAL level currently supported by this memory, for restricting it to activity below NAL8 */
    int level;

    /** for creating new stamps
     * TODO move this to and make this the repsonsibility of Clock implementations
     * */
    long currentStampSerial = 1;


    public Memory(Clock clock, TermIndex index) {
        this(clock, new XorShift128PlusRandom(1), index);
    }

    /**
     * Create a new memory
     */
    public Memory(Clock clock, Random rng, TermIndex index) {

        random = rng;

        level = 8;

        this.clock = clock;
        clock.clear(this);

        this.index = index;


        self = Global.DEFAULT_SELF; //default value

        event = new EventEmitter.DefaultEventEmitter();


        //temporary
        logic = new LogicMeter(this);
        emotion = new EmotionMeter(this);


    }

    public static Task makeTask(Memory memory, float[] b, Term content, Character p, Truth t, Tense tense) {

//        if (p == null)
//            throw new RuntimeException("character is null");
//
//        if ((t == null) && ((p == JUDGMENT) || (p == GOAL)))
//            t = new DefaultTruth(p);
//
        int blen = b!=null ? b.length : 0;
//        if ((blen > 0) && (Float.isNaN(b[0])))
//            blen = 0;
//

        if (!(content instanceof Compound)) {
            return null;
        }

        if (t == null) {
            t = memory.newDefaultTruth(p);
        }

        MutableTask ttt =
                new MutableTask(content)
                                .punctuation(p)
                                .truth(t)
                                .time(
                                    memory.time(), //creation time
                                    Tense.getOccurrenceTime(
                                        memory.time(),
                                        tense,
                                        memory.duration()
                                    ));

        switch (blen) {
            case 0:     /* do not set, Memory will apply defaults */ break;
            case 1:
                if ((p == Symbols.QUEST || p==Symbols.QUESTION)) {
                    ttt.budget(b[0],
                            memory.getDefaultDurability(p),
                            memory.getDefaultQuality(p));

                } else {
                    ttt.budget(b[0],
                            memory.getDefaultDurability(p));
                }
                break;
            case 2:     ttt.budget(b[1], b[0]); break;
            default:    ttt.budget(b[2], b[1], b[0]); break;
        }

        return ttt;
    }

    /**
     * gets a stream of raw immutable task-generating objects
     * which can be re-used because a Memory can generate them
     * ondemand
     */
    public static void tasks(String input, Consumer<Task> c, Memory m) {
        tasksRaw(input, o -> {
            Task t = decodeTask(m, o);
            if (t == null) {
                m.eventError.emit("Invalid task: " + input);
            } else {
                c.accept(t);
            }
        });
    }

    /** supplies the source array of objects that can construct a Task */
    public static void tasksRaw(String input, Consumer<Object[]> c) {

        ParsingResult r = Narsese.the().inputParser.run(input);

        int size = r.getValueStack().size();

        for (int i = size-1; i >= 0; i--) {
            Object o = r.getValueStack().peek(i);

            if (o instanceof Task) {
                //wrap the task in an array
                c.accept(new Object[] { o });
            }
            else if (o instanceof Object[]) {
                c.accept((Object[])o);
            }
            else {
                throw new RuntimeException("Unrecognized input result: " + o);
            }
        }
    }

    public static Task spawn(Task parent, Compound content, char punctuation, Truth truth, long occ, Budget budget) {
        return spawn(parent, content, punctuation, truth, occ, budget.getPriority(), budget.getDurability(), budget.getQuality());
    }

    public static Task spawn(Task parent, Compound content, char punctuation, Truth truth, long occ, float p, float d, float q) {
        return new MutableTask(content, punctuation)
                .truth(truth)
                .budget(p, d, q)
                .parent(parent)
                .occurr(occ);
    }

    /**
     * parse one task
     */
    public static Task task(ParseRunner singleTaskParser, String input, Memory memory) throws Narsese.NarseseException {
        ParsingResult r;
        try {
            r = singleTaskParser.run(input);
        }
        catch (Throwable ge) {
            //ge.printStackTrace();
            throw new Narsese.NarseseException(ge.toString() + ' ' + ge.getCause() + ": parsing: " + input);
        }

        if (r == null)
            throw new Narsese.NarseseException("null parse: " + input);


        try {
            return decodeTask(memory, (Object[])r.getValueStack().peek() );
        }
        catch (Exception e) {
            throw Narsese.newParseException(input, r, e);
        }
    }

    /** returns null if the Task is invalid (ex: invalid term) */
    public static Task decodeTask(Memory m, Object[] x) {
        if (x.length == 1 && x[0] instanceof Task)
            return (Task)x[0];
        return makeTask(m, (float[])x[0], (Term)x[1], (Character)x[2], (Truth)x[3], (Tense)x[4]);
    }

    /** returns number of tasks created */
    public static int tasks(String input, Collection<Task> c, Memory m) {
        int[] i = new int[1];
        tasks(input, t -> {
            c.add(t);
            i[0]++;
        }, m);
        return i[0];
    }


    @Override
    public final int nal() {
        return level;
    }

    public final void nal(int newLevel) {
        level = newLevel;
    }


//    public Concept concept(final String t) {
//        return concept(Atom.the(t));
//    }

//    /** provides fast iteration to concepts with questions */
//    public Set<Concept> getQuestionConcepts() {
//        return questionConcepts;
//    }
//
//    /** provides fast iteration to concepts with goals */
//    public Set<Concept> getGoalConcepts() {
//        throw new RuntimeException("disabled until it is useful");
//        //return goalConcepts;
//    }


    public final Atom self() {
        return self;
    }

    public void setSelf(Atom t) {
        self = t;
    }


//    public void delete() {
//        clear();
//
//        event.delete();
//    }


    @Override
    public synchronized void clear() {


        eventReset.emit(this);

        clock.clear(this);

        //NOTE: allow stamp serial to continue increasing after reset.
        //currentStampSerial = ;

        //questionConcepts.clear();

        index.clear();

        //goalConcepts.clear();

        emotion.clear();


    }


    /**
     * Get an existing (active OR forgotten) Concept identified
     * by the provided Term
     */
    public final Concept concept(Termed t) {
        //careful with this if multiple nars share termed's they may introduce conflicting concepts into each other's reasoners
        if (t instanceof Concept)
            return ((Concept)t);

        Term u = t.term().normalized();
        if (u == null) return null;

        Termed tt = index.get(u);
        if (tt instanceof Concept)
            return ((Concept)tt);
        return null;
    }



//    public void add(final Iterable<Task> source) {
//        for (final Task t : source)
//            add((Task) t);
//    }

    /** current temporal perception duration of the reasoner */
    public final int duration() {
        return duration.intValue();
    }






    /* ---------- new task entries ---------- */

    /**
     * called anytime a task has been removed, deleted, discarded, ignored, etc.
     */
    public final void remove(Task task, Object removalReason) {

        boolean willBeReceived = !eventTaskRemoved.isEmpty();

        if (willBeReceived && removalReason!=null)
            task.log(removalReason);

        if (!task.isDeleted()) {

            task.getBudget().delete();

            if (willBeReceived) {

                /*if (Global.DEBUG_DERIVATION_STACKTRACES && Global.DEBUG_TASK_LOG)
                    task.log(Premise.getStack());*/

                eventTaskRemoved.emit(task);
            }
            /* else: a more destructive cleanup of the discarded task? */
        }

    }

//    /**
//     * sends an event signal to listeners subscribed to channel 'c'
//     */
//    final public void emit(final Class c, final Object... signal) {
//        event.emit(c, signal);
//    }

//    /**
//     * sends an event signal to listeners subscribed to channel 'c'
//     */
//    final public void emit(final Class c) {
//        event.emit(c);
//    }


    /**
     * produces a new stamp serial #, used to uniquely identify inputs
     */
    public final long newStampSerial() {
        //TODO maybe AtomicLong ?
        return currentStampSerial++;
    }

//    /** whether the NAR is currently accepting new inputs */
//    public boolean isInputting() {
//        if (inputPausedUntil == -1) return true;
//        return time() >= inputPausedUntil;
//    }


//    /**
//     * samples a next active concept for processing;
//     * may return null if no concept is available depending on the control system
//     */
//    public Concept nextConcept() {
//        return getCycleProcess().nextConcept();
//    }
//
//    /** scan for a next concept matching the predicate */
//    public Concept nextConcept(Predicate<Concept> pred, float v) {
//        return getCycleProcess().nextConcept(pred, v);
//    }



//    /**
//     * TODO return value
//     */
//    public void delete(Term term) {
//        Concept c = concept(term);
//        if (c == null) return;
//
//        delete(c);
//    }

//    /** queues the deletion of a concept until after the current cycle ends.
//     */
//    public synchronized void delete(Concept c) {
//        if (!inCycle()) {
//            //immediately delete
//            _delete(c);
//        }
//        else {
//            pendingDeletions.add(c);
//        }
//
//    }
//
//    /** called by Memory at end of each cycle to flush deleted concepts */
//    protected void deletePendingConcepts() {
//        if (!pendingDeletions.isEmpty()) {
//
//            for (Concept c : pendingDeletions)
//                _delete(c);
//
//            pendingDeletions.clear();
//        }
//    }
//
//    /**
//     * actually delete procedure for a concept; removes from indexes
//     * TODO return value
//     */
//    protected void delete(Concept c) {
//
////        Concept removedFromActive = getCycleProcess().remove(c);
////
////        if (c!=removedFromActive) {
////            throw new RuntimeException("another instances of active concept " + c + " detected on removal: " + removedFromActive);
////        }
//
//        Concept removedFromIndex = concepts.remove(c.getTerm());
//        if (removedFromIndex == null) {
//            throw new RuntimeException("concept " + c + " was not removed from memory");
//        }
//        /*if (c!=removedFromIndex) {
//            throw new RuntimeException("another instances of concept " + c + " detected on removal: " + removedFromActive);
//        }*/
//
//        c.delete();
//    }


//    @Override
//    public final boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (!(obj instanceof Memory)) return false;
//        return Memory.equals(this, (Memory) obj);
//    }

//    public static boolean equals(final Memory a, final Memory b) {
//
//        //TODO
//        //for now, just accept that they include the same set of terms
//
//        Set<Term> aTerm = new LinkedHashSet();
//        Set<Term> bTerm = new LinkedHashSet();
//        a.concepts.forEach(ac -> aTerm.add(ac.getTerm()));
//        b.concepts.forEach(bc -> bTerm.add(bc.getTerm()));
//        if (!aTerm.equals(bTerm)) {
//            /*System.out.println(aTerm.size() + " " + aTerm);
//            System.out.println(bTerm.size() + " " + bTerm);*/
//            return false;
//        }
//
//
//        /*if (!a.concepts.equals(b.concepts)) {
//
//        }*/
//        return true;
//    }

    public final long time() {
        return clock.time();
    }

//    public final void put(final Concept c) {
//        concepts.put(c);
//    }

    public final TermIndex getIndex() {
        return index;
    }

    public final void cycle(int num) {

        //final Clock clock = this.clock;
        Topic<Memory> eachCycle = eventCycleEnd;

        //synchronized (clock) {

            for (; num > 0; num--) {


                eachCycle.emit(this);

            }

        //}

    }


//    /** called when a Concept's lifecycle has changed */
//    public void updateConceptState(Concept c) {
//        boolean hasQuestions = c.hasQuestions();
//        boolean hasGoals = !c.getGoals().isEmpty();
//
//        if (isActive(c)) {
//            //index an incoming concept with existing questions or goals
//            if (hasQuestions) updateConceptQuestions(c);
//            //if (hasGoals) updateConceptGoals(c);
//        }
//        else  {
//            //unindex an outgoing concept with questions or goals
//            if (hasQuestions) questionConcepts.remove(c);
//            //..
//        }
//
//    }
//
//    /** handles maintenance of concept question/goal indices when concepts change according to reports by certain events
//        called by a Concept when its questions state changes (becomes empty or becomes un-empty) */
//    public void updateConceptQuestions(Concept c) {
//        if (!c.hasQuestions() && !c.hasQuests()) {
//            if (!questionConcepts.remove(c))
//                throw new RuntimeException("Concept " + c + " never registered any questions");
//        }
//        else {
//            if (!questionConcepts.add(c)) {
//                throw new RuntimeException("Concept " + c + " aready registered existing questions");
//            }
//
//            //this test was cycle.size() previously:
//            if (questionConcepts.size() > getCycleProcess().size()) {
//                throw new RuntimeException("more questionConcepts " +questionConcepts.size() + " than concepts " + getCycleProcess().size());
//            }
//        }
//    }
//
//    public void updateConceptGoals(Concept c) {
//        //TODO
//    }


    //    private String toStringLongIfNotNull(Bag<?, ?> item, String title) {
//        return item == null ? "" : "\n " + title + ":\n"
//                + item.toString();
//    }
//
//    private String toStringLongIfNotNull(Item item, String title) {
//        return item == null ? "" : "\n " + title + ":\n"
//                + item.toStringLong();
//    }
//
//    private String toStringIfNotNull(Object item, String title) {
//        return item == null ? "" : "\n " + title + ":\n"
//                + item.toString();
//    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ':' + nal() + "[@" + time() + ",C=" + size() + ']';
    }

    public final int size() {
        return index.size();
    }

//    /**
//     * identifies the type of memory as a string
//     */
//    String toTypeString() {
//        return getClass().getSimpleName();
//    }

    public void start() {
        index.start(this);

    }



    //    public byte[] toBytes() throws IOException, InterruptedException {
//        //TODO probably will want to do something more careful
//        return new JBossMarshaller().objectToByteBuffer(this);
//    }

    //public Iterator<Concept> getConcepts(boolean active, boolean inactive) {
//        if (active && !inactive)
//            return getControl().iterator();
//        else if (!active && inactive)
//            return Iterators.filter(concepts.iterator(), c -> isActive(c));
//        else if (active && inactive)
//            return concepts.iterator(); //'concepts' should contain all concepts
//        else
//            return Iterators.emptyIterator();
//    }
}

