package com.hackduke14.costmeter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.hackduke14.fitstep.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	HoloCircularProgressBar progressBar;
    	TextView textView;
    	
    	/* Allowance in dollars. */
    	float allowance = 2.5f;
    	
    	long period;
    	
    	/* If all POST calls are successful, we set this to true. */
    	boolean postSuccess;
    	float progress;
    	
    	static boolean shouldNotify = false;
    	
    	Timer timer;
    	
    	SharedPreferences preferences;
    	
        public PlaceholderFragment() {
        }
        
        void produceNotification() {
        	NotificationCompat.Builder mBuilder =
        	        new NotificationCompat.Builder(getActivity())
        	        .setSmallIcon(R.drawable.ic_launcher)
        	        .setContentTitle("CostMeter")
        	        .setContentText("You are near your limit!");

        	// The stack builder object will contain an artificial back stack for the
        	// started Activity.
        	// This ensures that navigating backward from the Activity leads out of
        	// your application to the Home screen.
        	NotificationManager mNotificationManager =
        	    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        	// mId allows you to update the notification later on.
        	int id = 0;
        	
        	Notification notif = mBuilder.build();
        	notif.defaults |= Notification.DEFAULT_SOUND;
        	mNotificationManager.notify(id, notif);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            timer = new Timer();
            
            progressBar = (HoloCircularProgressBar) rootView.findViewById(R.id.progressBar1);
            textView = (TextView) rootView.findViewById(R.id.textView1);
            // Orange theme
            progressBar.setProgressColor(Color.parseColor("#FF8800"));
            progressBar.setProgressBackgroundColor(Color.parseColor("#FFBB33"));
            
            progress = 1.0f;
            progressBar.setProgress(1.0f);
            
            LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.linearLayout1);
            layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), SettingsActivity.class);
					startActivity(intent);
				}
            });
            
            return rootView;
        }
        
        public void onResume() {
        	super.onResume();
        	
        	textView.setText("Syncing");
        	
        	postSuccess = false;
        	
        	period = Long.parseLong(preferences.getString("sync_frequency", "5")) * 1000;
        	Log.i(this.getClass().getName(), "Period to update is: " + period);
        	progressBar.postDelayed(action, 0);
        	
        }
        
        Runnable action = new Runnable() {
    		public void run() {
    			progressBar.postDelayed(this, period);
    			if (!postSuccess) new PostRequest().execute(0);
    			new GetRequest().execute();
    		}
        };
        
        public void onPause() {
        	super.onPause();
        	progressBar.removeCallbacks(action);
        }
        
    	class GetRequest extends AsyncTask<Integer, Void, String> {

    		protected void onPreExecute() {
    		}
    		
    		@Override
    		protected String doInBackground(Integer... params) {
    			String url = "http://lit-hamlet-4028.herokuapp.com/devices/0";
				try {
					URL obj = new URL(url);
	    			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	    	    	 
	    			// optional default is GET
	    			con.setRequestMethod("GET");
	    	 
	    			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	    			String inputLine;
	    			StringBuffer response = new StringBuffer();
	    	 
	    			while ((inputLine = in.readLine()) != null) {
	    				response.append(inputLine);
	    			}
	    			
	    			in.close();
	    			
	    			return response.toString();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	 
    			//print result
    			return null;
    		}
    		
    		protected void onPostExecute(String result) {
    			if (result != null) {
    				textView.setText(String.format("$%.2f", Float.parseFloat(result)));
    				progressBar.setProgress(Float.parseFloat(result) / allowance);
    				if (!shouldNotify && progressBar.getProgress() < 0.25f) {
    					shouldNotify = true;
    					produceNotification();
    				}
    				if (progressBar.getProgress() > 0.25f)
    					shouldNotify = false;
    			} else
        			textView.setText("Syncing");
    		}
    	}
        
    	class PostRequest extends AsyncTask<Integer, Void, Boolean> {

    		@Override
    		protected Boolean doInBackground(Integer... params) {
    			Log.i(this.getClass().getName(), "Attempting to post...");
    		    try {
        			HttpClient httpclient = new DefaultHttpClient();
        		    HttpPost httppost = new HttpPost("http://lit-hamlet-4028.herokuapp.com/update");
    		        // Add your data
        		    
        		    boolean device1 = preferences.getBoolean("device_1", false);
        		    boolean device2 = preferences.getBoolean("device_2", false);
        		    boolean device3 = preferences.getBoolean("device_3", false);
        		    float wattage1 = Float.parseFloat(preferences.getString("device_1_wattage", "0"));
        		    float wattage2 = Float.parseFloat(preferences.getString("device_2_wattage", "0"));
        		    float wattage3 = Float.parseFloat(preferences.getString("device_3_wattage", "0"));
        		    
        		    JSONObject holder = new JSONObject();
					holder.put("first_device", device1? 1 : 0);
					holder.put("second_device", device2? 1 : 0);
	        		holder.put("third_device", device3? 1 : 0);
	        		holder.put("wattage_first_device", wattage1);
	        		holder.put("wattage_second_device", wattage2);
	        		holder.put("wattage_third_device", wattage3);

	        		Log.i(this.getClass().getName(), holder.toString());

        		    StringEntity se = new StringEntity(holder.toString());
	        		
    		        httppost.setEntity(se);

    		        // Execute HTTP Post Request
    		        HttpResponse response = httpclient.execute(httppost);
    		        
    		        return true;
    		    } catch (ClientProtocolException e) {
    		        // TODO Auto-generated catch block
    		    } catch (IOException e) {
    		        // TODO Auto-generated catch block
    		    } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			return false;
    		}
    		
    		protected void onPostExecute(Boolean result) {
    			postSuccess = result;
    		}
    	}
    }
}
