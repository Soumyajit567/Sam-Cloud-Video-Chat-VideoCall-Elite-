package com.example.videocallelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView myContactList;
    ImageView findPeopleBtn;
    private DatabaseReference contactsRef,userRef;
    private FirebaseAuth mAuth;
    private   String currentUserId;
    private String userName="",profileImage="",calledBy="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        findPeopleBtn = findViewById(R.id.find_people_btn);
        myContactList = findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findpeopleintent = new Intent(ContactsActivity.this,FindPeopleActivity.class);
                startActivity(findpeopleintent);
            }
        });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            switch(menuItem.getItemId())
            {
                case R.id.navigation_home:
                    Intent mainintent = new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(mainintent);
                    break;
                case R.id.navigation_settings:
                    Intent settingsintent = new Intent(ContactsActivity.this,SettingsActivity.class);
                    startActivity(settingsintent);
                    break;
                case R.id.navigation_notifications:
                    Intent  notificationsintent = new Intent(ContactsActivity.this,NotificationsActivity.class);
                    startActivity(notificationsintent);
                    break;
                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutintent = new Intent(ContactsActivity.this,Registration.class);
                    startActivity(logoutintent);
                    finish();
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();

        validateUser();



        FirebaseRecyclerOptions<Contacts> options
                = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsRef.child(currentUserId),Contacts.class).build();

        final FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int i, @NonNull Contacts contacts) {
                final String  listUserId = getRef(i).getKey();

                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            userName = dataSnapshot.child("name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            contactsViewHolder.userNameTxt.setText(userName);
                            Picasso.get().load(profileImage).into(contactsViewHolder.contactImageView);


                        }
                        contactsViewHolder.callBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callingIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                                callingIntent.putExtra("visit_user_id",listUserId);
                                startActivity(callingIntent);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsActivity.ContactsViewHolder viewHolder = new ContactsActivity.ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }




    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button callBtn;
        ImageView contactImageView;


        public ContactsViewHolder(@NonNull View itemView)
        {

            super(itemView);

            userNameTxt = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            contactImageView = itemView.findViewById(R.id.image_contact);

        }
    }
    private void validateUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    Intent settingsIntent = new Intent(ContactsActivity.this,SettingsActivity.class);
                    startActivity(settingsIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void checkForReceivingCall() {
        userRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("ringing")) {
                    calledBy = dataSnapshot.child("ringing").getValue().toString();

                    Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                    callingIntent.putExtra("visit_user_id", calledBy);
                    startActivity(callingIntent);

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
