package nars.prolog;

import nars.Events;
import nars.NAR;
import nars.tuprolog.*;
import nars.tuprolog.event.*;

/**
 * Wraps a Prolog instance loaded with nal.pl with some utility methods
 */
public class NARTuprolog extends DefaultProlog implements OutputListener, WarningListener, TheoryListener, QueryListener {

    private final NAR nar;

    public NARTuprolog(NAR n) throws InvalidLibraryException {
        super("nars.tuprolog.lib.BasicLibrary");


        addOutputListener(this);
        addTheoryListener(this);
        addWarningListener(this);
        addQueryListener(this);


        this.nar = n;
    }


    public SolveInfo query(PTerm s, double time) {
        return this.solve(s, time);
    }

    
    @Override public void onOutput(OutputEvent e) {        
        nar.emit(Prolog.class, e.getMsg());
    }
    
    @Override
    public void onWarning(WarningEvent e) {
        if (nar.memory.event.isActive(Events.ERR.class))
            nar.emit(Events.ERR.class, e.getMsg() + ", from " + e.getSource());
        else
            System.err.println(e.getMsg() + " from " + e.getSource());
    }

    @Override
    public void theoryChanged(TheoryEvent e) {
        nar.emit(Prolog.class, e.toString());
    }
    
    @Override
    public void newQueryResultAvailable(QueryEvent e) {
        nar.emit(Prolog.class, e.getSolveInfo());
    }

}
