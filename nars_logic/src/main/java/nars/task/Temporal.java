package nars.task;

import nars.Memory;
import nars.term.compound.Compound;
import nars.term.nal7.Interval;
import nars.term.nal7.Tense;

/**
 * interface for the temporal information about the
 * task to which this refers to.  used to separate
 * temporal tasks from non-temporal tasks
 */
public interface Temporal<T extends Compound> extends Tasked<T>, Interval {

    static boolean concurrent(java.time.temporal.Temporal a, java.time.temporal.Temporal b, int durationCycles) {
        return concurrent(a.getOccurrenceTime(), b.getOccurrenceTime(), durationCycles);
    }

    /**
     * whether two times are concurrent with respect ao a specific duration ("present moment") # of cycles
     */
    static boolean concurrent(long a, long b, int perceptualDuration) {
        //since Stamp.ETERNAL is Integer.MIN_VALUE,
        //avoid any overflow errors by checking eternal first

        if (a == Tense.ETERNAL) {
            //if both are eternal, consider concurrent.  this is consistent with the original
            //method of calculation which compared equivalent integer values only
            return (b == Tense.ETERNAL);
        } else if (b == Tense.ETERNAL) {
            return false; //a==b was compared above
        } else {
            return Tense.order(a, b, perceptualDuration) == Tense.ORDER_CONCURRENT;
        }
    }

    static boolean before(long a, long b, int perceptualDuration) {
        return Tense.after(b, a, perceptualDuration);
    }

    /** inner between: time difference of later.start() - earlier.end() */
    static int between(java.time.temporal.Temporal task, java.time.temporal.Temporal belief) {
        long tStart = task.start();
        long bStart = belief.start();

        java.time.temporal.Temporal earlier = tStart <= bStart ? task : belief;
        java.time.temporal.Temporal later = earlier == task ? belief : task;

        long a = earlier.end();
        long b = later.start();

        return (int)(b-a);
    }

    /** true if there is a non-zero overlap interval of the tasks */
    static boolean overlaps(Task a, Task b) {
        return Tense.overlaps(a.start(), a.end(), b.start(), b.end());
    }

    boolean isAnticipated();

    long getCreationTime();
    long getOccurrenceTime();

    void setOccurrenceTime(long t);


    default boolean concurrent(Task s, int duration) {
        return concurrent(s.getOccurrenceTime(), getOccurrenceTime(), duration);
    }

    default boolean startsAfter(Temporal other/*, int perceptualDuration*/) {
        long start = start();
        long other_end = other.end();
        return start - other_end > 0;
    }

    long start();
    long end();

    default long getLifespan(Memory memory) {
        long createdAt = getCreationTime();

        return createdAt >= Tense.TIMELESS ? memory.time() - createdAt : -1;

    }

    default boolean isTimeless() {
        return getOccurrenceTime() == Tense.TIMELESS;
    }

    default void setEternal() {
        setOccurrenceTime(Tense.ETERNAL);
    }

    default void setOccurrenceTime(Tense tense, int duration) {
        setOccurrenceTime(getCreationTime(), tense, duration);
    }

    default void setOccurrenceTime(long creation, Tense tense, int duration) {
        setOccurrenceTime(
            Tense.getOccurrenceTime(
                    creation,
                    tense,
                    duration));
    }
}
