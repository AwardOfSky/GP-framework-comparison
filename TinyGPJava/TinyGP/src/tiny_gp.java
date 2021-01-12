/* 
 * Program:   tiny_gp.java
 *
 * Author:    Riccardo Poli (email: rpoli@essex.ac.uk)
 *
 */

import java.util.*;
/*
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
*/
import java.io.*;
import java.nio.file.Paths;

//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;
//import javax.swing.*;


public class tiny_gp {
    List<Double> Best_Fitness = new ArrayList<Double>();
    private static final long serialVersionUID = -8309587926938512140L;


    public static String Solution;
    static int count = 0;	

    //JPanel panel;
    //JFrame frame;
    //private JTextArea solutionArea;

	
    double [] fitness;
    char [][] pop;
    private List ArrayList;
    static Random rd = new Random();
    static final int 
            ADD = 110, // arity 2
            SUB = 111, 
            MUL = 112, 
            DIV = 113,
            POW = 114,
            COS = 115, // arity 1
            SIN = 116,
            TAN = 117,
            ABS = 118,
            SIG = 119,
            FSET_START = ADD, 
            FSET_END = SIG;
    static double [] x = new double[FSET_START];
    static double minrandom, maxrandom;
    static char [] program;
    static int PC;
    static int varnumber, fitnesscases, randomnumber;
    static double fbestpop = 0.0, favgpop = 0.0;
    static long seed;
    static double avg_len; 
    static final int  
        MAX_LEN = 10000,  
        POPSIZE = 50,
        DEPTH   = 10,
        GENERATIONS = 50,
        TSIZE = 3;
    public static final double
        PMUT_PER_NODE  = 0.05,
        CROSSOVER_PROB = 0.9;
    static double [][] targets;
  
  
    static final double
        MIN_DOMAIN = -5.0,
        MAX_DOMAIN = 5.0;
    static final double DOMAIN_DELTA = (MAX_DOMAIN - MIN_DOMAIN);
    static int RESOLUTION[] = {512, 512};
    static String outfilename;
  
    // TODO: implement ops
    double run() { /* Interpreter */
      char primitive = program[PC++];
      if ( primitive < FSET_START )
        return(x[primitive]);
      switch ( primitive ) {
        case ADD : return( run() + run() );
        case SUB : return( run() - run() );
        case MUL : return( run() * run() );
        case DIV : { 
          double num = run(), den = run();
          if ( Math.abs( den ) <= 0.001 ) 
            return( num );
          else 
            return( num / den );
        }
        case POW : {
            double num = Math.abs(run());
            if (num <= 0.001) {
                return 0.0;
            } else {
                return Math.min(Math.pow(num, Math.abs(run())), Integer.MAX_VALUE);
            }
        }
        case COS : return Math.cos(run() * Math.PI);
        case SIN : return Math.sin(run() * Math.PI);
        case TAN : {
            double num = run();
            if (Math.abs(num - Math.PI * 0.5)  <= 0.001) {
                return 0.0;
            } else {
                return Math.tan(num * Math.PI);
            }
        }
        case ABS: return Math.abs(run());
        case SIG: return Math.signum(run());
        }
      return( 0.0 ); // should never get here
    }
  
    // TODO: add implemented ops
    int traverse( char [] buffer, int buffercount ) {
      if ( buffer[buffercount] < FSET_START )
        return( ++buffercount );

      switch(buffer[buffercount]) {
        case ADD: 
        case SUB: 
        case MUL: 
        case DIV:
        case POW:
        return( traverse( buffer, traverse( buffer, ++buffercount ) ) );
        case COS:
        case SIN:
        case TAN:
        case ABS:
        case SIG:
        return( traverse( buffer, ++buffercount ) );
        }
      return( 0 ); // should never get here
    }
  
    //public tiny_gp(){ // CONSTRUCTOR
    //  initialize();
    //}

    double function_pagie(double i, double j) {
        double i4 = i * i * i * i;
        double j4 = j * j * j * j;
        double m1 = (1.0 / (1.0 + (1.0 / i4)));
        double m2 = (1.0 / (1.0 + (1.0 / j4)));
        return m1 + m2;
    }
  
