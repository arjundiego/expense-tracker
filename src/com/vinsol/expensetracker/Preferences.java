/**
 * Copyright (c) 2012 Vinayak Solutions Private Limited 
 * See the file license.txt for copying permission.
*/     

package com.vinsol.expensetracker;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.view.Window;

import com.flurry.android.FlurryAgent;
import com.vinsol.expensetracker.helpers.SharedPreferencesHelper;
import com.vinsol.expensetracker.sync.LoginType;
import com.vinsol.expensetracker.utils.Strings;

public class Preferences extends PreferenceActivity {
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_key));
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getWindow().setBackgroundDrawableResource(R.color.white);
		getListView().setBackgroundColor(Color.TRANSPARENT);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		final CheckBoxPreference mCheckBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_key_delete_dialog));
		mCheckBoxPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("Checked ", ""+mCheckBoxPreference.isChecked());
				FlurryAgent.onEvent(getString(R.string.preference_delete_dialog),map);
				return true;
			}
		});
		
//		final Preference syncSetUpPref = findPreference(getString(R.string.pref_key_set_up_sync));
//		String token = SharedPreferencesHelper.getSharedPreferences().getString(getString(R.string.pref_key_token), null);
//		if(Strings.isEmpty(token)) {
//			syncSetUpPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//				
//				@Override
//				public boolean onPreferenceClick(Preference preference) {
//					startActivity(new Intent(Preferences.this, LoginType.class));
//					return true;
//				}
//			});
//		} else {
//			uiAfterSync(syncSetUpPref);
//		}
	}
	
//	private void uiAfterSync(Preference syncSetUpPref) {
//		String name = SharedPreferencesHelper.getSharedPreferences().getString(getString(R.string.pref_key_sync_name), "");
//		String email = SharedPreferencesHelper.getSharedPreferences().getString(getString(R.string.pref_key_sync_email), "");
//		syncSetUpPref.setTitle("Sync Details:");
//		syncSetUpPref.setSummary(Html.fromHtml("<font color=\"#6E6E6E\">"+name+"<br>"+email+"</font>"));
//	}
	
	
}
