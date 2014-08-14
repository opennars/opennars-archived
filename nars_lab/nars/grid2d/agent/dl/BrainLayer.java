package nars.grid2d.agent.dl;

import com.syncleus.dann.graph.BidirectedGraph;
import com.syncleus.dann.graph.Graph;
import com.syncleus.dann.neural.Neuron;
import com.syncleus.dann.neural.Synapse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author me
 */


abstract public class BrainLayer<N extends Neuron,S extends Synapse<N>> implements BidirectedGraph<N, S> {

    public int inputs;
    public int outputs;

    /** weights, indexed by [output,input] neurons */
    public final Synapse[][] W;
    public final List<N> ins;
    public final List<N> outs;
    private final Set<N> nodes;
    private final Set<S> edges;
    
    
    
    public BrainLayer(int numInputs, int numOutputs) {
        super();

        
        this.ins = new ArrayList(numInputs);
        for (int i = 0; i < numInputs; i++)
            this.ins.add(newInput(i));
        this.outs = new ArrayList(numOutputs);
        for (int i = 0; i < numOutputs; i++)
            this.outs.add(newOutput(i));
        this.inputs = numInputs;
        this.outputs = numOutputs;
        
        nodes = new HashSet(this.inputs * this.outputs);
        nodes.addAll(ins);
        nodes.addAll(outs);
        
        W = new Synapse[this.outputs][];
        edges = new HashSet(this.inputs * this.outputs);

        int oo = 0;
        for (N o : this.outs) {
            
            W[oo] = new Synapse[this.inputs];
            
            int ii = 0;
            for (N i : this.ins) {
        
                S s = newSynapse(i, o);
                W[oo][ii++] = s;
                edges.add(s);
            }
            oo++;
                        
        }

    }
    abstract protected N newInput(int i);
    abstract protected N newOutput(int o);
    
    abstract protected S newSynapse(N input, N output);
    

    @Override
    public Set<N> getNodes() {
        return nodes;
    }

    @Override
    public Set<S> getEdges() {
        return edges;
    }

    @Override
    public Set<S> getInEdges(N n) {
        if (ins.contains(n)) {            
            return Collections.unmodifiableSet(new HashSet(outs));
        }
        else {
            return Collections.unmodifiableSet(new HashSet(ins));            
        }
    }

    @Override
    public List<N> getAdjacentNodes(N n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<S> getAdjacentEdges(N n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<N> getTraversableNodes(N n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<S> getTraversableEdges(N n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> cloneAdd(S e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> cloneAdd(N n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> cloneAdd(Set<N> set, Set<S> set1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> cloneRemove(S e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> cloneRemove(N n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> cloneRemove(Set<N> set, Set<S> set1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph<N, S> clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isContextEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
    
}
