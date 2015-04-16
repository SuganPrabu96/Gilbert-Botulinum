package com.noteshare.noteshare;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class FileUpload extends ActionBarActivity {

    private final String fileServerURL = "";
    private Button btnFileUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        btnFileUpload = (Button) findViewById(R.id.buttonUploadFile);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();


        btnFileUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type!=null && action.equals(Intent.ACTION_SEND)){
                    if(type.startsWith("application/pdf")){
                        handlePDF(intent);
                    }
                    else if(type.startsWith("image/*")){
                        handleImage(intent);
                    }
                }

                else if (type!=null && action.equals(Intent.ACTION_SEND_MULTIPLE)) {
                    if (type.startsWith("image/")) {
                        handleMultipleImages(intent); // Handle multiple images being sent
                    }
                    else if(type.startsWith("application/pdf")){
                        handleMultiplePDF(intent);
                    }
                }
            }
        });

        //TODO : upload the file to server
    }

    void handleImage(Intent i){
        Uri imageUri = i.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageUri != null){
            fileUpload(new File(String.valueOf(imageUri)));
        }
    }

    void handlePDF(Intent i){
        Uri pdfUri = i.getParcelableExtra(Intent.EXTRA_MIME_TYPES);
        if(pdfUri != null){
            fileUpload(new File(String.valueOf(pdfUri)));
        }
    }

    void handleMultipleImages(Intent i){
        ArrayList <Uri> imageMultipleUri = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if(imageMultipleUri != null){
            fileUpload(new File(String.valueOf(imageMultipleUri)));
        }
    }

    void handleMultiplePDF(Intent i){
        ArrayList <Uri> pdfMultipleUri = i.getParcelableArrayListExtra(Intent.EXTRA_MIME_TYPES);
        if(pdfMultipleUri != null){
            fileUpload(new File(String.valueOf(pdfMultipleUri)));
        }
    }

    private void fileUpload(File f){
        Log.i("File", String.valueOf(f));
        new UploadAsync().execute(f);
    }

    private class UploadAsync extends AsyncTask<File,Void,Void> {

        private boolean fileUploadSuccess = false;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(File... f){

            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(fileServerURL);

                try {
                    InputStreamEntity entity = new InputStreamEntity(new FileInputStream(String.valueOf(f)),-1);
                    entity.setContentType("binary/octet-stream");
                    entity.setChunked(true);
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    fileUploadSuccess=true; //TODO have to check if file is uploaded successfully
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if(fileUploadSuccess){
                Toast.makeText(getApplicationContext(),"File uploaded successfully",Toast.LENGTH_SHORT).show();
            }

            else if(!fileUploadSuccess){
                Toast.makeText(getApplicationContext(),"File not uploaded",Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_upload, menu);
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