    void target_setup() {
        double step[] = {DOMAIN_DELTA / (RESOLUTION[0] - 1), DOMAIN_DELTA / (RESOLUTION[1] - 1)}; 
        
        for(int i = 0; i < RESOLUTION[0]; ++i) {
            for(int j = 0; j < RESOLUTION[1]; ++j) {
                double domaini = MIN_DOMAIN + (step[0] * i);
                double domainj = MIN_DOMAIN + (step[1] * j);
                int index = i * RESOLUTION[0] + j;
                //System.out.println("What index is this? i = "  + i + ", j = " + j);
                //System.out.println("Adding f(" + domaini + ", " + domainj + ") = " + function_pagie(domaini, domainj) + " to target.");
                targets[index][0] = domaini;
                targets[index][1] = domainj;
                targets[index][2] = function_pagie(domaini, domainj);
            }
        }
    }
    
    void setup_fitness(String fname) {
        //System.out.println("We are in string and the filename is: " + fname);
        try {
            //int i,j;
            String line;

            BufferedReader in = 
            new BufferedReader(
                          new
                          FileReader(fname));
            line = in.readLine();
            StringTokenizer tokens = new StringTokenizer(line);
            varnumber = Integer.parseInt(tokens.nextToken().trim());
            //System.out.println("Var number: " + varnumber);
            randomnumber = Integer.parseInt(tokens.nextToken().trim());
            minrandom =	Double.parseDouble(tokens.nextToken().trim());
            maxrandom =  Double.parseDouble(tokens.nextToken().trim());

            // GET fitness cases
            //fitnesscases = Integer.parseInt(tokens.nextToken().trim());
            fitnesscases = RESOLUTION[0];
            for(int i = 1; i < RESOLUTION.length; ++i) {
                fitnesscases *= RESOLUTION[i];
            }
            targets = new double[fitnesscases][varnumber+1];
      
            if (varnumber + randomnumber >= FSET_START ) {
              System.out.println("too many variables and constants");
            }
      
            /*
            // get fitness cases from file
            for (i = 0; i < fitnesscases; i ++ ) {
              line = in.readLine();
              tokens = new StringTokenizer(line);
              for (j = 0; j <= varnumber; j++) {
                targets[i][j] = Double.parseDouble(tokens.nextToken().trim());
              }
            }
            */
            //System.out.println("Are we getting here?");
            target_setup();
      
            in.close();
        }
        catch(FileNotFoundException e) {
           System.out.println("ERROR: Please provide a data file");
           System.exit(0);
        }
        catch(Exception e ) {
          System.out.println("ERROR: Incorrect data format");
          System.exit(0);
        }
    }

    double fitness_function( char [] Prog ) {
        int i = 0, len;
        double result, fit = 0.0;

        len = traverse( Prog, 0 );
        for (i = 0; i < fitnesscases; i ++ ) {
            for (int j = 0; j < varnumber; j ++ )
                x[j] = targets[i][j];
            program = Prog;
            PC = 0;
            result = run();
            fit += Math.pow(result - targets[i][varnumber], 2);
        }
        
        fit = Math.sqrt(fit / fitnesscases);
        
        return(-fit );
    }

  int grow( char [] buffer, int pos, int max, int depth, int method) {
    //char prim = (char) rd.nextInt(2);
    //char prim = 1;
    char prim = (method == 0) ? ((char) rd.nextInt(2)) : 1;
    
    int one_child;

    if ( pos >= max ) 
      return( -1 );
    
    if ( pos == 0 )
      prim = 1;
    
    if ( prim == 0 || depth == 0 ) {
      //prim = (char) rd.nextInt(varnumber + randomnumber);
      prim = (char) rd.nextInt(varnumber + 1);
      if (prim >= varnumber) {
          prim = (char) (rd.nextInt(randomnumber) + varnumber);
      }
      
      buffer[pos] = prim;
      return(pos+1);
      }
    else  {
      prim = (char) (rd.nextInt(FSET_END - FSET_START + 1) + FSET_START);
      switch(prim) {
      case ADD: 
      case SUB: 
      case MUL: 
      case DIV:
      case POW:
        buffer[pos] = prim;
	one_child = grow( buffer, pos+1, max,depth-1, method);
	if ( one_child < 0 ) 
		return( -1 );
        return( grow( buffer, one_child, max,depth-1, method) );
      case TAN:
      case COS:
      case SIN:
      case ABS:
      case SIG:
          buffer[pos] = prim;
          return grow( buffer, pos+1, max,depth-1, method);
      }
    }
    return( 0 ); // should never get here
  }
  
