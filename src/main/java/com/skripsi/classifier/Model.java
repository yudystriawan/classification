/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skripsi.classifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.misc.InputMappedClassifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 *
 * @author Yudystriawan
 */
public class Model {

    private InputStream trainFile = getClass().getResourceAsStream("/dataset_training_filtered.arff");
    File modelPath = new File(getClass().getResource("/myModel.model").getFile());

    private Instances trainInstances, testInstances;

    public Model() {
        ConverterUtils.DataSource dataTrain = new ConverterUtils.DataSource(trainFile);
        try {
            this.trainInstances = dataTrain.getDataSet();
        } catch (Exception ex) {
            Logger.getLogger(Classification.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    public void createModel() {

        trainInstances.setClassIndex(trainInstances.numAttributes() - 1);

        //filteredclassifier
        FilteredClassifier fc = new FilteredClassifier();

        //inputmapped
        InputMappedClassifier imc = new InputMappedClassifier();

        //bagging
        Bagging b = new Bagging();

        //naive bayes
        NaiveBayes nb = new NaiveBayes();

        //filter stringtowordvector
        StringToWordVector stwv = new StringToWordVector();

        //setting fc
        fc.setClassifier(imc);
        fc.setFilter(stwv);

        //setting imc
        imc.setClassifier(b);
        imc.setIgnoreCaseForNames(true);
        imc.setSuppressMappingReport(true);
        imc.setTrim(true);

        //setting b
        b.setClassifier(nb);
        b.setNumIterations(25);

        //setting stwv
        stwv.setIDFTransform(true);
        stwv.setTFTransform(true);
        stwv.setLowerCaseTokens(true);
        stwv.setTokenizer(new WordTokenizer());
        stwv.setWordsToKeep(10000000);
        try {
            fc.buildClassifier(trainInstances);

            ObjectOutputStream model = new ObjectOutputStream(new FileOutputStream(modelPath.toString()));
            model.writeObject(fc);
            model.flush();
            model.close();

        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public Instances customInstances(String text, String prediocion) {
        FastVector fastVector = new FastVector();
        fastVector.addElement("Dinas Lingkungan Hidup Dan Kebersihan Kota Denpasar");
        fastVector.addElement("Dinas Pekerjaan Umum Dan Penataan Ruang Kota Denpasar");
        fastVector.addElement("Dinas Perhubungan Kota Denpasar");
        fastVector.addElement("PDAM Kota Denpasar");
        Attribute a = new Attribute("isi pengaduan", true);
        Attribute a1 = new Attribute("instansi tujuan", fastVector);

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(a);
        attributes.add(a1);

        Instances testing = new Instances("testing", attributes, 0);

        double[] values = new double[testing.numAttributes()];
        values[0] = testing.attribute(0).addStringValue(text);
        values[1] = fastVector.indexOf(prediocion);

        testing.add(new DenseInstance(1.0, values));

        return testing;
    }
    
    public JSONObject process(String text) {
        File model = new File(modelPath.toString());
        if (!model.exists()) {
            createModel();
        }
        StringToWordVector stwv = new StringToWordVector();
        Preprocessing p = new Preprocessing(text);
        JSONObject jsono = new JSONObject();
        
        PlainText pt = new PlainText();
        StringBuffer sb = new StringBuffer();

        try {
            FilteredClassifier fc = (FilteredClassifier) SerializationHelper.read(modelPath.toString());
            testInstances = customInstances(p.result(), "PDAM Kota Denpasar");
            
            if (testInstances.classIndex() == -1) {
                testInstances.setClassIndex(testInstances.numAttributes() - 1);
            }
            if (trainInstances.classIndex() == -1) {
                trainInstances.setClassIndex(trainInstances.numAttributes() - 1);
            }

            //peform prediction
            double classValue = fc.classifyInstance(testInstances.lastInstance());
            // get the name classValue
            String prediction = testInstances.classAttribute().value((int) classValue);
//            jsono.put("coba", modelPath.toString());
            Instances predicInstances = customInstances(text, prediction);
            predicInstances.setClassIndex(predicInstances.numAttributes() - 1);
            //evalueate
            pt.setHeader(testInstances);
            pt.setBuffer(sb);
            Evaluation e = new Evaluation(trainInstances);
            e.evaluateModel(fc, testInstances, pt);
            String pre = sb.toString();
            String[] words = pre.split("   ");

            double acc = Double.parseDouble(words[words.length - 1].trim());
            double percent = acc * 100;

            //result
            jsono.put("isi", text);
            jsono.put("instansi", prediction);
            jsono.put("percent", percent + "%");
            
        } catch (Exception ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return jsono;
    }
}
