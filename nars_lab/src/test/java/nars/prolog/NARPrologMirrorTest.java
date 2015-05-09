/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.prolog;

import nars.Global;
import nars.NAR;
import nars.io.TextOutput;
import nars.model.impl.Default;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.term.Term;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class NARPrologMirrorTest {
    
    boolean prologAnswered = false;
    

    
    @Test
    public void testMultistep() throws Exception {


        Global.DEBUG = true;

        NAR nar = new NAR( new Default().setInternalExperience(null) );

        NARPrologMirror p = new NARPrologMirror(nar, 0.1f, true, true, true) {

            @Override
            protected void onQuestion(Sentence s) {
                super.onQuestion(s);
                //System.err.println("QUESTION: " + s);
            }
            
            
            
            @Override
            public Term answer(Task question, Term t, nars.tuprolog.Term pt) {
                Term r = super.answer(question, t, pt);

                //look for <a --> d> answer
                //if (t.equals(aInhd))
                prologAnswered = true;
                assertTrue(true);
                
                return r;
            }
            
            
        };        


        TextOutput.out(nar);

        int n  = 16;
        for (int i = 0; i < n; i++) {
            int j = n - i;
            nar.input("<x" + (j-1) + " --> x" + (j) + ">.");
        }
        nar.input("<x1 --> x" + (n) + ">?");
        nar.run(n*2);

//        //nal1.multistep.nal
//        NALPerformance nts = new NALPerformance(nar, LibraryInput.get(nar, "other/nars_multistep_2.nal").getSource()) {
////
////
////            @Override
////            public NAR newNAR() {
////
////                Term aInhd;
////                try {
////                    aInhd = new Narsese(nar).parseTerm("<a --> d>");
////                } catch (Narsese.InvalidInputException ex) {
////                    assertTrue(false);
////                    return null;
////                }
////
////                if (prolog) {
////
////                }
////
////                return nar;
////            }
////
//
//        };

        
        

        
        assertTrue(prologAnswered);
        
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-MultiStep-edited.txt")));
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-NAL1-edited.txt")));
//        nar.addInput(new TextInput(new File("nal/test/nal1.multistep.nal")));
//        nar.finish(10);
        
        
    }    
    
}
