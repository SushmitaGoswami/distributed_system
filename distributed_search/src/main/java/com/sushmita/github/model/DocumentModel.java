package com.sushmita.github.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentModel implements Serializable {
    private String name;
    private String path;
    private List<String> words;

    public DocumentModel(String name, String path, List<String> words){
        this.name = name;
        this.path = path;
        this.words = words;
    }

    // term frequency = (no of times the term present in the document)/total_no_of_documents
    public Double getTermFrequency(String term){
        int count = 0;
        for(String word:words){
            if(word.equalsIgnoreCase(term))
                count++;
        }

        return words.size()!=0?(double)count/words.size():0.0;
    }


}
