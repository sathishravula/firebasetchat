package com.personal.firebase;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
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
  private int REQUEST_Gallery = 1;
  private StorageReference storageRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    input = (EditText) findViewById(R.id.input);
    Button send = (Button) findViewById(R.id.send_button);
    FirebaseStorage storage = FirebaseStorage.getInstance();
    storageRef = storage.getReference();
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
    setUpRecycleView();
    getAllChatsFromFirebase(senderId, receiverId);
    getMessageFromFirebaseUser(senderId, receiverId);
  }

  private void setUpRecycleView() {
    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
    mLayoutManager.setStackFromEnd(true);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new ChatAdapter(this, mRecyclerView, chatList, senderId);
    mRecyclerView.setAdapter(mAdapter);
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
              chatList.add(chat);
            }
            mAdapter.notifyDataSetChanged();



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
              chatList.add(chat);
            }
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(chatList.size() - 1);
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
              Log.d(TAG, "getMessageFromFirebaseUser 1: " + room_type_1 + " exists");
              FirebaseDatabase.getInstance()
                  .getReference()
                  .child("")
                  .child(room_type_1).
                  limitToLast(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                  // Chat message is retreived.
                  Chat chat = dataSnapshot.getValue(Chat.class);
                  Log.e(TAG, "chat: " + room_type_1 + " exists" + chat);
                  chatList.add(chat);
                  mAdapter.notifyDataSetChanged();
                  mRecyclerView.scrollToPosition(chatList.size() - 1);
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
              Log.d(TAG, "getMessageFromFirebaseUser 2: " + room_type_2 + " exists");
              FirebaseDatabase.getInstance()
                  .getReference()
                  .child("chats_rooms")
                  .child(room_type_2).limitToLast(1).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                  // Chat message is retreived.
                  Chat chat = dataSnapshot.getValue(Chat.class);
                  Log.d(TAG, "chat: " + room_type_2 + " exists" + chat);
                  chatList.add(chat);
                  mAdapter.notifyDataSetChanged();
                  mRecyclerView.scrollToPosition(chatList.size() - 1);
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
              Log.d(TAG, "getMessageFromFirebaseUser: no such room available");
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
    if (v.getId() == R.id.send_button) {
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
      sendMessageToFirebaseUser(getApplicationContext(), chat);
      input.setText("");
    }
  }

  public void sendMessageToFirebaseUser(final Context context,
                                        final Chat chat) {
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


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_chat, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_attach) {
      openGallery();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void openGallery() {
    Intent intent = new Intent(Intent.ACTION_PICK,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_Gallery);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == REQUEST_Gallery) {
      Uri selectedImageUri = data.getData();
      storeInFile(selectedImageUri);
    }
  }

  private void storeInFile(Uri selectedImageUri) {
    String selectedImagePath = getImagePath(selectedImageUri);
    Uri file = Uri.fromFile(new File(selectedImagePath));
    StorageReference riversRef = storageRef.child("images/" + new Date().getTime() + file.getLastPathSegment());
    UploadTask uploadTask = riversRef.putFile(selectedImageUri);
// Register observers to listen for when the download is done or if it fails
    uploadTask.addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception exception) {
        // Handle unsuccessful uploads
        Log.d("test123", "onFailure :" + exception.getMessage());

      }
    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
        Uri downloadUrl = taskSnapshot.getDownloadUrl();
        Log.d("test123", "onSuccess path:" + downloadUrl);
        Chat chat = new Chat();
        chat.setMessage("");
        chat.setReceiverUid(receiverId);
        chat.setSenderUid(senderId);
        chat.setPath(downloadUrl.getPath());
        chat.setSender(sender.getEmail());
        chat.setReceiver(receiver.getEmail());
        chat.setTimestamp(new Date().getTime());
        sendMessageToFirebaseUser(getApplicationContext(), chat);
      }
    });
  }

  private String getImagePath(Uri selectedImageUri) {
    String[] projection = {MediaStore.Images.Media.DATA};
    CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
    Cursor cursor = cursorLoader.loadInBackground();
    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index_data);
  }


}
