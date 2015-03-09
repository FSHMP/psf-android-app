package org.psfcerd.blog.pondicherryscienceforum;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;

import org.psfcerd.blog.database.PostEntry;
import org.psfcerd.blog.database.PostEntryDBHandler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

            db.close();

        }

        @Override
        protected Void doInBackground(URL... urls) {

            try {
                // Get the database Handler
                PostEntryDBHandler db = new PostEntryDBHandler(getApplicationContext());
                int dbCount = db.getTotalCount();

                /** If there is no internet connection when the application
                 * starts for first time, redirect to NoInternet Activity.
                 */

                if (!isConnected && dbCount==0)
                {
                    Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (isConnected) {
                    // Get the RSS feeds
                    SyndFeedInput input = new SyndFeedInput();
                    feed = input.build(new XmlReader(urls[0]));

                    Iterator entryIter = feed.getEntries().iterator();

                    while (entryIter.hasNext()) {
                        SyndEntry entry = (SyndEntry) entryIter.next();
                        db.addPostEntry(new PostEntry(entry.getTitle(), entry.getUri(),
                                entry.getDescription().toString(), entry.getPublishedDate().toString(), null));
                    }

                    proceed = true;
                }

                db.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

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
