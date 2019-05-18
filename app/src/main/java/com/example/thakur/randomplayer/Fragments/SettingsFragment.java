

package com.example.thakur.randomplayer.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATECheckBoxPreference;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.example.thakur.randomplayer.BaseActivity.SettingsActivity;
import com.example.thakur.randomplayer.MyApp;
import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.Utilities.PreferencesUtility;
import com.example.thakur.randomplayer.Utilities.UserPreferenceHandler;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String NOW_PLAYING_SELECTOR = "now_playing_selector";
    private static final String LASTFM_LOGIN = "lastfm_login";

    private static final String LOCKSCREEN = "show_albumart_lockscreen";
    private static final String XPOSED = "toggle_xposed_trackselector";

    private static final String SHAKE_DETECTOR = "shake_listener";
    private static final String KEY_ABOUT = "preference_about";
    private static final String KEY_SOURCE = "preference_source";
    private static final String KEY_THEME = "theme_preference";
    private static final String TOGGLE_ANIMATIONS = "toggle_animations";
    private static final String TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations";
    private static final String KEY_START_PAGE = "start_page_preference";
    private boolean lastFMlogedin;

    Preference nowPlayingSelector,  lastFMlogin, lockscreen, xposed;

    SwitchPreference toggleAnimations;
    ListPreference themePreference, startPagePreference;
    PreferencesUtility mPreferences;
    private String mAteKey;
    private UserPreferenceHandler pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = new UserPreferenceHandler(getActivity());

        addPreferencesFromResource(R.xml.preferences);

        mPreferences = PreferencesUtility.getInstance(getActivity());

        lockscreen = findPreference(LOCKSCREEN);
        nowPlayingSelector = findPreference(NOW_PLAYING_SELECTOR);

        xposed = findPreference(XPOSED);

        lastFMlogin = findPreference(LASTFM_LOGIN);
       // updateLastFM();
//        themePreference = (ListPreference) findPreference(KEY_THEME);
        startPagePreference = (ListPreference) findPreference(KEY_START_PAGE);

        //nowPlayingSelector.setIntent(NavigationUtils.getNavigateToStyleSelectorIntent(getActivity(), Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING));

        setPreferenceClickListeners();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
    }

    private void setPreferenceClickListeners() {

//        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//                return true;
//            }
//        });











    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invalidateSettings();
        ATE.apply(view, mAteKey);


    }

    public void invalidateSettings() {
        //mAteKey = ((SettingsActivity) getActivity()).getATEKey();

//        ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
//        primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
//        primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
//                        .preselect(Config.primaryColor(getActivity(), mAteKey))
//                        .show(((SettingsActivity) getActivity()).getSupportFragmentManager());
//                return true;
//            }
//        });

//        ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
//        accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
//        accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
//                        .preselect(Config.accentColor(getActivity(), mAteKey))
//                        .show(((SettingsActivity) getActivity()).getSupportFragmentManager());
//                return true;
//            }
//        });


//        findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                // Marks both theme configs as changed so MainActivity restarts itself on return
//                Config.markChanged(getActivity(), "light_theme");
//                Config.markChanged(getActivity(), "dark_theme");
//                PreferencesUtility.getInstance(getActivity()).setIsDarkTheme((Boolean) newValue);
//                // The dark_theme preference value gets saved by Android in the default PreferenceManager.
//                // It's used in getATEKey() of both the Activities.
//                getActivity().recreate();
//                return true;
//            }
//        });

        //final ATECheckBoxPreference statusBarPref = (ATECheckBoxPreference) findPreference("colored_status_bar");
        //final ATECheckBoxPreference navBarPref = (ATECheckBoxPreference) findPreference("colored_nav_bar");
        final ATECheckBoxPreference shakedetector = (ATECheckBoxPreference) findPreference("shake_listener");
        final ATECheckBoxPreference colored_control = (ATECheckBoxPreference) findPreference("palette_control");

//        statusBarPref.setChecked(Config.coloredStatusBar(getActivity(), mAteKey));
//        statusBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                ATE.config(getActivity(), mAteKey)
//                        .coloredStatusBar((Boolean) newValue)
//                        .apply(getActivity());
//                return true;
//            }
//        });
//
//
//        navBarPref.setChecked(Config.coloredNavigationBar(getActivity(), mAteKey));
//        navBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                ATE.config(getActivity(), mAteKey)
//                        .coloredNavigationBar((Boolean) newValue)
//                        .apply(getActivity());
//                return true;
//            }
//        });

        shakedetector.setChecked(pref.getHearShake());
        shakedetector.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                pref.setHearShake((Boolean) o);
                MyApp.getMyService().setShakeListener(pref.getHearShake());
                return true;
            }
        });


        colored_control.setChecked(PreferencesUtility.getInstance(getActivity()).isColoredPlayingControl());
        colored_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                PreferencesUtility.getInstance(getActivity()).setColoredPlayingControl((Boolean) o);
                return true;
            }
        });

    }



}
