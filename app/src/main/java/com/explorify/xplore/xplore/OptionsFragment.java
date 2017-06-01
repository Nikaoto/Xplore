package com.explorify.xplore.xplore;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Locale;

public class OptionsFragment extends Fragment implements View.OnClickListener
{
    View myView;
    ImageView GeoButton;
    ImageView EngButton;
    ImageView RusButton;

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.options_layout, container, false);
        GeoButton = (ImageView) myView.findViewById(R.id.GeoFlag);
        GeoButton.setOnClickListener(this);
        EngButton = (ImageView) myView.findViewById(R.id.EngFlag);
        EngButton.setOnClickListener(this);
        RusButton = (ImageView) myView.findViewById(R.id.RusFlag);
        RusButton.setOnClickListener(this);

        DisableChosenLanguageButton();

        return myView;
    }

    // change this into a list after adding 5+ languages
    public void DisableChosenLanguageButton()
    {
        String sharedPrefs = getActivity().getSharedPreferences("lang",0).getString("lang","en");

        if(sharedPrefs.equals("ka"))
        {
            GeoButton.setBackgroundResource(R.drawable.flag_image_border);

            GeoButton.setEnabled(false);
            EngButton.setEnabled(true);
            RusButton.setEnabled(true);
        }
        else if(sharedPrefs.equals("en"))
        {
            EngButton.setBackgroundResource(R.drawable.flag_image_border);

            GeoButton.setEnabled(true);
            EngButton.setEnabled(false);
            RusButton.setEnabled(true);
        }

        else if(sharedPrefs.equals("ru"))
        {
            RusButton.setBackgroundResource(R.drawable.flag_image_border);

            GeoButton.setEnabled(true);
            EngButton.setEnabled(true);
            RusButton.setEnabled(false);
        }
    }

    public void ChangeLocale(String language_code)
    {
        preferences = getActivity().getSharedPreferences("lang",0);
        prefEditor = preferences.edit();

        Resources res = getActivity().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        Configuration config = res.getConfiguration();
        prefEditor.putString("lang",language_code);
        prefEditor.commit();

        Locale locale = new Locale(preferences.getString("lang",MainActivity.ENGLISH_LANG_CODE));
        Locale.setDefault(locale);

       /* if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //TODO enable this when out of alpha
            setSystemLocale(config, locale);
        }
        else {*/
            setSystemLocaleLegacy(config,locale, res, dm);
        //}
        //conf.locale = new Locale(preferences.getString("lang", MainActivity.ENGLISH_LANG_CODE));
        //res.updateConfiguration(conf, dm);
        MainActivity.RefreshApplication(getActivity());
        DisableChosenLanguageButton();
        MainActivity.manageBackStack(getFragmentManager());
        //getActivity().recreate();
    }

    @SuppressWarnings("deprecation")
    public void setSystemLocaleLegacy(Configuration c, Locale l, Resources r, DisplayMetrics d){
        c.locale = l; //TODO createConfigurationContext after API 16 gets ooooold
        r.updateConfiguration(c,d);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void setSystemLocale(Configuration c, Locale l){
        c.setLocale(l);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.GeoFlag:
                ChangeLocale(MainActivity.GEORGIAN_LANG_CODE);
                break;
            case R.id.EngFlag:
                ChangeLocale(MainActivity.ENGLISH_LANG_CODE);
                break;
            case R.id.RusFlag:
                ChangeLocale(MainActivity.RUSSIAN_LANG_CODE);
                break;

        }
    }
}
