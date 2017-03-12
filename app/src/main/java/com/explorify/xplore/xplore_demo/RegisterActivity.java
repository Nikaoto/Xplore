package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by nikao on 3/11/2017.
 */

public class RegisterActivity extends Activity {

    private int bYear, bMonth, bDay;
    public static FirebaseUser newUser;
    public static DatePickerDialog.OnDateSetListener myDateSetListener;

    private final int LIMIT_AGE = 16;

    private EditText fnameTxt, lnameTxt, emailTxt, telnumTxt;
    private TextView birthDate;
    private ImageView profileImg;
    private Button doneBtn;
    private Long tempTimeStamp;
    private UploadUser tempUser;

    DatabaseReference DBref = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        setupDateSetListener();
        getServerEpoch();

        //Init
        fnameTxt = (EditText) findViewById(R.id.reg_fname_text);
        lnameTxt = (EditText) findViewById(R.id.reg_lname_text);
        emailTxt = (EditText) findViewById(R.id.reg_email_text);
        telnumTxt = (EditText) findViewById(R.id.reg_telnum_text);
        doneBtn = (Button) findViewById(R.id.reg_done_button);

        profileImg = (ImageView) findViewById(R.id.reg_profile_image);
        //TODO CHOOSE GALLERY IMAGE OR TAKE PHOTO
        //TODO AI check for face in photo?

        birthDate = (TextView) findViewById(R.id.reg_birthdate_text);
        birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        //Loading user data
        tempUser = createTempUser();

        //Loading data into views
        fnameTxt.setText(tempUser.getFname());
        lnameTxt.setText(tempUser.getLname());
        emailTxt.setText(tempUser.getEmail());
        Picasso.with(RegisterActivity.this)
                .load(tempUser.getProfile_picture_url())
                .transform(new RoundedCornersTransformation(
                        getResources().getInteger(R.integer.pic_big_angle),
                        getResources().getInteger(R.integer.pic_big_margin)))
                .into(profileImg);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFields()) {
                    tempUser.setBirth_date(General.GetDateLong(bYear,bMonth,bDay));
                    tempUser.setTel_num(telnumTxt.getText().toString());
                    //TODO upload image to db if changed
                    addUserEntryToDataBase(tempUser);
                }
            }
        });
    }

    private UploadUser createTempUser()
    {
        String photoUrl = " ";
        if(newUser.getPhotoUrl() != null)
            photoUrl = newUser.getPhotoUrl().toString();

        String fullName = newUser.getDisplayName();

        String[] name = {fullName,"."};
        if(fullName.contains(" "))
            name = fullName.split(" ", 2);

        return new UploadUser(newUser.getUid(), name[0], name[1], photoUrl, newUser.getEmail());
    }

    private void addUserEntryToDataBase(UploadUser user)
    {
        Map<String, Object> userData = user.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + user.getUid(), userData);
        Log.println(Log.INFO, "BREJK", "uid: " + user.getUid());
        DBref.updateChildren(childUpdates);
        setResult(RESULT_OK);
        finish();
    }

    private class UploadUser extends User
    {
        String uid;

        public UploadUser(String uid, String fname, String lname, String profile_picture_url, String email) {
            this.uid = uid;
            this.fname = fname;
            this.lname = lname;
            this.profile_picture_url = profile_picture_url;
            this.age = 0;
            this.tel_num = " ";
            this.email = email;
            //this.tel_num = tel_num; //TODO add tel_num field that the user fills
            this.reputation = 0; //new user starts with 0 rep
        }

        public String getUid() {
            return uid;
        }

        @Exclude
        public Map<String, Object> toMap()
        {
            HashMap<String, Object> result = new HashMap<>();
            result.put("fname", this.fname);
            result.put("lname", this.lname);
            result.put("profile_picture_url", this.profile_picture_url);
            result.put("birth_date", this.birth_date);
            result.put("tel_num", this.tel_num);
            result.put("reputation", this.reputation);
            result.put("email", this.email);
            return result;
        }
    }

    private void setupDateSetListener() {
        myDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                if (General.isNetConnected(RegisterActivity.this)) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date(tempTimeStamp));
                    int nowYear = cal.get(Calendar.YEAR);
                    //int nowMonth = cal.get(Calendar.MONTH);
                    //int nowDay = cal.get(Calendar.DAY_OF_MONTH);

                    if ((nowYear - year) >= LIMIT_AGE) {
                        bYear = year;
                        bMonth = month + 1;
                        bDay = day;

                        birthDate.setText(bYear+"/"+bMonth+"/"+bDay);
                    } else
                        Toast.makeText(RegisterActivity.this, "You must be of age " + LIMIT_AGE + " to use Xplore", Toast.LENGTH_SHORT).show(); //TODO string resources
                } else
                    General.createNetErrorDialog(RegisterActivity.this);
            }
        };
    }

    private void getServerEpoch()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> dateValue = new HashMap<>();
        dateValue.put("timestamp", ServerValue.TIMESTAMP);
        ref.child("date").setValue(dateValue);
        Query query = ref.child("date").child("timestamp");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tempTimeStamp = dataSnapshot.getValue(Long.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private boolean checkFields()
    {
        //TODO check fields
        return true;
    }
}
