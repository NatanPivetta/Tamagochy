package com.tamagochy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tamagochy.databinding.FragmentAboutBinding;


public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;
    private String aboutText;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);



        return binding.getRoot();
    }
}
