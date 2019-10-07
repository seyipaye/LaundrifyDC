package ng.com.laundrifydc;

import androidx.annotation.NonNull;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String TAG = "EmailPassword";
    private EditText email, password;
    ImageView imageView7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        imageView7 = findViewById(R.id.imageView7);
        Glide.with(this)
                .load(R.drawable.nakedlogo)
                .into(imageView7);


        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), LoginorsignupActivity.class));
    }

    public void loginClicked (View view) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog((long) 10000);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            hideProgressDialog();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void forgotPassword (View view) {
        Toast.makeText(this, "Contact us", Toast.LENGTH_SHORT).show();
        contact("+2347012454239", "Hello, I can't remember my password, "
                + "can you help me get it back ?.\n My Firstname is... \n Lastname is... \n and My Email is...");
    }

    private void contact(String toNumber, String message) {
        try {
            toNumber = toNumber.replace("+", "").replace(" ", "");

            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Please make sure you have whatsapp", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

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
            password.setError("You should have a minimum of 8 characters in your password");
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

    }

    private ProgressDialog mProgressDialog;

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
                    Toast.makeText(LoginActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void changeToSignup (View view) {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
    }

}
