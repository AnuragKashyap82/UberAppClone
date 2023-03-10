package kashyap.anurag.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.uberclone.databinding.ActivityCustomerLoginBinding;
import kashyap.anurag.uberclone.databinding.ActivityDriverLoginBinding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerLoginActivity extends AppCompatActivity {

    private ActivityCustomerLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerLoginActivity.this, CustomerRegisterActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        email = binding.emailEt.getText().toString();
        password = binding.passwordEt.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email!!!", Toast.LENGTH_SHORT).show();
        }else if (password.length() < 8){
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
        }else {
            loginCustomerWithEmailAndPassword();
        }
    }

    private void loginCustomerWithEmailAndPassword() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            binding.progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(CustomerLoginActivity.this, CustomerMapActivity.class));
                            finishAffinity();
                        }else {
                            Toast.makeText(CustomerLoginActivity.this, "Login Failed:", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(CustomerLoginActivity.this, "failed:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}