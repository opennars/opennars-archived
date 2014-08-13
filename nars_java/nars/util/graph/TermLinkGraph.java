//package nars.util.graph;
//
//import nars.entity.Concept;
//import nars.entity.Task;
//import nars.entity.TaskLink;
//import nars.entity.TermLink;
//import nars.language.Term;
//import nars.util.NARGraph;
//
///**
// * Generates a graph of a set of Concept's TermLinks.  Each TermLink is an edge, and the
// * set of unique Concepts and Terms linked are the vertices.
// */
//public class TermLinkGraph extends NARGraph {
//    
//    public TermLinkGraph()  {
//        super();        
//    }
//    
//    public void add(Iterable<Concept> concepts, boolean includeTermLinks, boolean includeTaskLinks/*, boolean includeOtherReferencedConcepts*/) {
//        
//        for (final Concept c : concepts) {
//            
//            final Term source = c.term;
//            
//            if (!contains(source)) {
//                add(source);
//
//                if (includeTermLinks) {
//                    for (TermLink t : c.termLinks.values()) {
//                        Term target = t.target;
//                        if (!containsVertex(target))  {
//                            addVertex(target);
//                        }
//                        addEdge(source, target, t);
//                    }
//                }
//
//                if (includeTaskLinks) {            
//                    for (TaskLink t : c.taskLinks.values()) {
//                        Task task = t.targetTask;
//                        add(task.parentTask);
//                        
//                        Term target = t.target;
//                        if (!contains(target))  {
//                            add(target);
//                        }        
//                        addEdge(new TaskTerm(source, target, t));                    
//                    }            
//                }
//
//            }
//        }
//        
//        
//    }
//
//    
//    public boolean includeLevel(int l) {
//        return true;
//    }
//
//    @Override
//    public Object clone() {
//        return super.clone(); //To change body of generated methods, choose Tools | Templates.
//    }
//}
