package nz.org.geonet.sitnotebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nz.org.geonet.sitnotebook.changes.Change;
import nz.org.geonet.sitnotebook.changes.ChangeRecord;
import nz.org.geonet.sitnotebook.data.NotebookDAO;
import nz.org.geonet.sitnotebook.data.TestingDAO;


public class MainActivity extends AppCompatActivity {

    private static final int SITECODE_SEARCH_RETURN = 922;
    private static final int NEW_CHANGE_RETURN = 566;
    private static final int EDIT_CHANGE_RETURN = 782;

    ChangeRecord changeRecord;

    TextView tv_sitecode;
    Toolbar toolbar;
    ListView lv;
    Button b;
    FloatingActionButton fab;
    NotebookDAO dao;

    ArrayList<String> sitecodes;
    List<String> changeStrings;
    ArrayAdapter<String> change_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_sitecode = (TextView) findViewById(R.id.sitecode_tv);
        lv = (ListView) findViewById(R.id.lv);
        b = (Button) findViewById(R.id.button);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        dao = new TestingDAO();
        changeRecord = new ChangeRecord();
        changeRecord.setUsername(dao.getUsername());
        changeStrings = changeRecord.getDisplayList();

        sitecodes = dao.getValidSiteCodes();

        change_adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, changeStrings);
        lv.setAdapter(change_adapter);
        generateChangeList();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewChangeActivity.class);
                startActivityForResult(intent, NEW_CHANGE_RETURN);
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchList.class);
                intent.putStringArrayListExtra("SITECODES", sitecodes);
                startActivityForResult(intent, SITECODE_SEARCH_RETURN);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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

        switch (id) {
            case R.id.save_menu:
                if (changeRecord.getSitecode().length() == 0){
                    noticeDialog("You need to select a site before saving.", "Missing Sitecode");
                    return true;
                }
                if (changeRecord.size() == 0) {
                    noticeDialog("You cannot save a change record with no changes.", "No Changes");
                    return true;
                }
                dao.saveChangeRecord(changeRecord);
                break;
            case R.id.reset_menu:
                new AlertDialog.Builder(this)
                        .setTitle("Reset Change Record")
                        .setMessage("This will clear all currently recorded changes. Continue?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                changeRecord.clear();
                                generateChangeList();
                                tv_sitecode.setText("Please Select");
                                Toast.makeText(MainActivity.this, "Change Record Reset", Toast.LENGTH_SHORT).show();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            return;
        }

        switch(requestCode){
            case SITECODE_SEARCH_RETURN:
                String code = data.getStringExtra("RETURN_SITECODE");
                tv_sitecode.setText(code);
                changeRecord.setSitecode(code);
                break;
            case NEW_CHANGE_RETURN:
                Change newChange = data.getParcelableExtra("CHANGE");
                changeRecord.addChange(newChange);
                generateChangeList();
                break;
            case EDIT_CHANGE_RETURN:
                Change editChange = data.getParcelableExtra("CHANGE");
                changeRecord.update( data.getIntExtra("EDIT", -1), editChange);
                generateChangeList();
                break;
        }
    }
}
