package org.psfcerd.blog.pondicherryscienceforum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.psfcerd.blog.database.PostEntry;
import org.psfcerd.blog.database.PostEntryDBHandler;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Log.d("INFO: ", "Entering here");
            URL feedUrl = new URL("http://www.psfcerd.org/blog/feed/");
            new DownloadFeed().execute(feedUrl);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private class DownloadFeed extends AsyncTask<URL, Void, Void> {

        SyndFeed feed = null;
        boolean proceed = false;

        // Check for internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // Initiate a Progress Dialog box
        ProgressDialog progressDialog = new ProgressDialog(getApplication());

        @Override
        protected void onPreExecute(){
            // Grab the database handler
            PostEntryDBHandler db = new PostEntryDBHandler(getApplicationContext());

            if (db.getTotalCount() != 0) {

                // Get the ListView declared in the UI
                ListView list_view_handle = (ListView) findViewById(R.id.entries_list);

                // Array List of Strings to populate the ListView
                ArrayList<String> itemsList = new ArrayList<>();

                // Fetches the entries from Database
                List<PostEntry> entryList = db.getAllEntries();

                for (PostEntry entry : entryList) {
                    itemsList.add(entry.getTitle());
                }

                // Populate the ListView using ArrayAdapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, itemsList);
                list_view_handle.setAdapter(adapter);
            }

            if (isConnected) {
                progressDialog = ProgressDialog.show(MainActivity.this, "Please Wait", "checking for new entries");
            }

            db.close();

        }

        private String getEtag(URL url){
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(new HttpHead(url.toString()));
                return response.getFirstHeader("Etag").getValue();

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        private void fetch_and_add_to_db(URL link, PostEntryDBHandler dbHandler){
            try {
                // Fetch the RSS feeds
                SyndFeedInput input = new SyndFeedInput();
                feed = input.build(new XmlReader(link));

                Iterator entryIter = feed.getEntries().iterator();

                while (entryIter.hasNext()) {
                    SyndEntry entry = (SyndEntry) entryIter.next();
                    dbHandler.addPostEntry(new PostEntry(entry.getTitle(), entry.getUri(),
                            entry.getDescription().toString(), entry.getPublishedDate().toString(), null));
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(URL... urls) {

            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                // Get the database Handler
                PostEntryDBHandler db = new PostEntryDBHandler(getApplicationContext());
                int dbCount = db.getTotalCount();

                // Starting app for the very first time w/o Internet
                if (!isConnected && dbCount==0)
                {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    finish();
                }
                // Starting app for the very first time with Internet
                else if (isConnected && dbCount==0) {

                    Log.i("MESSAGE:", "First time with Internet");

                    // Get Etag from Server
                    String etag = getEtag(urls[0]);

                    // Write the Etag value to shared preferences
                    sharedPreferences.edit().putString("ETag", etag).apply();

                    // Add feeds to database
                    fetch_and_add_to_db(urls[0], db);

                    // Set the flag to True
                    proceed = true;
                }

                else if(dbCount !=0 && isConnected){

                    Log.i("MESSAGE:", "Other time with Internet");

                    String stored_etag = sharedPreferences.getString("ETag", "");
                    String etag_value = getEtag(urls[0]);

                    if (!stored_etag.equals(etag_value)) {
                        Log.i("MESSAGE:", "Fetching new entries");
                        fetch_and_add_to_db(urls[0], db);
                        proceed = true;
                    }
                }

                db.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // Dismiss the waiting progressDialog
            progressDialog.dismiss();

            if (proceed) {

                // Get the ListView declared in the UI
                ListView list_view_handle = (ListView) findViewById(R.id.entries_list);

                // Array List of Strings to populate the ListView
                ArrayList<String> itemsList = new ArrayList<>();

                // Grab the database handler
                PostEntryDBHandler db = new PostEntryDBHandler(getApplicationContext());

                // Fetches the entries from Database
                List<PostEntry> entryList = db.getAllEntries();

                for (PostEntry entry : entryList) {
                    itemsList.add(entry.getTitle());
                }

                // Populate the ListView using ArrayAdapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, itemsList);
                list_view_handle.setAdapter(adapter);

                db.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
