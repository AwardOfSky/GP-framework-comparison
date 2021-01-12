/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.tutorial4;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class MultiValuedRegression extends GPProblem implements SimpleProblemForm {
    private static final long serialVersionUID = 1;

    public double currentX;
    public double currentY;
    public int statisticslog = 0;
    
    public double minDomain = -5.0;
    public double maxDomain = 5.0;
    public double domainDelta = maxDomain - minDomain;
    //public int[] testCases = {64, 128, 256, 512, 1024, 2048};
    
    public int resolution = 64;
    public int fitCases =  resolution * resolution;
    public double step = domainDelta / (resolution - 1);
    public double[][] target = new double[resolution][resolution];
    
    public void setupTarget() {
        for(int i = 0; i < resolution; ++i) {
            for(int j = 0; j < resolution; ++j) {
                double x = minDomain + i * step;
                double y = minDomain + j * step;
                target[i][j]= pagiePoly(x, y);
            }
        }
        
    }
    
    public void setupRes(int res) {
        resolution = res;
        fitCases =  resolution * resolution;
        step = domainDelta / (resolution - 1);
    }
    
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        setupTarget();
        
        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof DoubleData))
            state.output.fatal("GPData class must subclass from " + DoubleData.class,
                base.push(P_DATA), null);
        
    }
    
    public void setup(final EvolutionState state, final Parameter base, int res) {
        super.setup(state, base);
        setupRes(res);
        setupTarget();
        
        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof DoubleData))
            state.output.fatal("GPData class must subclass from " + DoubleData.class,
                base.push(P_DATA), null);
        
    }

    public double pagiePoly(double x, double y) {
        return (1 / (1 + (1 / (x * x * x * x)))) + (1 / (1 + (1 / (y * y * y * y))));
    }
    
    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        if (!ind.evaluated) {
            DoubleData input = (DoubleData)(this.input);
        
            int hits = 0;
            double sum = 0.0;
            double expectedResult;
            double result;
            
            double fitness = 0;
            for(int i = 0; i < resolution; ++i) {
                for(int j = 0; j < resolution; ++j) {
                    
                    currentX = minDomain + i * step;
                    currentY = minDomain + j * step;
                    
                    // eval ind into input.x given currentX and currentY
                    ((GPIndividual)ind).trees[0].child.eval(state, threadnum, input, stack, ((GPIndividual)ind), this);
                    
                    //fitness += Math.pow(input.x - target[i][j], 2);
                    fitness += Math.pow(0 - target[i][j], 2);
                    
                }
            }
            
            fitness = Math.sqrt(fitness / (double)fitCases);
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, fitness);
            ind.evaluated = true;

        }
    }
}

