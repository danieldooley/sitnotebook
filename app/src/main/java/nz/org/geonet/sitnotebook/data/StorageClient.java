package nz.org.geonet.sitnotebook.data;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.util.IOUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import nz.org.geonet.sitnotebook.changes.Change;
import nz.org.geonet.sitnotebook.changes.ChangeAdapter;
import nz.org.geonet.sitnotebook.changes.ChangeRecord;

/**
 * Created by ddooley on 2/12/17.
 */

public class StorageClient {

    Context context;
    Gson gson;

    public StorageClient(Context context){
        this.context = context;
        Log.d("STORAGE", "DIR: " + context.getFilesDir().getPath());
        GsonBuilder gsonb = new GsonBuilder();
        gsonb.registerTypeAdapter(Change.class, new ChangeAdapter());
        gson = gsonb.create();
    }

    public ArrayList<String> listFiles(){
        String[] a = context.fileList();
        ArrayList<String> output = new ArrayList<>();
        for (String s : a) {
            if (s.endsWith(".md")){
                output.add(s);
            }
        }
        return output;
    }

    public ChangeRecord loadFile(String filename){
        try {
            FileInputStream stream = context.openFileInput(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            Log.d("STORAGE", json);
            br.close();
            return gson.fromJson(json, ChangeRecord.class);
        } catch (Exception e){
            Log.e("STORAGE", e.getMessage());
            return null;
        }
    }

    public void saveFile(ChangeRecord c){
        try {
            String json = gson.toJson(c);
            FileOutputStream stream = context.openFileOutput(c.getFilename(), context.MODE_PRIVATE);
            stream.write(json.getBytes("UTF-8"));
            stream.flush();
            stream.close();
        } catch (Exception e){
            Log.e("STORAGE", e.getMessage());
        }
    }

    public void deleteFile(String filename){
        context.deleteFile(filename);
    }

}
