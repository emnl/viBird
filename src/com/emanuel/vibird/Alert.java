/*
 * Copyright (c) 2010, Emanuel Andersson
 * All rights reserved.
*/

package com.emanuel.vibird;

import twitter4j.TwitterException;
import android.app.Activity;
import android.app.AlertDialog;

/**
 * An exception handler class with popup
 * 
 * @author Emanuel Andersson
 * @version 0.1
 *
 */

public class Alert
{
		
	AlertDialog.Builder ad;
	
	/**
	 * Creates the popup
	 * 
	 * @param e The TwitterException
	 * @param ac An activity which handles the popup
	 */

	Alert(TwitterException e, Activity ac)
	{
		ad = new AlertDialog.Builder(ac);
		ad.setTitle("Twitter " + e.getStatusCode());
		
		if(e.getStatusCode() == 401)
		{
			ad.setMessage("You are not logged in!");
		}
		else if(e.getStatusCode() == 400)
		{
			ad.setMessage("Bad request!");
		}
		else if(e.getStatusCode() == 404)
		{
			ad.setMessage("Could not connect to twitter!");
		}
		else
		{
			ad.setMessage(e.getMessage());
		}
		
		ad.setPositiveButton( "OK", null );
		ad.show();
	}
	
}