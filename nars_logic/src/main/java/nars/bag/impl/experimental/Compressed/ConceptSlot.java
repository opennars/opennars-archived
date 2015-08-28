package nars.bag.impl.experimental.Compressed;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ConceptSlot {
    public enum EnumState {
        FREE,
        ALLOCATED
    }

    // currently its the index to the memory cell which contains the start of the data, can be replaced with indirection
    public int conceptIndex;

    public List<Integer> upwardLinks = new ArrayList<>();

    public EnumState state = EnumState.FREE;
}
