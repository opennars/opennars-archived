/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.prolog;

import nars.Global;
import nars.Memory;
import nars.io.TextOutput;
import nars.model.impl.Default;
import nars.testing.TestNAR;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class NARPrologAgentTest {
    
    boolean prologAnswered = false;
    

    
    @Test
    public void testInheritance() throws Exception {
        testInheritance(4);
    }

    public void testInheritance(int n) throws Exception {
//        Memory.resetStatic(1);
//        System.out.println("\n\nnormal");
//        TestNAR narNormal = new TestNAR( new Default().setInternalExperience(null) );
//        long normalCycles = testInheritance(n, narNormal);

        Memory.resetStatic(1);
        System.out.println("\n\nprolog");
        TestNAR narProlog = new TestNAR( new Default().setInternalExperience(null) );
        NARPrologAgent p = new NARPrologAgent(narProlog, 0.7f, 0.9f, true, false);
        p.setReportAssumptions(true);
        p.setReportAnswers(true);
        long prologCycles = testInheritance(n, narProlog);

        //assertTrue(prologCycles < normalCycles);

    }

    public long testInheritance(int n, TestNAR nar) throws Exception {

        Global.DEBUG = true;


        int maxCycles = 2000;

        TextOutput.out(nar);

        for (int i = 0; i < n; i++) {
            int j = n - i;
            nar.believe("<x" + (j - 1) + " --> x" + (j) + ">.");
        }
        String solution = "<x1 --> x" + (n) + ">";
        nar.ask(solution + '?');

        nar.mustBelieve(maxCycles, solution, 1f, 1f, 0.01f, 1f);
        nar.run(maxCycles);

        return nar.time();

    }    
    
}
