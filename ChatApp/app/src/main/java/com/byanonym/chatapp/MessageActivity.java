package com.byanonym.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtMessageInput;
    private TextView txtChattingWith;
    private ProgressBar progressBar;
    private ImageView imgToolBar,imgSend;

    private  MessageAdapter messageAdapter;
    private ArrayList<Message> messages;


    String usernameOfTheRoommate, emailOfRoomate , chatRoomId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        usernameOfTheRoommate = getIntent().getStringExtra("username_of_roommate");
        emailOfRoomate = getIntent().getStringExtra("email_of_roommate");



        recyclerView= findViewById(R.id.recyclerMessages);
        imgSend= findViewById(R.id.imgSendMessage);
        edtMessageInput= findViewById(R.id.edtText);
        txtChattingWith = findViewById(R.id.txtChattingWith);
        progressBar = findViewById(R.id.progressMessage);
        imgToolBar = findViewById(R.id.img_toolbar);

        messageAdapter = new MessageAdapter(messages,getIntent().getStringExtra("my_img"),getIntent().getStringExtra("img_of_roommate"),MessageActivity.this);
        txtChattingWith.setText(usernameOfTheRoommate);
        messages = new ArrayList<>();

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference("message/"+chatRoomId).push().setValue(new Message(FirebaseAuth.getInstance().getCurrentUser().getEmail(),emailOfRoomate,edtMessageInput.getText().toString()));
                edtMessageInput.setText("");
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        Glide.with(MessageActivity.this).load(getIntent().getStringExtra("img_of_roommate")).placeholder(R.drawable.account_img).error(R.drawable.account_img).into(imgToolBar);

        setUpChatRoom();
    }

    private void setUpChatRoom(){
        FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String myUserName = snapshot.getValue(User.class).getUsername();
                if (usernameOfTheRoommate.compareTo(myUserName) > 0) {
                    chatRoomId = myUserName + usernameOfTheRoommate;
                } else if (usernameOfTheRoommate.compareTo(myUserName) == 0) {
                    chatRoomId = myUserName + usernameOfTheRoommate;
                }else {
                    chatRoomId = usernameOfTheRoommate + myUserName;
                }
                attachMessegeListener(chatRoomId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void attachMessegeListener(String chatRoomId){
        FirebaseDatabase.getInstance().getReference("messages/" + chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    messages.add(dataSnapshot.getValue(Message.class));
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size()-1);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}