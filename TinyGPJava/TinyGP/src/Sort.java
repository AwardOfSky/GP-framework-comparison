/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author fjrba
 */

import java.util.*;
public class Sort {
    
    public static void printArray(int[] array) {
        System.out.println();
        for(int i = 0; i < array.length; ++i) {
            System.out.print(array[i] + " ");
        }
    }
    
    public static void initArray(int[] array) {
        Random rd = new Random();
        for(int i = 0; i < array.length; ++i) {
            array[i] = rd.nextInt();
        }
    }
    
    public static void main(String[] args) {
        
        int n = 1000000;
        int array[] = new int[n];
        boolean raw = false;
        boolean internal = true;
        
        if (raw) {
            initArray(array);
            if (array.length < 10) {
                printArray(array);
            }

            long startTime = System.nanoTime();
            for (int i = n - 1; i >= 0; --i) {
                for(int j = 0; j < i; ++j) {
                    if(array[j] > array[j + 1]) {
                        int temp = array[j];
                        array[j] = array[j + 1];
                        array[j + 1] = temp;
                    }
                }
            }
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / (double)1000000000;  // divide by 1000000 to get milliseconds.
            if (array.length < 10) {
                printArray(array);
            }

            System.out.println("Raw time taken: " + duration + " s");
        }
        
        if (internal) {
            initArray(array);
            long startTime = System.nanoTime();
            if (array.length < 10) {
                printArray(array);
            }
            Arrays.sort(array);
            if (array.length < 10) {
                printArray(array);
            }
            long endTime = System.nanoTime();
            double duration = (endTime - startTime) / (double)1000000000;  // divide by 1000000 to get milliseconds.
            System.out.println("Internal time taken: " + duration + " s");
        }

        
    }
    
}
