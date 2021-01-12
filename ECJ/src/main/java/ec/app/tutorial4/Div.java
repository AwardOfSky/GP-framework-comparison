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
public class Div extends GPNode {
    public String toString() { 
        return "/";
    }

    public int expectedChildren() { 
        return 2; 
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

        children[1].eval(state,thread,input,stack,individual,problem);
        if(Math.abs(rd.x) < 0.001 ) {
            rd.x = 0.0;
        } else {
            rd.x = result / rd.x;
        }

    }
}