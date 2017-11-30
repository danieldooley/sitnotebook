package nz.org.geonet.sitnotebook;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchList extends AppCompatActivity {

    Intent intent;
    ArrayList<String> origlist;
    ArrayList<String> displaylist;
    ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_list);

        final Intent intent = getIntent();

        setTitle("Site Code Search");

        origlist = intent.getStringArrayListExtra("SITECODES");
        displaylist = new ArrayList<>();

        displaylist.addAll(origlist);

        lv = (ListView) findViewById(R.id.search_result);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displaylist);
        lv.setAdapter(adapter);

        TextInputEditText text = (TextInputEditText) findViewById(R.id.site_search);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                displaylist.clear();
                for (String s : origlist) {
                    if (s.contains(charSequence)){
                        displaylist.add(s);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String code = (String) lv.getItemAtPosition(i);
                Intent result = new Intent();
                result.putExtra("RETURN_SITECODE", code);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
    }
}
