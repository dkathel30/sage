package com.example.sage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int USER_TYPE = 1;
    private static final int BOT_TYPE = 2;

    private final List<ChatItem> chatItems;
    private final LayoutInflater inflater;

    public ChatAdapter(Context context, List<ChatItem> chatItems) {
        this.chatItems = chatItems;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        // Assuming prompts are user messages and responses are bot messages
        // You might need a more sophisticated check depending on your setup
        return position % 2 == 0 ? USER_TYPE : BOT_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == USER_TYPE) {
            View view = inflater.inflate(R.layout.chat_item, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.chat_item, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem chatItem = chatItems.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userMessage.setText(chatItem.getPrompt());
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).botMessage.setText(chatItem.getResponse());
        }
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.right_chat_text_view);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView botMessage;

        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            botMessage = itemView.findViewById(R.id.left_chat_text_view);
        }
    }
}
