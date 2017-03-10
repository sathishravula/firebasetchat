package com.personal.firebase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ehc on 16/2/16.
 */
public class ChatAdapter extends RecyclerView.Adapter {
  private Context context;
  private boolean loading;
  private List<Chat> chatList = new ArrayList<>();

  public ChatAdapter(Context applicationContext, RecyclerView recyclerView, List<Chat> chatList) {
    context = applicationContext;
    this.chatList = chatList;

  }

  public void setLoaded() {
    loading = false;
  }

  public boolean getLoaded() {
    return loading;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
    RecyclerView.ViewHolder vh;

    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.message_list_item, parent, false);
    vh = new ChatViewHolder(view, context);

    return vh;
  }


  private Chat getItem(int position) {
    return chatList.get(position);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    if (viewHolder instanceof ChatViewHolder) {
      Chat user = chatList.get(position);
      ChatViewHolder holder = (ChatViewHolder) viewHolder;
      holder.senderName.setText(user.getSender());
      holder.message.setText(user.getMessage());
      holder.timeSatmp.setText("" + user.getTimestamp());
    }
  }

  @Override
  public int getItemCount() {
    return chatList.size();
  }




  public static class ChatViewHolder extends RecyclerView.ViewHolder {
    private final TextView senderName, message, timeSatmp;

    ChatViewHolder(View v, Context context) {
      super(v);
      senderName = (TextView) v.findViewById(R.id.sender_name);
      message = (TextView) v.findViewById(R.id.message);
      timeSatmp = (TextView) v.findViewById(R.id.timestamp);

    }


  }
}

