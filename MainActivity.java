package com.android.textrecognition;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.net.URI;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button capture, recognize;
    EditText name, email;
    Bitmap bitmap;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        capture=findViewById(R.id.capture);
        recognize=findViewById(R.id.recognize);
        recognize.setEnabled(false);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values=new ContentValues(2);
                values.put(MediaStore.Images.Media.TITLE, "New Image");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Image for Text Recognition");
                ContentResolver resolver=getContentResolver();
                uri=resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, 4);
            }
        });

        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable= (BitmapDrawable) imageView.getDrawable();
                bitmap=bitmapDrawable.getBitmap();
                if(bitmap!=null){
                    final FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(bitmap);
                    FirebaseVisionTextRecognizer textRecognizer=FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                    textRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(
                            new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    List<FirebaseVisionText.TextBlock> textBlocks = firebaseVisionText.getTextBlocks();
                                    String result="";
                                    for(int i=0; i<textBlocks.size(); i++){
                                        result+=textBlocks.get(i).getText()+"\n";
                                    }
                                    String[] separate=result.split("\n");
                                    String[] firstLine=separate[0].split(": ");
                                    String[] secondLine=separate[1].split(": ");
                                    name.setText(firstLine[1]);
                                    email.setText(secondLine[1]);

                                    imageView.setImageResource(R.drawable.autodice);
                                    recognize.setEnabled(false);
                                }
                            }
                    );
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            imageView.setImageURI(uri);
            recognize.setEnabled(true);
        }
    }
}
