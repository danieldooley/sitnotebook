package nz.org.geonet.sitnotebook.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.TimeUnit;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.ListFilesResult;
import com.dropbox.core.v2.users.FullAccount;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.org.geonet.sitnotebook.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ddooley on 1/12/17.
 */

public class DropboxClient {

    Context context;
    String APP_KEY;

    String oauthtoken;

    DbxClientV2 client;

    SharedPreferences prefs;

    Long cacheTime = 86400000L;

    public DropboxClient(Context context, String APP_KEY){
        this.context = context;
        this.APP_KEY = APP_KEY;
        prefs = context.getSharedPreferences("sitnotebook", MODE_PRIVATE);
    }

    public void init(){
        oauthtoken = prefs.getString("dbx-oauth-token", null);
        if (oauthtoken == null) {
            oauthtoken = Auth.getOAuth2Token();
            Log.i("DROPBOX", "OAUTH: " + oauthtoken);

            if (oauthtoken != null) {
                prefs.edit().putString("dbx-oauth-token", oauthtoken).apply();
            } else {
                Auth.startOAuth2Authentication(context, APP_KEY);
            }
        } else {
            Log.i("DROPBOX", "OAUTH: " + oauthtoken);
        }

        if (oauthtoken != null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("sitnotebook").build();
            client = new DbxClientV2(requestConfig, oauthtoken);
        }
    }

    public void clearCache(){
        prefs.edit().clear().putString("dbx-oauth-token", oauthtoken).commit();
    }

    public static abstract class ReturnHandler<T> {
        public abstract void onReturn(T value);
        public abstract void onError(Exception e);
    }

    public void online(final ReturnHandler<Boolean> handler){
        if (client == null){
            handler.onReturn(false);
        }
        new GetCurrentAccountTask(client, new ReturnHandler<FullAccount>() {
            @Override
            public void onReturn(FullAccount value) {
                handler.onReturn(true);
            }
            @Override
            public void onError(Exception e) {
                handler.onReturn(false);
            }
        }).execute();
    }

    static class GetCurrentAccountTask extends AsyncTask<Void, Void, FullAccount> {
        DbxClientV2 client;
        ReturnHandler<FullAccount> handler;
        Exception e;
        protected GetCurrentAccountTask(DbxClientV2 client, ReturnHandler<FullAccount> handler){
            this.handler = handler;
            this.client = client;
        }
        @Override
        protected FullAccount doInBackground(Void... params) {
            try {
                return client.users().getCurrentAccount();
            } catch (DbxException e){
                this.e = e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(FullAccount account){
            super.onPostExecute(account);
            if (e==null){
                handler.onReturn(account);
            } else {
                handler.onError(e);
            }
        }
    }

    public void getUsername(final ReturnHandler<String> handler){
        if (client == null){
            handler.onError(new DbxException("No Dropbox Token"));
            return;
        }

        String username = prefs.getString("USERNAME", "");
        if (username.length() != 0){
            Long ts = prefs.getLong("USERNAME_TS", -1);
            if (new Date().getTime() - ts < cacheTime){
                handler.onReturn(username);
                return;
            }
        }

        new GetCurrentAccountTask(client, new ReturnHandler<FullAccount>() {
            @Override
            public void onReturn(FullAccount value) {
                String username = value.getName().getDisplayName();
                if (username.contains("(")){
                    username = username.split("\\(")[0];
                }
                prefs.edit().putString("USERNAME", username).putLong("USERNAME_TS", new Date().getTime()).apply();
                handler.onReturn(username);
            }
            @Override
            public void onError(Exception e) {
                String username = prefs.getString("USERNAME", "");
                if (username.length() != 0){
                    handler.onReturn(username);
                } else {
                    handler.onError(e);
                }
            }
        }).execute();
    }

    static class ListFilesTask extends AsyncTask<String, Void, ArrayList<String>> {
        DbxClientV2 client;
        ReturnHandler<ArrayList<String>> handler;
        Exception e;
        protected ListFilesTask(DbxClientV2 client, ReturnHandler<ArrayList<String>> handler){
            this.handler = handler;
            this.client = client;
        }
        @Override
        protected ArrayList<String> doInBackground(String... params) {
            try {
                String dir = params[0];
                Log.i("DROPBOX", "Listing: " + dir);
                ArrayList<String> output = new ArrayList<>();
                ListFolderResult res = client.files().listFolder(dir);
                do {
                    for (Metadata entry : res.getEntries()){
                        output.add(entry.getName().split("\\.")[0]);
                    }
                    if (res.getHasMore()) {
                        res = client.files().listFolderContinue(res.getCursor());
                    } else {
                        res = null;
                    }
                } while(res != null);
                return output;
            } catch (DbxException e){
                this.e = e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList<String> files){
            super.onPostExecute(files);
            if (e==null){
                handler.onReturn(files);
            } else {
                handler.onError(e);
            }
        }
    }

    public void getValidSiteCodes(final ReturnHandler<ArrayList<String>> handler) {
        if (client == null){
            handler.onError(new DbxException("No Dropbox Token"));
            return;
        }

        String sitecodes = prefs.getString("SITECODES", "");
        if (sitecodes.length() != 0){
            Long ts = prefs.getLong("SITECODES_TS", -1);
            if (new Date().getTime() - ts < cacheTime){
                ArrayList<String> out = new ArrayList<String>(Arrays.asList(sitecodes.split(",")));
                handler.onReturn(out);
                return;
            }
        }

        new ListFilesTask(client, new ReturnHandler<ArrayList<String>>() {
            @Override
            public void onReturn(ArrayList<String> value) {
                StringBuilder sb = new StringBuilder();
                for (String v : value){
                    if (sb.length() != 0) {
                        sb.append(",");
                    }
                    sb.append(v);
                }
                prefs.edit().putString("SITECODES", sb.toString()).putLong("SITECODES_TS", new Date().getTime()).apply();
                handler.onReturn(value);
            }
            @Override
            public void onError(Exception e) {
                String sitecodes = prefs.getString("SITECODES", "");
                if (sitecodes.length() != 0){
                    ArrayList<String> out = new ArrayList<String>(Arrays.asList(sitecodes.split(",")));
                    handler.onReturn(out);
                } else {
                    handler.onError(e);
                }
            }
        }).execute(context.getString(R.string.sit_pdf_dir));
    }

    static class SaveTextFileTask extends AsyncTask<String, Void, Void> {
        DbxClientV2 client;
        ReturnHandler<Void> handler;
        Exception e;
        protected SaveTextFileTask(DbxClientV2 client, ReturnHandler<Void> handler){
            this.handler = handler;
            this.client = client;
        }
        @Override
        protected Void doInBackground(String... params) {
            try {
                String dir = params[0];
                String filename = params[1];
                String contents = params[2];
                InputStream stream = new ByteArrayInputStream(contents.getBytes("UTF-8"));
                client.files().upload(dir+"/"+filename).uploadAndFinish(stream);
                return null;
            } catch (Exception e){
                this.e = e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v){
            super.onPostExecute(v);
            if (e==null){
                handler.onReturn(v);
            } else {
                handler.onError(e);
            }
        }
    }

    public void saveTextFile(String filename, String contents, final ReturnHandler<Void> handler){
        if (client == null){
            handler.onError(new DbxException("No Dropbox Token"));
            return;
        }
        new SaveTextFileTask(client, new ReturnHandler<Void>() {
            @Override
            public void onReturn(Void value) {
                handler.onReturn(value);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
                //TODO: Cache
            }
        }).execute(context.getString(R.string.sit_output_dir), filename, contents);
    }
}
