package com.tamagochy;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tamagochy.adapter.PetAdapter;
import com.tamagochy.databinding.FragmentHomeBinding;
import com.tamagochy.interfaces.LogoutListener;
import com.tamagochy.model.Pet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements LogoutListener {

    private FragmentHomeBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            //TODO
            Log.d("FIREBASE LOGOUT", user.getDisplayName() + "");

            setupRecyclerView();
        } else {
            //TODO
            // navigateToLoginFragment();
        }

        FloatingActionButton addPetButton = binding.fabAddPet;
        addPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToAddFragment(view);
            }
        });

        return binding.getRoot();
    }



    private void setupRecyclerView() {
        String storageUrl = getResources().getString(R.string.default_web_client_storage_url);
        RecyclerView recyclerView = binding.recyclerViewPets;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference petsRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("pets");

            // Lista para armazenar os pets
            List<Pet> pets = new ArrayList<>();
            PetAdapter adapter = new PetAdapter(pets);
            recyclerView.setAdapter(adapter);

            // Recuperar a lista de UIDs dos pets
            petsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    pets.clear(); // Limpa a lista de pets antes de adicionar novos dados


                    List<String> petIds = new ArrayList<>();
                    for (DataSnapshot petSnapshot : snapshot.getChildren()) {
                        String petId = petSnapshot.getKey();
                        Log.d("Firebase", "Pet ID: " + petId);
                        if (petId != null) {
                            petIds.add(petId);
                        }
                    }

                    // Recuperar detalhes de todos os pets
                    getPetDetails(petIds, pets, adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase Pets Ref", "Erro ao recuperar os UIDs dos pets: " + error.getMessage());
                    navigateToLoginFragment();
                }
            });
        }
    }

    // Método auxiliar para buscar os detalhes dos pets
    private void getPetDetails(List<String> petIds, List<Pet> pets, PetAdapter adapter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference petsDatabaseRef = FirebaseDatabase.getInstance().getReference("pets");
            for (String petId : petIds) {
                petsDatabaseRef.child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String petName = dataSnapshot.child("name").getValue(String.class);
                        String petImage = dataSnapshot.child("imageUrl").getValue(String.class);
                        String petCode = dataSnapshot.child("alphanumericCode").getValue(String.class);
                        Long petLastMealTimestamp = dataSnapshot.child("lastMeal").getValue(Long.class);
                        String petLastMeal;
                        if (petLastMealTimestamp != null) {
                            // Converte o timestamp para um objeto Date
                            Date petLastMealDate = new Date(petLastMealTimestamp);

                            // Define o formato da data
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                            // Formata a data para o formato desejado
                            petLastMeal = dateFormat.format(petLastMealDate);

                            // Agora você tem a data formatada
                            Log.d("Pet Last Meal", "Last Meal: " + petLastMeal);
                        } else {
                            // Caso o valor de lastMeal seja nulo, você pode definir um valor padrão
                            petLastMeal = "Data não disponível";
                            Log.d("Pet Last Meal", "Last Meal: " + petLastMeal);
                        }
                        // Verifica se os dados são válidos antes de adicionar à lista
                        if (petName != null && petImage != null) {
                            Pet pet = new Pet(petName, petImage);
                            pet.setId(petId);
                            pet.setAlphanumericCode(petCode);
                            pet.setLastMeal(petLastMealTimestamp);
                            pets.add(pet);
                        }

                        // Atualiza o adapter somente após a última iteração
                        if (pets.size() == petIds.size()) {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase Pets Details", "Erro ao recuperar detalhes do pet: " + error.getMessage());
                        navigateToLoginFragment();
                    }
                });
            }
        }
    }




    @Override
    public void onLogout() {
        // Navegar para a página de login
        Navigation.findNavController(requireView()).navigate(R.id.action_fragment_home_to_fragment_login);
    }

    private void navigateToAddFragment(View view) {
        NavController navController = Navigation.findNavController(binding.getRoot());
        navController.navigate(R.id.fragment_add_pet); // Use the action ID from your navigation graph
    }

    public void navigateToLoginFragment() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.fragment_login);
    }


}
