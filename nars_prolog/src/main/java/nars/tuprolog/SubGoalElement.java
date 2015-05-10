package nars.tuprolog;

abstract public interface SubGoalElement extends AbstractSubGoalTree {

    
    public Term getValue();
    
    default public boolean isLeaf() { return true; }
    default public boolean isRoot() { return false; }


}