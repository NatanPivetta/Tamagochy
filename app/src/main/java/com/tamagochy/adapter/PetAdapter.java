package com.tamagochy.adapter;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tamagochy.R;
import com.tamagochy.dao.ProfilePicDAO;
import com.tamagochy.dao.PetDAO;
import com.tamagochy.dao.UserDAO;
import com.tamagochy.model.Pet;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<Pet> petList;
    private String userId;

    private ProfilePicDAO profilePicDAO;
    private PetDAO petDAO;
    private UserDAO userDAO;

    public PetAdapter(List<Pet> petList) {
        this.petList = petList;
        this.profilePicDAO = new ProfilePicDAO();
        this.petDAO = new PetDAO();
        this.userDAO = new UserDAO();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.petName.setText(pet.getName());
        holder.petCode.setText(pet.getAlphanumericCode());

        String petCode = pet.getAlphanumericCode();
        if(petCode != null){
        Log.d("PET CODE ADAPTER", petCode);
        }

        if(pet.getLastMeal() == null){
            holder.petLastMeal.setText("Pet não alimentado");
        }else{
            Date lastMealDate = new Date(pet.getLastMeal());
            // Definir o formato de data
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formatedLastMealString = dateFormat.format(lastMealDate);
            holder.petLastMeal.setText(formatedLastMealString);

        }


        String imagePath = "gs://" + profilePicDAO.getStorageBucket() + "/pets_images/" + pet.getImageUrl();
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);

        Log.d("GLIDE", imagePath);

        if (imagePath != null && !imagePath.isEmpty()) {
            // Get the download URL for the image
            photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Load the image into the ImageView using Glide
                Glide.with(holder.itemView.getContext())
                        .load(uri)  // The URI from Firebase Storage
                        .placeholder(R.drawable.pet_placeholder)  // Placeholder image while loading
                        .into(holder.petProfilePic);  // Load into the ImageView
            }).addOnFailureListener(e -> {
                // In case of failure, use a placeholder image
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.pet_placeholder)  // Placeholder
                        .into(holder.petProfilePic);  // Load into the ImageView
                Log.e("PET ADAPTER", "Failed to load image", e);
            });
        }

        holder.feedButton.setOnClickListener(v -> {
            feedPet(pet, position);
        });

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            deletePet(pet, position);
        });

        holder.editButton.setOnClickListener(v ->{
            Bundle petBundle = new Bundle();
            petBundle.putString("petId", pet.getPetId());
            petBundle.putString("petName", pet.getName());
            petBundle.putString("petImageUrl", pet.getImageUrl());

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_fragment_home_to_fragment_login, petBundle);
        });
    }




    @Override
    public int getItemCount() {
        return petList.size();
    }

    private void feedPet(Pet pet, int position) {
        // Get the current time in milliseconds (timestamp)
        long currentTime = System.currentTimeMillis();

        // Get the pet ID from the pet object in the current position (assuming the pet object is available)
        String petId = pet.getPetId(); // Get the pet ID

        // Reference to the pet in the Realtime Database
        DatabaseReference petRef = FirebaseDatabase.getInstance().getReference("pets").child(petId);

        // Update the 'lastMeal' field with the current timestamp
        petRef.child("lastMeal").setValue(currentTime).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("PET_ADAPTER", "Pet's lastMeal timestamp updated successfully.");
                pet.setLastMeal(currentTime);
                petList.set(position, pet);
                notifyItemChanged(position);
            } else {
                Log.e("PET_ADAPTER", "Failed to update pet's lastMeal timestamp.", task.getException());
            }
        });
    }


    private void deletePet(Pet pet, int position) {
        // Step 1: Remove pet from the user's pet list in the Realtime Database
        userDAO.deletePetFromUser(userId, pet.getPetId(), new UserDAO.DeletionCallback() {
            @Override
            public void onSuccess() {
                // Step 2: Delete pet from the Realtime Database
                petDAO.deletePet(pet.getPetId(), new PetDAO.DeletionCallback() {
                    @Override
                    public void onSuccess() {
                        // Step 3: Delete pet's profile picture from Firebase Storage
                        if(!pet.getImageUrl().equals("pet_placeholder.png")){

                        profilePicDAO.deleteProfilePic(pet.getImageUrl(), new ProfilePicDAO.DeletionCallback() {
                            @Override
                            public void onSuccess() {
                                // After all deletions are successful, remove pet from the list and notify the adapter
                                if (position >= 0 && position < petList.size()) {
                                    petList.remove(position);
                                    notifyItemRemoved(position);
                                } else {
                                    Log.e("PET ADAPTER", "Invalid index when trying to remove pet: " + position);
                                }
                            }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("PET ADAPTER", "Error deleting pet's profile picture", e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("PET ADAPTER", "Error deleting pet from database", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("PET ADAPTER", "Error removing pet from user's list", e);
            }
        });
    }


    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petName, petCode, petLastMeal;
        Button deleteButton, feedButton, editButton;
        ImageView petProfilePic;


        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petName = itemView.findViewById(R.id.text_pet_name);
            deleteButton = itemView.findViewById(R.id.button_delete_pet);
            feedButton = itemView.findViewById(R.id.button_feed_pet);
            petProfilePic = itemView.findViewById(R.id.image_pet);
            petCode = itemView.findViewById(R.id.text_pet_code);
            petLastMeal = itemView.findViewById(R.id.text_last_meal);
            editButton = itemView.findViewById(R.id.button_edit_pet);
        }
    }
}
