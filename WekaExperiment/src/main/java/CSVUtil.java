import weka.core.converters.CSVSaver;

import java.io.*;
import java.util.ArrayList;

public class CSVUtil {
    final static String TRAINING_CSV = "_training.csv";
    final static String TO_TRAIN_CSV = "_to_train.csv";
    final static String TEST_CSV = "_test.csv";

    public static ArrayList<String> openCSVasRows(String Filename) {
        BufferedReader br = null;
        String line = null;
        ArrayList<String> result = new ArrayList<>();
        try {
            br = new BufferedReader(
                    new FileReader(Filename));
            while ((line = br.readLine()) != null){
                result.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static void writeCSV(ArrayList<String> rows, String fileName){
        try{
            Writer bw = new BufferedWriter(
                    new OutputStreamWriter( new FileOutputStream(fileName),"UTF-8"));
            for(int i = 0; i < rows.size(); i++){
                bw.write(rows.get(i));
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void convertArff(String arffName, String csvName) throws IOException {
        try {
            CSVSaver saver = new CSVSaver();
            String[] strings = new String[]{"-i", arffName, "-o", csvName};
            saver.setOptions(strings);
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        } catch (Exception e) {
            // If we get here, it means that for some reason the options were wrong.
            e.printStackTrace();
            throw new IllegalStateException();
        }

    }
//    public static String moveLineToEnd(String fromCSVName, String toCSVName, int index){
//
//    }
}
