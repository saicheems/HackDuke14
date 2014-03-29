package com.hackduke14.fitstep;

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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	HoloCircularProgressBar progressBar;
    	TextView textView;
    	
    	/* Allowance in dollars. */
    	float allowance = 50.0f;
    	
    	long period;
    	
    	/* If all POST calls are successful, we set this to true. */
    	boolean postSuccess;
    	float progress;
    	
    	Timer timer;
    	
    	SharedPreferences preferences;
    	
        public PlaceholderFragment() {
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
    				textView.setText("$" + result);
    				progressBar.setProgress(Float.parseFloat(result) / allowance);
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
        		    HttpPost httppost = new HttpPost("http://www.google.com");
    		        // Add your data
    		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    		        nameValuePairs.add(new BasicNameValuePair("device", Integer.toString(params[0])));
    		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    		        // Execute HTTP Post Request
    		        HttpResponse response = httpclient.execute(httppost);
    		        
    		        return true;
    		    } catch (ClientProtocolException e) {
    		        // TODO Auto-generated catch block
    		    } catch (IOException e) {
    		        // TODO Auto-generated catch block
    		    }
    			return false;
    		}
    		
    		protected void onPostExecute(Boolean result) {
    			postSuccess = result;
    		}
    	}
    }
}
