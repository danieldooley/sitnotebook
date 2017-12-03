package nz.org.geonet.sitnotebook.changeActivities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import nz.org.geonet.sitnotebook.R;
import nz.org.geonet.sitnotebook.changes.EquipmentChange;

public class EquipmentChangeActivity extends ChangeActivity<EquipmentChange> {

    TextInputLayout equipment_date_layout;
    TextInputEditText asset, serial, manufacturer, model, equipment_date;

    Button now_button, date_picker_button, done_button;

    Date selectedDate = new Date();

    Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_equipment_change;
        super.onCreate(savedInstanceState);

        setTitle("Equipment Change - " + changeType);

        now_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedDate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
                format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
                equipment_date.setText(format.format(selectedDate));
            }
        });

        date_picker_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar tempC = Calendar.getInstance();
                new DatePickerDialog(EquipmentChangeActivity.this, date, tempC.get(Calendar.YEAR), tempC.get(Calendar.MONTH), tempC.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        switch (changeType) {
            case "REMOVE":
                equipment_date_layout.setHint("Removal Date");
                break;
            case "INSTALL":
                equipment_date_layout.setHint("Install Date");
                break;
        }
    }

    @Override
    void findViews() {
        asset = findViewById(R.id.asset);
        serial = findViewById(R.id.serial);
        manufacturer = findViewById(R.id.manufacturer);
        model = findViewById(R.id.model);
        equipment_date = findViewById(R.id.equipment_date);
        equipment_date.setEnabled(false);
        equipment_date_layout = findViewById(R.id.equipment_date_layout);
        now_button = findViewById(R.id.now);
        date_picker_button = findViewById(R.id.select_date);
        done_button = findViewById(R.id.done_button);
    }

    @Override
    void setupEdit(EquipmentChange ec) {
        asset.setText(ec.getAsset_num());
        serial.setText(ec.getSerial_num());
        manufacturer.setText(ec.getManufacturer());
        model.setText(ec.getModel());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        try {
            selectedDate = format.parse(ec.getDate());
            SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
            format2.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            equipment_date.setText(format2.format(selectedDate));
        } catch (Exception e) {
            selectedDate = new Date();
        }
    }

    @Override
    EquipmentChange createChange() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return new EquipmentChange(changeType, asset.getText().toString(),
                serial.getText().toString(),
                manufacturer.getText().toString(),
                model.getText().toString(),
                format.format(selectedDate));
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            c = new GregorianCalendar();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Calendar tempC = Calendar.getInstance();
            new TimePickerDialog(EquipmentChangeActivity.this, time, tempC.get(Calendar.HOUR), tempC.get(Calendar.MINUTE), true).show();
        }

    };

    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            c.set(Calendar.HOUR, i);
            c.set(Calendar.MINUTE, i1);
            selectedDate = c.getTime();
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");
            format.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            equipment_date.setText(format.format(selectedDate));
        }
    };

    public boolean validate() {
        if (serial.getText().length() == 0) {
            noticeDialog("Please provide a serial number.", "Missing Serial Number");
            return false;
        }
        if (manufacturer.getText().length() == 0) {
            noticeDialog("Please provide a manufacturer.", "Missing Make");
            return false;
        }
        if (model.getText().length() == 0) {
            noticeDialog("Please provide a model.", "Missing Model");
            return false;
        }
        if (equipment_date.getText().length() == 0) {
            noticeDialog("Please select a date.", "Missing Date");
            return false;
        }
        return true;
    }

    public void noticeDialog(String message, String title) {
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
