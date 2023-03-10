package kashyap.anurag.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.uberclone.databinding.ActivityDriverRegisterBinding;

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

public class DriverRegisterActivity extends AppCompatActivity {

    private ActivityDriverRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    private DatabaseReference databaseReference;
    private String onlineDriverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        onlineDriverId = firebaseAuth.getCurrentUser().getUid();


        binding.alreadyAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverRegisterActivity.this, DriverLoginActivity.class));
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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email!!!", Toast.LENGTH_SHORT).show();
        }else if (password.length() < 8){
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
        }else {
            createDriverAccountWithEmailAndPassword();
        }
    }

    private void createDriverAccountWithEmailAndPassword() {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                           saveDataToDb();
                        }else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(DriverRegisterActivity.this, "Failed:", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(DriverRegisterActivity.this, "Failed due to:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveDataToDb() {
        binding.progressBar.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(onlineDriverId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userType", "driver");
        hashMap.put("email", email);
        databaseReference.setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            binding.progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(DriverRegisterActivity.this, DriverMapActivity.class));
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