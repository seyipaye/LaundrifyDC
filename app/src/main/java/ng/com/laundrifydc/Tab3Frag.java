package ng.com.laundrifydc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static ng.com.laundrifydc.MainActivity.dcDB;
import static ng.com.laundrifydc.MainActivity.deRefreshed;
import static ng.com.laundrifydc.MainActivity.deliveryModels;
import static ng.com.laundrifydc.MainActivity.gettingSnap;
import static ng.com.laundrifydc.MainActivity.dcOrdersSnap;
import static ng.com.laundrifydc.MainActivity.ordersRef;

public class Tab3Frag extends Fragment {
    Context context;
    RecyclerView.Adapter deliveryAdapter;
    TextView descText;
    RecyclerView deliveryRV;
    ProgressBar pBar;
    private String firstDayKey;
    private String TAG = "test";

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2_frag,null);
        context = v.getContext();

        deliveryRV = v.findViewById(R.id.tab2Rec);
        descText = v.findViewById(R.id.descText);
        pBar = v.findViewById(R.id.progressBar);
        descText.setText("This shows all pending deliveries for the next seven days");
        deliveryRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        deliveryRV.setAdapter(deliveryAdapter);
        firstDayKey = null;
        if (deliveryModels.size() != 0) {
            pBar.setVisibility(View.GONE);
            deliveryRV.setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deliveryAdapter = new Delivery_Adapter();
        deliveryModels = new ArrayList<>();
    }



    //Get snap again from database
    private void getSnapAgain () {
        if (ordersRef == null) {
            ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        }
        showProgressDialog("Please wait...", "Fetching data...", (long) 50000);
        dcDB.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dcOrdersSnap = dataSnapshot;
                    feedData();
                    Toast.makeText(context, "Data fetched", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideProgressDialog();
                Toast.makeText(context, "Couldn't fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
        if (visible && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            //onResume();

            if (dcOrdersSnap == null && !gettingSnap) {
                getSnapAgain();
            } else if (!(deliveryModels.size() > 0)) {
                feedData();
            } else if (deRefreshed) {
                feedData();
            }
        }
    }

    private void feedData() {

        //Get Next 7 days Keys + today
        for (int i = 0; i < 8; i++) {
            Date date;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, i);
            date = cal.getTime();

            final String dayKey = new SimpleDateFormat("yyyyMMdd").format(date);
            final String dayDay = new SimpleDateFormat("EEEE").format(date);
            final String dayMonth = new SimpleDateFormat("MMM").format(date);
            String dayNumber = (new SimpleDateFormat("dd").format(date));
            String addition;
            final String dayString;

            if (dayNumber.matches("01")) {
                addition = "st ";
            } else if (dayNumber.matches("02")) {
                addition = "nd ";
            } else if (dayNumber.matches("03")) {
                addition = "rd ";
            } else {
                addition = "th ";
            }

            if (dayNumber.substring(0, 1).matches("0")) {
                dayNumber = dayNumber.substring(1);
            }

            if (i == 0) {
                dayString = "Today " + "(" + dayNumber + addition + dayMonth + ")";
            } else if (i == -1) {
                dayString = "Yesterday " + "(" + dayNumber + addition + dayMonth + ")";
            } else {
                dayString = dayDay + " " + dayNumber + addition + dayMonth;
            }

            // Database Stuffs
            final DataSnapshot dateRef = dcOrdersSnap.child("Delivery").child(dayKey);

            final String count = String.valueOf(dateRef.getChildrenCount());
            final ArrayList<String> keys = new ArrayList<>();
            for (DataSnapshot child : dateRef.getChildren()) {
                keys.add(child.getKey());
            }

            if (i == 0) {
                deliveryModels.clear();
                firstDayKey = dayKey;
            }

            deliveryModels.add(new Collected_Model(dayString, count, keys, dayKey));
            deliveryAdapter.notifyDataSetChanged();
            pBar.setVisibility(View.GONE);
            deliveryRV.setVisibility(View.VISIBLE);
            //Log.i("test", "No orders for " + dataSnapshot.toString());

            if (deliveryModels.size() == 7 && deRefreshed) {
                deRefreshed = false;
            }

            if (i == 7) {
                cleanDB(firstDayKey);
            }
        }
    }

    private void cleanDB (final String firstDayKey) {
        final DataSnapshot deliverySnap = dcOrdersSnap.child("Delivery");
        for (final DataSnapshot day : deliverySnap.getChildren()) {

            // Check if sync
            if (Integer.parseInt(day.getKey()) < Integer.parseInt(firstDayKey)) {
                Log.i(TAG, "cleanDB: removed " + day.getKey());
                for (final DataSnapshot orders : day.getChildren()) {
                    dcDB.child("Orders").child("Incomplete").child(orders.getKey()).setValue(orders.getValue(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            dcDB.child("Orders").child("Delivery").child(day.getKey()).child(orders.getKey()).removeValue();
                        }
                    });
                }
            }
        }
        dcDB.child("Orders").child("Delivery").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class Delivery_Adapter extends RecyclerView.Adapter<Tab3Frag.Delivery_Adapter.MyViewHolder> {

        public Delivery_Adapter() {}

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView collectedTotal;
            TextView collectedDay;
            Button collectedButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.collectedTotal = itemView.findViewById(R.id.collectedTotal);
                this.collectedDay = itemView.findViewById(R.id.collectedDay);
                this.collectedButton = itemView.findViewById(R.id.collectedButton);
            }
        }

        @NonNull
        @Override
        public Tab3Frag.Delivery_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.collected_row, parent, false);
            Tab3Frag.Delivery_Adapter.MyViewHolder myViewHolder = new Tab3Frag.Delivery_Adapter.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull Tab3Frag.Delivery_Adapter.MyViewHolder holder, final int position) {
            TextView collectedTotal = holder.collectedTotal;
            TextView collectedDay = holder.collectedDay;
            Button collectedButton = holder.collectedButton;

            collectedTotal.setText(deliveryModels.get(position).getTotal());

            collectedDay.setText(deliveryModels.get(position).getDay());

            collectedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFrag = ActiveDeFrag.newInstance(deliveryModels.get(position).getKeys(),
                            deliveryModels.get(position).getDataDay());
                    newFrag.show(getFragmentManager(), "tag");
                }
            });
        }

        @Override
        public int getItemCount() {
            return deliveryModels.size();
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
            mProgressDialog = new ProgressDialog(context, R.style.DialogStyle);
        } else {
            mProgressDialog = new ProgressDialog(context, R.style.BigDialogStyle);
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
                    Toast.makeText(context, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
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
