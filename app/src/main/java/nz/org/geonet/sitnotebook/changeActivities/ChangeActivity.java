package nz.org.geonet.sitnotebook.changeActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import nz.org.geonet.sitnotebook.R;
import nz.org.geonet.sitnotebook.changes.Change;

/**
 * Created by ddooley on 1/12/17.
 */

public abstract class ChangeActivity<C extends Change> extends AppCompatActivity {

    int layout;

    String changeType;

    int editmode;
    
    Intent intent;

    Button done_button;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        intent = getIntent();

        done_button = findViewById(R.id.done_button);
        findViews();

        editmode = intent.getIntExtra("EDIT", -1);
        if (editmode >= 0){
            C c = intent.getParcelableExtra("EDIT_CHANGE");
            changeType = c.getChangeType();
            setupEdit(c);
        } else {
            changeType = intent.getStringExtra("CHANGE_TYPE");
        }

        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()){
                    return;
                }
                C c = createChange();
                Intent result = new Intent();
                result.putExtra("CHANGE", c);
                result.putExtra("EDIT", editmode);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
    }

    public void noticeDialog(String message, String title){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton("Ok", null).show();
    }
    
    abstract boolean validate();

    abstract void findViews();
    
    abstract void setupEdit(C c);
    
    abstract C createChange();

    @Override
    public void onBackPressed() {
        Intent cancel = new Intent();
        setResult(RESULT_CANCELED, cancel);
        super.onBackPressed();
    }
}
