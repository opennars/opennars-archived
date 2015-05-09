package nars.prolog;

import nars.NAR;
import nars.model.impl.Default;
import nars.tuprolog.*;

import java.util.List;

/**
 * Created by me on 5/9/15.
 */
public class NARPrologStructuralInference {

    NAR n = new NAR(new Default());
    public final NARPrologMirror pl = new NARPrologMirror(n, 0.90f, true, true, false) {
        @Override
        public List<String> initAxioms() {
            List<String> l = super.initAxioms();
            l.add("rdf(S,P,O) :- inheritance(product(S,O),P).");


            //https://www.cpp.edu/~jrfisher/www/prolog_tutorial/2_15.html
            l.add("path(A,B,Path) :- travel(A,B,[A],Q),reverse(Q,Path).");
            l.add("travel(A,B,P,[B|P]) :- connected(A,B).");
            l.add("travel(A,B,Visited,Path) :- connected(A,C),C \\== B,\\+member(C,Visited),travel(C,B,[C|Visited],Path).");
            return l;
        }
    };

    public static void main(String[] args) throws MalformedGoalException, NoSolutionException, NoMoreSolutionException, InvalidTheoryException {
        NARPrologStructuralInference p = new NARPrologStructuralInference();
        p.solve("subject(X)");
        p.solve("[A,B]");
        p.solve("rdf(S,P,O)");
        p.solve("connected(A,B)");
        p.solve("path(A,B,C)");
        p.solve("path(a,d,C)");

        p.solve("rdf(a,b,c)");
        p.solve("not(X)");
        p.solve("is(inheritance(this, truthy),true)");
        p.solve("inheritance(this, truthy)=true");
        p.pl.apply("assert", "itis(something).");
        p.solve("is(itis(something),true)");
    }

    public NARPrologStructuralInference() {

        pl.setReportAnswers(true);
        pl.setReportAssumptions(true);


        n.input("<a --> b>.");
        n.run(1);
        n.input("<x <-> y>.");
        n.run(1);


        n.input("<a --> b>?");
        n.run(1);

        n.input("(a,b).");
        n.run(1);

        n.input("(b, a).");
        n.run(1);

        n.input("(b,c).");
        n.run(1);
        n.input("(c,d).");
        n.run(1);
        n.input("(x1,x2,x3,x4,c).");
        n.run(1);

        n.input("(--,<this --> truthy>).");
        n.run(1);

        n.input("<(*,subj,obj) --> predicate>.");
        n.run(1);

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
