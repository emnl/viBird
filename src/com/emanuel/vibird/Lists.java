/*
 * Copyright (c) 2010, Emanuel Andersson
 * All rights reserved.
*/

package com.emanuel.vibird;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.http.AccessToken;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Main list activity that handles the user lists and global static variables
 * 
 * @author Emanuel Andersson
 * @version 0.1
 *
 */

public class Lists extends ListActivity 
{
		
	/*
	 * CLASS-RELATED
	 */
	
	List<String> listsOwner = new ArrayList<String>();
	List<Integer> listsId = new ArrayList<Integer>();
	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	SimpleAdapter adapter;
	PagableResponseList<UserList> userLists = null;
	
	/** Static application-used twitter client */
	public static Twitter twitter = new TwitterFactory().getInstance();
	
	/** Connectivity status */
	public static boolean connected = false;
	/** Logged in user's username */
	public static String username = null;
	/** Logged in user's userid */
	public static int userId;
	
	/* APPLICATION SETTINGS */
	
	/** Twitter.com api consumer-token */
	public static final String CONSUMER_TOKEN = "xSfFOhDg06o3JzOlzumPhA";
	/** Twitter.com api consumer-token-secret */
	public static final String CONSUMER_TOKEN_SECRET = "xoziD8aljBbbxhJqPQykK2OIHddYeisVlx8UKIxFEFM";
	
	/** Database name used for request-token */
	public static final String REQUEST_TOKEN = "RequestToken";
	/** Database name used for request-token-secret */
	public static final String REQUEST_TOKEN_SECRET = "RequestTokenSecret";
	
	/** Database name used for access-token */
	public static final String ACCESS_TOKEN = "AccessToken";
	/** Database name used for access-token-secret */
	public static final String ACCESS_TOKEN_SECRET = "AccessTokenSecret";
	
	/** Tweets showed in a list (preferably not above 40, twitter rate limits) */
	public static final int MAX_ITEM_COUNT = 15;
	
	/**
	 * Called when activity is created, (Android standard), handles authentication and initiate list
	 */
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* Get Android app-db information */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String AccessToken = prefs.getString(ACCESS_TOKEN, null);
        String AccessTokenSecret = prefs.getString(ACCESS_TOKEN_SECRET, null);
		AccessToken at = new AccessToken(AccessToken, AccessTokenSecret);
				
		/* Check if user is logged in */
		try
		{
			if(!connected)
			{
				twitter.setOAuthConsumer(CONSUMER_TOKEN, CONSUMER_TOKEN_SECRET);
			    twitter.setOAuthAccessToken(at);
			}
			
			User us = twitter.verifyCredentials();
			username = us.getScreenName();
			userId = us.getId();
			connected = true;
		}
		catch(Exception e)
		{
			connected = false;
			Intent intent = new Intent(this, Settings.class);
  	        startActivityForResult(intent, 1);
		}
			
		adapter = new SimpleAdapter( 
				this, 
				list,
				R.layout.two_line_item,
				new String[] { "line1","line2" },
				new int[] { R.id.text3, R.id.text4 }  );
						
		setListAdapter(adapter);
		
    }
	
	/**
	 * Refresh only if lists is empty due to performance
	 */
	
	protected void onResume() 
	{
        super.onResume();
        
        if(connected && list.size() == 0)
        {
        	TextView tv = (TextView)findViewById(R.id.listsHeader);
			tv.setText(username + "'s Lists");
			
        	new GetLists().execute(); 
        }
        else if(!connected)
        {
        	TextView tv = (TextView)findViewById(R.id.listsHeader);
			tv.setText("Please Log in!");
			
        	list.clear();
        	adapter.notifyDataSetChanged();
        }
    }
	
	/**
	 * Unused
	 */
	
	protected void onDestroy()
	{
		super.onDestroy();
		//Cache anything?
	}
	
	/**
	 * Unused, could be used for better structure
	 */
	
	protected void onStart()
	{
		super.onStart();
	}
	
	
	/**
	 * Update the main lists with twitter-lists
	 */
	
	public void getLists()
	{
		
		try 
		{
			
			/* Clear old lists */
			listsOwner.clear();
			listsId.clear();
			list.clear();
			
			/* Get both subscribed-lists and user-lists */
			userLists = twitter.getUserLists(username, -1);
			userLists.addAll(twitter.getUserListSubscriptions(username, -1));
			
			/* Put each list in the UI list */
			for(UserList userList : userLists)
  			{	    	
				HashMap<String,String> item = new HashMap<String,String>();
  				listsOwner.add(userList.getUser().getScreenName());
  				listsId.add(userList.getId());
  				
  				String line1 = userList.getUser().getScreenName() + " / " + userList.getName();
  				
  				item.put( "line1", line1.toUpperCase());
  				
  				if(userList.getDescription().length() < 1)
  					item.put( "line2", userList.getName());
  				else
  					item.put( "line2", userList.getDescription());
  				
  				list.add( item );
  			}
			
			
		} 
		catch (TwitterException e) 
		{
			new Alert(e, this);
		}
				
	}
	
	/**
     * AsyncTask for getting lists (class)
     */
    
    private class GetLists extends AsyncTask<String, Void, Void> 
    {  

        private ProgressDialog Dialog = new ProgressDialog(Lists.this);  
          
        protected void onPreExecute() 
        {  
            Dialog.setMessage("Loading...");  
            Dialog.show();  
        }  

        protected Void doInBackground(String... urls) 
        {  
        	getLists();
            return null;  
        }  
          
        protected void onPostExecute(Void unused) 
        {  
        	adapter.notifyDataSetChanged();
            Dialog.dismiss();
        }  
          
    }  
	
	/**
	 * Handles lists-clicks (Android standard)
	 */
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		/* Send list information with the new intent */
		Bundle bundle = new Bundle();
		bundle.putInt("ListId", listsId.get(position));
		bundle.putString("ListOwner", listsOwner.get(position));

		Intent i = new Intent(this, Dashboard.class);
		i.putExtras(bundle);
		startActivity(i);
	}
	
	/**
	 * Handles option-menu (Android standard)
	 */
	
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lists_menu, menu);
        return true;
    }
    
    /**
     * Handles option-menu clicks (Android standard)
     */
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        
    	/* Go to twitter */
    	if(item.getItemId() == R.id.tweet_menu_item)
    	{
    		
    		Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://twitter.com/" + username));
    	    startActivity(viewIntent);  
    		
    		return true;
    	}
    	/* Start "settings" activity */
    	else if(item.getItemId() == R.id.settings_menu_item)
    	{
    		
	    	Intent intent = new Intent();
	        intent.setClass(this, Settings.class);
	        this.startActivity(intent);
    		
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
    		menu.findItem(R.id.tweet_menu_item).setEnabled(true);
    	}
    	else
    	{
    		menu.findItem(R.id.tweet_menu_item).setEnabled(false);
    	}
    	
    	return true;
    }
}