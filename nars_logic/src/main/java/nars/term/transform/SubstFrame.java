package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.term.CommonVariable;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.Variable;
import nars.util.math.ShuffledPermutations;

import java.util.Map;

/**
 * Created by me on 11/15/15.
 */
public class SubstFrame {

    public final Map<Term, Term> xy;
    public final Map<Term, Term> yx;

    ShuffledPermutations perm;

    TermContainer compx, compy;
    //int pos;

    //Term current
    final SubstFrame parent;
    //public int len; //terms in the current compound pair
    public int limit;
    public int pos;

    boolean yxChanged = false;
    boolean xyChanged = false;

    public SubstFrame() {
        this.parent = null;
        this.xy = Global.newHashMap();
        this.yx = Global.newHashMap();
    }

    public SubstFrame(SubstFrame parent) {
        this.parent = parent;

        this.xy = Global.newHashMap(parent.xy.size());
        this.xy.putAll(parent.xy);

        this.yx = Global.newHashMap(parent.yx.size());
        this.yx.putAll(parent.yx);
    }

    public SubstFrame(Map<Term, Term> xy, Map<Term, Term> yx) {
        this.parent = null;
        this.xy = xy;
        this.yx = yx;
    }

    @Override
    public String toString() {
        return "Frame{" +
                //", xy=" + xy +
                //", yx=" + yx +
                ", cx=" + compx +
                ", cy=" + compy +
                //", parent=" + parent +
                //", limit=" + limit +
                ", pos=" + pos +
                ", perm=" + perm +
                '}';
    }

    final void nextVarX(Op type, final Variable xVar, final Term y) {
        final Op xOp = xVar.op();

        //boolean m = false;

        if (xOp == type) {
            putVarX(xVar, y);
        } else {
            final Op yOp = y.op();
            if (yOp == xOp) {
                putCommon(xVar, (Variable) y);
            }
        }

    }

    /**
     * elimination
     */
    protected final void putVarY(final Term x, final Variable yVar) {
    /*if (yVar.op()!=type) {
        throw new RuntimeException("tried to set invalid map: " + yVar + "->" + x + " but type=" + type);
    }*/
        yxPut(yVar, x);
        if (yVar instanceof CommonVariable) {
            xyPut(yVar, x);
        }
        //return true;
    }

    /**
     * elimination
     */
    private final void putVarX(final Variable xVar, final Term y) {
    /*if (xVar.op()!=type) {
        throw new RuntimeException("tried to set invalid map: " + xVar + "->" + y + " but type=" + type);
    }*/
        xyPut(xVar, y);
        if (xVar instanceof CommonVariable) {
            yxPut(xVar, y);
        }
        //return true;
    }


    protected final void putCommon(final Variable x, final Variable y) {
        final Variable commonVar = CommonVariable.make(x, y);
        xyPut(x, commonVar);
        yxPut(y, commonVar);
        //return true;
    }

    private final void yxPut(Variable y, Term x) {
        yxChanged |= (yx.put(y, x) != x);
    }

    private final void xyPut(Variable x, Term y) {
        xyChanged |= (xy.put(x, y) != y);
    }


    public int shuffledPos(int pos) {
        ShuffledPermutations p = this.perm;
        if (p == null) return pos;
        return p.get(pos);
    }
}
