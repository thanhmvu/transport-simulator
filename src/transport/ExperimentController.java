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

/**
 * Setting up and control the experiment
 *
 * @author hongha912
 */
public class ExperimentController {

    public ExperimentController() {

    }

//=============================== MAIN =============================     
    public static void main(String[] args) {

    }

//================CORRECTNESS CHECKING=======================================
    
    public void checkCorrectness(String outputFile) {
        
    }
    
//==================EXPERIMENT=====================================
    public void runExperiments(String outputFile) {
        
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
