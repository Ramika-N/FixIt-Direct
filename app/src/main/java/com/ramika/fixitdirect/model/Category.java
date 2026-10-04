package com.ramika.fixitdirect.model;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category implements Serializable {

    @DocumentId
    private String id;

    private String name;
    private String iconName;

}
