package com.noteshare.noteshare;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.intsig.csopen.sdk.CSOpenAPI;
import com.intsig.csopen.sdk.CSOpenAPIParam;
import com.intsig.csopen.sdk.CSOpenApiFactory;
import com.intsig.csopen.sdk.CSOpenApiHandler;

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
    private File pictureFile=null;
    private CSOpenAPI mApi;
    private final String camScannerApiKey = "Cy3EQTWeSH2bDMf3D8hX4bJU";
    private final int REQ_CODE_PICK_IMAGE = 1;
    private final int REQ_CODE_CALL_CAMSCANNER = 2;
    private static final String SCANNED_IMAGE = "scanned_img";
    private static final String SCANNED_PDF = "scanned_pdf";
    private static final String ORIGINAL_IMG = "ori_img";
    private String mSourceImagePath;
    private String mOutputImagePath;
    private String mOutputPdfPath;
    private String mOutputOrgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApi = CSOpenApiFactory.createCSOpenApi(this, camScannerApiKey, null);
        if(checkCamera(MainActivity.this))
            mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this,mCamera);
        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraPreview.addView(mCameraPreview);
        Button captureImageButton = (Button) findViewById(R.id.button_capture);

        if(mCamera!=null&&checkCamera(MainActivity.this))
            mCamera.setDisplayOrientation(90);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
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
            pictureFile = getOutputMediaFile();
            if(pictureFile==null){
                return;
            }
            try{
                Log.i("Picture URI",pictureFile.getPath());
                FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
                fileOutputStream.write(data);
                fileOutputStream.close();
                Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                try {
                    startActivityForResult(i, REQ_CODE_PICK_IMAGE);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
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
      public void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            File file = new File(String.valueOf(pictureFile));
            if (pictureFile != null)
                file.delete();
            if(requestCode == REQ_CODE_CALL_CAMSCANNER){
                mApi.handleResult(requestCode, resultCode, data, new CSOpenApiHandler() {

                    @Override
                    public void onSuccess() {
                        Log.i("Image Processed","Successful");
                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.e("Image Processed",String.valueOf(errorCode));
                    }

                    @Override
                    public void onCancel() {
                        Log.i("Image Processed","Cancelled");
                    }
                });
            } else if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == RESULT_OK) {	// result of go2Gallery
                if (data != null) {
                    Uri u = data.getData();
                    Cursor c = getContentResolver().query(u, new String[] { "_data" }, null, null, null);
                    if (c == null || c.moveToFirst() == false) {
                        return;
                    }
                    mSourceImagePath = c.getString(0);
                    c.close();
                    go2CamScanner();
                }
            }

        }
        catch (Exception e){
            Log.e("Error e",e.getMessage());
        }
    }

    private void go2CamScanner(){
        String timeExtension = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mOutputImagePath = pictureFile.getPath().split("IMG")[0]+timeExtension+".jpg";
        mOutputPdfPath = pictureFile.getPath().split("IMG")[0]+timeExtension+".jpg";
        mOutputOrgPath = pictureFile.getPath().split("IMG")[0]+"_ORG"+timeExtension+".jpg";

        try{
            FileOutputStream fos = new FileOutputStream(mOutputOrgPath);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        CSOpenAPIParam param = new CSOpenAPIParam(mSourceImagePath,mOutputImagePath,mOutputPdfPath,mOutputOrgPath,1.0f);
        boolean res = mApi.scanImage(this,REQ_CODE_CALL_CAMSCANNER,param);
        Log.d("Send to CS result", String.valueOf(res));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        releaseCamera();
    }

    @Override
    protected void onResume(){
        super.onResume();
        try {
            if(mCamera!=null)
            mCamera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
