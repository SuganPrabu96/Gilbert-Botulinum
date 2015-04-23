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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class FileUpload extends ActionBarActivity {

    private final String fileServerURL = "52.74.135.20/";
    private Button btnFileUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_upload);

        btnFileUpload = (Button) findViewById(R.id.buttonUploadFile);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        Log.i("intent",intent.toString());



        btnFileUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type!=null && action.equals(Intent.ACTION_SEND)){
                    if(type.startsWith("application/pdf")){
                        handlePDF(intent);
                    }
                    else if(type.startsWith("image/")){
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

        ArrayList<Uri> uris = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if(uris != null){
            Toast.makeText(FileUpload.this,"In file upload image", Toast.LENGTH_SHORT).show();
            fileUpload((String.valueOf(uris.get(0))));
        }
    }

    void handlePDF(Intent i){
        Uri pdfUri = i.getParcelableExtra(Intent.EXTRA_MIME_TYPES);
        if(pdfUri != null){
            fileUpload((String.valueOf(pdfUri)));
        }
    }

    void handleMultipleImages(Intent i){
        ArrayList <Uri> imageMultipleUri = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if(imageMultipleUri != null){
            fileUpload((String.valueOf(imageMultipleUri)));
        }
    }

    void handleMultiplePDF(Intent i){
        ArrayList <Uri> pdfMultipleUri = i.getParcelableArrayListExtra(Intent.EXTRA_MIME_TYPES);
        if(pdfMultipleUri != null){
            fileUpload((String.valueOf(pdfMultipleUri)));
        }
    }

    private void fileUpload(String f){
        Log.i("File", String.valueOf(f));
        new UploadAsync().execute(f);
    }

    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        int serverResponseCode = 0;

        if (!sourceFile.isFile()) {

            runOnUiThread(new Runnable() {
                public void run() {
                    Log.i("err", "source file does not exist");
                }
            });

            return 0;

        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(fileServerURL);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; filename=" + fileName + "" + lineEnd);

                        dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {

                            Toast.makeText(FileUpload.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(FileUpload.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(FileUpload.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                        + e.getMessage(), e);
            }
            return serverResponseCode;

        } // End else block
    }
    private class UploadAsync extends AsyncTask<String,Void,Void> {

        private boolean fileUploadSuccess = false;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... f){

           /* try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(fileServerURL);

                    InputStreamEntity entity = new InputStreamEntity(new FileInputStream(String.valueOf(f)),-1);
                    entity.setContentType("binary/octet-stream");
                    entity.setChunked(true);
                    httpPost.setEntity(entity);
                    Toast.makeText(FileUpload.this,"In background", Toast.LENGTH_SHORT).show();
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    fileUploadSuccess=true; //TODO have to check if file is uploaded successfully

            }catch (Exception e){
                e.printStackTrace();
            }*/

            uploadFile(f.toString());

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
