package nz.org.geonet.sitnotebook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import nz.org.geonet.sitnotebook.changeActivities.EquipmentChangeActivity;
import nz.org.geonet.sitnotebook.changeActivities.NoteChangeActivity;


public class NewChangeActivity extends AppCompatActivity {

    private static final int CHANGE_REQUEST_CODE = 97;

    String sitecode;

    ListView change_list;

    static final String[] change_types = {"Equipment - Install", "Equipment - Remove", "Other - Note"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_change);

        Intent intent = getIntent();
        change_list = (ListView) findViewById(R.id.change_list);
        ArrayAdapter<String> change_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, change_types);
        change_list.setAdapter(change_adapter);

        setTitle("New Change");

        change_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = change_types[i];

                String[] split = selection.split(" - ");

                Intent newIntent;
                switch (split[0].toUpperCase()){
                    case "EQUIPMENT":
                        newIntent = new Intent(NewChangeActivity.this, EquipmentChangeActivity.class);
                        newIntent.putExtra("CHANGE_TYPE", split[1].toUpperCase());
                        startActivityForResult(newIntent, CHANGE_REQUEST_CODE);
                        break;
                    case "OTHER":
                            switch (split[1].toUpperCase()){
                                case "NOTE":
                                    newIntent = new Intent(NewChangeActivity.this, NoteChangeActivity.class);
                                    startActivityForResult(newIntent, CHANGE_REQUEST_CODE);
                                    break;
                            }
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHANGE_REQUEST_CODE:
                setResult(resultCode, data);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent cancel = new Intent();
        setResult(RESULT_CANCELED, cancel);
        super.onBackPressed();
    }
}
