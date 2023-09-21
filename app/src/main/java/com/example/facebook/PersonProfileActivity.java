package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView personProfileName, personPhoneNumber, personStatus;
    private CircleImageView personProfileImage;
    private Button sendFriendRequestBtn, declineFriendRequestBtn;

    private CollectionReference usersRef;

    private FirebaseAuth mAuth;


    private String senderUserid, receiverUserid, CURRENT_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        senderUserid = mAuth.getCurrentUser().getUid();
        receiverUserid = getIntent().getExtras().get("visitUserid").toString();
        usersRef = FirebaseFirestore.getInstance().collection("Users");

        setContentView(R.layout.activity_person_profile);
        personProfileName = findViewById(R.id.person_profile_full_name);
        personPhoneNumber = findViewById(R.id.person_profile_mobile);
        personStatus = findViewById(R.id.person_profile_status);
        personProfileImage = findViewById(R.id.person_profile_image);
        CURRENT_STATE = "not_friends";

        sendFriendRequestBtn = findViewById(R.id.person_send_friend_request);
        declineFriendRequestBtn = findViewById(R.id.decline_friend_request);


        usersRef.document(receiverUserid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("FullName");
                    String phone = documentSnapshot.getString("phone");
                    personProfileName.setText(userName);
                    personPhoneNumber.setText(phone);

                    maintenanceOfButtons();
                } else {
                    Toast.makeText(PersonProfileActivity.this, "Data not Retrieved", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PersonProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
        declineFriendRequestBtn.setEnabled(false);

        if (!senderUserid.equals(receiverUserid)) {

            sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendFriendRequestBtn.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        sendRequestToPerson();
                    }
                }
            });

        } else {
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
            sendFriendRequestBtn.setVisibility(View.INVISIBLE);


        }
    }

    private void maintenanceOfButtons() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference senderRequestRef = db.collection("Friend Requests")
                .document(senderUserid)
                .collection("sentRequests")
                .document(receiverUserid);
        senderRequestRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (snapshot != null && snapshot.exists()) {
                    String request_type = snapshot.getString("request_type");
                    if ("sent".equals(request_type)) {
                        CURRENT_STATE = "request_sent";
                        sendFriendRequestBtn.setText("Cancel Friend Request");
                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                        declineFriendRequestBtn.setEnabled(false);
                    }
                } else {
                    CURRENT_STATE = "not_friends";
                    sendFriendRequestBtn.setText("Send Friend Request");
                    declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                    declineFriendRequestBtn.setEnabled(false);
                }
            }
        });
    }

    private void sendRequestToPerson() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference senderRequestRef = db.collection("Friend Requests")
                .document(senderUserid).collection("sentRequests").document(receiverUserid);
        DocumentReference receiverRequestRef = db.collection("Friend Requests")
                .document(receiverUserid).collection("receivedRequests").document(senderUserid);
        senderRequestRef
                .set(new HashMap<String, Object>() {{
                    put("request_type", "sent");
                }})
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Set the request_type field for the receiver
                            receiverRequestRef
                                    .set(new HashMap<String, Object>() {{
                                        put("request_type", "received");
                                    }})
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendFriendRequestBtn.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendFriendRequestBtn.setText("Cancel Friend Request");
                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}