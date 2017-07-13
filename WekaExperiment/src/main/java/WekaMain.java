import weka.core.Instances;
import java.io.IOException;
import java.util.HashMap;


public class WekaMain {
    private static int runs = 20;
    // must be smaller than 1;
    private static final double percentOfDataThatIsTraining = 0.7;
    // We're going to test both 0.5 and 0.75 percent of the training data;
    private static double percentOfTrainingDataToTrain = 1;

    private static final String CSVHEADER = "Precision,Recall,F1,FalseP,FalseN,TrueP,TrueN,TestedSamples,TrainedSamples";
    private static final String OUTPUTFOLDER = "OUTPUT/";
    private static final int uselessFeatures = 3;

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        HashMap<String,Instances> instances = createDataSources(args);
        double[] percentsToTrain = new double[]{0.1, 0.2, 0.35, 0.5, 0.75, 1};

        for(double d: percentsToTrain){
            percentOfTrainingDataToTrain = d;
            String suffix = percentOfDataThatIsTraining + "_" +  percentOfTrainingDataToTrain;
            if(percentOfTrainingDataToTrain == 1){
                testSuite("Baseline",suffix,instances,args,true);
                runs = 10;
                // Lowering runs saves time
            } else {
                testSuite("GP",suffix,instances,args,false);
                testSuite("Random",suffix,instances,args,true);
            }
        }
        System.out.printf("The whole process took %d ms", System.currentTimeMillis()-start);
    }

    private static void testSuite(String prefix, String suffix, HashMap<String,Instances> dataSources, String[] args, boolean random){
        long startTime = System.currentTimeMillis();
        for(int h = 0; h < args.length; h++){

            String trainName = parseName(args[h]);
            String outputFolder = OUTPUTFOLDER +trainName + "/" + suffix;
            HashMap<String,FileSpool> fileSpools = createFileSpools(prefix,args,suffix,outputFolder);
            FileSpool currentSpool = new FileSpool(prefix + "_" + suffix, outputFolder,CSVHEADER);
            ModelManager model;
            for(int i = 0; i < runs; i++) {
                model = test(args[h], percentOfDataThatIsTraining, random, i, percentOfTrainingDataToTrain);
                currentSpool.addLine(model.toString());
//                if (h < args.length - 1) {
//                    fileSpools.get(args[h + 1]).addLine(
//                            ModelManager.formatOutput(
//                                    model.testModel(
//                                            dataSources.get(args[h + 1])), model)
//                    );
//                }
            }
            fileSpools.put("UniqueKeyWhichShouldn'tExist",currentSpool);
            writeFilesIfNotEmpty(fileSpools);
            System.out.println("Finished " + prefix);
            System.out.println("It took " + (System.currentTimeMillis() - startTime) + " ms");
            System.out.println();
        }



    }
    private static HashMap<String,Instances> createDataSources(String[] args){
        HashMap<String,Instances> instances = new HashMap<>();
        for(int i = 0; i < args.length; i++){
            Instances data = ModelManager.loadData(args[i],uselessFeatures);
            instances.put(args[i],data);
        }
        return instances;

    }
    private static HashMap<String, FileSpool> createFileSpools(String prefix, String[] args, String suffix, String outputFolder){
        if(percentOfTrainingDataToTrain == 1){
            prefix = "Baseline";
        }
        HashMap<String,FileSpool> fileSpools = new HashMap<>();
        for(int i = 0; i < args.length; i++){
            fileSpools.put(args[i],new FileSpool(prefix + "_" + parseName(args[i]) + "_" + suffix,
                    outputFolder,CSVHEADER));
        }
        return fileSpools;
    }
    private static void writeFilesIfNotEmpty(HashMap<String,FileSpool> fs){
        for(String key: fs.keySet()){
            FileSpool f = fs.get(key);
            if (!(f.getLines() < 2)){
                f.write();
            }
        }
    }
    private static ModelManager test(String dataName, double percentOfDataThatIsTraining, boolean isRandom, int seed, double percentOfTrainingDataToTrain){
        ModelManager model = new ModelManager(dataName,isRandom, seed, percentOfDataThatIsTraining);
        int samples = (int) (model.getTrainingSetSize() * percentOfTrainingDataToTrain);
        for(int i = model.samplesToStartWith; i < samples; i++){
            model.trainShallowModel();
            if(!model.addNextSample()){
                break;
            }
        }
        model.trainShallowModel();
        model.trainDeepModel();
        model.testModel();
        return model;
    }


    private static String parseName(String fileName){
        String[] s = fileName.split("/");
        String temp = s[s.length-1];
        return temp.substring(0,temp.length()-4);
    }
}
