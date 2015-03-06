package org.psfcerd.blog.pondicherrysciecneforum;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            try {
                URL feedUrl = new URL("http://www.psfcerd.org/blog/feed/");
                new DownloadFeed().execute(feedUrl);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            Intent intent = new Intent(getApplicationContext(), NoInternetActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private class DownloadFeed extends AsyncTask<URL, Void, Void> {

        SyndFeed feed = null;

        @Override
        protected Void doInBackground(URL... urls) {
            // boolean ok = false;

            //int count = urls.length;
            try {
                SyndFeedInput input = new SyndFeedInput();
                feed = input.build(new XmlReader(urls[0]));
                Log.d("PSF_FEED: ", feed.getTitle());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ListView list_view_handle = (ListView) findViewById(R.id.entries_list);
            ArrayList<String> itemsList = new ArrayList<>();

            Iterator entryIter = feed.getEntries().iterator();
            while (entryIter.hasNext()) {
                SyndEntry entry = (SyndEntry) entryIter.next();
                itemsList.add(entry.getTitle());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<> (MainActivity.this, android.R.layout.simple_list_item_1, itemsList);
            list_view_handle.setAdapter(adapter);
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