  int print_indiv( char []buffer, int buffercounter ) {
    int a1=0, a2=0;
    if ( buffer[buffercounter] < FSET_START ) {
      if ( buffer[buffercounter] < varnumber )
        System.out.print( "X"+ (buffer[buffercounter] + 1 )+ " ");
      else
        System.out.print( x[buffer[buffercounter]]);
      return( ++buffercounter );
      }
    switch(buffer[buffercounter]) {
      case ADD: System.out.print( "(");
        a1=print_indiv( buffer, ++buffercounter ); 
        System.out.print( " + "); 
        a2=print_indiv( buffer, a1 );
        break;
      case SUB: System.out.print( "(");
        a1=print_indiv( buffer, ++buffercounter ); 
        System.out.print( " - "); 
        a2=print_indiv( buffer, a1 );
        break;
      case MUL: System.out.print( "(");
        a1=print_indiv( buffer, ++buffercounter ); 
        System.out.print( " * "); 
        a2=print_indiv( buffer, a1 );
        break;
      case DIV: System.out.print( "(");
        a1=print_indiv( buffer, ++buffercounter ); 
        System.out.print( " / "); 
        a2=print_indiv( buffer, a1 );
        break;
      case POW: System.out.print( "(");
        a1=print_indiv( buffer, ++buffercounter ); 
        System.out.print( " ^ "); 
        a2=print_indiv( buffer, a1 );
        break;
      case COS: System.out.print( "cos(");
        a2=print_indiv( buffer, ++buffercounter ); 
        break;
      case SIN: System.out.print( "sin(");
        a2=print_indiv( buffer, ++buffercounter ); 
        break;
      case TAN: System.out.print( "tan(");
        a2=print_indiv( buffer, ++buffercounter ); 
        break;
      case ABS: System.out.print( "abs(");
        a2=print_indiv( buffer, ++buffercounter ); 
        break;
      case SIG: System.out.print( "sig(");
        a2=print_indiv( buffer, ++buffercounter ); 
        break;
      }
    System.out.print( ")");

    return( a2);
  }
  

  static char [] buffer = new char[MAX_LEN];
  char [] create_random_indiv( int depth, int method) {
    char [] ind;
    int len;

    len = grow( buffer, 0, MAX_LEN, depth, method);

    while (len < 0 )
      len = grow( buffer, 0, MAX_LEN, depth, method);

    ind = new char[len];

    System.arraycopy(buffer, 0, ind, 0, len ); 
    return( ind );
  }

    char [][] create_random_pop(int n, int depth, double [] fitness ) {
        char [][]pop = new char[n][];
        int i;

        for ( i = 0; i < n; i ++ ) {
            
            int block = (n / depth);
            int dep = (i / block) + 1;
            int rem = i % block;
            if (rem >= (block >> 1)) {
                pop[i] = create_random_indiv(dep, 0);
            } else {
                pop[i] = create_random_indiv(dep, 1);
            }
            
            //System.out.println("i, block, dep, rem = " + i + ", " + block + ", " + dep + ", " + rem);
            //pop[i] = create_random_indiv( depth , 1);
            //System.out.print("Individual number " + i + ": ");
            //print_indiv(pop[i], 0);
            //System.out.println();
            fitness[i] = fitness_function( pop[i] );
        }
        return( pop );
    }


  void stats( double [] fitness, char [][] pop, int gen ) {
     count++;
    int i, best = rd.nextInt(POPSIZE);
    int node_count = 0;
    fbestpop = fitness[best];
    favgpop = 0.0;

    for ( i = 0; i < POPSIZE; i ++ ) {
      //System.out.println("Pop[i]: " + print_indiv(pop[i], 0) + " with fitn: " + fitness[i]);
      node_count +=  traverse( pop[i], 0 );
      favgpop += fitness[i];
      if ( fitness[i] > fbestpop ) {
      best = i;
      fbestpop = fitness[i];
      }
    }
    avg_len = (double) node_count / POPSIZE;
    favgpop /= POPSIZE;
    //System.out.print("Generation="+gen+" Avg Fitness="+(-favgpop)+" Best Fitness="+(-fbestpop)+" Avg Size="+avg_len+"\nBest Individual: ");
    
    if (gen == 0)  {
        System.out.println("Genration, Average Fitness, Best Fitness, Best Individual.");
    }
    System.out.print(gen + ", " + favgpop +  ", " + fbestpop + ", ");
    
    print_indiv( pop[best], 0 );
    System.out.print( "\n");
    System.out.flush();
        double absfitness = -fbestpop;
    
    
    //Best_Fitness.add(fbestpop);
    int j; 
    j = gen;
    if(j <= Best_Fitness.size() ) 
      Best_Fitness.add(j, absfitness);
    else
    	System.out.println("Data Complete");
  }

