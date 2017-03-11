package com.personal.firebase;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by ehc on 16/2/16.
 */
public class ChatAdapter extends RecyclerView.Adapter {
  private final String senderId;
  private Context context;
  private boolean loading;
  private List<Chat> chatList = new ArrayList<>();

  public ChatAdapter(Context applicationContext, RecyclerView recyclerView, List<Chat> chatList, String senderId) {
    context = applicationContext;
    this.chatList = chatList;
    this.senderId = senderId;

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
      Chat chat = chatList.get(position);
      ChatViewHolder holder = (ChatViewHolder) viewHolder;
//      holder.senderName.setText(chat.getSender());
      holder.message.setText(chat.getMessage());
      holder.timeSatmp.setText(convertTime(chat.getTimestamp()));
//      LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.root.getLayoutParams();
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
      if (senderId.equalsIgnoreCase(chat.getSenderUid())) {
        params.gravity = Gravity.END;
      } else
        params.gravity = Gravity.START;

      holder.root.setLayoutParams(params);
    }
  }

  @Override
  public int getItemCount() {
    return chatList.size();
  }


  public static class ChatViewHolder extends RecyclerView.ViewHolder {
    private final TextView  message, timeSatmp;
    private final CardView root;

    ChatViewHolder(View v, Context context) {
      super(v);
      root = (CardView) v.findViewById(R.id.card_view);
//      senderName = (TextView) v.findViewById(R.id.sender_name);
      message = (TextView) v.findViewById(R.id.message);
      timeSatmp = (TextView) v.findViewById(R.id.timestamp);

    }


  }

  public String convertTime(long time) {
    Date date = new Date(time);
    Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
    return format.format(date);
  }
}

