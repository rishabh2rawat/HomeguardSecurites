package com.rishabhrawat.homeguardsecurites;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CAMERA = 10, SELECT_FILE = 1;
    private String userChoosenTask;
    private CircleImageView profileimage;
    private AppCompatSpinner spinner;
    private AppCompatMultiAutoCompleteTextView autoCompleteTextView;
    Bitmap thumbnail;
    TextView uploadimage;
    Uri FilePathUri;
    String photourl;
    // Folder path for Firebase Storage.
    String Storage_Path = "All_Image_Uploads/";
    ProgressDialog progressDialog;
    Button register;

    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;

    String Flatlist[] = new String[]{"A101", "A102", "A103", "A104", "A105", "A106", "A107", "A108", "A109", "A110",
            "B101", "B102", "B103", "B104", "B105", "B106", "B107", "B108", "B109", "B110",
            "C101", "C102", "C103", "C104", "C105", "C106", "C107", "C108", "C109", "C110",
            "D101", "D102", "D103", "D104", "D105", "D106", "D107", "D108", "D109", "D110",
            "E101", "E102", "E103", "E104", "E105", "E106", "E107", "E108", "E109", "E110"};
    String Departmentlist[] = new String[]{"Big Bazaar", "Shoppers Stop", "Woodlands", "Lifestyle", "Marks & Spencer",
            "Max", "Metro Inc.", "Van Heusen", "Walmart", "HomeStop", "Louis Phillipe", "Hamleys", "Archies", "Peter England",
            "Akbarally's", "Vip", "FamilyMart", "Foodworld", "Arrow"};
    String Shoplist[] = new String[]{"Big Bazaar", "Shoppers Stop", "Woodlands", "Lifestyle", "Marks & Spencer",
            "Max", "Metro Inc.", "Van Heusen", "Walmart", "HomeStop", "Louis Phillipe", "Hamleys", "Archies", "Peter England",
            "Akbarally's", "Vip", "FamilyMart", "Foodworld", "Arrow"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*linking to layout*/
        profileimage = (CircleImageView) findViewById(R.id.profile_image);
        spinner = (AppCompatSpinner) findViewById(R.id.visit_dropdown);
        autoCompleteTextView = (AppCompatMultiAutoCompleteTextView) findViewById(R.id.multipleselect);
        register = (Button) findViewById(R.id.register);
        uploadimage = (TextView) findViewById(R.id.uploadimage);
        progressDialog = new ProgressDialog(MainActivity.this);


/**********************on click image***********************************/
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

/************************visitin dropdown **************************************/
        List<String> Visit = new ArrayList<String>();
        Visit.add("Flat");
        Visit.add("Department");
        Visit.add("Shop");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, Visit);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equals("Flat")) {
                    ArrayAdapter<String> multiadapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, Flatlist);
                    autoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    autoCompleteTextView.setAdapter(multiadapter);

                } else if (item.equals("Department")) {
                    ArrayAdapter<String> multiadapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, Departmentlist);
                    autoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    autoCompleteTextView.setAdapter(multiadapter);
                } else if (item.equals("Shop")) {
                    ArrayAdapter<String> multiadapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, Shoplist);
                    autoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    autoCompleteTextView.setAdapter(multiadapter);
                }

                Toast.makeText(MainActivity.this, item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        /**************************************Uploading profile image to the firebase and getting a uniqe url*********************/

        uploadimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thumbnail != null) {
                    storageReference = FirebaseStorage.getInstance().getReference();

                    UploadImageFileToFirebaseStorage();
                } else {
                    Toast.makeText(MainActivity.this, "first take a profile photo", Toast.LENGTH_SHORT).show();
                }
            }
        });



        /*----------------------------------------on Click register------------------------------------------------------------*/
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "redy to setup api to register to the server", Toast.LENGTH_SHORT).show();
            }
        });

    }


    /*********************select image method*************************************************************/
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {


                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                            cameraIntent();
                        } else {
                            String[] permitionRequested = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permitionRequested, REQUEST_CAMERA);
                        }
                    }
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            galleryIntent();
                        } else {
                            String[] permitionRequested = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            requestPermissions(permitionRequested, SELECT_FILE);
                        }
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: //Request camera code
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo")) {
                        cameraIntent();
                    }

                }
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Choose from Library")) {
                        galleryIntent();
                    }
                    break;
                }
        }

    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        thumbnail = (Bitmap) data.getExtras().get("data");


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);


        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");


        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FilePathUri = getImageUri(getApplicationContext(), thumbnail);
        profileimage.setImageBitmap(thumbnail);
    }

    /*////////////////////////////////geting uri from bitmap///////////////////////////////////////*/
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        thumbnail = null;
        if (data != null) {
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FilePathUri = data.getData();
        profileimage.setImageBitmap(thumbnail);
    }

    /***************************************upload image to firebase storage*******************************************/

    public void UploadImageFileToFirebaseStorage() {
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            progressDialog.show();

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + ".jpg");

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressLint("VisibleForTests")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

                            photourl = taskSnapshot.getDownloadUrl().toString();


                            // Showing toast message after done uploading.
                            Toast.makeText(getApplicationContext(), "Image Uploaded Successfully url=" + photourl, Toast.LENGTH_LONG).show();
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            // Setting progressDialog Title.
                            progressDialog.setTitle("Image is Uploading...");

                        }
                    });
        } else {

            Toast.makeText(MainActivity.this, "Please Select Image", Toast.LENGTH_LONG).show();

        }
    }

}
