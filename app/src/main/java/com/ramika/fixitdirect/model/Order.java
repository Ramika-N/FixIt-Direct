package com.ramika.fixitdirect.model;

import com.google.firebase.firestore.DocumentId;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @DocumentId
    private String documentId;

    private String orderId;
    private String userId;
    private String orderDate;
    private String status;
    private String paymentMethod;
    private double subtotal;
    private double deliveryFee;
    private double totalAmount;
    private Address shippingAddress;
    private List<Map<String, Object>> items;

}
