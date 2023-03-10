package kashyap.anurag.uberclone;

import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.uberclone.databinding.ActivitySplashBinding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               checkUser();
            }
        }, 3000);
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finishAffinity();
        }else {
            Intent intent = new Intent(SplashActivity.this, CustomerMapActivity.class);
            startActivity(intent);
            finishAffinity();
        }
    }
}