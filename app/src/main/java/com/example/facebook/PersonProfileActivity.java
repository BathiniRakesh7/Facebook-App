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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.HashMap;


import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView personProfileName, personPhoneNumber, personStatus;
    private CircleImageView personProfileImage;
    private Button sendFriendRequestBtn, declineFriendRequestBtn;

    private CollectionReference usersCollection, friendsCollection,friendRef;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private DocumentReference senderRequestRef, receiverRequestRef;


    private String senderUserid, receiverUserid, CURRENT_STATE, saveCurrentDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        senderUserid = mAuth.getCurrentUser().getUid();
        receiverUserid = getIntent().getExtras().get("visitUserid").toString();
        usersCollection = FirebaseFirestore.getInstance().collection("Users");
        friendRef = db.collection("Friends");

        setContentView(R.layout.activity_person_profile);
        personProfileName = findViewById(R.id.person_profile_full_name);
        personPhoneNumber = findViewById(R.id.person_profile_mobile);
        personStatus = findViewById(R.id.person_profile_status);
        personProfileImage = findViewById(R.id.person_profile_image);
        CURRENT_STATE = "not_friends";

        sendFriendRequestBtn = findViewById(R.id.person_send_friend_request);
        declineFriendRequestBtn = findViewById(R.id.decline_friend_request);


        usersCollection.document(receiverUserid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                    if (CURRENT_STATE.equals("request_sent")) {
                        cancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("received")) {
                        acceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        unFriendThePerson();
                    }
                }
            });

        } else {
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
            sendFriendRequestBtn.setVisibility(View.INVISIBLE);


        }
    }

    private void unFriendThePerson() {
        DocumentReference senderRef = db.collection("Friends")
                .document(senderUserid).collection("acceptRequests")
                .document(receiverUserid);


        senderRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DocumentReference receiverRef = db.collection("Friends")
                            .document(receiverUserid).collection("acceptRequests")
                            .document(senderUserid);
                    receiverRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                CURRENT_STATE = "not_friends";
                                sendFriendRequestBtn.setText("Send Friend Request");
                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            } else {
                                Toast.makeText(PersonProfileActivity.this, "Request not Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        saveCurrentDateTime = currentDate.format(calFordDate.getTime());
        friendRef
                .document(senderUserid)
                .collection("acceptRequests")
                .document(receiverUserid)
                .set(new HashMap<String, Object>() {{
                    put("dateTime", saveCurrentDateTime);
                }})
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRef
                                    .document(receiverUserid)
                                    .collection("acceptRequests")
                                    .document(senderUserid)
                                    .set(new HashMap<String, Object>() {{
                                        put("dateTime", saveCurrentDateTime);
                                    }})
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                senderRequestRef = db.collection("Friend Requests")
                                                        .document(senderUserid).collection("requests")
                                                        .document(receiverUserid);

                                                senderRequestRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            receiverRequestRef = db.collection("Friend Requests")
                                                                    .document(receiverUserid).collection("requests")
                                                                    .document(senderUserid);
                                                            receiverRequestRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        CURRENT_STATE = "friends";
                                                                        sendFriendRequestBtn.setText("Unfriend this Person");
                                                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                        declineFriendRequestBtn.setEnabled(false);
                                                                    } else {
                                                                        Toast.makeText(PersonProfileActivity.this, "Request not Cancelled", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void cancelFriendRequest() {
        senderRequestRef = db.collection("Friend Requests")
                .document(senderUserid).collection("requests")
                .document(receiverUserid);

        senderRequestRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    receiverRequestRef = db.collection("Friend Requests")
                            .document(receiverUserid).collection("requests")
                            .document(senderUserid);
                    receiverRequestRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                CURRENT_STATE = "not_friends";
                                sendFriendRequestBtn.setText("Send Friend Request");
                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            } else {
                                Toast.makeText(PersonProfileActivity.this, "Request not Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }


    private void maintenanceOfButtons() {
        senderRequestRef = db.collection("Friend Requests")
                .document(senderUserid).collection("requests")
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
                    } else {
                        receiverRequestRef = db.collection("Friend Requests")
                                .document(receiverUserid).collection("requests")
                                .document(senderUserid);

                        senderRequestRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                if (snapshot != null && snapshot.exists()) {
                                    String request_type = snapshot.getString("request_type");
                                    if ("received".equals(request_type)) {
                                        CURRENT_STATE = "received";
                                        sendFriendRequestBtn.setText("Accept Friend Request");
                                        declineFriendRequestBtn.setVisibility(View.VISIBLE);
                                        declineFriendRequestBtn.setEnabled(true);
                                        declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                cancelFriendRequest();
                                            }
                                        });
                                    }
                                    else {
                                        CURRENT_STATE = "not_friends";
                                        sendFriendRequestBtn.setText("Send Friend Request");
                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                        declineFriendRequestBtn.setEnabled(false);
                                    }
                                }
                            }
                        });


                    }
                }
                else {
                    DocumentReference senderRef = friendRef.document(senderUserid).collection("acceptRequests").document(receiverUserid);
                    senderRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if (snapshot != null && snapshot.exists()) {
                                CURRENT_STATE = "friends";
                                sendFriendRequestBtn.setText("Unfriend this Person");
                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                declineFriendRequestBtn.setEnabled(false);
                            }

                        }
                    });
                }
            }
        });
    }

    private void sendRequestToPerson() {
        CollectionReference friendRequestsRef = db.collection("Friend Requests");
        friendRequestsRef
                .document(senderUserid)
                .collection("requests")
                .document(receiverUserid)
                .set(new HashMap<String, Object>() {{
                    put("request_type", "sent");
                }})
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRequestsRef
                                    .document(receiverUserid)
                                    .collection("requests")
                                    .document(senderUserid)
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