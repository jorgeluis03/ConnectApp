package com.example.connectapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectapp.databinding.IrvContainerReveivedMessageBinding;
import com.example.connectapp.databinding.IrvContainerSentMessageBinding;
import com.example.connectapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ChatMessage> chatMessageList;
    private final Bitmap receiverProfileImage;
    private final String senderID;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    public ChatAdapter(List<ChatMessage> chatMessageList, Bitmap receiverProfileImage, String senderID) {
        this.chatMessageList = chatMessageList;
        this.receiverProfileImage = receiverProfileImage;
        this.senderID = senderID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENT){
            return new SentMessageViewHolder(IrvContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }else {
            return new ReceivedMessageViewHolder(IrvContainerReveivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).senderId.equals(senderID)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final IrvContainerSentMessageBinding binding;
        SentMessageViewHolder(IrvContainerSentMessageBinding irvContainerSentMessageBinding) {
            super(irvContainerSentMessageBinding.getRoot());
            binding = irvContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final IrvContainerReveivedMessageBinding binding;
        ReceivedMessageViewHolder(IrvContainerReveivedMessageBinding irvContainerReveivedMessageBinding) {
            super(irvContainerReveivedMessageBinding.getRoot());
            binding = irvContainerReveivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
    }
}
