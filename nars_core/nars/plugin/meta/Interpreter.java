package nars.plugin.meta;

import nars.language.CompoundTerm;
import nars.language.Product;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

public class Interpreter {
    final static String maintainDisappointedAnticipationsIndirectedPseudoOp = "pseudo-maintainDisappointedAnticipationsIndirected";

    Memory memory;

    public void entry(CompoundTerm ct, CallContext callContext) {
        Term t = ct.term[0]; // HACK< because we can't pass in the meta-(update) operator family raw (pseudo)operations because it doesn't make much sense now, TODO< fix this > >

        //if( ct.isExecutable(memory) ) {
        //    // TODO< call operator >

        /*Operation op=(Operation)ct;
            Operator operator = op.getOperator();
            Product args = op.getArguments();

            String operatorName = operator.name().toString();
            if( operatorName == maintainDisappointedAnticipationsIndirectedPseudoOp ) {
                callContext.concept.maintainDisappointedAnticipationsIndirected();
            }*/
        //}

        String x = t.name().toString();

        if( t.name().toString().equals("pseudoop-maintainDisappointedAnticipationsIndirected") ) {
            callContext.concept.maintainDisappointedAnticipationsIndirected();
        }
    }
}
