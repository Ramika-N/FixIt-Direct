package com.ramika.fixitdirect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    private String productId;
    private String title;
    private double price;
    private int quantity;
    private String imageUrl;

}