package com.noteshare.noteshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import CameraUtility.CameraPreview;


public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private final int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this,mCamera);
        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.addView(mCameraPreview);
        Button captureImageButton = (Button) findViewById(R.id.button_capture);

        mCamera.setDisplayOrientation(90);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
             //   mCamera.stopPreview();
              //  mCamera.startPreview();
            }
        });
    }

    private boolean checkCamera(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;
        else
            return false;
    }

    private Camera getCameraInstance(){
        Camera cam = null;
        try{
             cam = Camera.open();
        }catch (Exception e){
             Log.e("Accessing Camera", "Unable to open camera");
             e.printStackTrace();
        }
        return cam;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i("Picture taken","Success");
            File pictureFile = getOutputMediaFile();
            if(pictureFile==null){
                return;
            }
            try{
                String timeExtension = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Log.i("Picture URI",pictureFile.getPath());
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                File file = new File(String.valueOf(pictureFile));
                fileOutputStream.write(data);
                Intent intent = new Intent("com.intsig.camscanner.ACTION_SCAN");
                Uri imgSource = Uri.fromFile(new File(pictureFile.getPath()));
                intent.putExtra(Intent.EXTRA_STREAM, imgSource);
                intent.putExtra("scanned_image", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        +File.separator+"NoteShare"+File.separator+"IMG_"+timeExtension+".jpg");
                intent.putExtra("pdf_path", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + "NoteShare" + File.separator + "IMG_" + timeExtension + ".pdf");
                startActivityForResult(intent,REQUEST_CODE);
                fileOutputStream.close();
                mCamera.stopPreview();
                mCamera.startPreview();
            }catch(FileNotFoundException e){
                Log.e("File not found",e.getMessage());
            }catch (IOException e){
                Log.e("Error accessing file",e.getMessage());
            }
        }
    };

    private void releaseCamera(){
        if(mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }

    private static File getOutputMediaFile(){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"NoteShare");
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.i("Error creating File","Failed to create file");
                return null;
            }
            else
                Log.d("Storage Path",mediaStorageDir.getAbsolutePath());
        }
        String timeExtension = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath()+File.separator+"IMG_"+timeExtension+".jpg");
        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==REQUEST_CODE){
            if(data!=null) {
                int responseCode = data.getIntExtra("RESULT_OK", -1);
                if (requestCode == Activity.RESULT_OK) {
                    Log.i("Processed Image", "Successful");
                } else if (requestCode == Activity.RESULT_FIRST_USER) {
                    Log.i("Processed Image", "Failed");
                } else if (requestCode == Activity.RESULT_CANCELED) {
                    Log.i("Processed Image", "User Cancelled");
                }
            }

            else
                Log.e("Error retrieving","data = null");

        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        releaseCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
