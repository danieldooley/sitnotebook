package nz.org.geonet.sitnotebook;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import nz.org.geonet.sitnotebook.changes.Change;
import nz.org.geonet.sitnotebook.changes.ChangeRecord;
import nz.org.geonet.sitnotebook.data.DropboxClient;
import nz.org.geonet.sitnotebook.data.StorageClient;


public class MainActivity extends AppCompatActivity {

    private static final int SITECODE_SEARCH_RETURN = 922;
    private static final int LOAD_SEARCH_RETURN = 818;
    private static final int NEW_CHANGE_RETURN = 566;
    private static final int EDIT_CHANGE_RETURN = 782;

    ChangeRecord changeRecord;

    TextView tv_sitecode;
    Toolbar toolbar;
    ListView lv;
    Button b;
    FloatingActionButton fab;
    ProgressBar pb;

    ArrayList<String> sitecodes;
    String username;
    List<String> changeStrings;
    ArrayAdapter<String> change_adapter;

    DropboxClient dbxc;
    StorageClient stc;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MAIN", "OnResume");
        dbxc.init();

        if (username == null || username.length() == 0) {
            incrementProgress();
            dbxc.getUsername(new DropboxClient.ReturnHandler<String>() {
                @Override
                public void onReturn(String username) {
                    MainActivity.this.username = username;
                    if (changeRecord != null){
                        changeRecord.setUsername(username);
                    }
                    Log.i("MAIN", "USERNAME: " + username);
                    decrementProgress();
                }
                @Override
                public void onError(Exception e) {
                    Log.e("MAIN", e.getMessage());
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    decrementProgress();
                }
            });
        }

        if (sitecodes == null) {
            incrementProgress();
            dbxc.getValidSiteCodes(new DropboxClient.ReturnHandler<ArrayList<String>>() {
                @Override
                public void onReturn(ArrayList<String> value) {
                    Log.i("MAIN", "sitecodes.size() = " + value.size());
                    sitecodes = value;
                    decrementProgress();
                }

                @Override
                public void onError(Exception e) {
                    Log.e("MAIN", e.getMessage());
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    decrementProgress();
                }
            });
        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i("MAIN", "OnCreate");

        dbxc = new DropboxClient(this, getString(R.string.APP_KEY));
        stc = new StorageClient(this);

        tv_sitecode = findViewById(R.id.sitecode_tv);
        lv = findViewById(R.id.lv);
        b = findViewById(R.id.button);
        fab = findViewById(R.id.fab);
        pb = findViewById(R.id.loading_indicator);

        changeStrings = new ArrayList<>();
        change_adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, changeStrings);
        lv.setAdapter(change_adapter);

