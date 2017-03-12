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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import java.text.Format;
import java.text.SimpleDateFormat;
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
    private FirebaseRecyclerAdapter<Chat, MessageViewHolder> mFirebaseAdapter;
    String senderId;
    User receiver;
    String receiverId;
    private int REQUEST_Gallery = 1;
    private StorageReference storageRef;
    private FirebaseStorage storage;
    private DatabaseReference chatRoomRef;
    private LinearLayoutManager mLayoutManager;
    private String chatRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        input = (EditText) findViewById(R.id.input);
        Button send = (Button) findViewById(R.id.send_button);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        storage = FirebaseStorage.getInstance();
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
        if (sender.getCustomId() <= receiver.getCustomId()) {
            chatRoom = sender.getCustomId() + "_" + receiver.getCustomId();
        } else {
            chatRoom = receiver.getCustomId() + "_" + sender.getCustomId();
        }
        chatRoomRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("chats_rooms").child(chatRoom);
        Log.d(TAG, "chat_room" + chatRoom);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chat, MessageViewHolder>(Chat.class, R.layout.message_list_item,
                MessageViewHolder.class, chatRoomRef) {

            @Override
            protected Chat parseSnapshot(DataSnapshot snapshot) {
                Chat chat = super.parseSnapshot(snapshot);
                return chat;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder holder, Chat chat, int position) {
//      mProgressBar.setVisibility(ProgressBar.INVISIBLE);
//      holder.senderName.setText(chat.getSender());
                holder.message.setText(chat.getMessage());
                holder.timeSatmp.setText(convertTime(chat.getTimestamp()));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (TextUtils.isEmpty(chat.getPath()))
                    holder.image.setVisibility(View.GONE);
                else {
                    StorageReference httpsReference = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com" + chat.getPath());
                    holder.image.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext())
                            .using(new FirebaseImageLoader())
                            .load(httpsReference)
                            .into(holder.image);
                }

                if (senderId.equalsIgnoreCase(chat.getSenderUid())) {
                    params.gravity = Gravity.END;
                } else
                    params.gravity = Gravity.START;

                holder.root.setLayoutParams(params);
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                Log.d("test1234", "positionStart:"+positionStart+" itemCount"+itemCount);

                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView message, timeSatmp;
        private final CardView root;
        private final ImageView image;

        MessageViewHolder(View v) {
            super(v);
            root = (CardView) v.findViewById(R.id.card_view);
//      senderName = (TextView) v.findViewById(R.id.sender_name);
            message = (TextView) v.findViewById(R.id.message);
            image = (ImageView) v.findViewById(R.id.image);
            timeSatmp = (TextView) v.findViewById(R.id.timestamp);

        }
    }


    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
        return format.format(date);
    }


    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
            sendMessageToFirebaseUser(chat);
            input.setText("");
        }
    }

    public void sendMessageToFirebaseUser(
            final Chat chat) {
        chatRoomRef
                .push()
                .setValue(chat);
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
                sendMessageToFirebaseUser(chat);
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