    int tournament( double [] fitness, int tsize ) {
        int best = rd.nextInt(POPSIZE), i, competitor;
        double  fbest = -1.0e34;

        for ( i = 0; i < tsize; i ++ ) {
            competitor = rd.nextInt(POPSIZE);
            if ( fitness[competitor] > fbest ) {
                fbest = fitness[competitor];
                best = competitor;
            }
        }
        return( best );
    }
  
    int negative_tournament( double [] fitness, int tsize ) {
        int worst = rd.nextInt(POPSIZE), i, competitor;
        double fworst = 1e34;

        for ( i = 0; i < tsize; i ++ ) {
            competitor = rd.nextInt(POPSIZE);
            if ( fitness[competitor] < fworst ) {
                fworst = fitness[competitor];
                worst = competitor;
            }
        }
        return( worst );
    }
  
  char [] crossover( char []parent1, char [] parent2 ) {
    int xo1start, xo1end, xo2start, xo2end;
    char [] offspring;
    int len1 = traverse( parent1, 0 );
    int len2 = traverse( parent2, 0 );
    int lenoff;
    
    xo1start =  rd.nextInt(len1);
    xo1end = traverse( parent1, xo1start );
    
    xo2start =  rd.nextInt(len2);
    xo2end = traverse( parent2, xo2start );
    
    lenoff = xo1start + (xo2end - xo2start) + (len1-xo1end);

    offspring = new char[lenoff];

    System.arraycopy( parent1, 0, offspring, 0, xo1start );
    System.arraycopy( parent2, xo2start, offspring, xo1start,  
    		  (xo2end - xo2start) );
    System.arraycopy( parent1, xo1end, offspring, 
    		  xo1start + (xo2end - xo2start), 
    		  (len1-xo1end) );

    return( offspring );
  }
  
  char [] mutation( char [] parent, double pmut ) {
    int len = traverse( parent, 0 ), i;
    int mutsite;
    char [] parentcopy = new char [len];
    
    System.arraycopy( parent, 0, parentcopy, 0, len );
    for (i = 0; i < len; i ++ ) {  
      if ( rd.nextDouble() < pmut ) {
      mutsite =  i;
      if ( parentcopy[mutsite] < FSET_START )
        parentcopy[mutsite] = (char) rd.nextInt(varnumber+randomnumber);
      else
        switch(parentcopy[mutsite]) {
      	case ADD: 
      	case SUB: 
      	case MUL: 
      	case DIV:
        case POW:
           parentcopy[mutsite] = 
              (char) (rd.nextInt(POW - ADD + 1)
                     + ADD);
           break;
        case COS:
        case SIN:
        case TAN:
        case ABS:
        case SIG:
            parentcopy[mutsite] = 
              (char) (rd.nextInt(SIG - COS + 1)
                     + COS);
           break;
        }
      }
    }
    return( parentcopy );
  }
  