        newChangeRecord();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progress > 0) {
                    Toast.makeText(MainActivity.this, "Activity in Progress. Please Wait.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (changeRecord.getSitecode().length() == 0){
                    noticeDialog("You need to select a site before adding changes", "Missing Sitecode");
                    return;
                }
                Intent intent = new Intent(MainActivity.this, NewChangeActivity.class);
                startActivityForResult(intent, NEW_CHANGE_RETURN);
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (progress > 0) {
                    Toast.makeText(MainActivity.this, "Activity in Progress. Please Wait.", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, SearchList.class);
                intent.putStringArrayListExtra("VALUES", sitecodes);
                intent.putExtra("SEARCH_TITLE", "Site Code Search");
                startActivityForResult(intent, SITECODE_SEARCH_RETURN);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (progress > 0) {
                    Toast.makeText(MainActivity.this, "Activity in Progress. Please Wait.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (changeRecord.size() > 0) {
                    Change toEdit = changeRecord.getChange(i);
                    editChange(toEdit, i);
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Change c = changeRecord.getChange(i);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Change")
                        .setMessage("You are about to delete the change: '" + c.getDisplayString() + "'. Do you wish to continue?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                changeRecord.removeChange(c);
                                generateChangeList();
                                Toast.makeText(MainActivity.this, "Removed Change", Toast.LENGTH_SHORT).show();
                                stc.saveFile(changeRecord);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
    }

    public void editChange(Change c, int i) {
        Intent editIntent = new Intent(MainActivity.this, c.getEditingClass());
        editIntent.putExtra("EDIT_CHANGE", c);
        editIntent.putExtra("EDIT", i);
        startActivityForResult(editIntent, EDIT_CHANGE_RETURN);
    }

    private void newChangeRecord(){
        changeRecord = new ChangeRecord();
        if (username != null && username.length() > 0){
            changeRecord.setUsername(username);
        }
        generateChangeList();
    }

    public void generateChangeList() {
        changeStrings.clear();
        if (changeRecord.size() == 0) {
            changeStrings.add("Use the + button to add changes.");
        } else {
            changeStrings.addAll(changeRecord.getDisplayList());
        }
        change_adapter.notifyDataSetChanged();
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

        if (progress > 0) {
            Toast.makeText(MainActivity.this, "Activity in Progress. Please Wait.", Toast.LENGTH_LONG).show();
            return true;
        }

        switch (id) {
            case R.id.load_item:
                if (progress > 0) {
                    Toast.makeText(MainActivity.this, "Activity in Progress. Please Wait.", Toast.LENGTH_LONG).show();
                    return true;
                }
                ArrayList<String> files = stc.listFiles();
                Intent intent = new Intent(MainActivity.this, SearchList.class);
                intent.putStringArrayListExtra("VALUES", files);
                intent.putExtra("SEARCH_TITLE", "Load File Dialog");
                startActivityForResult(intent, LOAD_SEARCH_RETURN);
                break;
            case R.id.export_menu:
                if (changeRecord.getSitecode().length() == 0){
                    noticeDialog("You need to select a site before saving.", "Missing Sitecode");
                    return true;
                }
                if (changeRecord.size() == 0) {
                    noticeDialog("You cannot export a change record with no changes.", "No Changes");
                    return true;
                }
                final String filename = changeRecord.getFilename();
                incrementProgress();
                dbxc.saveTextFile(filename, changeRecord.generateFile(), new DropboxClient.ReturnHandler<Void>() {
                    @Override
                    public void onReturn(Void value) {
                        Toast.makeText(MainActivity.this, "File: '" + filename + "' saved to Dropbox.", Toast.LENGTH_SHORT).show();
                        decrementProgress();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Delete Exported File?")
                                .setMessage("File has been exported to Dropbox. Delete the local copy?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        stc.deleteFile(changeRecord.getFilename());
                                        newChangeRecord();
                                        tv_sitecode.setText("Please Select");
                                        Toast.makeText(MainActivity.this, "Local File Deleted", Toast.LENGTH_SHORT).show();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("MAIN", e.getMessage());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        decrementProgress();
                    }
                });
                break;
            case R.id.new_menu:
                    newChangeRecord();
                    tv_sitecode.setText("Please Select");
                    Toast.makeText(MainActivity.this, "New Change Record", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.delete_menu:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Change Record?")
                        .setMessage("Delete this local file?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                stc.deleteFile(changeRecord.getFilename());
                                newChangeRecord();
                                tv_sitecode.setText("Please Select");
                                Toast.makeText(MainActivity.this, "Local File Deleted", Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

        }

        return super.onOptionsItemSelected(item);
    }

    public void noticeDialog(String message, String title){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton("Ok", null).show();
    }

    int progress = 0;

    private void incrementProgress(){
        if (progress++ == 0){
            pb.setVisibility(View.VISIBLE);
        }
    }

    private void decrementProgress(){
        if (--progress == 0){
            pb.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            return;
        }

        switch(requestCode){
            case SITECODE_SEARCH_RETURN:
                String code = data.getStringExtra("RETURN_VALUE");
                tv_sitecode.setText(code);
                if (changeRecord.getSitecode().length() > 0) {
                    stc.deleteFile(changeRecord.getFilename());
                }
                changeRecord.setSitecode(code);
                stc.saveFile(changeRecord);
                break;
            case LOAD_SEARCH_RETURN:
                String file = data.getStringExtra("RETURN_VALUE");
                Log.d("MAIN", file);
                changeRecord = stc.loadFile(file);
                tv_sitecode.setText(changeRecord.getSitecode());
                generateChangeList();
                stc.saveFile(changeRecord);
                break;
            case NEW_CHANGE_RETURN:
                Change newChange = data.getParcelableExtra("CHANGE");
                changeRecord.addChange(newChange);
                generateChangeList();
                stc.saveFile(changeRecord);
                break;
            case EDIT_CHANGE_RETURN:
                Change editChange = data.getParcelableExtra("CHANGE");
                changeRecord.update( data.getIntExtra("EDIT", -1), editChange);
                generateChangeList();
                stc.saveFile(changeRecord);
                break;
        }
    }

}
