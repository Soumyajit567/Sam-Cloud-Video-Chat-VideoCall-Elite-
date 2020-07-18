package com.example.videocallelite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Registration extends AppCompatActivity
{
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button ContinueAndNextBtn;
    private String checker = "", phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallbacks;
    private FirebaseAuth mAuth;
    private String mVertificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        ContinueAndNextBtn = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        mAuth= FirebaseAuth.getInstance();
        loadingbar = new ProgressDialog(this);

        ccp= (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        ContinueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContinueAndNextBtn.getText().equals("Submit") || checker.equals("Code Sent"))
                {
                 String verificationCode = codeText.getText().toString();

                 if(verificationCode.equals(""))
                 {
                     Toast.makeText(Registration.this,"Please write verification code first",Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                     loadingbar.setTitle("Code Verification");
                     loadingbar.setMessage("Please wait till your code is verified");
                     loadingbar.setCanceledOnTouchOutside(false);
                     loadingbar.show();

                     PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVertificationID,verificationCode);
                     signInWithPhoneAuthCredential(credential);
                 }
                }
                else
                {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if(!phoneNumber.equals(""))
                    {
                        loadingbar.setTitle("Phone Number Verification");
                        loadingbar.setMessage("Please wait till your phone number is verified");
                        loadingbar.setCanceledOnTouchOutside(false);
                        loadingbar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60,TimeUnit.SECONDS, Registration.this,mcallbacks);
                    }
                    else
                    {
                        Toast.makeText(Registration.this,"Please enter valid phone number...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mcallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(Registration.this,"Phone Number is in wrong format",Toast.LENGTH_SHORT).show();
            loadingbar.dismiss();
            relativeLayout.setVisibility(View.VISIBLE);
                ContinueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVertificationID = s;
                mResendToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = "Code has been sent";
                ContinueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingbar.dismiss();
                Toast.makeText(Registration.this,"Code has been sent,please check..",Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null)
        {
            Intent homeintent = new Intent(Registration.this, ContactsActivity.class);
            startActivity(homeintent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            loadingbar.dismiss();
                            Toast.makeText(Registration.this, "You are logged in", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            String e = task.getException().toString();
                            Toast.makeText(Registration.this, "Error" + e, Toast.LENGTH_SHORT).show();
                            // The verification code entered was invalid
                        }
                    }
                });
    }
    private void sendUserToMainActivity(){
        Intent intent = new Intent(Registration.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}
