package com.tamagochy.model;

import java.util.List;
import java.util.Random;

public class Pet {
    private String name;
    private Long lastMeal;
    private String imageUrl;
    private List<String> tutorID;
    private String petId;// ID do tutor
    private String alphaCode;

    // Construtor
    public Pet(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }


    // Getters
    public String getName() {
        return name;
    }

    public Long getLastMeal() {
        return lastMeal;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAlphanumericCode() {
        return alphaCode;
    }

    public String getPetId(){return  this.petId;}

    public List<String> getTutorID() {
        return tutorID;
    }


    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setLastMeal(Long lastMeal) {
        this.lastMeal = lastMeal;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setId(String petId) {
        this.petId = petId;
    }

    public void setTutorID(List<String> tutorID) {
        this.tutorID = tutorID;
    }

    public void addTutorId(String id){
        this.tutorID.add(id);
    }

    public void setAlphanumericCode(String alphanumericCode) {
        this.alphaCode = alphanumericCode;
    }
}
