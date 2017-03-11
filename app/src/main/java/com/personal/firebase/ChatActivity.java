package com.personal.firebase;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements UserAdapter.OnItemClickListener, View.OnClickListener {
  private static final String TAG = "test123";
  private EditText input;
  private RecyclerView mRecyclerView;
  List<Chat> chatList = new ArrayList<>();
  private ChatAdapter mAdapter;
  User sender;
  String senderId;
  User receiver;
  String receiverId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    input = (EditText) findViewById(R.id.input);
    Button send = (Button) findViewById(R.id.send_button);
    send.setOnClickListener(this);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    sender = new Gson().fromJson(getIntent().getStringExtra("sender"), User.class);
    receiver = new Gson().fromJson(getIntent().getStringExtra("receiver"), User.class);
    toolbar.setTitle(receiver.getEmail());
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    receiverId = receiver.getUid();
    senderId = sender.getUid();
    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//    mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
//    DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
//        mLayoutManager.getOrientation());
//    mRecyclerView.addItemDecoration(mDividerItemDecoration);
    mLayoutManager.setStackFromEnd(true);

    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new ChatAdapter(this, mRecyclerView, chatList, senderId);
    mRecyclerView.setAdapter(mAdapter);
    getMessageFromFirebaseUser(senderId, receiverId);
    getAllChatsFromFirebase(senderId, receiverId);
  }

  public void getAllChatsFromFirebase(String senderId, String receiverId) {
    FirebaseDatabase.getInstance()
        .getReference()
        .child("chats_rooms")
        .child(senderId + "_" + receiverId)
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                .iterator();
            while (dataSnapshots.hasNext()) {
              DataSnapshot dataSnapshotChild = dataSnapshots.next();
              Chat chat = dataSnapshotChild.getValue(Chat.class);
              Log.d("test123", "user:" + chat);
              chatList.add(chat);
            }
            mAdapter.notifyDataSetChanged();


            // All users are retrieved except the one who is currently logged
            // in device.
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            // Unable to retrieve the users.
          }
        });

    FirebaseDatabase.getInstance()
        .getReference()
        .child("chats_rooms")
        .child(receiverId + "_" + senderId)
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren()
                .iterator();
            while (dataSnapshots.hasNext()) {
              DataSnapshot dataSnapshotChild = dataSnapshots.next();
              Chat chat = dataSnapshotChild.getValue(Chat.class);
//              if (!TextUtils.equals(user.uid, FirebaseAuth.getInstance().getCurrentUser().getUid())) {
              Log.d("test123", "user:" + chat);
              chatList.add(chat);
//              }
            }
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(chatList.size() - 1);

            // All users are retrieved except the one who is currently logged
            // in device.
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            // Unable to retrieve the users.
          }
        });
  }

  @Override
  public void onItemClick(View view, int position) {

  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  public void getMessageFromFirebaseUser(String senderUid, String receiverUid) {
    final String room_type_1 = senderUid + "_" + receiverUid;
    final String room_type_2 = receiverUid + "_" + senderUid;

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
        .getReference();

    databaseReference.child("chats_rooms")
        .getRef()
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(room_type_1)) {
              Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_1 + " exists");
              FirebaseDatabase.getInstance()
                  .getReference()
                  .child("")
                  .child(room_type_1)
                  .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                      // Chat message is retreived.
                      Chat chat = dataSnapshot.getValue(Chat.class);
                      Log.e(TAG, "chat: " + room_type_1 + " exists" + chat);
                      chatList.add(chat);
                      mAdapter.notifyDataSetChanged();
                      mRecyclerView.scrollToPosition(chatList.size()-1);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                      // Unable to get message.
                    }
                  });
            } else if (dataSnapshot.hasChild(room_type_2)) {
              Log.d(TAG, "getMessageFromFirebaseUser: " + room_type_2 + " exists");
              FirebaseDatabase.getInstance()
                  .getReference()
                  .child("chats_rooms")
                  .child(room_type_2)
                  .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                      // Chat message is retreived.
                      Chat chat = dataSnapshot.getValue(Chat.class);
                      Log.e(TAG, "chat: " + room_type_2 + " exists" + chat);
                      chatList.add(chat);
                      mAdapter.notifyDataSetChanged();
                      mRecyclerView.scrollToPosition(chatList.size()-1);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                      // Unable to get message.
                    }
                  });
            } else {
              Log.e(TAG, "getMessageFromFirebaseUser: no such room available");
            }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            // Unable to get message
          }
        });
  }

  @Override
  public void onClick(View v) {
    String message = input.getText().toString();
    if (TextUtils.isEmpty(message))
      return;
    Chat chat = new Chat();
    chat.setMessage(message);
    chat.setReceiverUid(receiverId);
    chat.setSenderUid(senderId);
    chat.setSender(sender.getEmail());
    chat.setReceiver(receiver.getEmail());
    chat.setTimestamp(new Date().getTime());
    sendMessageToFirebaseUser(getApplicationContext(), chat, "");
    input.setText("");
  }

  public void sendMessageToFirebaseUser(final Context context,
                                        final Chat chat,
                                        final String receiverFirebaseToken) {
    final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;
    final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
        .getReference();

    databaseReference.child("chats_rooms")
        .getRef()
        .addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(room_type_1)) {
              Log.d("test123", "sendMessageToFirebaseUser: " + room_type_1 + " exists");
              databaseReference.child("chats_rooms")
                  .child(room_type_1)
                  .child(String.valueOf(chat.timestamp))
                  .setValue(chat);
            } else if (dataSnapshot.hasChild(room_type_2)) {
              Log.d("test123", "sendMessageToFirebaseUser: " + room_type_2 + " exists");
              databaseReference.child("chats_rooms")
                  .child(room_type_2)
                  .child(String.valueOf(chat.timestamp))
                  .setValue(chat);

            } else {
              Log.d("test123", "sendMessageToFirebaseUser: success");
              databaseReference.child("chats_rooms")
                  .child(room_type_1)
                  .child(String.valueOf(chat.timestamp))
                  .setValue(chat);
            }
            chatList.add(chat);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(chatList.size() - 1);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            // Unable to send message.
          }
        });
  }
}
