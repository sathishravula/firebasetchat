package com.personal.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserActivity extends BaseActivity implements UserAdapter.OnItemClickListener {

  private RecyclerView mRecyclerView;
  List<User> users = new ArrayList<>();
  private UserAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("Users");
    setSupportActionBar(toolbar);
    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
    DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
        mLayoutManager.getOrientation());
    mRecyclerView.addItemDecoration(mDividerItemDecoration);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new UserAdapter(this, this, mRecyclerView, users);
    mRecyclerView.setAdapter(mAdapter);
    getAllUsersFromFirebase();
    showProgressDialog();
  }

  public void getAllUsersFromFirebase() {
    FirebaseDatabase.getInstance()
        .getReference()
        .child("users")
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                .iterator();
            while (dataSnapshots.hasNext()) {
              DataSnapshot dataSnapshotChild = dataSnapshots.next();
              User user = dataSnapshotChild.getValue(User.class);
              if (!TextUtils.equals(user.uid, FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                Log.d("test123", "user:" + user);
                users.add(user);
              }
            }
            mAdapter.notifyDataSetChanged();
            hideProgressDialog();

            // All users are retrieved except the one who is currently logged
            // in device.
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            // Unable to retrieve the users.
            hideProgressDialog();
          }
        });
  }

  @Override
  public void onItemClick(View view, int position) {
    Intent intent = new Intent(this, ChatActivity.class);
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    User user = new User();
    user.setEmail(firebaseUser.getEmail());
    user.setUid(firebaseUser.getUid());
    intent.putExtra("sender", new Gson().toJson(user));
    intent.putExtra("receiver", new Gson().toJson(users.get(position)));
    startActivity(intent);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_sing_out) {
      FirebaseAuth mAuth = FirebaseAuth.getInstance();
      mAuth.signOut();
      Intent intent = new Intent(this, AuthenticationActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
