package com.ramika.fixitdirect.model;

import com.google.firebase.firestore.DocumentId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @DocumentId
    private String id;

    private String type; // Home or Office
    private String fullName;
    private String fullAddress;
    private String phone;
    private boolean isDefault;

}
