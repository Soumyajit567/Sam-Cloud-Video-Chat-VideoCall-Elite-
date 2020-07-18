package com.example.videocallelite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
{
 private String recieverUserID="",recieverUserImage="",recieverUserName="";
 private ImageView background_profile_view;
 private TextView name_profile;
 private Button AddFriend,DeclineFriendRequest;
 private FirebaseAuth mAuth;
 private  String senderUserId;
 private String currentState = "new";
 private DatabaseReference friendRequestRef,contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        recieverUserImage = getIntent().getExtras().get("profile_image").toString();
        recieverUserName = getIntent().getExtras().get("profile_name").toString();

        background_profile_view = findViewById(R.id.background_profile_view);
        name_profile = findViewById(R.id.name_profile);
        AddFriend = findViewById(R.id.add_friend);
       DeclineFriendRequest = findViewById(R.id.decline_friend);

        Picasso.get().load(recieverUserImage).into(background_profile_view);
        name_profile.setText(recieverUserName);

        manageClickEvents();
    }

    private void manageClickEvents()
    {
        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverUserID))
                {
                    final String requestType=dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();
                    if (requestType.equals("sent"))
                    {
                        currentState = "request_sent";
                        AddFriend.setText("Cancel Friend Request");
                    }
                    else  if (requestType.equals("received"))
                    {
                        currentState = "request_received";
                        AddFriend.setText("Accept Friend Request");

                        DeclineFriendRequest.setVisibility(View.VISIBLE);
                        DeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                    else
                    {
                        contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(recieverUserID))
                                {
                                    currentState = "friends";
                                    AddFriend.setText("Delete Contact");
                                }
                                else
                                {
                                    currentState = "new";
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(senderUserId.equals(recieverUserID))
        {
            AddFriend.setVisibility(View.GONE);
        }
        else
        {
            AddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new"))
                    {
                        SendFriendRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                         AcceptFriendRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                         CancelFriendRequest();
                    }
                }

            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(senderUserId).child(recieverUserID).child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful())
                      {
                          contactsRef.child(recieverUserID).child(senderUserId).child("Contact").setValue("Saved")
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if(task.isSuccessful())
                                          {
                                              friendRequestRef.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      if(task.isSuccessful())
                                                      {
                                                          friendRequestRef.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                              @Override
                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                  if(task.isSuccessful())
                                                                  {
                                                                      currentState = "friends";
                                                                      AddFriend.setText("Delete Contact");
                                                                      DeclineFriendRequest.setVisibility(View.GONE);
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

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if(task.isSuccessful())
              {
                  friendRequestRef.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          if(task.isSuccessful())
                          {
                              currentState = "new";
                            AddFriend.setText("AddFriend");
                          }
                      }
                  });
              }
            }
        });
    }

    private void SendFriendRequest()
    {
        friendRequestRef.child(senderUserId).child(recieverUserID).child("request_type")
                .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                friendRequestRef.child(recieverUserID).child(senderUserId).child("request_type")
                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful())
                    {
                      currentState = "request_sent";
                      AddFriend.setText("Cancel friend Request");
                        Toast.makeText(ProfileActivity.this,"Friend Request Sent",Toast.LENGTH_SHORT).show();
                    }
                    }
                });
            }
        });

    }
}