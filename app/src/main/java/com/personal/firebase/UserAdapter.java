package com.personal.firebase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ehc on 16/2/16.
 */
public class UserAdapter extends RecyclerView.Adapter {
  static OnItemClickListener onItemClickListener;
  private Context context;
  private boolean loading;
  private List<User> userList = new ArrayList<>();

  public UserAdapter(Context applicationContext, OnItemClickListener onItemClickListener, RecyclerView recyclerView, List<User> userList) {
    Log.d("test", "DoctorsAdapter: ");
    context = applicationContext;
    this.onItemClickListener = onItemClickListener;
    this.userList = userList;

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
        .inflate(R.layout.user_list_item, parent, false);
    vh = new UserViewHolder(view, context);

    return vh;
  }


  private User getItem(int position) {
    return userList.get(position);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
    if (viewHolder instanceof UserViewHolder) {
      User user = userList.get(position);
      UserViewHolder holder = (UserViewHolder) viewHolder;
      holder.userName.setText(user.getEmail());
    }
  }

  @Override
  public int getItemCount() {
    return userList.size();
  }

  public interface OnItemClickListener {
    void onItemClick(View view, int position);
  }


  public static class UserViewHolder extends RecyclerView.ViewHolder {
    private final TextView userName;

    UserViewHolder(View v, Context context) {
      super(v);
      userName = (TextView) v.findViewById(R.id.user_name);
      v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onItemClickListener.onItemClick(v, getAdapterPosition());
        }
      });
    }


  }
}

