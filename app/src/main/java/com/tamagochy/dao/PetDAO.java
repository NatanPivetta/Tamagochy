package com.tamagochy.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tamagochy.model.Pet;

import java.util.ArrayList;

public class PetDAO {
    private DatabaseReference petsRef;

    public PetDAO() {
        petsRef = FirebaseDatabase.getInstance().getReference("pets");
    }


    public void savePet(Pet pet, String petId, final DataCallback callback) {
        petsRef.child(petId).setValue(pet)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Erro ao salvar dados do pet"));
    }

    public void deletePet(String petId, DeletionCallback callback) {
        petsRef.child("pets").child(petId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("PET DAO", "Pet deleted from database");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("PET DAO", "Error deleting pet from database", e);
                    callback.onFailure(e);
                });
    }

    public void addTutorToPet(String petId, String userId, DataCallback callback) {
        DatabaseReference petTutorsRef = FirebaseDatabase.getInstance()
                .getReference("pets")
                .child(petId)
                .child("tutors");

        // Add the user ID under the tutors node of the pet
        petTutorsRef.child(userId).setValue(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    public interface DeletionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface DataCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
