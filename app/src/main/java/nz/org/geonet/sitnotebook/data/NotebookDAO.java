package nz.org.geonet.sitnotebook.data;

import java.util.ArrayList;

import nz.org.geonet.sitnotebook.changes.ChangeRecord;

/**
 * Created by ddooley on 28/11/17.
 */

public interface NotebookDAO {

    ArrayList<String> getValidSiteCodes();
    void saveChangeRecord(ChangeRecord cr);
    String getUsername();
    
}
