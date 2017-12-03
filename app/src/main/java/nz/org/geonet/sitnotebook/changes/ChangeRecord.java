package nz.org.geonet.sitnotebook.changes;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ddooley on 28/11/17.
 */

public class ChangeRecord {

    private String sitecode;
    private String username;
    private List<Change> changes;

    private String timestamp;

    public ChangeRecord() {
        sitecode = "";
        username = "";
        changes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm");
        timestamp = sdf.format(new Date());
    }

    public String getSitecode() {
        return sitecode;
    }

    public void setSitecode(String sitecode) {
        this.sitecode = sitecode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addChange(Change c) {
        changes.add(c);
    }

    public List<String> getDisplayList(){
        List<String> dispList = new ArrayList<>();
        for (Change c : changes){
            dispList.add(c.getDisplayString());
        }
        return dispList;
    }

    public int size(){
        return changes.size();
    }

    public Change getChange(int i) {
        return changes.get(i);
    }

    public void removeChange(Change c) {
        changes.remove(c);
    }

    public void clear() {
        changes.clear();
    }

    public void update(int i, Change c){
        changes.remove(i);
        changes.add(i, c);
    }

    public String getFilename() {
        return sitecode + "_" + username.replace(" ", "_").replace(".", "") + timestamp + ".md";
    }

    public String generateFile() {
        StringBuilder sb = new StringBuilder();

        sb.append("# Changes to " + sitecode + " by " + username + "\n");
        sb.append("\n");
        sb.append("###### Change Record Generated: " + SimpleDateFormat.getDateInstance().format(new Date()));
        sb.append("\n\n");

        for (Change c : changes) {
            sb.append(c.generateMarkdownFragment());
            sb.append("\n");
        }

        return sb.toString();
    }
}
