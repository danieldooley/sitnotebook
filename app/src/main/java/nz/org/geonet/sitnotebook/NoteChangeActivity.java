package nz.org.geonet.sitnotebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import nz.org.geonet.sitnotebook.changes.NoteChange;

public class NoteChangeActivity extends AppCompatActivity {

    int editmode;

    Button done_button;
    TextInputEditText note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_change);

        note = findViewById(R.id.note_text);
        done_button = findViewById(R.id.done_button);

        Intent intent = getIntent();
        editmode = intent.getIntExtra("EDIT", -1);
        if (editmode >= 0){
            NoteChange nc = intent.getParcelableExtra("EDIT_CHANGE");
            note.setText(nc.getNote());
        }

        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()){
                    return;
                }
                NoteChange nc = new NoteChange(note.getText().toString());
                Intent result = new Intent();
                result.putExtra("CHANGE", nc);
                result.putExtra("EDIT", editmode);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

    }

    public boolean validate() {
        if (note.getText().length() == 0) {
            noticeDialog("Please enter a note", "Missing Serial Number");
            return false;
        }
        return true;
    }

    public void noticeDialog(String message, String title){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton("Ok", null).show();
    }

    @Override
    public void onBackPressed() {
        Intent cancel = new Intent();
        setResult(RESULT_CANCELED, cancel);
        super.onBackPressed();
    }
}
