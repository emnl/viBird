/*
 * Copyright (c) 2010, Emanuel Andersson
 * All rights reserved.
*/

package com.emanuel.vibird;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Feedviewer activity (list)
 * 
 * @author Emanuel Andersson
 * @version 0.1
 *
 */

public class Dashboard extends ListActivity
{

	/*
	 * Class settings
	 */
	
	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	ArrayList<Long> tweetids = new ArrayList<Long>();
	SimpleAdapter adapter;
	
	/*
	 * List settings
	 */
		
	int listId;
	String listOwner;
	
	
	/**
     * Called when activity is created, (Android standard), get passed information and create list
     */
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	/* Set xml layout */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboardslayout);
        
        /*
         * Get passed information about the clicked list
         */
                    	
        Bundle bundle = this.getIntent().getExtras();
        listId = bundle.getInt("ListId");
        listOwner = bundle.getString("ListOwner");
        
		try 
		{
			TextView tv = (TextView)findViewById(R.id.dashText);
			tv.setText(Lists.twitter.showUserList(listOwner, listId).getName());
		} 
		catch (TwitterException e) 
		{
			new Alert(e, this);
		}
        
        adapter = new SimpleAdapter( 
				this, 
				list,
				R.layout.line_item,
				new String[] { "line1" },
				new int[] { R.id.text1 }  );
                
        setListAdapter(adapter);
        		
		registerForContextMenu(getListView());
			    
    }
    
    /**
     * Update list when created or resumed (change to onStart?)
     */
    
    protected void onResume() 
	{
        super.onResume();
        new GetTweets().execute(); 
        //getTweets();
        //adapter.notifyDataSetChanged();
    }
    
    /**
     * Update list
     */
    
    public void getTweetsList()
    {
    	
    	list.clear();
    	tweetids.clear();
    	
    	try
    	{
	    	for(Status status : Lists.twitter.getUserListStatuses(listOwner, listId, new Paging(1, Lists.MAX_ITEM_COUNT)))
	        {
	    		HashMap<String,String> item = new HashMap<String,String>();
	    		item.put("line1", status.getUser().getName() + ": " + status.getText());
	    		list.add(item);
	    		
	    		tweetids.add(status.getId());
	        }
	    	
    	}
    	catch(TwitterException e)
    	{
    		new Alert(e, this);
    	} 
    	
    }
    
    /**
     * AsyncTask for getting tweets (class)
     */
    
    private class GetTweets extends AsyncTask<String, Void, Void> 
    {  

        private ProgressDialog Dialog = new ProgressDialog(Dashboard.this);  
          
        protected void onPreExecute() 
        {  
            Dialog.setMessage("Loading...");  
            Dialog.show();  
        }  

        protected Void doInBackground(String... urls) 
        {  
        	getTweetsList();
            return null;  
        }  
          
        protected void onPostExecute(Void unused) 
        {  
        	adapter.notifyDataSetChanged();
            Dialog.dismiss();
        }  
          
    }  
    
    /**
	 * Handles tweet-clicks (Android standard)
	 */
    
    @Override
	public void  onListItemClick(ListView l, View v, int position, long id) 
	{
		String tweet = list.get(position).get("line1");
		
		if(tweet.contains("http://"))
		{
			int start = tweet.indexOf("http://");
			int end = 0;
			
			for(int i = start;i < tweet.length(); i++)
			{
								
				if(tweet.charAt(i) == ' ')
					break;
				
				end = i+1;
								
			}
			
			String url = tweet.substring(start, end);
			
			if(url.charAt(url.length()-1) == '.' || url.charAt(url.length()-1) == ',')
				url = url.substring(0, url.length()-1);
						
			startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
		}
		
	}
    
    /**
     * Handles option-menu settings (Android standard)
     */
    
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	try 
    	{
    		User ul = null;
    		ul = Lists.twitter.checkUserListMembership(listOwner, listId, Lists.userId);
			if(ul != null)
			{
				menu.findItem(R.id.newtweet_menu_item).setVisible(true);
			}
		} 
    	catch (TwitterException e) 
		{
    		
		}
    	
    	return true;
    }
    
    /**
     * Handles option-menu (Android standard)
     */
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        return true;
    }
    
    /**
     * Handles option-menu clicks (Android standard)
     */
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        
    	if(item.getItemId() == R.id.refresh_menu_item)
    	{
    		new GetTweets().execute();
    		adapter.notifyDataSetChanged();
    		
    		return true;
    	}
    	else if(item.getItemId() == R.id.newtweet_menu_item)
    	{
    		Bundle bundle = new Bundle();
    		bundle.putString("lastTweet", list.get(0).get("line1"));

    		Intent i = new Intent(this, Tweet.class);
    		i.putExtras(bundle);
    		startActivityForResult(i, 2);
    		
    		return true;
    	}
    	
        return false;
    }
    
    /**
     * Handles context-menu (Android standard)
     */
    
    public void onCreateContextMenu(ContextMenu menu, View v,
    ContextMenuInfo menuInfo) 
    {
    	/*
    	 * NOTE: 1 & 2 are random values used as ids
    	 */
    	menu.add(0, 1, 0, "Retweet");
    	menu.add(0, 2, 0, "Similar tweets");
    }
    
    /**
     * Handles context-menu clicks (Android standard)
     */
    
    public boolean onContextItemSelected(MenuItem item) 
    {
    	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	  
    	  if(item.getItemId() == 1)
    	  {
    		  try
    		  {
    			  Lists.twitter.retweetStatus(tweetids.get(info.position));
    			  Toast.makeText(this, "Retweet posted!", Toast.LENGTH_SHORT).show();
    		  }
    		  catch(TwitterException e)
    		  {
    			  new Alert(e, this);
    		  }
    	  }
    	  else if(item.getItemId() == 2)
    	  {
    		  AlertDialog.Builder ad = new AlertDialog.Builder(this);
    		  ad.setTitle("viBird");
    		  ad.setMessage("Coming soon!");
    		  ad.setPositiveButton( "OK", null );
    		  ad.show();
    	  }
    	  
    	  return true;
    }
    
    /**
     * Used to notify user when he posted a new tweet (Android standard)
     */
    
    protected void onActivityResult(int requestCode, int resultCode, 
    		Intent data) 
    { 
    	if (resultCode == 1) 
    	{ 
    		Toast.makeText(this, "Tweet posted!", Toast.LENGTH_LONG).show();
    	}
    }

        
}