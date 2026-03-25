package com.ramika.fixitdirect.model;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    @DocumentId
    private String id;

    private String title;
    private String category;
    private double price;
    private double rating;
    private String description;
    private List<String> images;
    private int stockCount;
    private String status;

}
