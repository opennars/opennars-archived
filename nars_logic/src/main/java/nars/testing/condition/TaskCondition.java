package nars.testing.condition;


import nars.Events;
import nars.Global;
import nars.NAR;
import nars.io.Texts;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.nal7.Tense;
import nars.nal.stamp.Stamp;
import nars.nal.term.Term;
import nars.narsese.InvalidInputException;
import nars.util.language.JSON;

import java.io.Serializable;
import java.util.*;


public class TaskCondition extends OutputCondition implements Serializable {

    //@Expose
    public final Class channel;
    //@Expose
    public final Term term;
    //@Expose
    public final char punc;

    //@Expose
    public final float freqMin;
    //@Expose
    public final float freqMax;
    //@Expose
    public final float confMin;
    //@Expose
    public final float confMax;
    //@Expose
    public final long cycleStart; //-1 for not compared
    //@Expose
    public final long cycleEnd;  //-1 for not compared

    private final boolean relativeToCondition; //whether to measure occurence time relative to the compared task's creation time, or the condition's creation time

    //@Expose
    private long creationTime;


    //@Expose
    public Tense tense = Tense.Eternal;

    public final List<Task> exact = new ArrayList();
    final int maxExact = 4;

    public final Deque<Task> removals = new ArrayDeque();

    int maxClose = 7;

    public final TreeMap<Double,Task> close = new TreeMap();
    int maxRemovals = 2;

    //enable true for more precise temporality constraints; this may be necessary or not
    private boolean strictDurationWindow = true;


    public TaskCondition(NAR n, Class channel, Task t, long creationTimeOffset, final boolean relativeToCondition)  {
        super(n, Events.OUT.class, Events.TaskRemove.class, channel);

        //TODO verify that channel is included in the listened events

        this.channel = channel;
        this.relativeToCondition = relativeToCondition;



        if (t.sentence.isEternal()) {
            setEternal();
            t.getStamp().setTime(creationTimeOffset, Stamp.ETERNAL);
        }
        else {
            long oc = t.getOccurrenceTime(); //relative occurenceTime of the original task which may not be at the given creationTimeOffset
            setRelativeOccurrenceTime(oc, n.memory.duration());
            t.getStamp().setTime(creationTimeOffset, oc);
        }


        this.cycleStart = this.cycleEnd = -1;
        if (t.sentence.truth!=null) {
            float f = t.sentence.truth.getFrequency();
            float c = t.sentence.truth.getConfidence();
            float e = Global.TESTS_TRUTH_ERROR_TOLERANCE/ 2.0f; //error tolerance epsilon
            this.freqMin = f - e;
            this.freqMax = f + e;
            this.confMin = c - e;
            this.confMax = c + e;
        }
        else {
            this.freqMin = this.freqMax =this.confMin = this.confMax = -1;
        }
        this.punc = t.sentence.punctuation;
        this.term = t.getTerm();
    }

    public TaskCondition(NAR n, Class channel, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        super(n, channel);

        this.relativeToCondition = false;

        if (freqMax < freqMin) throw new RuntimeException("freqMax < freqMin");
        if (confMax < confMin) throw new RuntimeException("confMax < confMin");

        if (cycleEnd - cycleStart < 1) throw new RuntimeException("cycleEnd must be after cycleStart by at least 1 cycle");

        this.creationTime = n.time();
        this.channel = channel;
        this.cycleStart = cycleStart;
        this.cycleEnd = cycleEnd;
        setEternal();
        this.freqMax = Math.min(1.0f, freqMax);
        this.freqMin = Math.max(0.0f, freqMin);
        this.confMax = Math.min(1.0f, confMax);
        this.confMin = Math.max(0.0f, confMin);
        this.punc = punc;
        this.term = n.term(sentenceTerm);
    }

    public TaskCondition(NAR n, Class<Events.IN> inClass, Task t, int cycle, boolean b, int similarResultsToSave) {
        this(n, inClass, t, cycle, b);

        this.maxClose = similarResultsToSave;

        if (similarResultsToSave == 0) {
            this.maxRemovals = 0;
        }
    }


    //continue examining output task in case a closer result appears
    @Override
    protected boolean continueAfterSuccess() { return true; }

    public double getAcceptableDistanceThreshold() {
        return 0.01;
    }

    //how many multiples of the range it is away from the acceptable time interval
    public static double rangeError(double value, double min, double max, boolean squash) {
        double dt;
        if (value < min)
            dt = min - value;
        else if (value > max)
            dt = value - max;
        else
            return 0;

        double result = dt/(max-min);

        if (squash)
            return Math.tanh(result);
        else
            return result;
    }

    //time distance function
    public double getTimeDistance(long now) {
        return rangeError(now, cycleStart, cycleEnd, true);
    }

    //truth distance function
    public double getTruthDistance(Truth t) {
        //manhattan distance:
        return rangeError(t.getFrequency(), freqMin, freqMax, true) +
                rangeError(t.getConfidence(), confMin, confMax, true);

        //we could also calculate geometric/cartesian vector distance
    }

    /** task's tense is compared by its occurence time delta to the current time when processing */
    public void setRelativeOccurrenceTime(long ocRelative, int duration) {
        //may be more accurate if duration/2

        if (ocRelative > duration/2) tense = Tense.Future;
        if (ocRelative < -duration/2) tense = Tense.Past;
        else tense = Tense.Present;
    }
    public void setRelativeOccurrenceTime(long creationTime, int ocRelative, int duration) {
        this.creationTime = creationTime;
        setRelativeOccurrenceTime(ocRelative, duration);
    }

