package nars.gui.output;

import nars.core.NAR;
import nars.util.NARGraph;
import nars.util.graph.TermLinkGraph;

/**
 *
 * @author me
 */


public class TermLinkGraphPanel {

    public TermLinkGraphPanel(final NAR nar) {
        new ProcessingGraphPanel(nar) {

            TermLinkGraph t;
            
            long lastClock = -1;
            
            public TermLinkGraph newGraph() {
                TermLinkGraph s = new TermLinkGraph();
                s.add(nar.memory.concepts, false, false, true);
                return s;
            }
            
            @Override
            public NARGraph getGraph(ProcessingGraphPanel p) {
                
                if (t == null) {
                    t = newGraph();
                    
                    //if (autoupdate) ..
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while (true) {            
                                try {
                                    
                                    long now = nar.getTime();
                                    
                                    if (now != lastClock) {


                                        TermLinkGraph s = newGraph();
                                                
                                        t = s;                                    
                                        p.update();

                                        Thread.sleep(2500);

                                        lastClock = nar.getTime();
                                    }
                                    
                                } catch (InterruptedException ex) {
                                }
                            }
                        }
                        
                    }).start();
                
                }
                
                
                return t;
            }
                        
        };
                

    }

    
}
