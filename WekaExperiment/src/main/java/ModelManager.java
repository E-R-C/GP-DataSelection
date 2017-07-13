import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.M5P;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;


import java.util.ArrayList;
import java.util.Random;

public class ModelManager {

    // This is declared because it never changes after it's first declaration
    Instances trainingData, toTrainData, testDataInstance;
    public int samplesToStartWith = 20;
    int classIndex;
    private int trainingSetSize;
    private boolean isRandom = false;
    private ArrayList<Prediction> lastSetOfResults;

    AbstractClassifier model, shallowModel;

    public ModelManager(String sourceName, boolean isRandom, int seed, double percentToSplit) {

        Random random = new Random(seed);
        int unnecessaryFeatures = 3;
        Instances data = loadData(sourceName,unnecessaryFeatures);
        classIndex = data.numAttributes() -1;
        data.randomize(random);
        this.isRandom = isRandom;
        int indexOfSplit = (int) (percentToSplit * data.size());
        trainingSetSize = indexOfSplit;
        trainingData = new Instances(data, 0, samplesToStartWith);
        toTrainData = new Instances(data, samplesToStartWith, indexOfSplit - samplesToStartWith);
        testDataInstance = new Instances(data, indexOfSplit, data.size() - indexOfSplit);
        if (!(percentToSplit < 1)){
            System.out.println(testModel());
        }
    }
    public ModelManager(Instances instances, boolean isRandom, int seed, double percentToSplit) {
        Random random = new Random(seed);
        Instances data = instances;
        classIndex = data.numAttributes() -1;
        data.randomize(random);
        this.isRandom = isRandom;
        int indexOfSplit = (int) (percentToSplit * data.size());
        trainingSetSize = indexOfSplit;
        trainingData = new Instances(data, 0, samplesToStartWith);
        toTrainData = new Instances(data, samplesToStartWith, indexOfSplit - samplesToStartWith);
        testDataInstance = new Instances(data, indexOfSplit, data.size() - indexOfSplit);
    }



    public int getTrainingSetSize(){
        return trainingSetSize;
   }
    public static Instances removeXAttributes( Instances i,int x){
        Instances returnvalue = i;
        try {
            String[] options = new String[]{"-R", "1-" + x};
            Remove remove = new Remove();
            remove.setOptions(options);
            remove.setInputFormat(i);
            returnvalue = Filter.useFilter(i,remove);
        } catch (Exception e) {
            System.out.printf("Failed to remove first %f columns\n", x);
            e.printStackTrace();
        }
        return returnvalue;
    }
    public static Instances loadData(String dataCSVName) {
        try {
            DataSource source = new DataSource(dataCSVName);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            return data;

        } catch (Exception e) {
            System.out.println("Failed to Load the saved csv: " + dataCSVName);
            e.printStackTrace();
            return null;
        }
    }
    public static Instances loadData(String dataCSVName,int removeFirstNFeatures) {
        Instances i = loadData(dataCSVName);
        return removeXAttributes(i,removeFirstNFeatures);
    }

    private AbstractClassifier buildJ48(String[] options) {
        J48 m = new J48();
        try {
            m.setOptions(options);
            m.buildClassifier(trainingData);
        } catch (Exception e) {
            System.out.println("Failed to build classifier from:  " + trainingData);
            e.printStackTrace();
        }
        return m;
    }

    private AbstractClassifier buildM5P(String[] options){
        M5P m = new M5P();
        try {
            m.setOptions(options);
            m.buildClassifier(trainingData);
        } catch (Exception e) {
            System.out.println("Failed to build classifier from:  " + trainingData);
            e.printStackTrace();
        }
        return m;
    }

    private GaussianProcesses buildGaussianProcess(String[] options){
        GaussianProcesses gp = new GaussianProcesses();
        try {
            gp.setOptions(options);
            gp.buildClassifier(trainingData);
        } catch (Exception e) {
            System.out.println("Failed to build Gaussian Process");
            e.printStackTrace();
        }
        return gp;
    }
    private AbstractClassifier trainModel(boolean shallow) {
        return shallow ? buildGaussianProcess(new String[]{}) : buildM5P(new String[]{"-R","-M","4.0"});
    }

    public ArrayList<Prediction> testModel() {
        try {
            Evaluation eval = new Evaluation(trainingData);
            double[] results = eval.evaluateModel(model, testDataInstance);
            lastSetOfResults = eval.predictions();
            return lastSetOfResults;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not Test Model");
            return null;
        }
    }

    public ArrayList<Prediction> testModel(Instances testingInstances){
        try {
            Evaluation eval = new Evaluation(trainingData);
            eval.evaluateModel(model,testingInstances);
            lastSetOfResults = eval.predictions();
            return lastSetOfResults;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to test on Given Data");
            return null;
        }
    }


    public void trainShallowModel(){
        shallowModel = trainModel(true);
    }

    public void trainDeepModel(){
        model = trainModel(false);
    }

    public boolean addNextSample(){
        if(toTrainData.size() > 0){
            Instance nextDataPoint = toTrainData.remove(getNextSampleIndex());
            trainingData.add(nextDataPoint);
            return true;
        }
        return false;

    }

    public static String formatOutput(ArrayList<Prediction> e, ModelManager m){

        String s = null;
        EvalReport stats = new EvalReport(e);
        try {
            // Precision,Recall,F1,FalseP,FalseN,TrueP,TrueN"
            s = String.format("%f,%f,%f,%f,%f,%f,%f,%f,%d",
                    stats.precision, stats.recall, stats.fMeasure,stats.fpRate,stats.fnRate,stats.tpRate,stats.tnRate,stats.samples,m.trainingData.size());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;

    }
    private int getNextSampleIndex()  {
        if (isRandom){
            Random random = new Random();
            return random.nextInt(toTrainData.size());
        }
        double highestVariance = 0;
        int savedIndex = 0;
        try{
            for(int i = 0; i < toTrainData.size(); i++){
                double variance = Math.pow(((GaussianProcesses) shallowModel).getStandardDeviation(toTrainData.get(i)),2);
                if(variance > highestVariance){
                    highestVariance = variance;
                    savedIndex = i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to get variance");
        }

        return savedIndex;
    }
    @Override
    public String toString(){
        return formatOutput(lastSetOfResults, this);
    }
}


