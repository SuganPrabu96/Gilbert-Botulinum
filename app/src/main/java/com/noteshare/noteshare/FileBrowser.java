package com.noteshare.noteshare;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FileBrowser extends ListActivity{

    private String filePath;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        filePath= String.valueOf(Environment.getExternalStorageDirectory())+File.separator+"/";

        if(getIntent().hasExtra("filePath"))
            filePath=getIntent().getStringExtra("filePath");

        //setTitle(filePath);

        List values = new ArrayList();
        File directory = new File(filePath);

        if(directory.canRead()){
            String[] list = directory.list();
            if(list!=null){
                for(String file : list){
                    if(!file.startsWith("."))
                        values.add(file);
                }
            }
        }

        Collections.sort(values);

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_2,android.R.id.text1,values);
        setListAdapter(arrayAdapter);
    }

    @Override
    protected void onListItemClick(ListView list, View v, int pos, long id){
        String fileName = (String) getListAdapter().getItem(pos);

        if(filePath.endsWith(File.separator)){
            fileName = filePath + fileName;
        }
        else {
            fileName = filePath + File.separator + fileName;
        }

        if (new File(fileName).isDirectory()) {
            Intent intent = new Intent(this, FileBrowser.class);
            intent.putExtra("filePath", fileName);
            startActivity(intent);
        }

        else {
            ContentResolver cr = getApplicationContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String fileExtension = mime.getExtensionFromMimeType(cr.getType(Uri.parse(fileName)));
            if(fileExtension!=null)
            Log.i("File Extension", fileExtension.toString());
            Intent i = new Intent(FileBrowser.this, FileUpload.class);
            i.setAction(Intent.ACTION_SEND);
            i.setType(fileExtension);
            startActivity(i);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_browser, menu);
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
