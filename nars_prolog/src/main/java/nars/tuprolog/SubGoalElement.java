package nars.tuprolog;

abstract public class SubGoalElement implements AbstractSubGoalTree {

    
    abstract public Term getValue();
    
    public boolean isLeaf() { return true; }
    public boolean isRoot() { return false; }
    
    
    public String toString() {
        return getValue().toString();
    }
}