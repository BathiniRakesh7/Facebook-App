package com.example.facebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private ImageButton searchBtn;
    private RecyclerView searchList;
    private EditText searchInputText;
    private CollectionReference allUsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        allUsersRef = db.collection("Users");

        mToolBar = findViewById(R.id.find_friends_layout_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchBtn = findViewById(R.id.search_friends_button);
        searchInputText = findViewById(R.id.search_box_input);

        searchList = findViewById(R.id.search_result_list);
        searchList.setHasFixedSize(true);
        searchList.setLayoutManager(new LinearLayoutManager(this));

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchInput = searchInputText.getText().toString();
                searchPeopleAndFriends(searchInput);
            }
        });
    }

    private void searchPeopleAndFriends(String searchInput) {
        Toast.makeText(this, "searching...", Toast.LENGTH_SHORT).show();
        Query searchPeopleAndFriendsQuery = allUsersRef
                .orderBy("FullName")
                .startAt(searchInput)
                .endAt(searchInput + "\uf8ff");
        FirestoreRecyclerOptions<FindFriends> options = new FirestoreRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleAndFriendsQuery, FindFriends.class)
                .build();

        FirestoreRecyclerAdapter<FindFriends, FindFriendsViewHolder> adapter =
                new FirestoreRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {
                        holder.setFullName(model.getFullName());
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
                        String usersId = snapshot.getId();
                        allUsersRef.document(usersId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String profileImage = snapshot.getString("profileImage");
                                    holder.setProfileImage(profileImage);
                                } else {
                                    Toast.makeText(FindFriendsActivity.this, "Profile image does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int adapterPosition = holder.getAdapterPosition();
                                if (adapterPosition != RecyclerView.NO_POSITION) {
                                    String visitUserId = getSnapshots().getSnapshot(adapterPosition).getId();

                                    Intent personProfileActivity = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                                    personProfileActivity.putExtra("visitUserid", visitUserId);
                                    startActivity(personProfileActivity);
                                }
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.all_users_display_layout, parent, false);
                        return new FindFriendsViewHolder(view);
                    }
                };
        searchList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FindFriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String FullName) {
            TextView userName = mView.findViewById(R.id.all_users_profile_name);
            userName.setText(FullName);
        }

        public void setProfileImage(String profileImage) {
            ImageView profileImageView = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileImage).into(profileImageView);
        }
    }
}