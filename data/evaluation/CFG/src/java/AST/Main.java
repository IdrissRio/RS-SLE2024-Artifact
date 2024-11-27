package src.java.AST;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import AST.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Main <grammarFilePath> <maxIterations>");
            return;
        }

        String grammarFilePath = args[0];
        int maxIterations = Integer.parseInt(args[1]);

        try {
            long[] executionTimes = new long[maxIterations];
            double[] standardDeviations = new double[10];
            int iteration = 0;

            do {
                CFGrammar g = new CFGrammar(new FileReader(grammarFilePath));
                long startTime = System.nanoTime();
                executeMain(grammarFilePath,g);
                long endTime = System.nanoTime();

                long executionTimeNano = endTime - startTime;
                executionTimes[iteration] = executionTimeNano;

                // Calculate standard deviation every 10 iterations
                if (iteration >= 10) {
                    double stdDev = calculateStandardDeviation(executionTimes, iteration);
                    standardDeviations[iteration % 10] = stdDev;

                    // Check if standard deviation is less than a reasonable value
                    // if (stdDev < 0.0001) {  // Adjust this value as needed
                    //     break;
                    // }
                }
                iteration++;
            } while (iteration < maxIterations);

            double averageExecutionTimeSeconds = calculateAverage(executionTimes, iteration);
            System.out.printf("%.9f\n",averageExecutionTimeSeconds);
            // System.out.printf("Average execution time: %.9f seconds after %d iterations%n", averageExecutionTimeSeconds, iteration);

        } catch (IOException e) {
            System.err.println("Error reading the grammar file: " + e.getMessage());
        } catch (ParseException e) {
			System.err.println("Error parsing the grammar file: " + e.getMessage());
		}
    }

    private static void executeMain(String grammarFilePath, CFGrammar g) throws IOException, ParseException {
        
        CFG root = g.CFGrammar();
		for(int i = 0; i < root.getNumRule(); i++) {
			Rule r = root.getRule(i);
			r.getNDecl().follow();
            // r.getNDecl().nullable();
            // r.getNDecl().first();
		}
	
    }

    private static double calculateAverage(long[] values, int count) {
        int startIndex = Math.max(0, count - 10); // Start index for the last 10 iterations
        long sum = 0;
        for (int i = startIndex; i < count; i++) {
            sum += values[i];
        }
        return (double) sum / Math.min(count, 10) / 1_000_000_000.0;
    }


    private static double calculateStandardDeviation(long[] values, int count) {
        double average = calculateAverage(values, count);

        double sumSquaredDiff = 0;
        for (int i = 0; i < count; i++) {
            double diff = (double) values[i] / 1_000_000_000.0 - average;
            sumSquaredDiff += diff * diff;
        }

        double variance = sumSquaredDiff / count;
        return Math.sqrt(variance);
    }
}
