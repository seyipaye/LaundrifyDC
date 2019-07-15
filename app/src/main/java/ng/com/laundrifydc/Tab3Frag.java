package ng.com.laundrifydc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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

import static ng.com.laundrifydc.MainActivity.mainRef;
import static ng.com.laundrifydc.MainActivity.mainSnap;
import static ng.com.laundrifydc.MainActivity.orderDB;

public class Tab3Frag extends Fragment {
    Context context;
    RecyclerView.Adapter deliveryAdapter;
    List<Collected_Model> deliveryModels;
    TextView descText;
    RecyclerView deliveryRV;
    ProgressBar pBar;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2_frag,null);
        context = v.getContext();

        deliveryRV = v.findViewById(R.id.tab2Rec);
        descText = v.findViewById(R.id.descText);
        pBar = v.findViewById(R.id.progressBar);
        descText.setText("This shows all pending deliveries for the next seven days");
        deliveryRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        deliveryAdapter = new Tab3Frag.Delivery_Adapter();
        deliveryModels = new ArrayList<>();
        deliveryRV.setAdapter(deliveryAdapter);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mainSnap == null) {
            getSnapAgain();
        } else if (deliveryModels.size() == 0) {
            feedData();
        } else {

        }
    }

    //Get snap again from database
    private void getSnapAgain () {
        if (orderDB == null) {
            orderDB = FirebaseDatabase.getInstance().getReference().child("Orders");
        }
        showProgressDialog("Please wait...", "Fetching data...", (long) 50000);
        orderDB = mainRef.child("Orders");
        orderDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
        for (int i = 0; i <= 7; i++) {
            Date date;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, i);
            date = cal.getTime();

            //Add tab !!!
            String dayDate = new SimpleDateFormat("EEEE dd").format(date);
            final String dayMonth = new SimpleDateFormat("MMMM").format(date);
            final String dataDay = new SimpleDateFormat("yyyyMMdd").format(date);
            String addition;
            String number = new SimpleDateFormat("dd").format(date);

            if (number.matches("01")) {
                addition = "st ";
            } else if (number.matches("01")) {
                addition = "nd ";
            } else if (number.matches("01")) {
                addition = "rd ";
            } else {
                addition = "th ";
            }

            if (number.substring(0,1).matches("0")) {
                number = number.substring(1);
            }
            if (i == 0) {
                dayDate = "Today " + "(" + String.valueOf(number) + addition + dayMonth + ")" ;
            } else if (i == 1) {
                dayDate = "Tomorrow " + "(" + String.valueOf(number) + addition + dayMonth + ")" ;
            } else {
                dayDate = dayDate + addition + dayMonth;
            }

            //Active orders are neither collected nor pending deliveries
            final DatabaseReference dateRef  = mainRef.child("Users").child("DryCleaners").child("0rYxL9Vp5yM3NrKs0upy0xz4S0D3")
                    .child("PendingDeliveries").child(dataDay);
            final String finalDayDate = dayDate;
            dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final String count = String.valueOf(dataSnapshot.getChildrenCount());
                    final ArrayList<String> keys = new ArrayList<>();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        keys.add(child.getKey());
                    }
                    deliveryModels.add(new Collected_Model(finalDayDate, count, keys, dataDay));
                    deliveryAdapter.notifyDataSetChanged();
                    pBar.setVisibility(View.GONE);
                    deliveryRV.setVisibility(View.VISIBLE);
                    //Log.i("test", "No orders for " + dataSnapshot.toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        }
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
                    DialogFragment newFrag = new ActiveDeFrag();
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
