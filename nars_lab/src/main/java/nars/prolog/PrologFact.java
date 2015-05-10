package nars.prolog;

import nars.Symbols;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.tuprolog.*;

import java.util.List;

/**
* Created by me on 2/19/15.
*/
public class PrologFact extends Operator {


    private final PrologContext context;

    protected PrologFact(PrologContext p) {
        super("^fact");
        this.context = p;
    }

    @Override
    protected List<Task> execute(Operation operation, Term[] args) {

        Prolog p = null; //default
        try {
            p = context.getProlog(null);
        } catch (InvalidLibraryException e) {
            e.printStackTrace();
            return null;
        }

        Sentence s = operation.getTask().sentence;
        if (s.punctuation == Symbols.GOAL) {
            PTerm factTerm = NARPrologMirror.pterm(args[0]).resolveTerm();

            if (factTerm == null) return null;

            if (factTerm instanceof Struct)
                try {
                    p.addTheory((Struct)factTerm);
                } catch (InvalidTheoryException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            else {
                throw new RuntimeException("Could not assert non-struct: " + factTerm);
            }
        }

        return null;
    }
}
