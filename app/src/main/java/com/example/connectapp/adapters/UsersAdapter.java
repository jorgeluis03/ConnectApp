package com.example.connectapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectapp.databinding.IrvContainerUserBinding;
import com.example.connectapp.models.Users;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private final List<Users> listaUsers;

    public UsersAdapter(List<Users> listaUsers) {
        this.listaUsers = listaUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        IrvContainerUserBinding irvContainerUserBinding = IrvContainerUserBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(irvContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(listaUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return listaUsers.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder{
        IrvContainerUserBinding binding;
        UserViewHolder(IrvContainerUserBinding irvContainerUserBinding) {
            super(irvContainerUserBinding.getRoot());
            binding = irvContainerUserBinding;
        }
        void setUserData(Users user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
        }


    }

    private Bitmap getUserImage(String encondedImage){
        byte[] bytes = Base64.decode(encondedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
