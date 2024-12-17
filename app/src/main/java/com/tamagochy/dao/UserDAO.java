package com.tamagochy.dao;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDAO {
    private DatabaseReference usersRef;

    public UserDAO() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public void addPetToUser(String userId, String petId, DataCallback callback) {
        // Reference to the user's pets collection
        DatabaseReference userPetsRef = usersRef.child(userId).child("pets");

        // Add the petID under the petCode in the user's pets collection
        userPetsRef.child(petId).setValue(true)  // Assuming we only store the petId; adjust if needed
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                        Log.d("USERDAO", "Pet added successfully to the user's collection.");
                    } else {
                        callback.onError("Failed to add pet to user's collection: " + task.getException().getMessage());
                        Log.e("USERDAO", "Failed to add pet: " + task.getException().getMessage());
                    }
                });
    }

    public void deletePetFromUser(String userId, String petId, DeletionCallback callback) {
        // Reference to the user's pets collection
        DatabaseReference userPetsRef = usersRef.child(userId).child("pets");

        // Remove the petID from the user's pets collection
        userPetsRef.child(petId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                        Log.d("USERDAO", "Pet removed successfully from the user's collection.");
                    } else {
                        callback.onFailure(task.getException());
                        Log.e("USERDAO", "Failed to remove pet: " + task.getException().getMessage());
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