    void print_parms() {
        //   System.out.print("-- TINY GP (Java version) --\n");
        //   System.out.print("SEED="+seed+"\nMAX_LEN="+MAX_LEN+
        //   	    "\nPOPSIZE="+POPSIZE+"\nDEPTH="+DEPTH+
        //     	    "\nCROSSOVER_PROB="+CROSSOVER_PROB+
        //     	    "\nPMUT_PER_NODE="+PMUT_PER_NODE+
        //     	    "\nMIN_RANDOM="+minrandom+
        //     	    "\nMAX_RANDOM="+maxrandom+
        //     	    "\nGENERATIONS="+GENERATIONS+
        //     	    "\nTSIZE="+TSIZE+
        //     	    "\n----------------------------------\n");     
        
        File f = new File(outfilename);
        if(!f.exists())
        {
            try {
                f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(f);
            PrintStream ps = new PrintStream(fos);
            System.setOut(ps);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
   

    public tiny_gp( String fname, long s ) {
      fitness =  new double[POPSIZE];
      seed = s;
      if ( seed >= 0 )
          rd.setSeed(seed);
      setup_fitness(fname);
      for ( int i = 0; i < FSET_START; i ++ )
        x[i]= (maxrandom-minrandom)*rd.nextDouble()+minrandom;
      pop = create_random_pop(POPSIZE, DEPTH, fitness );
    }

    void evolve() {

        int gen = 0, indivs, offspring, parent1, parent2, parent;
        double newfit;
        char []newind;
        print_parms();
        System.out.println(seed + "[ " + RESOLUTION[0] + ", " + RESOLUTION[1] + " ]");
        stats( fitness, pop, 0 );
        
        //System.out.println("Hallo!");
        
        for ( gen = 1; gen < GENERATIONS; gen ++ ) {

            //System.out.println("Generation: " + gen);
            
            /*
            if (  fbestpop > -0.02 ) {
               System.out.print("PROBLEM SOLVED\n");              
               invokeDataset();
               try{
                Thread.sleep(10);
               }
               catch(InterruptedException e){
                e.printStackTrace();
               }
               return;
               //System.exit( 0 ); 
            }
            */
            
            for ( indivs = 0; indivs < POPSIZE; indivs ++ ) {
              if ( rd.nextDouble() < CROSSOVER_PROB  ) {
                parent1 = tournament( fitness, TSIZE );
                parent2 = tournament( fitness, TSIZE );
                newind = crossover( pop[parent1],pop[parent2] );
              }
              else {
                parent = tournament( fitness, TSIZE );
                newind = mutation( pop[parent], PMUT_PER_NODE );
              }
              newfit = fitness_function( newind );
              offspring = negative_tournament( fitness, TSIZE );
              pop[offspring] = newind;
              fitness[offspring] = newfit;
            }
          stats( fitness, pop, gen );
        }
        //System.out.print("PROBLEM *NOT* SOLVED\n");
        //invokeDataset();
        //System.exit( 1 );
        /*
        try{
            Thread.sleep(10);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        */
        return;
    }
  


  /*
  public void initialize(){  // this method reads the console.dat and writes all of the content on a JFrame.
	     
	  	  
		  try{

		    	Scanner scanner = new Scanner(new File(outfilename)); // console.dat its readed by this object
				String all = scanner.useDelimiter("\\A").next(); // and the variable "all" has everything that is in the console.dat

			  	 				  
		  		solutionArea = new JTextArea();
		  		solutionArea.setEditable(true);
		  		solutionArea.setBounds(10, 11, 400, 400);
		  		solutionArea.setText(all);
		  		solutionArea.setLineWrap(false);
		  		solutionArea.setWrapStyleWord(true);
		  		//solutionArea.setColumns(1000);
		  		
		  		JScrollPane scroll = new JScrollPane(solutionArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		  		
		  		frame = new JFrame("Best Solution Found!");
		  		frame.getContentPane().add(scroll);
		  		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setBounds(100, 100, 100, 80);
				frame.setSize(600, 400);
				
				//frame.getContentPane().add(solutionArea);
				
		
				
				frame.pack();		
				frame.setVisible(true);
		  		frame.repaint();
		
		    		    
		        scanner.close();
		    		    
		     } catch (IOException e) {
		        e.printStackTrace();}
	}
*/


		  
  
  
    void invokeDataset(){
	  
	 GraphPanel dataSet = new GraphPanel(Best_Fitness);
	  String[]args = {};
	  //dataSet.main(args);
	  dataSet.createAndShowGui();
    }
  
        

        
    public static void main(String[] args) {

        int runs = 1;
        String fname = "src/problem.dat";
        String fname1 = Paths.get("").toAbsolutePath().toString();
        try {
            String fname2 = Paths.get(System.getProperty("user.dir")).toRealPath().toString();
            System.out.println(fname2);
        } catch(IOException e) {
            System.out.println("Fuck");
        }
        System.out.println(fname1);

        
        
        //long s = 984123;
        //int[] test_cases = {64, 128, 256, 512, 1024, 2048};
        int[] test_cases = {64};
        
        for (int t = 0; t < test_cases.length; ++t) {
            for (int i = 0; i < runs; ++i) {
                

                int res = test_cases[i];
                for(int j = 0; j < RESOLUTION.length; ++j) {
                    RESOLUTION[j] = res;   // default at 512
                }
                
                long s = System.currentTimeMillis();
                //outfilename = "runs/console_" + res + "_" + s + ".dat";
                System.out.println(outfilename);
                //System.out.println("Args len: " + args.length);

                //if ( args.length == 1 ) {
                //    int res = Integer.parseInt(args[0]);
                //    for(int j = 0; j < RESOLUTION.length; ++j) {
                //        RESOLUTION[j] = res;   // default at 512
                //    }
                //}

                tiny_gp gp = new tiny_gp(fname, s);
                //System.out.println("Initialization done.");

                //System.out.println("X array: " + x + " s");
                //for(int i = 0; i < x.length; ++i) {
                //    System.out.println("X array: " + x[i]);
                //} 

                // evolve
                long startTime = System.nanoTime();
                gp.evolve();
                long endTime = System.nanoTime();
                double duration = (endTime - startTime) / (double)1000000000;  // divide by 1000000 to get milliseconds.

                System.out.println("Time taken: " + duration + " s");

                //tiny_gp window = new  tiny_gp();

            }
        }
        
        
        
    }
}

