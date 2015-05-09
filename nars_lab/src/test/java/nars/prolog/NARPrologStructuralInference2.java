package nars.prolog;

import nars.NAR;
import nars.io.TextOutput;
import nars.model.impl.Default;
import nars.tuprolog.InvalidTheoryException;
import nars.tuprolog.MalformedGoalException;
import nars.tuprolog.NoMoreSolutionException;
import nars.tuprolog.NoSolutionException;

import java.util.List;

/**
 * Created by me on 5/9/15.
 */
public class NARPrologStructuralInference2 {

    NAR n = new NAR(new Default());
    public final NARPrologMirror pl = new NARPrologMirror(n, 0.90f, true, true, false) {
        @Override
        public List<String> initAxioms() {
            List<String> l = super.initAxioms();

            return l;
        }
    };

    public static void main(String[] args) throws MalformedGoalException, NoSolutionException, NoMoreSolutionException, InvalidTheoryException {
        NARPrologStructuralInference2 p = new NARPrologStructuralInference2();

    }

    public NARPrologStructuralInference2() {

        pl.setReportAnswers(true);
        pl.setReportAssumptions(true);


        TextOutput.out(n);

        n.input("<c --> d>.");
        n.input("<a --> b>.");
        n.input("<d --> e>.");
        n.input("<b --> c>.");
        n.input("<a --> e>?");

        n.run(100);

    }


    public void solve(String s) {


        try {
            boolean x = pl.solve(s, 0.05f, t -> {
                System.out.println(s + " ==== " + t);
            });
            if (!x) {
                System.out.println(s + " ==== NO SOLUTION");
            }
        } catch (InvalidTheoryException e) {
            e.printStackTrace();
        }


    }

}
