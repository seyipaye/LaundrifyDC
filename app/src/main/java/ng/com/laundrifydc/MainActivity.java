package ng.com.laundrifydc;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private FirebaseDatabase database;
    static DatabaseReference mainRef;
    static DataSnapshot mainSnap;
    static DatabaseReference orderDB;
    static DatabaseReference dcDB;
    TabLayout tabLayout;
    static boolean gettingSnap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updated();
        mainSnap = null;
        showProgressDialog("Please wait...", "Fetching Data", (long) 30000);
    }

    private void updated() {
        database = FirebaseDatabase.getInstance();
        mainRef = database.getReference();
        dcDB = mainRef.child("Users").child("DryCleaners").child("0rYxL9Vp5yM3NrKs0upy0xz4S0D3");
        mainRef.child("AppsVersion").child("Android").child("LaundrifyDC")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hideProgressDialog();
            if (web_update(dataSnapshot.getValue().toString())) {
                rateThisApplication(MainActivity.this);
                Toast.makeText(MainActivity.this, "Please update the app", Toast.LENGTH_LONG).show();
            } else {
                launchactivity();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
        }
    });
    }

    private void launchactivity() {
        // OneSignal Initialization
        /*
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
                */
        getSnap();
        startService(new Intent(this, AwsomeNotification.class));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);

        tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    //Update Checker
    public void rateThisApplication(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try
        {
            context.startActivity(goToMarket);
        }
        catch (ActivityNotFoundException e)
        {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
        finally
        {
            uri = null;
            goToMarket = null;
        }
    }
    private long value(String string) {
        string = string.trim();
        if( string.contains( "." )){
            final int index = string.lastIndexOf( "." );
            return value( string.substring( 0, index ))* 100 + value( string.substring( index + 1 ));
        }
        else {
            return Long.valueOf( string );
        }
    }
    private boolean web_update(String newVersion){
        try {
            String curVersion = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;;
            Log.i("Stuff", getPackageName() + newVersion + curVersion);
            return value(curVersion) < value(newVersion);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private void feedData() {
        for (int i = 1; i<=7; i++) {
            //DD_Models = new ArrayList<>();
            Date date;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, i);
            date = cal.getTime();

            //Add tab !!!
            String dayDate = new SimpleDateFormat("EEEE dd").format(date);
            String addition;
            int number = Integer.valueOf(new SimpleDateFormat("dd").format(date));
            if (number == 01) {
                addition = "st";
            } else if (number == 02) {
                addition = "nd";
            } else if (number == 03) {
                addition = "rd";
            } else {
                addition = "th";
            }
        }


    }

    private void getSnap() {
        showProgressDialog("Please wait...", "Fetching data...", (long) 30000);
        gettingSnap = true;
        orderDB = mainRef.child("Orders");
        orderDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mainSnap = dataSnapshot;
                    //feedData();
                    Toast.makeText(MainActivity.this, "Data fetched", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                    gettingSnap = false;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgressDialog();
                Toast.makeText(MainActivity.this, "Couldn't fetch data", Toast.LENGTH_SHORT).show();
            }
        });
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem (int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new Tab1Frag();
                case 1:
                    return new Tab2Frag();
                case 2:
                    return new Tab3Frag();
                case 3:
                    return new Tab4Frag();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show total pages.
            return 4;
        }
    }

    //Progress dialog
    Handler handler = new Handler();
    public ProgressDialog mProgressDialog;
    public void showProgressDialog(String title, String message, Long time) {
        if(mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (title.matches("")) {
            mProgressDialog = new ProgressDialog(this, R.style.DialogStyle);
        } else {
            mProgressDialog = new ProgressDialog(this, R.style.BigDialogStyle);
            mProgressDialog.setTitle(title);
        }
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(mProgressDialog!=null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            handler.removeCallbacksAndMessages(null);
        }
    }
}