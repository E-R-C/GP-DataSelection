import weka.classifiers.evaluation.Prediction;

import java.util.ArrayList;

public class EvalReport {
    public double falsePositives, falseNegatives, truePositives, trueNegatives, fMeasure, meanSquareError,
            precision, recall, samples, fpRate, fnRate, tpRate, tnRate;

    public EvalReport(ArrayList<Prediction> results){
        double maxError = 0;
        double roundPoint = 0.1;
        for (Prediction p: results){
            double predicted = rounder(p.predicted(), roundPoint);
            double actual = rounder(p.actual(), roundPoint);
            if (predicted == 0 && actual == 0){
                trueNegatives += 1;
            } else if(predicted > 0 && actual > 0){
                truePositives += 1;
            } else if (predicted == 0 && actual > 0){
                falseNegatives += 1;
            } else if (predicted > 0 && actual == 0){
                falsePositives += 1;
            } else {
                System.out.printf("This is what broke the string Predicted %d, Actual: %d", predicted, actual);
            }
            maxError += Math.pow(p.actual() - p.predicted(),2);
            samples += 1;
        }
        meanSquareError = maxError/results.size();
        precision = truePositives/(truePositives + falsePositives);
        recall = truePositives/(truePositives + falseNegatives);
        fMeasure = 2 * (precision * recall) / (precision + recall);
        fpRate = falsePositives/samples;
        fnRate = falseNegatives/samples;
        tpRate = truePositives/samples;
        tnRate = trueNegatives/samples;

    }

    private static double rounder(double point, double roundpoint){
        double base = Math.floor(point);
        double deci = point - base;
        if (deci < roundpoint){
            return base;
        }
        return Math.ceil(point);
    }
}
