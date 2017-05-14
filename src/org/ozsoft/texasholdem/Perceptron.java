package org.ozsoft.texasholdem;

import java.util.Random;

/**
 * https://gist.github.com/orhandemirel/666270
 * @author Orhan Demirel
 */
public class Perceptron {

    double[] weights;
    double w0;
    public void Train(double[][] inputs, int[] outputs, double lrate, int epoch)
    {

        int n = inputs[0].length;
        int p = outputs.length;
        weights = new double[n];
        Random r = new Random();
        w0 = r.nextDouble();
        
        //initialize weights
        for(int i=0;i<n;i++)
        {
            weights[i] = r.nextDouble();
        }

        int minError = Integer.MAX_VALUE;
        double minw0 = w0;
        double minw1 = weights[0];
        double minw2 = weights[1];
        
        for(int i=0;i<epoch;i++)
        {
            for(int z=0;z<p;z++)
            {
            	int j = (z+i) % p;
                int output = Output(inputs[j]);
                int error = outputs[j] - output;
               
                w0 += lrate * error;
                for(int k = 0;k<n;k++)
                {
                    double delta = (lrate * inputs[j][k] * error);
                    weights[k] += delta;
                }

            }
            int totalError = 0;
            for(int z=0;z<p;z++)
            {
                int output = Output(inputs[z]);
                int error = outputs[z] - output;
                totalError += Math.abs(error);
            }
            
            if(totalError < minError ) {
                minw0 = w0;
            	minw1 = weights[0];
            	minw2 = weights[1];
            	minError = totalError;
                System.out.println("min= "+minError);
                System.out.printf("ws:\n%.2f\n%.2f\n%.2f\n", minw0,minw1,minw2);
                
                for (int j =0;j<p;j++) {
                	double sum = w0;
                    for(int i2=0;i2<inputs[j].length;i2++)
                    {
                        sum += weights[i2]*inputs[j][i2];
                    }
                    //System.out.printf("point %d value = %.2f\n",j,sum);
                }
            }
        }
        w0 = minw0;
        weights[0] = minw1;
        weights[1] = minw2;
        System.out.printf("[FINAL]ws:\n%.2f\n%.2f\n%.2f\n", w0,weights[0],weights[1]);
        
        for (int j =0;j<p;j++) {
        	double sum = w0;
            for(int i=0;i<inputs[j].length;i++)
            {
                sum += weights[i]*inputs[j][i];
            }
            //System.out.printf("point %d value = %.2f\n",j,sum);
        }
    }

    public Perceptron(double w0, double[] weights) {
    	this.w0 = w0;
    	this.weights = weights;
    }
    
    public int Output(double[] input)
    {
        double sum = w0;
        for(int i=0;i<input.length;i++)
        {
            sum += weights[i]*input[i];
        }
        
        if(sum>=0)
            return 1;
        else
            return 0;
    }
    

}