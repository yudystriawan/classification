/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skripsi.classifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsastrawi.morphology.DefaultLemmatizer;
import jsastrawi.morphology.Lemmatizer;

/**
 *
 * @author Yudystriawan
 */
public class Preprocessing {

    private String text;

    public Preprocessing(String text) {
        this.text = text;
    }

    private List<String> casefoldingTokenizing() {
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s");
        List<String> caseToken = new ArrayList<>();
        for (String word : words) {
            caseToken.add(word);
        }
        return caseToken;
    }

    private List<String> filtering() {
        List<String> filter = new ArrayList<>();
        List<String> words = casefoldingTokenizing();

        InputStream inputStream = getClass().getResourceAsStream("/kamus_stopword.txt");
        List<String> lists = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                lists.add(line);
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (String word : words) {
            if (!lists.contains(word)) {
                filter.add(word);
            }
        }
        return filter;
    }

    private List<String> stemming() {
        List<String> stem = new ArrayList<>();
        List<String> words = filtering();
        Set<String> dictionaries = new HashSet<>();
        if (dictionaries.isEmpty()) {
            InputStream is = Lemmatizer.class.getResourceAsStream("/root-words.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    dictionaries.add(line);
                }
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Lemmatizer l = new DefaultLemmatizer(dictionaries);
        for (String word : words) {
            stem.add(l.lemmatize(word));
        }
        return stem;
    }

    public String result() {
        List<String> words = stemming();
        String result = null;
        for (String word : words) {
            if (result == null) {
                result = word;
            } else {
                result += " " + word;
            }
        }
        return result;
    }
}
