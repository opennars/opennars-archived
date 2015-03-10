package nars.nlp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TupleGenerator<Type> {
    private static class TupleWithMeta<Type> {
        public TupleWithMeta(List<Type> content) {
            this.content = content;
            this.count = 1;
        }

        public List<Type> content;

        public int count; // how many times has this tuple apeared
    }

    public interface ICompare<Type> {
        boolean isEqual(List<Type> a, List<Type> b);
    }

    public void addTuplesOf(List<Type> list) {
        int startIndex;

        for(startIndex = 0; startIndex < list.size(); startIndex++) {
            int endIndex;

            for(endIndex = startIndex; endIndex < Math.min(startIndex+maxTupleSize, list.size()); endIndex++) {
                List<Type> part;

                part = extractPart(list, startIndex, endIndex);
                incrementCountOrAddFor(part);
            }
        }
    }

    /**
     * tries to find a TupleWithMeta with the same content
     * if it was found thee counter is incremented
     * if it was not found it is added
     *
     * \param part
     */
    private void incrementCountOrAddFor(List<Type> part) {
        for(TupleWithMeta<Type> iterationTupleWithMeta : tuples) {
            if(compare.isEqual(iterationTupleWithMeta.content, part)) {
                iterationTupleWithMeta.count++;
                return;
            }
        }
        
        // if we are here it wasn't found
        tuples.add(new TupleWithMeta<Type>(part));
    }


    private List<Type> extractPart(List<Type> list, int startIndex, int endIndex) {
        List<Type> result;
        int i;

        result = new ArrayList<>();

        for(i = startIndex;i < endIndex;i++) {
            result.add(list.get(i));
        }

        return result;
    }


    private ICompare<Type> compare;
    private List<TupleWithMeta<Type>> tuples = new ArrayList<TupleWithMeta<Type>>();
    private int maxTupleSize;
}
