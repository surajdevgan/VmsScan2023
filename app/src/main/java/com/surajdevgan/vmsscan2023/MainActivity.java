package com.surajdevgan.vmsscan2023;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity

{
    SurfaceView cameraPreview;
    TextView cresult;
    BarcodeDetector detector;
    CameraSource cameraSource;
    //final int RequestCameraPermissionId = 1;
    Bitmap bitmap;
    static final String IMAGE_DIRECTORY = "/ServiceCodeImages";
    File f;
    String path="";
    long date;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        date=System.currentTimeMillis();
        cameraPreview = (findViewById(R.id.camera_view));
        cresult = (findViewById(R.id.cresult));

        detector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, detector)
                .setRequestedPreviewSize(640, 480)
                .build();
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                 //   ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestCameraPermissionId);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {



            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {


                cameraSource.stop();



            }
        });
        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {


            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {


                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();

                if(qrcodes.size()!=0)
                {
                    //detector.release();

                    cresult.post(new Runnable() {


                                     @Override
                                     public void run() {




                                         // create vibrate when detected qr code
                                         Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                         vibrator.vibrate(100);


                                         String serviceQr=qrcodes.valueAt(0).displayValue;
                                         generateQR(serviceQr);

                                         if (generateQR(serviceQr)) {
                                             cameraPreview.setVisibility(View.GONE);
                                             cameraSource.release();
                                             showDialouge();

                                         }
                                     }
                                 }
                    );
                }
            }
        });

    }


    void showDialouge(){
        final Uri uri=Uri.parse(path);
        Button appCompatButton, appCompatButton1;

        ImageView imageView ;
        final Dialog dialog=new Dialog(MainActivity.this);
        dialog.setTitle("Service ID Code");
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialouge_layout);
        appCompatButton=dialog.findViewById(R.id.share);
        appCompatButton1 = dialog.findViewById(R.id.cancel);
        imageView = dialog.findViewById(R.id.qrcode);
        imageView.setImageBitmap(bitmap);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setPackage("com.whatsapp");
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Share Using"));
                dialog.dismiss();
                finish();

            }

        });

        appCompatButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();


            }
        });
        dialog.show();
    }


    boolean generateQR(String serviceId){

        MultiFormatWriter formatWriter = new MultiFormatWriter();
        try {
            BitMatrix matrix = formatWriter.encode(serviceId, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            path = saveImage(bitmap);  //give read write permission
            // Toast.makeText(CameraActivity.this, "QRCode saved to -> "+path, Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            f = new File(wallpaperDirectory, date + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this, new String[]{f.getPath()}, new String[]{"image/jpeg"}, null);
            fo.close();

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


}