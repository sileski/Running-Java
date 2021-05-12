package com.example.runningevents.Login.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.runningevents.Login.fragments.LoginFragment;
import com.example.runningevents.Login.fragments.SignupFragment;

public class LoginAdapter extends FragmentStateAdapter {

    int totalTabs;

    public LoginAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, int totalTabs) {
        super(fragmentManager, lifecycle);
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new LoginFragment();
            case 1:
                return new SignupFragment();
            default:
                 return new LoginFragment();
        }
    }

    @Override
    public int getItemCount() {
        return totalTabs;
    }
}
