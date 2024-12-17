package com.tamagochy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tamagochy.R;
import com.tamagochy.databinding.FragmentLoginBinding;
import com.tamagochy.model.User;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    handleGoogleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
                } else {
                    Log.d(TAG, "Google Sign-In failed or was canceled.");
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        setupGoogleSignIn();
        setupUI();

        return binding.getRoot();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions);
    }

    private void setupUI() {
        binding.googleSignInButton.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            binding.getRoot().post(()-> {
                    Log.d(TAG, "User already signed in: " + currentUser.getDisplayName());
                    navigateToHomeFragment();

        });

    }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful: " + account.getEmail());
                authenticateWithFirebase(account);
            }
        } catch (ApiException e) {
            Log.d(TAG, "Google Sign-In failed: " + e.getStatusCode(), e);
            if (e.getStatusCode() == 12501) {
                Log.e(TAG, "The user canceled the sign-in.");
            }
        }
    }

    private void authenticateWithFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    saveUserToDatabase(new User(user.getEmail(), user.getDisplayName(), user.getUid()));
                    navigateToHomeFragment();
                }
            } else {
                Log.d(TAG, "Firebase authentication failed.", task.getException());
            }
        });
    }

    private void saveUserToDatabase(User user) {
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUserUID());
        usersReference.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                usersReference.setValue(user).addOnSuccessListener(aVoid -> Log.d(TAG, "New user added to database."));
            } else {
                Log.d(TAG, "User already exists in database: " + user.getUserUID());
            }
        });
    }

    private void navigateToHomeFragment() {
        NavController navController = Navigation.findNavController(binding.getRoot());
        navController.navigate(R.id.fragment_home); // Use the action ID from your navigation graph
    }
}
