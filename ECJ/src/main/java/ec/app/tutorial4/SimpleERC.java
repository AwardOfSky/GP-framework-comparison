/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.app.tutorial4;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;

/**
 *
 * @author fjrba
 */
public class SimpleERC extends ERC {
    
    public double value;
    
    @Override
    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
    
        DoubleData rd = ((DoubleData)(input));
        rd.x = value;
    }
    
    @Override
    public String encode() {
        return "" + value;
    }
    
    @Override
    public void resetNode(final EvolutionState state, int thread) {
        value = state.random[thread].nextDouble();
    }

    @Override
    public boolean nodeEquals(final GPNode node) {
        return nodeEquivalentTo(node) && (value == ((SimpleERC)node).value); 
    }
    
    @Override
    public int expectedChildren() { 
        return 0; 
    }
    
    @Override
    public String toStringForHumans() { 
        return encode(); 
    }
    
}
