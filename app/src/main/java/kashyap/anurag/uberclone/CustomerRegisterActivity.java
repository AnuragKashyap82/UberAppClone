package kashyap.anurag.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.uberclone.databinding.ActivityCustomerRegisterAcitvityBinding;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CustomerRegisterActivity extends AppCompatActivity {

    private ActivityCustomerRegisterAcitvityBinding binding;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    private DatabaseReference databaseReference;
    private String onlineCustomerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerRegisterAcitvityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        onlineCustomerId = firebaseAuth.getCurrentUser().getUid();

        binding.alreadyAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CustomerRegisterActivity.this, CustomerLoginActivity.class));
                finish();
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email!!!", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 8) {
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
        } else {
            createCustomerAccountWithEmailAndPassword();
        }
    }

    private void createCustomerAccountWithEmailAndPassword() {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveDataToDb();
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(CustomerRegisterActivity.this, "Failed:", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(CustomerRegisterActivity.this, "Failed due to:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDataToDb() {
        binding.progressBar.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Customers").child(onlineCustomerId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userType", "customer");
        hashMap.put("email", email);
        databaseReference.setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            binding.progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(CustomerRegisterActivity.this, CustomerMapActivity.class));
                            finishAffinity();
                        }else {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}