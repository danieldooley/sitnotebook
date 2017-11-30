package nz.org.geonet.sitnotebook.changes;

import android.os.Parcel;
import android.os.Parcelable;

import nz.org.geonet.sitnotebook.NoteChangeActivity;

/**
 * Created by ddooley on 30/11/17.
 */

public class NoteChange implements Change {

    private String note;

    public NoteChange(String note) {
        this.note = note;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(note);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<NoteChange> CREATOR = new Parcelable.Creator<NoteChange>() {
        public NoteChange createFromParcel(Parcel in) {
            return new NoteChange(in);
        }

        public NoteChange[] newArray(int size) {
            return new NoteChange[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private NoteChange(Parcel in) {
        note = in.readString();
    }


    @Override
    public String getDisplayString() {
        return "Note: " + note.substring(0, Math.min(40, note.length())) + "...";
    }

    @Override
    public String generateMarkdownFragment() {
        StringBuilder sb = new StringBuilder();
        sb.append("## Note:\n");
        sb.append("```\n" + note + "\n```\n");
        return sb.toString();
    }

    public String getNote() {
        return note;
    }

    @Override
    public Class getEditingClass() {
        return NoteChangeActivity.class;
    }
}
