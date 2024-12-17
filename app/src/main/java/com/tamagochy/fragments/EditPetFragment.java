package com.tamagochy.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tamagochy.R;
import com.tamagochy.dao.PetDAO;
import com.tamagochy.dao.ProfilePicDAO;
import com.tamagochy.databinding.FragmentEditPetBinding;

public class EditPetFragment extends Fragment {

    private FragmentEditPetBinding binding;
    private PetDAO petDAO;
    private String petId;
    private Uri selectedImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPetBinding.inflate(inflater, container, false);
        petDAO = new PetDAO();

        // Retrieve pet data from arguments
        if (getArguments() != null) {
            petId = getArguments().getString("petId");
            String petName = getArguments().getString("petName");
            String imageUrl = getArguments().getString("imageUrl");

            // Populate UI elements
            binding.editTextPetName.setText(petName);
            if (imageUrl != null) {
                Glide.with(requireContext()).load(imageUrl).into(binding.imageViewPet);
            }
        }

        // Save button logic
        binding.buttonEditPet.setOnClickListener(view -> {
            String updatedName = binding.editTextPetName.getText().toString().trim();

            if (updatedName.isEmpty()) {
                binding.editTextPetName.setError("O nome é obrigatório!");
                return;
            }

            // Update pet in database
            if (selectedImageUri != null) {
                uploadImage(updatedName);
            } else {
                updatePetInDB(updatedName, null); // Pass null if no new image is uploaded
            }
        });

        // Upload new image logic
        binding.buttonUploadImage.setOnClickListener(view -> openGallery());

        return binding.getRoot();
    }

    // Opens the gallery for the user to select an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    // Handle the result of image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.imageViewPet.setImageURI(selectedImageUri);
        }
    }

    // Upload new image to Firebase Storage
    private void uploadImage(String updatedName) {
        ProfilePicDAO profilePicDAO = new ProfilePicDAO();
        profilePicDAO.uploadProfilePic(selectedImageUri, new ProfilePicDAO.UploadCallback() {
            @Override
            public void onSuccess(String uniqueId) {
                updatePetInDB(updatedName, uniqueId);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update pet details in the database
    private void updatePetInDB(String updatedName, @Nullable String newImageId) {
        DatabaseReference petRef = FirebaseDatabase.getInstance().getReference("pets").child(petId);

        petRef.child("name").setValue(updatedName);

        if (newImageId != null) {
            petRef.child("profilePic").setValue(newImageId);
        }

        petRef.child("lastMeal").setValue(System.currentTimeMillis()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Pet atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                navigateToHomeFragment();
            } else {
                Toast.makeText(getContext(), "Erro ao atualizar o pet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Navigate back to the HomeFragment
    private void navigateToHomeFragment() {
        NavController navController = Navigation.findNavController(binding.getRoot());
        navController.navigate(R.id.fragment_home);
    }
}
