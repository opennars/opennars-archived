package nars.bag.impl.experimental.Compressed;

import nars.bag.impl.CacheBag;
import nars.concept.Concept;
import nars.term.Term;

import java.util.Iterator;

/**
 * Stores all Concepts in a machine near which is easy and fast to manipulate.
 *
 * Terminology:
 * #                : number of concepts
 * "concept index"  : each concept gets a unique number/index where it got allocated
 *
 * Basic functionality
 * ===================
 * The concept itself is encoded in a representation close to the machine.
 * Downward links are stored as "concept index" values directly in the concept representation.
 * Upward links are stored in Arrays associated with each concept.
 *
 * Memory layout (Level 1)
 * =======================
 * the first # of "concept index"es are reserved/later allocated for "root concept"s, like humming, hummingbird, bird.
 * the following # of "concept index"es are reserved/later allocated for "nonroot concept"s, like hummingbird-->bird
 *
 * each "concept index" slot has a boolean flag for the state of free/allocated (where free is invalid to reference)
 * each "concept index" slot has a list with the upward links, which are just "concept index"es.
 *
 * Memory layout (Level 0) (fragmented)
 * ====================================
 * The memory is managed in a memory cell O(1) indexable array of byte where each cell has a power two size (for example 128 bytes).
 * Because the length of a single concept in a concept slot can be unbounded large, there needs to be a way to allocate new memory
 * which is either defragmented (so the concept can be stored linearly in the array) or fragmented (where the readout of the concept
 * can be scattered in memory). Mixed modes are also possible.
 *
 * This all requires the usual memory allocation/reallocation/GC logic.
 *
 */
public class CompressedBag extends CacheBag<Term, Concept> {

    public CompressedBag(final int numberOfConcepts) {
        this.numberOfConcepts = numberOfConcepts;

        allocateAllConceptSlots();
    }

    @Override
    public void clear() {

    }

    @Override
    public Concept get(Term key) {
        return null;
    }

    @Override
    public Concept remove(Term key) {
        return null;
    }

    @Override
    public void put(Concept concept) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<Concept> iterator() {
        return null;
    }

    private void allocateAllConceptSlots() {
        conceptSlots = new ConceptSlot[numberOfConcepts*2];

        for( int conceptSlotI = 0; conceptSlotI < conceptSlots.length; conceptSlotI++ ) {
            conceptSlots[conceptSlotI] = new ConceptSlot();
        }
    }

    protected final int numberOfConcepts;

    protected ConceptSlot[] conceptSlots;
}
