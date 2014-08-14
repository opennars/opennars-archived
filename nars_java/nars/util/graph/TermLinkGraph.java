package nars.util.graph;

import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.util.NARGraph;

/**
 * Generates a graph of a set of Concept's TermLinks.  Each TermLink is an edge, and the
 * set of unique Concepts and Terms linked are the vertices.
 */
public class TermLinkGraph extends NARGraph {
    
    
    
    public TermLinkGraph()  {
        super();        
    }
    
    public void add(Iterable<Concept> concepts, boolean includeConcepts, boolean includeTermLinks, boolean includeTaskLinks/*, boolean includeOtherReferencedConcepts*/) {
        
        for (final Concept c : concepts) {
            
            final Term source = c.term;
            
            if (!contains(source)) {
                if (includeConcepts)
                    add(source);

                if (includeConcepts && includeTermLinks) {
                    for (TermLink t : c.termLinks.values()) {
                        Term target = t.target;
                        
                        add(target);
                        
                        addEdge(new TermLinkEdge(source, target), false);
                    }
                }

                if (includeTaskLinks) {            
                    for (TaskLink t : c.taskLinks.values()) {
                        Task task = t.targetTask;
                        
                        add(task);
                        
                        if (includeConcepts)
                            addEdge(new TermTask(source, task), false);                    
                        
                        /*Term target = t.target;
                        if (target!=null) {
                            add(target);
                        }*/

                        if (task.parentTask!=null) {
                            add(task.parentTask);
                            addEdge(new TaskInherit(task, task.parentTask), false);
                        }
                    
                    }            
                }

            }
        }
        
        
    }

    
    public boolean includeLevel(int l) {
        return true;
    }

}
