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
public class Tan extends GPNode {
    public String toString() { 
        return "tan";
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
        double result;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        
        result = rd.x;
        if (Math.abs(result - Math.PI * 0.5)  <= 0.001) {
            rd.x = 0.0;
        } else {
            rd.x = Math.tan(result * Math.PI);
        }

    }
}