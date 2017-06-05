package ru.tlrs.iss.fragments;

import android.content.Intent;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.ListAdapter;

import javax.inject.Inject;

import ru.tlrs.iss.App;
import ru.tlrs.iss.Config;
import ru.tlrs.iss.activities.SettingsActivity;
import ru.tlrs.iss.di.AppModule;

public class PreferenceListenerFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Inject protected Config config;

    public PreferenceListenerFragment() {
        super();
        App.getComponent().inject(this);
    }

    protected void performClick(Preference preference){
        ListAdapter listAdapter = getPreferenceScreen().getRootAdapter();
        for (int itemNumber = 0; itemNumber < listAdapter.getCount(); itemNumber++)
            if (listAdapter.getItem(itemNumber).equals(preference))
                getPreferenceScreen().onItemClick(null, null, itemNumber, 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(
                    index >= 0 ? listPreference.getEntries()[index] : null);
        } else {
            preference.setSummary(stringValue);
        }
        return true;

        /*String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;*/

    }

    protected static void bindPreferenceSummaryToValue(Preference preference, Preference.OnPreferenceChangeListener listener) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference, App.getComponent().getPreferences().getString(preference.getKey(), ""));
    }

    public void updatePreferenceSummary(Preference preference){
        onPreferenceChange(preference, config.getPreferences().getString(preference.getKey(), ""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
