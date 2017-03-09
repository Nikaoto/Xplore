package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * Created by nikao on 3/8/2017.
 */

public class SignInActivity extends Activity {

    Button googleSignIn_b;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_layout);

        googleSignIn_b = (Button) findViewById(R.id.signin_google_button);
        googleSignIn_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sign into google
            }
        });
    }
}
