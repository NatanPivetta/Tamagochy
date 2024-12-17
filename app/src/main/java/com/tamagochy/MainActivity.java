package com.tamagochy;

import com.tamagochy.fragments.HomeFragment;
import com.tamagochy.interfaces.LogoutListener;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tamagochy.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private LogoutListener logoutListener;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActivityMainBinding binding;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private String userEmail;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userName = "";
        userEmail = "";

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        HomeFragment homeFragment = (HomeFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
        logoutListener = homeFragment;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_view, R.id.fragment_home, R.id.fragment_login)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_action_logout) {
                    Log.d("DRAWER MENU", "LOGOUT TRIGGERED");
                    logout(); // Chama a mesma função de logout da toolbar
                    if(logoutListener != null){
                        drawer.closeDrawer(GravityCompat.START);
                        homeFragment.navigateToLoginFragment();
                    }

                    return true;
                }
                else if (id == R.id.menu_action_about) {
                    navController.navigate(R.id.fragment_about);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;

                }else if(id == R.id.fragment_add_pet) {
                    navController.navigate(R.id.fragment_add_pet);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }

                return false;
            }
        });

       // BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        // NavigationUI.setupWithNavController(bottomNavigationView,Navigation.findNavController(this, R.id.nav_host_fragment));


        if (!userIsLoggedIn()) {
            navController.navigate(R.id.fragment_login);
        } else {
            View headerView = navigationView.getHeaderView(0);
            TextView userNameTxt = headerView.findViewById(R.id.userName);
            TextView userEmailTxt = headerView.findViewById(R.id.userMail);
            userNameTxt.setText(userName);
            userEmailTxt.setText(userEmail);
            navController.navigate(R.id.fragment_home);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.fragment_login) {
            Log.d("MENU", "LogOut Triggered");

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
            // Chama revokeAccess
            googleSignInClient.revokeAccess()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // O revokeAccess foi concluído com sucesso
                                FirebaseAuth.getInstance().signOut();
                                Log.d("SignOut", "Revoke Access concluído e signOut realizado.");
                                Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.action_fragment_home_to_fragment_login);
                            } else {
                                // Algo deu errado no revokeAccess
                                Log.e("SignOut", "Erro ao revogar acesso.", task.getException());
                            }
                        }
                    });


            return true;
        }else if(item.getItemId() == R.id.fragment_add_pet) {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_fragment_home_to_fragment_addPet);
            return true;
        } else if (item.getItemId() == R.id.fragment_about) {
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragment_about);
                return true;
            }

        else {
            return super.onOptionsItemSelected(item);
            }

    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private boolean userIsLoggedIn() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
        userEmail = currentUser.getEmail();
        userName = currentUser.getDisplayName();
        return true;
        }
        return false;
    }

    private void logout() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.revokeAccess();
        FirebaseAuth.getInstance().signOut();
    }


}

