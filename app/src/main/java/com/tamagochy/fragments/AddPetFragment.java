package com.tamagochy.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tamagochy.R;
import com.tamagochy.dao.PetDAO;
import com.tamagochy.dao.ProfilePicDAO;
import com.tamagochy.dao.UserDAO;
import com.tamagochy.databinding.FragmentAddPetBinding;
import com.tamagochy.model.Pet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddPetFragment extends Fragment {

    private FragmentAddPetBinding binding;
    private Button savePetButton;
    private Button addPetProfilePic;
    private EditText editTextPetCode;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private Button buttonAddPetByCode;
    private PetDAO petDAO;
    private UserDAO userDAO;
    private String storageURL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPetBinding.inflate(inflater, container, false);
        savePetButton = binding.buttonSavePet;
        addPetProfilePic = binding.buttonUploadImage;
        buttonAddPetByCode = binding.buttonAddPetByCode;
        editTextPetCode = binding.editTextPetCode;
        storageURL = getResources().getString(R.string.default_web_client_storage_url);
        petDAO = new PetDAO();
        userDAO = new UserDAO();

        // Configuração do botão para abrir a galeria
        addPetProfilePic.setOnClickListener(view -> openGallery());

        // Configuração do botão para salvar os dados do pet
        savePetButton.setOnClickListener(view -> {
            if (selectedImageUri != null) {
                uploadImage(); // Upload da imagem selecionada
            } else{
                savePetOnDB("pet_placeholder.png");
            }
        });

        buttonAddPetByCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alphanumericCode = binding.editTextPetCode.getText().toString();
                Log.d("ALPHA CODE", alphanumericCode);

                // Reference to the "pets" collection
                DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("pets");
                Log.d( " CURRENTUSER ", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                // Query to find the pet by its alphanumericCode
                petsRef.orderByChild("alphanumericCode").equalTo(alphanumericCode)
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Iterate through the snapshot to get the petID
                                    for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                                        String petID = petSnapshot.getKey();  // Get the petID (the key is the petID)
                                        String name = petSnapshot.child("name").getValue(String.class);

                                        // Log the petID and name
                                        Log.d("PET_FOUND", "Pet found: " + name + " with ID: " + petID);
                                    }
                                } else {
                                    Log.e("PET_NOT_FOUND", "No pet found with the alphanumeric code.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("QUERY_ERROR", "Database error: " + error.getMessage());
                            }
                        });
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Abre a galeria para o usuário selecionar uma imagem
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Trata o resultado da seleção de imagem
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData(); // Salva o URI da imagem selecionada
            binding.imageViewPet.setImageURI(selectedImageUri); // Exibe a imagem no ImageView
        }
    }

    // Faz upload da imagem para o Firebase Storage e salva os dados do pet no banco de dados
    private void uploadImage() {
        if (selectedImageUri != null) {
            ProfilePicDAO profilePicDAO = new ProfilePicDAO();
            profilePicDAO.uploadProfilePic(selectedImageUri, new ProfilePicDAO.UploadCallback() {
                @Override
                public void onSuccess(String uniqueId) {
                    // Salvar o uniqueId no banco de dados do pet
                    savePetOnDB(uniqueId); // Passa o uniqueId ao invés da URL
                }

                @Override
                public void onError(String errorMessage) {
                    // Handle errors (e.g., show a toast or log the error)
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Se não houver imagem selecionada, salva com um uniqueId de placeholder
            // savePetOnDB("/img/pet_placeholder.png");
            Log.d("UPLOAD FOTO", "SEM FOTO");
        }
    }

    // Método para salvar os dados do pet no banco de dados
    private void savePetOnDB(String profilePicId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String petName = binding.editTextPetName.getText().toString().trim();

        if (petName.isEmpty()) {
            binding.editTextPetName.setError("O nome é obrigatório!");
            return;
        }

        // Criar o objeto Pet com o nome e o profilePicId
        String petCode = generateAlphanumericCode();
        Pet pet = new Pet(petName, profilePicId);
        List<String> tutores = new ArrayList<>();
        pet.setTutorID(tutores);
        pet.addTutorId(userId);
        pet.setAlphanumericCode(petCode);
        long currentTime = System.currentTimeMillis();
        pet.setLastMeal(currentTime);
        String petId = FirebaseDatabase.getInstance().getReference("pets").push().getKey();

        // Salvar o pet e vinculá-lo ao usuário
        petDAO.savePet(pet, petId, new PetDAO.DataCallback() {
            @Override
            public void onSuccess() {
                userDAO.addPetToUser(userId, petId, new UserDAO.DataCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Pet adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                        navigateToHomeFragment();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateAlphanumericCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    private void navigateToHomeFragment() {
        if (getView() != null) {
            NavController navController = Navigation.findNavController(binding.getRoot());
            navController.navigate(R.id.fragment_home); // Navega para a tela inicial
        }
    }
}
