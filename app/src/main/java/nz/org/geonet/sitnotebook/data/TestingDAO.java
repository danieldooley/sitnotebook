package nz.org.geonet.sitnotebook.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import nz.org.geonet.sitnotebook.changes.ChangeRecord;

/**
 * Created by ddooley on 28/11/17.
 */

public class TestingDAO implements NotebookDAO {

    @Override
    public ArrayList<String> getValidSiteCodes() {
        ArrayList<String> output = new ArrayList<>();

        output.add("WARK");
        output.add("YALD");
        output.add("TEST");
        output.add("AVLN");
        output.add("HATZ");
        output.add("RATZ");
        output.add("CLIM");
        output.add("WEL");
        output.add("WINR");
        output.add("TCEA");
        output.add("RIM");

        return output;
    }

    @Override
    public void saveChangeRecord(ChangeRecord cr) {
        String markdown = cr.generateMarkdown();
        Log.i("OUTPUT", "\n" + markdown);
    }

    @Override
    public String getUsername() {
        return "TEST";
    }
}
