package ng.com.laundrifydc;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private FirebaseDatabase database;
    static DatabaseReference mainRef;
    static DataSnapshot dcOrdersSnap;
    static DatabaseReference ordersRef;
    static DatabaseReference dcDB;
    static boolean coRefreshed;
    static boolean deRefreshed;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    TabLayout tabLayout;
    static boolean gettingSnap;
    static List<Collected_Model> collectedModels;
    static List<Collected_Model> deliveryModels;
    List<Fragment> tabFragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        mainRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        dcOrdersSnap = null;
        checkUpdate();
    }

    //Update Checker
    private void checkUpdate() {
        showProgressDialog("Please wait...", "Starting up...", (long) 60000);
        mainRef.child("AppsVersion").child("Android").child("LaundrifyDC")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        hideProgressDialog();
                        if (compareVersions(dataSnapshot.getValue().toString())) {
                            askToUpdate();
                        } else {
                            checkLogin();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void askToUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.IncompleteDialog);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("Out dated app");
        builder.setMessage("Our developers are working to ensure you get the best of our services, and we've got a new update all in place for you" +
                " we're sorry but it's necessary to update before continuation")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        update(MainActivity.this);
                    }})
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }});

        final AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void update(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        } finally {
            uri = null;
            goToMarket = null;
        }
    }
    private boolean compareVersions(String freshVersion){
        try {
            String curVersion = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            //Log.i("Stuff", getPackageName() + freshVersion + curVersion);
            return value(curVersion) < value(freshVersion);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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


    private void checkLogin() {
        user = mAuth.getCurrentUser();
        if (user != null) {
            launchactivity();
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginorsignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Launch activity
    private void launchactivity() {
        showProgressDialog("", "Geting data...", (long) 60000);
        dcDB = mainRef.child("Users").child("DryCleaners").child(user.getUid());

        getSnap();

        // OneSignal Initialization
        OneSignal.startInit(MainActivity.this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        OneSignal.setEmail(user.getEmail());
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                dcDB.child("Info").child("NotificationKey").setValue(userId);
                Log.i("test", "Id Available");
            }
        });
        startService(new Intent(this, AwsomeNotification.class));

        //stopService(new Intent(this, AwsomeNotification.class));

        tabFragments = new ArrayList<>();
        tabFragments.add(new Tab1Frag());
        tabFragments.add(new Tab2Frag());
        tabFragments.add(new Tab3Frag());
        tabFragments.add(new Tab4Frag());
;        Toolbar toolbar = findViewById(R.id.toolbar);
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void getSnap() {
        showProgressDialog("Please wait...", "Fetching data...", (long) 30000);
        gettingSnap = true;
        dcDB.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dcOrdersSnap = dataSnapshot;
                    Toast.makeText(MainActivity.this, "Data fetched", Toast.LENGTH_SHORT).show();
                    coRefreshed = true;
                    deRefreshed = true;
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
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            // Show total pages.
            return tabFragments.size();
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