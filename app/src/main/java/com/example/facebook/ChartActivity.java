package com.example.facebook;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChartActivity extends AppCompatActivity {
    private Toolbar chartToolBar;
    private ImageButton sendMessageBtn, sendImageBtn;
    private EditText userMessageInput;
    private RecyclerView usersMessageList;
    private String messageReceiverId, messageReceiverName;

    private TextView receiverName;
    private CircleImageView receiverProfileImage;
    private FirebaseFirestore rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        rootRef = FirebaseFirestore.getInstance();

        messageReceiverId = getIntent().getExtras().get("visitUserid").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        chartToolBar = findViewById(R.id.chart_bar_layout);
        setSupportActionBar(chartToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chart_custom_bar, null);
        actionBar.setCustomView(action_bar_view);


        receiverName = findViewById(R.id.custom_profile_name);
        receiverProfileImage = findViewById(R.id.custom_profile_image);
        sendMessageBtn = findViewById(R.id.send_message_button);
        sendImageBtn = findViewById(R.id.send_image_button);
        userMessageInput = findViewById(R.id.input_message);
        usersMessageList = findViewById(R.id.users_messages_list);


        displayReceiverInfo();
    }

    private void displayReceiverInfo() {
        receiverName.setText(messageReceiverName);
//        rootRef.collection("Users").document(messageReceiverId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot snapshot) {
//                if(snapshot.exists()){
////                    final String  profileImage = snapshot.getString("profileImage");
////                    Picasso.with(ChartActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
//                }
//
//            }
//        });
    }
}