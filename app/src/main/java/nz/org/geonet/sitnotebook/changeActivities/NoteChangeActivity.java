package nz.org.geonet.sitnotebook.changeActivities;

import android.support.design.widget.TextInputEditText;
import android.os.Bundle;

import nz.org.geonet.sitnotebook.R;
import nz.org.geonet.sitnotebook.changes.NoteChange;

public class NoteChangeActivity extends ChangeActivity<NoteChange> {

    TextInputEditText note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_note_change;
        super.onCreate(savedInstanceState);

        setTitle("Notes");
    }

    @Override
    void findViews() {
        note = findViewById(R.id.note_text);
    }

    @Override
    public boolean validate() {
        if (note.getText().length() == 0) {
            noticeDialog("Please enter a note", "Empty Note");
            return false;
        }
        return true;
    }

    @Override
    void setupEdit(NoteChange change) {
        note.setText(change.getNote());
    }

    @Override
    NoteChange createChange() {
        return new NoteChange(note.getText().toString());
    }
}
