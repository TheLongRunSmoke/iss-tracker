package ru.tlrs.iss.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;

import ru.tlrs.iss.R;
import ru.tlrs.iss.dialogs.DialogHelper;
import ru.tlrs.iss.fragments.PreferenceListenerFragment;

import static java.lang.Math.abs;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        // Show multipane for 7 inch and more.
        return (this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_LARGE) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || LocationPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class LocationPreferenceFragment extends PreferenceListenerFragment{

        public static final String LAT = "latitude";
        public static final String LONG = "longitude";
        public static final String UPDATE = "location_update";


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_location, false);
            addPreferencesFromResource(R.xml.pref_location);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference(LAT), this);
            bindPreferenceSummaryToValue(findPreference(LONG), this);
            bindPreferenceSummaryToValue(findPreference(UPDATE), this);
        }

        @Override
        public boolean onPreferenceChange(final Preference preference, Object newValue) {
            boolean result = true;
            String value = newValue.toString();
            if (TextUtils.equals(preference.getKey(), LAT)) {
                double absValue = abs(Double.parseDouble(value));
                if (absValue >= 82 || absValue == 0) {
                    result = false;
                    DialogHelper.createOKDialog(getActivity(), R.string.latitude_error_message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            performClick(preference);
                        }
                    }).show();
                }
            }else if (TextUtils.equals(preference.getKey(), LONG)){
                double absValue = abs(Double.parseDouble(value));
                if (abs(Double.parseDouble(value)) >= 180 || absValue == 0) {
                    result = false;
                    DialogHelper.createOKDialog(getActivity(), R.string.longitude_error_message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            performClick(preference);
                        }
                    }).show();
                }
            }
            if (result) super.onPreferenceChange(preference, newValue);
            return result;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class NotificationPreferenceFragment extends PreferenceListenerFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"), this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
