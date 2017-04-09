package transport;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Setting up and control the experiment
 *
 * @author hongha912
 */
public class ExperimentController {

    private NetworkSimulator ns;
    private static final String CORRECTNESS_FILE_PATH = "./correctnessTest.txt";
    private static final String EXP_FILE_PATH = "./expTest.txt";
    private static final int DEBUG_SETTING_EXP = 0;

    private static final int GBN = 0;
    private static final int TCP = 1;

    /**
     * Create an experiment controller
     */
    public ExperimentController() {
        ns = new NetworkSimulator();
    }

//=============================== MAIN =============================     
    public static void main(String[] args) {
        ExperimentController ec = new ExperimentController();
        ec.checkCorrectness();
        ec.runExperiments("./expResults.csv");

    }

//================CORRECTNESS CHECKING=======================================
    /**
     * Print out sending and receiving at least 5 messages ( with at least one
     * loss and one corruption), one for TCP, one for GBN
     */
    public void checkCorrectness() {
        //set up numOfExp
        int numRuns = 2;

        //set up control vars
        int timeBtwMsgs = 3;
        float lossProb = 0f;
        float corrProb = 0f;
        int winSize = 3;
        int tracing = 2;

        // Test Go-back-N
        for (int i = 0; i < numRuns; i++) {
            System.out.println("==============Check correctness for Go-back-N - Test " + i + "==============");
            ns.run(CORRECTNESS_FILE_PATH, timeBtwMsgs, lossProb, corrProb, winSize, GBN, tracing);
            // Test TCP
            System.out.println("==============Check correctness for TCP - Test " + i + "==============");
            ns.run(CORRECTNESS_FILE_PATH, timeBtwMsgs, lossProb, corrProb, winSize, TCP, tracing);
        }

    }

//==================EXPERIMENT=====================================
    public void runExperiments(String outputFilePath) {
        int runs = 20;
        int numTrialsPerRun = 6;
        String finalResult = "";
        //============Time Between Sends==================//
        int initialTimeBtwSends = 2;
        int maxTimeBtwSends = 22;
        int tbsIncrement = (maxTimeBtwSends - initialTimeBtwSends)/runs;
        finalResult += this.runTimeBtwSendsExp(initialTimeBtwSends, tbsIncrement, runs, numTrialsPerRun).toCsvString();

        //============Loss Probability==================//
        float initialLossProb = 0.0f;
        float maxLossProb = 0.50f;
        float lossIncrement = (maxLossProb - initialLossProb)/runs;
        finalResult += this.runLossProbExp(initialLossProb, lossIncrement, runs, numTrialsPerRun).toCsvString();

        //============Corruption Probability==================//
        float initialCorrProb = 0.0f;
        float maxCorrProb = 0.50f;
        float corrIncrement = (maxCorrProb - initialCorrProb)/runs;
        finalResult += this.runCorrProbExp(initialCorrProb, corrIncrement, runs, numTrialsPerRun).toCsvString();
        
        //============Windows size==================//
        int initialSize = 5;
        int sizeIncrement = 5;
        finalResult += this.runWindowsSizeExp(initialSize, sizeIncrement, runs, numTrialsPerRun).toCsvString();
        
        
        try {
            this.printToFile(outputFilePath, finalResult);
        } catch (IOException ex) {
            Logger.getLogger(ExperimentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        

    }

    /**
     * Return the experiment results to run a between sends experiment
     *
     * @param initialTime initial time between sends
     * @param increment increment to increase independent var
     * @param numRuns number of runs
     * @param numTrialsPerRun number of trials per run
     * @return the results of the experiment
     */
    private ExperimentResults runTimeBtwSendsExp(int initialTime, int increment, int numRuns, int numTrialsPerRun) {
        ExperimentResults results = new ExperimentResults("Time Between Sends vs. Total Time");

        //control vars
        float lossProb = 0.10f;
        float corrProb = 0.10f;
        int windowsSize = 4;

        //run exp
        for (int i = 0; i < numRuns; i++) {
            int timeBtwSends = initialTime + i * increment;
            int totalGBN = 0;
            int totalTCP = 0;

            for (int j = 0; j < numTrialsPerRun; j++) {
                System.out.println("\n================================= GBN =================================\n");
                totalGBN += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, GBN, DEBUG_SETTING_EXP);
                
                System.out.println("\n================================= TCP =================================\n");
                totalTCP += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, TCP, DEBUG_SETTING_EXP);
            }
            //take the average
            int gbnTime = totalGBN / numTrialsPerRun;
            int tcpTime = totalTCP / numTrialsPerRun;
            //add to result table
            results.add("Time Between Sends", "" + timeBtwSends);
            results.add("Total Time (GBN)", "" + gbnTime);
            results.add("Total Time (TCP)", "" + tcpTime);

        }
        return results;
    }

