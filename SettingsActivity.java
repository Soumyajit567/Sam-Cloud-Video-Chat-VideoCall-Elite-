package com.example.videocallelite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsActivity extends AppCompatActivity {

    private Button saveBtn;
    private EditText userNameET, userBioET;
    private ImageView profileImageView;

    private final static int GalleryPick = 1;
    private Uri ImageUri;
    private StorageReference userProfileImgRef;
    private String downloadUrl;
    private DatabaseReference userRef;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        saveBtn = findViewById(R.id.save_settings_btn);
        userNameET = findViewById(R.id.username_settings);
        userBioET = findViewById(R.id.bio_settings);
        profileImageView = findViewById(R.id.settings_profile_image);
        progressDialog = new ProgressDialog(this);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            profileImageView.setImageURI(ImageUri);
        }
    }

    private void saveUserData() {
        final String getUserName = userNameET.getText().toString();
        final String getUserstatus = userBioET.getText().toString();

        if (ImageUri == null) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("images")) {
                        saveInfoOnlyWithoutImage();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Please select image first.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (getUserName.equals("")) {
            Toast.makeText(this, "userName is mandatory", Toast.LENGTH_SHORT).show();
        } else if (getUserstatus.equals("")) {
            Toast.makeText(this, "bio is mandatory", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait....");
            progressDialog.show();

            final StorageReference filePath = userProfileImgRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            final UploadTask uploadTask = filePath.putFile(ImageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    downloadUrl = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if ((task.isSuccessful())) {
                        downloadUrl = task.getResult().toString();

                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", getUserName);
                        profileMap.put("status", getUserstatus);
                        profileMap.put("image", downloadUrl);

                        userRef.child(FirebaseAuth.getInstance().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                                    startActivity(intent);
                                    finish();

                                    Toast.makeText(SettingsActivity.this, "Profile settings has been updated.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfoOnlyWithoutImage() {
        final String getUserName = userNameET.getText().toString();
        final String getUserstatus = userBioET.toString();



        if (getUserName.equals("")) {
            Toast.makeText(this, "userName is mandatory", Toast.LENGTH_SHORT).show();
        } else if (getUserstatus.equals("")) {
            Toast.makeText(this, "bio is mandatory", Toast.LENGTH_SHORT).show();
        } else {

            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please wait....");
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name", getUserName);
            profileMap.put("status", getUserstatus);


            userRef.child(FirebaseAuth.getInstance().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();

                        Toast.makeText(SettingsActivity.this, "Profile settings has been updated.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void retrieveUserInfo()
    {
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String imageDb = dataSnapshot.child("image").getValue().toString();
                    String nameDb = dataSnapshot.child("name").getValue().toString();
                    String bioDb = dataSnapshot.child("status").getValue().toString();

                    userNameET.setText(nameDb);
                    userBioET.setText(bioDb);

                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
