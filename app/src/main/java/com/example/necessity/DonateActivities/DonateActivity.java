package com.example.necessity.DonateActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.necessity.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DonateActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private int REQUEST_CODE = 11;
    SupportMapFragment mapFragment;
    EditText mFullName,mFoodItem,mDescription,mPhone, mExpiry;
    Button mSubmitBtn;

    ImageView mLeft, mRight;
    ImageSwitcher imageSwitcher;
    int PICK_IMAGE_MULTIPLE = 1;
    ArrayList<Uri> ImageList;
    int position = 0;
    Button mSelect, mClear;
    private int uploads = 0;
    private ProgressDialog progressDialog;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID, timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        mFullName = findViewById(R.id.donorname);
        mFoodItem = findViewById(R.id.fooditem);
        mPhone = findViewById(R.id.phone);
        mDescription = findViewById(R.id.description);
        mExpiry = findViewById(R.id.expiry);
        mSubmitBtn=findViewById(R.id.submit);

        mLeft = findViewById(R.id.imgLeft);
        mRight = findViewById(R.id.imgRight);
        imageSwitcher = findViewById(R.id.image_switcher);
        mSelect = findViewById(R.id.selectImg);
        mClear = findViewById(R.id.clearImg);
        ImageList = new ArrayList<Uri>();

        fAuth=FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView1 = new ImageView(getApplicationContext());
                return imageView1;
            }
        });

        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSwitcher.removeAllViews();
                imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                    @Override
                    public View makeView() {
                        ImageView imageView1 = new ImageView(getApplicationContext());
                        return imageView1;
                    }
                });
                ImageList.clear();
            }
        });

        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position < ImageList.size() - 1) {
                    // increase the position by 1
                    position++;
                    imageSwitcher.setImageURI(ImageList.get(position));
                } else {
                    Toast.makeText(DonateActivity.this, "Last Image Already Shown", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position > 0) {
                    // decrease the position by 1
                    position--;
                    imageSwitcher.setImageURI(ImageList.get(position));
                }
            }
        });

    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // When an Image is picked
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
            // Get the Image from data
            if (data.getClipData() != null) {
                int cout = data.getClipData().getItemCount();
                for (int i = 0; i < cout; i++) {
                    // adding imageuri in array
                    Uri imageurl = data.getClipData().getItemAt(i).getUri();
                    ImageList.add(imageurl);
                }
                // setting 1st selected image into image switcher
            } else {
                Uri imageurl = data.getData();
                ImageList.add(imageurl);
            }
            imageSwitcher.setImageURI(ImageList.get(0));
            position = 0;
        } else {
            // show this if no image is selected
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        //MarkerOptions markerOptions1 = new MarkerOptions().position(latLng).title("You are here");
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mMap.addMarker(markerOptions1).showInfoWindow();

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(markerOptions).showInfoWindow();


        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = mFullName.getText().toString().trim();
                String fooditem= mFoodItem.getText().toString().trim();
                String description= mDescription.getText().toString().trim();
                String phone= mPhone.getText().toString().trim();
                String exp = mExpiry.getText().toString().trim();
                String type= "Donor";

                if(TextUtils.isEmpty(fullname))
                {
                    mFullName.setError("Name is Required.");
                    return;
                }

                if(TextUtils.isEmpty(fooditem))
                {
                    mFoodItem.setError("Required.");
                    return;
                }

                if(phone.length() < 10)
                {
                    mPhone.setError("Phone Number Must be >=10 Characters");
                    return;
                }

                if(TextUtils.isEmpty(exp))
                {
                    mExpiry.setError("Required.");
                    return;
                }

                progressDialog = new ProgressDialog(DonateActivity.this);
                progressDialog.setMessage("Uploading....please wait");
                progressDialog.show();

                userID = fAuth.getCurrentUser().getUid();
                //DocumentReference documentReference = fStore.collection("donate").document(userID);
                CollectionReference collectionReference = fStore.collection("donations");

                GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                timestamp  = dateFormat.format(new Date());

                long expiry= TimeUnit.DAYS.toMillis(Long.parseLong(exp)) + System.currentTimeMillis();

                Log.d("testdays", String.valueOf(TimeUnit.DAYS.toMillis(Long.parseLong(exp))));
                Log.d("current", String.valueOf(System.currentTimeMillis()));

                Map<String,Object> user = new HashMap<>();
                user.put("timestamp", timestamp);
                user.put("name",fullname);
                user.put("fooditem",fooditem);
                user.put("phone",phone);
                user.put("description",description);
                user.put("location",geoPoint);
                user.put("userid",userID);
                user.put("expiry",expiry);
                user.put("type",type);


                collectionReference.document(timestamp).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        upload();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error", e.getMessage());
                    }
                });
            }
        });
    }

    public void upload() {
        final StorageReference ImageFolder =  FirebaseStorage.getInstance().getReference();

        ArrayList<String> imageLinks = new ArrayList<String>();

        for (uploads = 0; uploads < ImageList.size(); uploads++) {
            Uri Image  = ImageList.get(uploads);
            final StorageReference imagename = ImageFolder.child("images/"+timestamp+"/"+Image.getLastPathSegment());

            if (uploads == ImageList.size()-1) {
                imagename.putFile(ImageList.get(uploads)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagename.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = String.valueOf(uri);
                                imageLinks.add(url);
                                Log.d("imglst", imageLinks.toString());
                                SendLink(imageLinks);
                            }
                        });

                    }
                });
            } else {
                imagename.putFile(ImageList.get(uploads)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imagename.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = String.valueOf(uri);
                                imageLinks.add(url);
                                Log.d("imglst", imageLinks.toString());
                            }
                        });

                    }
                });
            }

        }

    }

    private void SendLink(ArrayList<String> imageLinks) {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("links", imageLinks);

        CollectionReference collectionReference = fStore.collection("donations");

        collectionReference.document(timestamp).update(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                ImageList.clear();
                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                mapFragment.getMapAsync(this);
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
