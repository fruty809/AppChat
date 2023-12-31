package com.example.appchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.appchat.adapters.UserAdapter;
import com.example.appchat.databinding.ActivityUsersBinding;
import com.example.appchat.listeners.UserListeners;
import com.example.appchat.models.User;
import com.example.appchat.utilities.Constants;
import com.example.appchat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListeners {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager =  new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener( v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database  = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener(task -> {
           loading(false);
           String currentUSerId = preferenceManager.getString(Constants.KEY_USER_ID);
           if (task.isSuccessful() && task.getResult() !=null){
               List<User> users = new ArrayList<>();
               for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                   if (currentUSerId.equals(queryDocumentSnapshot.getId())){
                       continue;
                   }
                   User user = new User();
                   user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                   user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                   user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                   user.token= queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                   users.add(user);
               }
               if (users.size() > 0){
                   UserAdapter userAdapter = new UserAdapter(users , this);
                   binding.usersRecyclerView.setAdapter(userAdapter);
                   binding.usersRecyclerView.setVisibility(View.VISIBLE);
               }else{
                   showErrorMessage();
               }
           }else {
               showErrorMessage();
           }
        });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s" , "No users available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUseClicked(User user) {
        Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}