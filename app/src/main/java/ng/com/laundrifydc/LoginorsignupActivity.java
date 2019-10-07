package ng.com.laundrifydc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class LoginorsignupActivity extends AppCompatActivity {

    ImageView imageView2, imageView3, imageView1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginorsignup);

        imageView2 = findViewById(R.id.imageView2);
        imageView3 = findViewById(R.id.imageView3);
        imageView1 = findViewById(R.id.imageView1);
        Glide.with(this)
                .load(R.drawable.whitelogo)
                .into(imageView3);
        Glide.with(this)
                .load(R.drawable.text)
                .into(imageView1);

    }

    public void login (View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    public void signup (View view) {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
    }
}