    public void setEternal() {
        this.tense = Tense.Eternal;
    }
    public boolean isEternal() { return this.tense == Tense.Eternal; }

    public boolean matches(Task task) {
        if (task == null || task.sentence == null) {
            return false;
        }
        if (task.sentence.punctuation != punc)
            return false;
        //long now = nar.time();

        Term tterm = task.getTerm();

        //require exact term
        if (!tterm.equals(this.term)) {
            return false;
        }

        return true;

    }

    @Override
    public void event(Class channel, Object... args) {
        if (!succeeded && (channel == Events.TaskRemove.class)) {
            Task task = (Task)args[0];
            //String rule = (String)args[1];

            if (matches(task)) {
                removals.addLast(task);
                if (removals.size() > maxRemovals)
                    removals.removeFirst();
            }
        }

        super.event(channel, args);


    }

    @Override
    public boolean condition(Class channel, Object signal) {

        if (channel == this.channel) {

            if (signal instanceof Task) {

                Task task = (Task) signal;

                if (!matches(task)) return false;

                double distance = 0;
                long now = nar.time();


                boolean match = true;

                if ( ((cycleStart!=-1) && (now < cycleStart)) ||
                        ((cycleEnd!=-1) && (now > cycleEnd)))  {
                    distance += getTimeDistance(now);
                    match = false;
                }


                float tenseCost = 0.35f;
                float temporalityCost = 0.75f;

                //require right kind of tense
                if (isEternal()) {
                    if (!task.sentence.isEternal()) {
                        distance += temporalityCost;
                        match = false;
                    }
                }
                else {
                    if (task.sentence.isEternal()) {
                        distance += temporalityCost - 0.01;
                        match = false;
                    }
                    else {

                            final long oc = task.getOccurrenceTime();

                            final int durationWindow = task.sentence.getStamp().getDuration();

                            final int durationWindowNear = durationWindow / 2;
                            final int durationWindowFar = strictDurationWindow ? durationWindowNear : durationWindow;


                            long at = relativeToCondition ? creationTime : task.getCreationTime();

                            final boolean tmatch;
                            switch (tense) {
                                case Past: tmatch = oc <= (-durationWindowNear + at); break;
                                case Present: tmatch = oc >= (-durationWindowFar + at) && (oc <= +durationWindowFar + at); break;
                                case Future: tmatch = oc > (+durationWindowNear + at); break;
                                default:
                                    throw new RuntimeException("Invalid tense for non-eternal TaskCondition: " + this);
                            }
                            if (!tmatch) {
                                //beyond tense boundaries
                                //distance += rangeError(oc, -halfDur, halfDur, true) * tenseCost;
                                distance += tenseCost + rangeError(oc, creationTime, creationTime, true); //error distance proportional to occurence time distance
                                match = false;
                            }
                            else {
                                //System.out.println("matched time");
                            }

                    }

                }


                //TODO use range of acceptable occurrenceTime's for non-eternal tests


                if ((punc == '.') || (punc == '!')) {
                    float fr = task.sentence.truth.getFrequency();
                    float co = task.sentence.truth.getConfidence();

                    if ((co > confMax) || (co < confMin) || (fr > freqMax) || (fr < freqMin)) {
                        match = false;
                        distance += getTruthDistance(task.sentence.truth);
                    }
                }

                if (!match && distance < getAcceptableDistanceThreshold())
                    match = true;

                if (match) {
                    //TODO record a different score for fine-tune optimization?
                    if (exact.size() < maxExact)
                        exact.add(task);
                }

                if (distance > 0) {
                    close.put(distance, task);
                    if (close.size() > maxClose) {
                        close.remove( close.lastEntry().getKey() );
                    }
                }

                return match;

            }
        }
        return false;
    }


    @Override
    public String getFalseReason() {
        String x = "Unmatched; ";

        if (!close.isEmpty()) {
            x += "Similar:\n";
            for (Map.Entry<Double,Task> et : close.entrySet()) {
                Task tt = et.getValue();
                x += Texts.n4(et.getKey().floatValue()) + ' ' + tt.toString() + ' ' + tt.getHistory() + '\n';
            }
        }
        else {
            x += "No similar\n";
        }
        if (!removals.isEmpty()) {
            x += "Matching removals:\n";
            for (Task t : removals)
                x += t.toString() + ' ' + t.getHistory() + '\n';
        }
        return x;
    }

    public Truth getTruthMean() {
        return new Truth.DefaultTruth(0.5f * (freqMax + freqMin), 0.5f * (confMax + confMin));
    }

    @Override
    public List<Task> getTrueReasons() {
        return exact;
        //if (!isTrue()) throw new RuntimeException(this + " is not true so has no true reasons");
        /*return Lists.newArrayList("match at: " +

                Iterables.transform(trueAt, new Function<Task, String>() {
                    @Override
                    public String apply(Task task) {
                        return task.toString() + " @ " + task.sentence.getCreationTime();
                    }
                }));
                */
        //return exact;
    }

    @Override
    public String toString() {
        return succeeded  +": "  + JSON.stringFrom(this);
    }

    public long getCreationTime() {
        return creationTime;
    }


}