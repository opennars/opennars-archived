package nars.nal;


public interface AbstractSubGoalTree {
    
    default public boolean isLeaf() {
        return true;
    }
    
    default public boolean isRoot() {
        return false;
    }
    
}