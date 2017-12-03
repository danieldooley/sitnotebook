package nz.org.geonet.sitnotebook.changes;

import android.os.Parcel;
import android.os.Parcelable;

import nz.org.geonet.sitnotebook.changeActivities.EquipmentChangeActivity;

/**
 * Created by ddooley on 29/11/17.
 */

public class EquipmentChange implements Change {

    private String change_type;

    private String asset_num;
    private String serial_num;
    private String manufacturer;
    private String model;
    private String date;

    EquipmentChange(){}

    public EquipmentChange(String change_type, String asset_num, String serial_num, String manufacturer, String model, String date) {
        this.change_type = change_type;
        this.asset_num = asset_num;
        this.serial_num = serial_num;
        this.manufacturer = manufacturer;
        this.model = model;
        this.date = date;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(change_type);
        parcel.writeString(asset_num);
        parcel.writeString(serial_num);
        parcel.writeString(manufacturer);
        parcel.writeString(model);
        parcel.writeString(date);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<EquipmentChange> CREATOR = new Parcelable.Creator<EquipmentChange>() {
        public EquipmentChange createFromParcel(Parcel in) {
            return new EquipmentChange(in);
        }

        public EquipmentChange[] newArray(int size) {
            return new EquipmentChange[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private EquipmentChange(Parcel in) {
        change_type = in.readString();
        asset_num = in.readString();
        serial_num = in.readString();
        manufacturer = in.readString();
        model = in.readString();
        date = in.readString();
    }


    @Override
    public String getDisplayString() {
        return change_type.substring(0, 1).toUpperCase() + change_type.substring(1).toLowerCase() + " Equipment: " + serial_num;
    }

    @Override
    public String generateMarkdownFragment() {
        StringBuilder sb = new StringBuilder();
        sb.append("## Equipment - " + change_type + ":\n");

        if (asset_num.length() > 0) {
            sb.append("**Asset Number:** `" + (asset_num.length() > 0 ? asset_num : "n/a") + "`\n");
        }
        sb.append("**Serial Number:** `" + serial_num + "`\n");
        sb.append("**Manufacturer:** `" + manufacturer + "`\n");
        sb.append("**Model:** `" + model + "`\n");

        switch (change_type){
            case "REMOVE":
                sb.append("**Date Removed:** `" + date + "`\n");
                break;
            case "INSTALL":
                sb.append("**Date Installed:** `" + date + "`\n");
        }
        return sb.toString();
    }

    @Override
    public Class getEditingClass() {
        return EquipmentChangeActivity.class;
    }

    @Override
    public String getChangeType() {
        return change_type;
    }

    public String getAsset_num() {
        return asset_num;
    }

    public String getSerial_num() {
        return serial_num;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getDate() {
        return date;
    }
}
