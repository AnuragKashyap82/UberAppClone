package kashyap.anurag.uberclone;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import kashyap.anurag.uberclone.databinding.ActivitySettingsBinding;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private String getType;
    private ActivitySettingsBinding binding;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private String checker = "";
    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsRef;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getType = getIntent().getStringExtra("type");
        Toast.makeText(this, getType, Toast.LENGTH_SHORT).show();


        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");


        if (getType.equals("Drivers"))
        {
            binding.driverCarName.setVisibility(View.VISIBLE);
        }



        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (getType.equals("Drivers"))
                {
                    startActivity(new Intent(SettingsActivity.this, DriverMapActivity.class));
                }
                else
                {
                    startActivity(new Intent(SettingsActivity.this, CustomerMapActivity.class));
                }
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (checker.equals("clicked"))
                {
                    validateControllers();
                }
                else
                {
                    validateAndSaveOnlyInformation();
                }
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                checker = "clicked";

                pickImageGallery();
            }
        });

        getUserInformation();
    }

    private void pickImageGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher0.launch(intent);

    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher0 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        binding.profileImage.setImageURI(imageUri);
                        uploadImages();

                    } else {
                        Toast.makeText(SettingsActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void uploadImages() {
        long timestamp = System.currentTimeMillis();

        String filePathAndName = "ProductsImages/" + timestamp + 0;
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = "" + uriTask.getResult();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("productId", "" + timestamp);
                        hashMap.put("productImage", "" + uploadedImageUrl);

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("productImages").child(""+timestamp).child("BannerImages").child("0");
                        databaseReference.setValue(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {


                                        } else {

                                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(SettingsActivity.this, "Failed to upload image due to\"+e.getMessage()", Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double p = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==6022 &&  resultCode==RESULT_OK  &&  data!=null)
        {

        }
        else
        {
            if (getType.equals("Drivers"))
            {
                startActivity(new Intent(SettingsActivity.this, DriverMapActivity.class));
            }
            else
            {
                startActivity(new Intent(SettingsActivity.this, CustomerMapActivity.class));
            }

            Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show();
        }
    }



    private void validateControllers()
    {
        if (TextUtils.isEmpty(binding.name.getText().toString()))
        {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(binding.phoneNumber.getText().toString()))
        {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show();
        }
        else if (getType.equals("Drivers")  &&  TextUtils.isEmpty(binding.driverCarName.getText().toString()))
        {
            Toast.makeText(this, "Please provide your car Name.", Toast.LENGTH_SHORT).show();
        }
        else if (checker.equals("clicked"))
        {
            uploadProfilePicture();
        }
    }




    private void uploadProfilePicture()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Settings Account Information");
        progressDialog.setMessage("Please wait, while we are settings your account information");
        progressDialog.show();


        if (imageUri != null)
        {
            final StorageReference fileRef = storageProfilePicsRef
                    .child(mAuth.getCurrentUser().getUid()  +  ".jpg");

            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();


                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", mAuth.getCurrentUser().getUid());
                        userMap.put("name", binding.name.getText().toString());
                        userMap.put("phone", binding.phoneNumber.getText().toString());
                        userMap.put("image", myUrl);

                        if (getType.equals("Drivers"))
                        {
                            userMap.put("car", binding.driverCarName.getText().toString());
                        }

                        databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);

                        progressDialog.dismiss();

                        if (getType.equals("Drivers"))
                        {
                            startActivity(new Intent(SettingsActivity.this, DriverMapActivity.class));
                        }
                        else
                        {
                            startActivity(new Intent(SettingsActivity.this, CustomerMapActivity.class));
                        }
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, "Image is not selected.", Toast.LENGTH_SHORT).show();
        }
    }




    private void validateAndSaveOnlyInformation()
    {
        if (TextUtils.isEmpty(binding.name.getText().toString()))
        {
            Toast.makeText(this, "Please provide your name.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(binding.phoneNumber.getText().toString()))
        {
            Toast.makeText(this, "Please provide your phone number.", Toast.LENGTH_SHORT).show();
        }
        else if (getType.equals("Drivers")  &&  TextUtils.isEmpty(binding.driverCarName.getText().toString()))
        {
            Toast.makeText(this, "Please provide your car Name.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("name", binding.name.getText().toString());
            userMap.put("phone", binding.phoneNumber.getText().toString());

            if (getType.equals("Drivers"))
            {
                userMap.put("car", binding.driverCarName.getText().toString());
            }

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);

            if (getType.equals("Drivers"))
            {
                startActivity(new Intent(SettingsActivity.this, DriverMapActivity.class));
            }
            else
            {
                startActivity(new Intent(SettingsActivity.this, CustomerMapActivity.class));
            }
        }
    }


    private void getUserInformation()
    {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    binding.name.setText(name);
                    binding.phoneNumber.setText(phone);

                    if (getType.equals("Drivers"))
                    {
                        String car = dataSnapshot.child("car").getValue().toString();
                        binding.driverCarName.setText(car);
                    }


                    if (dataSnapshot.hasChild("image"))
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(binding.profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}