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
import com.google.firebase.database.Query;
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
import static ng.com.laundrifydc.MainActivity.gettingSnap;
import static ng.com.laundrifydc.MainActivity.mainRef;
import static ng.com.laundrifydc.MainActivity.mainSnap;
import static ng.com.laundrifydc.MainActivity.orderDB;

public class Tab2Frag extends Fragment {
    Context context;
    RecyclerView.Adapter collectedAdapter;
    List<Collected_Model> collectedModels;
    TextView descText;
    RecyclerView collectedRV;
    ProgressBar pBar;
    String lastDayKey;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2_frag,null);
        context = v.getContext();

        collectedRV = v.findViewById(R.id.tab2Rec);
        descText = v.findViewById(R.id.descText);
        descText.setText("This shows all collected orders within the last seven days");
        collectedRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        collectedAdapter = new Tab2Frag.Collected_Adapter();
        collectedModels = new ArrayList<>();
        collectedRV.setAdapter(collectedAdapter);
        pBar = v.findViewById(R.id.progressBar);
        lastDayKey = null;
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mainSnap == null && !gettingSnap) {
            getSnapAgain();
        } else if (!(collectedModels.size() > 0)) {
            feedData();
        }
    }

    //Get snap again from database
    private void getSnapAgain () {
        gettingSnap = true;
        if (orderDB == null) {
            orderDB = FirebaseDatabase.getInstance().getReference().child("Orders");
        }
            showProgressDialog("", "Refreshing data...", (long) 50000);
            orderDB = mainRef.child("Orders");
            orderDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        gettingSnap = false;
                        mainSnap = dataSnapshot;
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

    private void feedData() {

        //Get Last 7 days Keys + today
        ArrayList<TwoStringsModel> twoStringsModels = new ArrayList<>();
        for (int i = 0; i > -8; i--) {
            Date date;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, i);
            date = cal.getTime();

            final String dayKey = new SimpleDateFormat("yyyyMMdd").format(date);
            final String dayDay = new SimpleDateFormat("EEEE").format(date);
            final String dayMonth = new SimpleDateFormat("MMMM").format(date);
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

            final Query dateRef = dcDB.child("Orders").child("Collected").child(dayKey).orderByChild("TimeStamp");
            dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final String count = String.valueOf(dataSnapshot.getChildrenCount());
                    final ArrayList<String> keys = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        keys.add(child.getKey());
                    }
                    collectedModels.add(new Collected_Model(dayString, count, keys, dayKey));
                    collectedAdapter.notifyDataSetChanged();
                    pBar.setVisibility(View.GONE);
                    collectedRV.setVisibility(View.VISIBLE);
                    if (dayKey == lastDayKey) {
                        cleanDB(lastDayKey);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            
            if (i == -7) {
                lastDayKey = dayKey;
                //Log.i("test", "deleting...");
            }
        }
    }

    private void cleanDB (final String lastDate) {
        dcDB.child("Orders").child("Collected").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot day : dataSnapshot.getChildren()) {
                    //Log.i("test","Found " + child.getKey());
                    if (Integer.parseInt(day.getKey()) < Integer.parseInt(lastDate)) {
                        //Log.i("test", "moving... " + child.getKey());
                        for (final DataSnapshot orders : day.getChildren()) {
                            dcDB.child("Orders").child("Incomplete").child(orders.getKey()).setValue(orders.getValue(), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    dcDB.child("Orders").child("Collected").child(day.getKey()).child(orders.getKey()).removeValue();
                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void moveOrders(String orderKey, Object value) {
        orderDB.child(orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class Collected_Adapter extends RecyclerView.Adapter<Collected_Adapter.MyViewHolder> {

        public Collected_Adapter() {}

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
        public Tab2Frag.Collected_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.collected_row, parent, false);
            Tab2Frag.Collected_Adapter.MyViewHolder myViewHolder = new Tab2Frag.Collected_Adapter.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull Tab2Frag.Collected_Adapter.MyViewHolder holder, final int position) {
            TextView collectedTotal = holder.collectedTotal;
            final TextView collectedDay = holder.collectedDay;
            Button collectedButton = holder.collectedButton;

            collectedTotal.setText(collectedModels.get(position).getTotal());

            collectedDay.setText(collectedModels.get(position).getDay());

            collectedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFrag = ActiveCoFrag.newInstance(collectedModels.get(position).getKeys(),
                            collectedModels.get(position).getDataDay());
                    newFrag.show(getFragmentManager(), "tag");
                }
            });
        }

        @Override
        public int getItemCount() {
            return collectedModels.size();
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
