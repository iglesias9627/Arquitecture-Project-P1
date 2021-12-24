package com.company;

import java.io.IOException;
import java.lang.management.ManagementFactory;
//import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Random;
import javax.management.MBeanServerConnection;
import com.sun.management.OperatingSystemMXBean;

public class MainClient {

    public static String convertmillis(long input) {
		int days = 0, hours = 0, minutes = 0, seconds = 0, millis = 0;
			        
		int day = 86400000;
		int hour = 3600000;
		int minute = 60000;
		int second = 1000;
			    
			       
		if(input >= day) {
		     days = (int) (input / day);
		     millis = (int) (input % day);
		} else 
			millis = (int) input;
			           
		if(millis >= hour) {
		     hours = millis / hour;
		     millis = millis% hour;
		}
			       
		if(millis >= minute) {
			 minutes = millis / minute;
			 millis = millis % minute;
		}
		
		if(millis >= second) {
			seconds = millis / second;
			millis = millis % second;
		}
			      
		return (days  + " day(s), " + hours + "h, " + minutes + "min, " + seconds + "s and " + millis + "ms");
	}


    public static long distribution_gaussian(){
        //Method or algorithm to generate random numbers with a normal distribution

        //In our experiment to generate values with an average of 500 and a standard deviation of 100, we call the method
        //Using nextGaussian() we scale and shift the number returned to get other normal distribution:
        // *to change the mean (average) of the distribution, we add the required value
        // *to change the standard deviation, we multiply the value.
        //With a standard deviation of 100, this means that 70% of values will fall between 500 +/- 100,
        //in other words between 400 and 600 milliseconds; 95% of values will fall between 300 and 700 milliseconds.
        //We can argue about (and calculate from actual measurements) what a realistic standard deviation is, but in any case,
        //the result is going to be more realistic than calling nextInt(1000) and allowing, say, a time of 50ms to have the
        //same likelihood as a time of 500ms.
        Random r = new Random();
        long delay;
        do {
            double val = r.nextGaussian() * 100 + 1100;
            delay = (int) Math.round(val);
        } while (delay <= 0);
        System.out.println("EL valor de de r es: " + delay);
        
        return delay;
        
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        ArrayList<ThreadClient> datasourceThreads = new ArrayList<>();
        long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        for(int i=0;i<100;i++) { 
           
            long randomGaussian=distribution_gaussian();
            //System.out.println("tiempo: "+randomGaussian);
            
            Thread.sleep(randomGaussian);
        
            
            ThreadClient tp = new ThreadClient(i);
            datasourceThreads.add(tp);
            tp.start();
        }


        for (int i = 0; i < datasourceThreads.size(); i++)           
            {                   
                datasourceThreads.get(i).join();                            
            }
        System.out.println("Whole process took: " + convertmillis(System.currentTimeMillis() - start));
        
    
            
    }
    
    
    
}
