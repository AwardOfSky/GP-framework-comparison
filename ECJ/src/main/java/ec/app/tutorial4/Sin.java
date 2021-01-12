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
public class Sin extends GPNode {
    public String toString() { 
        return "sin";
    }

    public int expectedChildren() { 
        return 1;
    }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        
        rd.x = Math.sin(Math.PI * rd.x);

    }
}