    /**
     * Return the experiment results to run a loss probability experiment
     *
     * @param initialProb initial loss probability
     * @param increment increment to increase independent var
     * @param numRuns number of runs
     * @param numTrialsPerRun number of trials per run
     * @return the results of the experiment
     */
    private ExperimentResults runLossProbExp(float initialProb, float increment, int numRuns, int numTrialsPerRun) {
        ExperimentResults results = new ExperimentResults("Loss Probability vs. Total Time");

        //control vars
        int timeBtwSends = 10;
        float corrProb = 0f;
        int windowsSize = 4;

        //run exp
        for (int i = 0; i < numRuns; i++) {
            float lossProb = initialProb + i * increment;
            int totalGBN = 0;
            int totalTCP = 0;

            for (int j = 0; j < numTrialsPerRun; j++) {
                totalGBN += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, GBN, DEBUG_SETTING_EXP);
                totalTCP += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, TCP, DEBUG_SETTING_EXP);
            }
            //take the average
            int gbnTime = totalGBN / numTrialsPerRun;
            int tcpTime = totalTCP / numTrialsPerRun;
            //add to result table
            results.add("Loss Probability", "" + lossProb);
            results.add("Total Time (GBN)", "" + gbnTime);
            results.add("Total Time (TCP)", "" + tcpTime);

        }
        return results;
    }

    /**
     * Return the experiment results to run a corruption probability experiment
     *
     * @param initialProb initial corruption probability
     * @param increment increment to increase independent var
     * @param numRuns number of runs
     * @param numTrialsPerRun number of trials per run
     * @return the results of the experiment
     */
    private ExperimentResults runCorrProbExp(float initialProb, float increment, int numRuns, int numTrialsPerRun) {
        ExperimentResults results = new ExperimentResults("Corruption Probability vs. Total Time");

        //control vars
        int timeBtwSends = 10;
        float lossProb = 0f;
        int windowsSize = 4;

        //run exp
        for (int i = 0; i < numRuns; i++) {
            float corrProb = initialProb + i * increment;
            int totalGBN = 0;
            int totalTCP = 0;

            for (int j = 0; j < numTrialsPerRun; j++) {
                totalGBN += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, GBN, DEBUG_SETTING_EXP);
                totalTCP += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, TCP, DEBUG_SETTING_EXP);
            }
            //take the average
            int gbnTime = totalGBN / numTrialsPerRun;
            int tcpTime = totalTCP / numTrialsPerRun;
            //add to result table
            results.add("Corruption Probability", "" + corrProb);
            results.add("Total Time (GBN)", "" + gbnTime);
            results.add("Total Time (TCP)", "" + tcpTime);

        }
        return results;
    }

    /**
     * Return the experiment results to run a between sends experiment
     *
     * @param initialSize initial time between sends
     * @param increment increment to increase independent var
     * @param numRuns number of runs
     * @param numTrialsPerRun number of trials per run
     * @return the results of the experiment
     */
    private ExperimentResults runWindowsSizeExp(int initialSize, int increment, int numRuns, int numTrialsPerRun) {
        ExperimentResults results = new ExperimentResults("Windows Size vs. Total Time");

        //control vars
        float lossProb = 0.10f;
        float corrProb = 0.10f;
        int timeBtwSends = 10;

        //run exp
        for (int i = 0; i < numRuns; i++) {
            int windowsSize = initialSize + i * increment;
            int totalGBN = 0;
            int totalTCP = 0;

            for (int j = 0; j < numTrialsPerRun; j++) {
                totalGBN += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, GBN, DEBUG_SETTING_EXP);
                totalTCP += ns.run(EXP_FILE_PATH, timeBtwSends, lossProb, corrProb, windowsSize, TCP, DEBUG_SETTING_EXP);
            }
            //take the average
            int gbnTime = totalGBN / numTrialsPerRun;
            int tcpTime = totalTCP / numTrialsPerRun;
            //add to result table
            results.add("Windows Size", "" + windowsSize);
            results.add("Total Time (GBN)", "" + gbnTime);
            results.add("Total Time (TCP)", "" + tcpTime);

        }
        return results;
    }
//========================HELPERS=============================
    /**
     * Print a string to a new file
     *
     * @param filePath The path of the file
     * @param text The text to be printed
     * @throws IOException Input output exception
     */
    private void printToFile(String filePath, String text) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = new ArrayList<>();
        lines.add(text);
        Files.write(path, lines, Charset.defaultCharset());
    }

    /**
     * This class holds all the experiment results
     */
    private class ExperimentResults {

        private final String nameOfExperiment;
        private final HashMap<String, List<String>> cols;

        ExperimentResults(String nameOfExp) {
            System.out.println("Start experiment " + nameOfExp);
            nameOfExperiment = nameOfExp;
            cols = new LinkedHashMap<>();
        }

        void add(String colName, String colValue) {
            List<String> col = cols.get(colName);
            if (col == null) {
                col = new ArrayList<>();
                cols.put(colName, col);
            }
            col.add(colValue);
        }

        /**
         * Convert Experiment Results into a CSV String
         *
         * @return a CSV String
         */
        String toCsvString() {
            StringBuilder result = new StringBuilder();
            result.append(nameOfExperiment).append("\n");
            for (String colName : cols.keySet()) {
                result.append(colName);
                for (String val : cols.get(colName)) {
                    result.append(",").append(val);
                }
                result.append("\n");
            }
            return result.toString();
        }
    }

}
