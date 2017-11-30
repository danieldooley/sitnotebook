package nz.org.geonet.sitnotebook.changes;

import android.os.Parcelable;

/**
 * Created by ddooley on 28/11/17.
 */

public interface Change extends Parcelable {

    String getDisplayString();
    String generateMarkdownFragment();
    Class getEditingClass();

}
