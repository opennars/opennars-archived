/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.build.Solid;
import nars.core.NAR;
import nars.gui.NARSwing;

import java.io.File;

/**
 *
 * @author me
 */
public class NARSwingNeuromorphic {
 
    public static void main(String[] args) throws Exception {
        int ants = 6;

        NAR n = new NAR(
                //new Neuromorphic(ants).simulationTime().setConceptBagSize(4096).setNovelTaskBagSize(100).setSubconceptBagSize(8192).setTaskLinkBagSize(50).setTermLinkBagSize(200)
                new Solid(1, 1024, 0, 9, 0, 3)
        );

        n.input(new File("/tmp/h.nal"));
        
        NARSwing.themeInvert();
        
        NARSwing s = new NARSwing(n);
        
        s.enableJMX();
        
        
        
    }
}