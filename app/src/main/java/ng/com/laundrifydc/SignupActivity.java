package ng.com.laundrifydc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String TAG = "EmailPassword";
    private EditText email, password, laundryName, dcName, phoneNumber, deliveryMan;
    ImageView imageView4, imageView5;
    private DatabaseReference mCustomerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        imageView5 = findViewById(R.id.imageView5);
        Glide.with(this)
                .load(R.drawable.nakedlogo)
                .into(imageView5);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        laundryName = findViewById(R.id.laundryNameEditText);
        dcName = findViewById(R.id.dcNameEditText);
        phoneNumber = findViewById(R.id.phoneNumberEditText);
        password = findViewById(R.id.passwordEditText);
        deliveryMan = findViewById(R.id.deliveryManEditText);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), LoginorsignupActivity.class));
    }

    public void signupClicked (View view) {
        if (!validateForm()) {
            return;
        }
        Log.d(TAG, "signIn:" + email);
        showProgressDialog((long) 60000);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {

                            // If sign in fails, display a message to the user.
                            hideProgressDialog();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;
        Log.d(TAG, "validating...:");

        String laundryNameT = laundryName.getText().toString();
        if (!TextUtils.isEmpty(laundryNameT)) {
        } else {
            valid = false;
            laundryName.setError("Required");
        }

        String dcNameT= dcName.getText().toString();
        if (!TextUtils.isEmpty(dcNameT)) {
        } else {
            valid = false;
            dcName.setError("Required");
        }

        String deliveryManT= deliveryMan.getText().toString();
        if (!TextUtils.isEmpty(deliveryManT)) {
        } else {
            valid = false;
            deliveryMan.setError("Required");
        }

        String emailT = email.getText().toString();
        if (!TextUtils.isEmpty(emailT)) {
            if (!Patterns.EMAIL_ADDRESS.matcher(emailT).matches()) {
                valid = false;
                email.setError("Please enter a valid email address");
            } else {
                email.setError(null);
            }
        } else {
            valid = false;
            email.setError("Required");
        }

        String phoneNumberT = phoneNumber.getText().toString();
        if (!TextUtils.isEmpty(emailT)) {
            if (!Patterns.PHONE.matcher(phoneNumberT).matches()) {
                valid = false;
                phoneNumber.setError("Please enter a valid phone no.");
            } else {
                phoneNumber.setError(null);
            }
        } else {
            valid = false;
            phoneNumber.setError("Required");
        }

        String passwordT = password.getText().toString();
        if (!TextUtils.isEmpty(passwordT)) {
            if (!passwordT.matches("[a-zA-Z0-9]+")) {
                valid = false;
                password.setError("Please note that password can only contain alphabets and letters");
            } else {
                password.setError(null);
            }
        } else if (passwordT.length() < 8){
            valid = false;
            password.setError("You must have a minimum of 8 characters in your password");
        }
        return valid;
    }

    private void updateUI(final FirebaseUser user) {

        if (user != null) {
            //Update User info
            mCustomerDB = FirebaseDatabase.getInstance().getReference().child("Users").child("DryCleaners").child(user.getUid());

            Map userInfo = new HashMap();
            userInfo.put("CompanyName", laundryName.getText().toString());
            userInfo.put("DryCleanerName", dcName.getText().toString());
            userInfo.put("DeliveryManName", deliveryMan.getText().toString());
            userInfo.put("PhoneNumber", phoneNumber.getText().toString());
            userInfo.put("EmailAddress", email.getText().toString());
            userInfo.put("Password", password.getText().toString());

            mCustomerDB.child("Info").setValue(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    // OneSignal Initialization
                    OneSignal.startInit(SignupActivity.this)
                            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                            .unsubscribeWhenNotificationsAreDisabled(true)
                            .init();
                    OneSignal.setEmail(user.getEmail());
                    OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                        @Override
                        public void idsAvailable(String userId, String registrationId) {
                            mCustomerDB.child("Info").child("NotificationKey").setValue(userId);
                            Log.i("test", "Id Available");
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    });
                }
            });
        }
    }



    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog(Long time) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.DialogStyle);
            mProgressDialog.setMessage("Signing  up...");
            mProgressDialog.setTitle("Please wait...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(SignupActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void changeToLogin (View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}
