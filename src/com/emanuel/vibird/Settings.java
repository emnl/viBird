/*
 * Copyright (c) 2010, Emanuel Andersson
 * All rights reserved.
*/

package com.emanuel.vibird;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 * Settings activity
 * 
 * @author Emanuel Andersson
 * @version 0.1
 *
 */

public class Settings extends Activity 
{
	    
    /* Global textview holder */
    TextView tv;
	
    /**
     * Called when activity is created, (Android standard), handles UI and sets buttonlistner
     */
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.settingslayout);
        
        tv = (TextView)findViewById(R.id.status_settings_form);
        
        if(!Lists.connected)
        {
        	tv.setText("Status: Please Log in!");
        }
        else
        {
        	tv.setText("Status: Log in successful!");
        }
        
        /* Auth-button listner */
        
        OnClickListener submitListener = new OnClickListener() {
        	
        	/*
        	 * Sends the user to get pin-code and also saves the used request-data
        	 * @see android.view.View.OnClickListener#onClick(android.view.View)
        	 */
        	
    	    public void onClick(View v) {
    	    	
    	    	/* Open shared preferences db */
    	    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
    	        final SharedPreferences.Editor pEditor = prefs.edit();
    	    	
    	    	reset("Status: Please Log in!");
    	    	
    	    	Lists.twitter = new TwitterFactory().getInstance();
    	    	    	    	
    	    	Lists.twitter.setOAuthConsumer(Lists.CONSUMER_TOKEN, Lists.CONSUMER_TOKEN_SECRET);
    	        RequestToken requestToken;
    	        
    	        try 
				{
					requestToken = Lists.twitter.getOAuthRequestToken();
					
					pEditor.putString(Lists.REQUEST_TOKEN, requestToken.getToken());
					pEditor.putString(Lists.REQUEST_TOKEN_SECRET, requestToken.getTokenSecret());
					pEditor.commit();
					
		    	    Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(requestToken.getAuthorizationURL()));
		    	    startActivity(viewIntent);  
		    	    
					tv = (TextView)findViewById(R.id.pintext_form);
	    	        tv.setVisibility(1);
	    	        	
	    	        tv = (TextView)findViewById(R.id.pin_form);
	    	        tv.setVisibility(1);
	    	        
	    	        tv = (TextView)findViewById(R.id.submit2_settings_form);
	    	        tv.setVisibility(1);
	    	        
	    	        tv = (TextView)findViewById(R.id.submit_settings_form);
	    	        tv.setVisibility(4);

				} 
				catch (TwitterException te) 
				{
					new Alert(te, Settings.this);
				}
    	        
    	    }
    	};   
    	
    	/* Pin-button listner */
    	
    	OnClickListener submit2Listener = new OnClickListener() {
    		
    		/*
    		 * @see android.view.View.OnClickListener#onClick(android.view.View)
    		 */
    		
    	    public void onClick(View v) {
    	    	  	    	
    	    	new Authenticate().execute(); 
	    	        
    	    	}			

    	};
    	
    	/* Assign listners to buttons */
        
        Button smbutton = (Button)findViewById(R.id.submit_settings_form);
    	smbutton.setOnClickListener(submitListener);
    	
    	Button sm2button = (Button)findViewById(R.id.submit2_settings_form);
    	sm2button.setOnClickListener(submit2Listener);
                
        
    }
    
    /*
	 * Use collected data to get a accesskey
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
    
    public void authenticate(String pincode)
    {
    	
    	try
    	{
    		
    		/* Open shared preferences db */
    		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
	        final SharedPreferences.Editor pEditor = prefs.edit();
    		
    		Twitter tempTwitter = new TwitterFactory().getInstance();
    		
    		tempTwitter.setOAuthConsumer(Lists.CONSUMER_TOKEN, Lists.CONSUMER_TOKEN_SECRET);

	    	String rqt = prefs.getString(Lists.REQUEST_TOKEN, null);
	    	String rqts = prefs.getString(Lists.REQUEST_TOKEN_SECRET, null);
	    	    	        
	    	RequestToken rt = new RequestToken(rqt, rqts);
	        AccessToken accessToken = tempTwitter.getOAuthAccessToken(rt, pincode);
	        
	        pEditor.putString(Lists.ACCESS_TOKEN, accessToken.getToken());
	        pEditor.putString(Lists.ACCESS_TOKEN_SECRET, accessToken.getTokenSecret());
	        pEditor.commit();
	        
	        User us = tempTwitter.verifyCredentials();
	        Lists.username = us.getScreenName();
	        Lists.userId = us.getId();
	        Lists.connected = true;
	        
	        Lists.twitter = tempTwitter;
    	}
    	catch(TwitterException te)
    	{
    		Lists.connected = false;
    	}
    }
    
    /**
     * AsyncTask for authenticating (class)
     */
    
    private class Authenticate extends AsyncTask<String, Void, Void> 
    {  

        private ProgressDialog Dialog = new ProgressDialog(Settings.this);  
          
        protected void onPreExecute() 
        {  
            Dialog.setMessage("Logging in...");  
            Dialog.show();  
        }  

        protected Void doInBackground(String... urls) 
        {  
        	tv = (TextView)findViewById(R.id.pin_form);
        	authenticate(tv.getText().toString());
            return null;  
        }  
          
        protected void onPostExecute(Void unused) 
        {  
            Dialog.dismiss();
            
    		if(Lists.connected)
    		{
    			tv = (TextView)findViewById(R.id.status_settings_form);
    			tv.setText("Status: Log in successful!");
    			finish();
    		}
    		else
    		{
    			reset("Status: Error, please try again!");
    		}
        }  
          
    }  
    
    /**
	 * Handles option-menu (Android standard)
	 */
	
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }
    
    /**
     * Handles option-menu clicks (Android standard)
     */
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        
    	/* Show about information */
    	if(item.getItemId() == R.id.about_menu_item)
    	{
    		Toast toast = Toast.makeText(this, "viBird - Version 0.1", Toast.LENGTH_SHORT);
    		toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
    		toast.show();
    		return true;
    	}
    	/* Log out current user */
    	else if(item.getItemId() == R.id.logout_menu_item)
    	{
        	reset("Status: Please Log in!");
    		return true;
    	}
    	
        return false;
    }
    
    /**
     * Handles option-menu settings (Android standard)
     */
    
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	/* Check if user is logged in and can access the user account */
    	if(Lists.connected)
    	{
    		menu.findItem(R.id.logout_menu_item).setEnabled(true);
    	}
    	else
    	{
    		menu.findItem(R.id.logout_menu_item).setEnabled(false);
    	}
    	
    	return true;
    }
    
    /**
     * Reset settings and view
     */
    
    public void reset(String status)
    {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor pEditor = prefs.edit();
		Lists.connected = false;
    	pEditor.clear();
    	pEditor.commit();
    	
    	Lists.twitter = null;
    	
    	tv = (TextView)findViewById(R.id.status_settings_form);
    	tv.setText(status);
    	
    	tv = (TextView)findViewById(R.id.pintext_form);
        tv.setVisibility(4);
        	
        tv = (TextView)findViewById(R.id.pin_form);
        tv.setVisibility(4);
        
        tv = (TextView)findViewById(R.id.submit2_settings_form);
        tv.setVisibility(4);
        
        tv = (TextView)findViewById(R.id.submit_settings_form);
        tv.setVisibility(1);
    }
}