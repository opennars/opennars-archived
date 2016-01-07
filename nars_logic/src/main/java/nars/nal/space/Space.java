package nars.nal.space;

import com.gs.collections.impl.list.mutable.primitive.FloatArrayList;
import nars.Op;
import nars.term.TermVector;
import nars.term.compound.GenericCompound;
import nars.util.data.Util;

import java.io.IOException;


/** linear combination of a sequence
 * of terms representing basis vectors of a vector space  */
public class Space extends GenericCompound {

    /** matches each of the subterms in their order */
    final FloatArrayList vector;
    final int hash2;

    public Space(TermVector subterms, float... f) {
        this(subterms, new FloatArrayList(f));
    }

    public Space(TermVector subterms, FloatArrayList vector) {
        super(Op.SPACE, -1, subterms);
        this.vector = vector;
        if (vector.size()!=subterms.size())
            throw new RuntimeException("invalid dimensions: " + subterms + " with " + vector.size());
        this.hash2 = Util.hashCombine( super.hashCode(), vector.hashCode() );
    }

    @Override
    public boolean equals(Object that) {
        return super.equals(that) &&
                ((Space)that).vector.equals(vector);
    }

    @Override
    public int hashCode() {
        return hash2;
    }

    @Override
    public int compareTo(Object that) {
        int c = super.compareTo(that);
        if (c == 0) {
            FloatArrayList a = vector;
            FloatArrayList b = ((Space)that).vector;
            if (a == b) return 0;
            //TODO maybe hash2 can be compared first for fast

            int n = a.size();
            for (int i = 0; i < n; i++) {
                int d = Float.compare(a.get(i), b.get(i));
                if (d != 0) return d;
            }
            return 0;
        }
        return c;
    }

    @Override
    public void appendArg(Appendable p, boolean pretty, int i) throws IOException {
        term(i).append(p, pretty);
        p.append('*');
        p.append(Float.toString(vector.get(i)));
    }
}
