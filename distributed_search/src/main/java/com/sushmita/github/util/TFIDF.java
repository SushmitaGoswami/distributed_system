package com.sushmita.github.util;

import com.sushmita.github.model.DocumentModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TFIDF = Term Frequency/ Inverse document frequency
 */
public class TFIDF {

    public static Map<String, Double> scoreAllDocuments(List<DocumentModel> documents, List<String> searchTerms){
        Map<String, Double> scoredDocuments = new TreeMap<>();
        Map<String, Map<DocumentModel,Double>> termFrequencies = calculateTermFrequencies(documents, searchTerms);
        Map<String, Double> inverseTermFrequency = calculateInverseDocumentFrequency(documents, termFrequencies, searchTerms);
        for(DocumentModel document : documents){
            calculateScoreOfDocument(termFrequencies, inverseTermFrequency, documents, searchTerms);
        }
        return scoredDocuments;
    }

    private static Map<String, Map<DocumentModel,Double>> calculateTermFrequencies(List<DocumentModel> documentModels, List<String> searchTerms){
        Map<String, Map<DocumentModel,Double>> termFrequencies = new HashMap<>();

        for(String term : searchTerms){
            termFrequencies.put(term,calculateTermFrequency(term, documentModels));
        }

        return termFrequencies;
    }

    private static Map<DocumentModel,Double> calculateTermFrequency(String term, List<DocumentModel> documentModels) {
        Map<DocumentModel, Double> termFrequencies = new HashMap<>();

        for(DocumentModel documentModel:documentModels){
            termFrequencies.put(documentModel, documentModel.getTermFrequency(term));
        }
        return termFrequencies;
    }


    private static Map<String, Double> calculateInverseDocumentFrequency(List<DocumentModel> documents, Map<String, Map<DocumentModel, Double>> termFrequencies, List<String> searchTerms) {

        Map<String, Double> inverseDocumentFrequencies = new HashMap<>();
        for(String term:searchTerms){
            int count = 0;
            for(DocumentModel documentModel:documents){
                if(termFrequencies.get(term).get(documentModel)!=0.0){
                    count++;
                }
            }

            inverseDocumentFrequencies.put(term, (double)documents.size()/count);
        }

        return inverseDocumentFrequencies;
    }

    private static Double calculateScoreOfDocument(Map<String, Map<DocumentModel, Double>> termFrequencies,
                                                   Map<String, Double> inverseTermFrequency,
                                                   List<DocumentModel> documents,
                                                   List<String> searchTerms){
        Double score = 0.0;


        return score;
    }
}
