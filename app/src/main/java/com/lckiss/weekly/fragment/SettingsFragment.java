package com.lckiss.weekly.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;

import com.lckiss.weekly.R;
import com.lckiss.weekly.UpdateManager;

/**
 * Created by root on 17-7-9.
 */

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "info";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.setting);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey() == null) {
            return false;
        }

        switch (preference.getKey()) {
            case "about":
                new AboutDialog(getActivity()).show();
                break;

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class AboutDialog extends AlertDialog.Builder {

        AboutDialog(Context context) {
            super(context);
            View v = getActivity().getLayoutInflater().inflate(R.layout.about_item, null);
            setTitle("更新日志");
            setNegativeButton("检查更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new UpdateManager(getActivity(),true);
                }
            });
            setPositiveButton("确定", null);
            setView(v);
        }
    }


}
