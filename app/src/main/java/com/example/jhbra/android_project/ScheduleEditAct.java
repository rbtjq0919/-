package com.example.jhbra.android_project;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class ScheduleEditAct extends AppCompatActivity {

    public static final String ARG_SCHEDULE_ID = "SCHEDULE_ID";
    public static final String ARG_TARGET_TIMESTAMP = "TARGET_TIMESTAMP";

    @Nullable
    private Long mScheduleID;
    @NonNull
    private Date mTargetDate;
    @Nullable
    private Double mLongitude;
    @Nullable
    private Double mLatitude;
    @NonNull
    private String mTransport = "car";

    public ScheduleEditAct() {
        long millis = (System.currentTimeMillis() + 25 * 60 * 60 * 1000);
        // Discard time under hour
        millis -= millis % (60 * 60 * 1000);
        mTargetDate = new Date(millis);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm
                = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.editTextTitle).getWindowToken(), 0);
        imm.hideSoftInputFromWindow(findViewById(R.id.editTextMemo).getWindowToken(), 0);
    }

    private @Nullable
    ContentValues fetchScheduleByID() {
        Uri uri = ContentUris.withAppendedId(DBProvider.CONTENT_URI, mScheduleID);
        Log.d("ScheduleEditAct", "initViewsFromScheduleID: querying to "
                + uri.toString());

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(uri, null, null, null, null);

        if (cur != null) {
            if (cur.moveToFirst()) {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cur, cv);
                cur.close();
                Log.d("ScheduleEditAct", "initViewsFromScheduleID: just fetched schedule "
                        + String.valueOf(mScheduleID) + ": size of " + cv.size());
                return cv;
            } else {
                Log.e("ScheduleEditAct", "initViewsFromScheduleID: cannot fetch "
                        + "schedule " + String.valueOf(mScheduleID));
                cur.close();
            }
        } else {
            Log.e("ScheduleEditAct", "initViewsFromScheduleID: cursor is null fetching"
                    + String.valueOf(mScheduleID));
        }

        Toast.makeText(
                this,
                "일정을 불러올 수 없습니다.",
                Toast.LENGTH_LONG
        ).show();
        return null;
    }

    private boolean initValuesAndViews() {
        TextView textViewToolbar = (TextView) findViewById(R.id.textViewToolbar);
        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextMemo = (EditText) findViewById(R.id.editTextMemo);
        TextView textViewTargetDate = (TextView) findViewById(R.id.textViewTargetDate);
        TextView textViewTargetTimeAMPM = (TextView) findViewById(R.id.textViewTargetTimeAMPM);
        TextView textViewTargetTimeClock = (TextView) findViewById(R.id.textViewTargetTimeClock);

        if (mScheduleID != null) {
            // Set toolbar label
            textViewToolbar.setText("일정 수정");

            ContentValues cv = fetchScheduleByID();
            if (cv == null) {
                return false;
            }

            String title = cv.getAsString("title");
            editTextTitle.setText(title);

            long millis = cv.getAsLong("millis");
            mTargetDate = new Date(millis);

            try {
                double longitude = cv.getAsDouble("longitude");
                double latitude = cv.getAsDouble("latitude");
                mLongitude = Double.valueOf(longitude);
                mLatitude = Double.valueOf(latitude);
            } catch (NullPointerException e) {
            }

            String memo = cv.getAsString("memo");
            editTextMemo.setText(memo);
        } else {
            // Set toolbar label
            textViewToolbar.setText("일정 추가");
        }

        // Set date/time views
        String dateStr = DateFormat.format("yyyy-MM-dd", mTargetDate).toString();
        String ampmStr = DateFormat.format("aa", mTargetDate).toString();
        String timeStr = DateFormat.format("hh:mm", mTargetDate).toString();
        textViewTargetDate.setText(dateStr);
        textViewTargetTimeAMPM.setText(ampmStr);
        textViewTargetTimeClock.setText(timeStr);

        return true;
    }

    private @Nullable
    ContentValues buildContentValuesWithoutID() {
        ContentValues cv = new ContentValues();
        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        EditText editTextMemo = (EditText) findViewById(R.id.editTextMemo);

        String title = editTextTitle.getText().toString().trim();
        if ("".equals(title)) {
            Toast.makeText(
                    this,
                    "제목을 입력해주세요.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }
        cv.put("title", title);

        long millis = mTargetDate.getTime();
        cv.put("millis", millis);

        // TEST TODO test data
        cv.put("departure", millis - 10);

        if (mLongitude != null && mLatitude != null) {
            cv.put("longitude", mLongitude);
            cv.put("latitude", mLatitude);
        } else {
            cv.putNull("longitude");
            cv.putNull("latitude");
        }

        cv.put("transport", mTransport);

        String memo = editTextMemo.getText().toString().trim();
        cv.put("memo", memo);

        return cv;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            Log.i("ScheduleEditAct", "Intent bundle is given");
            // mScheduleID
            long scheduleID = args.getLong(ARG_SCHEDULE_ID, -1);
            if (scheduleID != -1) {
                mScheduleID = Long.valueOf(scheduleID);
            }
            // mTargetDate
            long millis = args.getLong(ARG_TARGET_TIMESTAMP);
            if (millis != 0) {
                // Discard time under hour
                millis -= millis % (60 * 60 * 1000);
                mTargetDate = new Date(millis);
            }
        } else {
            Log.i("ScheduleEditAct", "No intent bundle!");
        }

        // TEST TEST TEST TEST for loading
        // mScheduleID = 1L;

        // Log values
        if (mScheduleID != null) {
            Log.d("ScheduleEditAct", "onCreate: mScheduleID is "
                    + String.valueOf(mScheduleID));
        } else {
            Log.d("ScheduleEditAct", "onCreate: mScheduleID is null");
        }
        Log.d("ScheduleEditAct", "onCreate: mTargetTime is "
                + DateFormat.format("yyyy-MM-dd'T'HH:mm:ss", mTargetDate).toString());

        if (!initValuesAndViews()) {
            mScheduleID = null;
            initValuesAndViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onClickCancelOnToolbar(View v) {
        finish();
    }

    public void onClickSaveOnToolbar(View v) {
        hideSoftKeyboard();

        ContentValues cv = buildContentValuesWithoutID();
        if (cv == null) {
            return;
        }

        ContentResolver cr = getContentResolver();
        if (mScheduleID != null) {
            // Update mode
            Log.d("ScheduleEditAct", "onClickSaveOnToolbar: started to update "
                    + String.valueOf(mScheduleID));

            Uri uri = ContentUris.withAppendedId(DBProvider.CONTENT_URI, mScheduleID);
            cv.put("_id", mScheduleID);

            try {
                int count = cr.update(uri, cv, null, null);
                if (count < 0) {
                    throw new RuntimeException();
                }

                Log.d("ScheduleEditAct", "onClickSaveOnToolbar: just updated "
                        + uri);
                Toast.makeText(
                        this,
                        "일정을 수정했습니다!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } catch (Exception e) {
                Log.d("ScheduleEditAct", "onClickSaveOnToolbar: failed to update", e);
                Toast.makeText(
                        this,
                        "일정 수정에 실패했습니다!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        } else {
            // Insert mode
            Log.d("ScheduleEditAct", "onClickSaveOnToolbar: started to insert");

            try {
                Uri resultUri = cr.insert(DBProvider.CONTENT_URI, cv);
                if (resultUri == null) {
                    throw new RuntimeException();
                }

                Log.d("ScheduleEditAct", "onClickSaveOnToolbar: just inserted to "
                        + resultUri.toString());
                Toast.makeText(
                        this,
                        "일정을 추가했습니다!",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            } catch (Exception e) {
                Log.d("ScheduleEditAct", "onClickSaveOnToolbar: failed to insert", e);
                Toast.makeText(
                        this,
                        "일정 추가에 실패했습니다!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

}
