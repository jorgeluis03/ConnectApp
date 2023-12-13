package com.example.connectapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectapp.databinding.IrvContainerRecentConversionBinding;
import com.example.connectapp.listeners.ConversionListener;
import com.example.connectapp.models.ChatMessage;
import com.example.connectapp.models.Users;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>{
    private final List<ChatMessage> listChatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationAdapter(List<ChatMessage> listChatMessages, ConversionListener conversionListener) {
        this.listChatMessages = listChatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        IrvContainerRecentConversionBinding binding = IrvContainerRecentConversionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ConversionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(listChatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return listChatMessages.size();
    }


    class ConversionViewHolder extends RecyclerView.ViewHolder{
        IrvContainerRecentConversionBinding binding;
        public ConversionViewHolder(IrvContainerRecentConversionBinding irvContainerRecentConversionBinding) {
            super(irvContainerRecentConversionBinding.getRoot());
            binding = irvContainerRecentConversionBinding;
        }

        void setData (ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentConversation.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                Users user = new Users();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }
    private Bitmap getConversionImage(String encodedImage){ //devule el tipo de imagen Bitmap para ponerla en la ImageView
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
