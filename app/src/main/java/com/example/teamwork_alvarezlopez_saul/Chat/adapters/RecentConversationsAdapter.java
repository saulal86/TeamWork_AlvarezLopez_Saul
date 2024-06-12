package com.example.teamwork_alvarezlopez_saul.Chat.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamwork_alvarezlopez_saul.Chat.models.ChatMessage;
import com.example.teamwork_alvarezlopez_saul.Chat.models.User;
import com.example.teamwork_alvarezlopez_saul.Chat.listeners.ConversionListener;
import com.example.teamwork_alvarezlopez_saul.databinding.ItemContainerRecentConversionBinding;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),parent,false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.datos(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

            void datos(ChatMessage chatMessage){
                binding.textName.setText(chatMessage.conversionName);
                binding.textRecentMessage.setText(chatMessage.mensaje);
                binding.getRoot().setOnClickListener(v -> {
                    User user = new User();
                    user.id = chatMessage.conversionId;
                    user.email = chatMessage.conversionName;
                    conversionListener.onConversionClicked(user);
                });
            }

    }
}
