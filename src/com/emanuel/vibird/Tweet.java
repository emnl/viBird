/*
 * Copyright (c) 2010, Emanuel Andersson
 * All rights reserved.
*/

package com.emanuel.vibird;

import twitter4j.TwitterException;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * New tweet activity
 * 
 * @author Emanuel Andersson
 * @version 0.1
 *
 */

public class Tweet extends Activity 
{
	
	/**
	 * Called when activity is created, (Android standard), handles UI and sets buttonlistner
	 */
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	/* Set xml layout */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweetlayout);
                    	
        /* Get additional information send by previous intent (last tweet) */
        Bundle bundle = this.getIntent().getExtras();
        String lastTweet = bundle.getString("lastTweet");
        
        TextView tv = (TextView)findViewById(R.id.lastTweet);
        tv.setText("\"" + lastTweet + "\"");
        
        /*
         * Form-button listner
         */
        
        OnClickListener submitListener = new OnClickListener() {
    	    public void onClick(View v) 
    	    {
    	    	TextView tv = (TextView)findViewById(R.id.newTweet_field);
    	    	try 
    	    	{
					Lists.twitter.updateStatus(tv.getText().toString());
					Tweet.this.setResult(1); 
					Tweet.this.finish();
				} 
    	    	catch (TwitterException e) 
    	    	{
    	    		new Alert(e, Tweet.this);
				}
    	    }
    	    
        };
        
        /*
         * Assign listner to button
         */
        Button smbutton = (Button)findViewById(R.id.submitTweet);
    	smbutton.setOnClickListener(submitListener);
        
    }
}