package ng.com.laundrifydc;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static ng.com.laundrifydc.MainActivity.dcDB;
import static ng.com.laundrifydc.MainActivity.mainRef;
import static ng.com.laundrifydc.MainActivity.orderDB;

public class Tab1Frag extends Fragment {
    Context context;
    RecyclerView.Adapter pendingAdapter;
    List<Pending_Model> pending_models;
    private final int CALL_REQUEST = 100;
    private String callNumber;
    TextView noOrder;
    RecyclerView pendingRC;
    ProgressBar pBar;
    DatabaseReference dcPendingOrdersDB;
    boolean gettingKeys;
    boolean gettingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tab_frag,null);
        context = v.getContext();

        pendingRC = v.findViewById(R.id.tabRec);
        noOrder = v.findViewById(R.id.noOrders);
        pBar = v.findViewById(R.id.progressBar);

        pendingRC.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        pendingAdapter = new Pending_Adapter();
        pending_models = new ArrayList<>();
        pendingRC.setAdapter(pendingAdapter);

        gettingKeys = false;
        gettingData = false;
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!(pending_models.size() > 0) && !gettingKeys) {
            getKeys();
        }
    }

    //Get Pending Keys by timeStamp
    //Check the pending Orders issue with multiple getData request
    //inteferes when deleting at the sametime requesting data
    private void getKeys() {
        gettingKeys = true;
        if (orderDB == null) {
            orderDB = FirebaseDatabase.getInstance().getReference().child("Orders");
        }
        Log.i("test", "getting Keys");
        dcPendingOrdersDB = dcDB.child("Orders").child("Pending");
        Query pendingQuery = dcPendingOrdersDB.orderByChild("TimeStamp");
        pendingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot != null) {
                    if (!gettingData) {
                        Log.i("test", "getting Data");
                        int childNumber = 0;
                        for (DataSnapshot dataChild : dataSnapshot.getChildren()) {
                            childNumber ++;
                            getData(dataChild.getKey(), childNumber, dataSnapshot.getChildrenCount());
                            Log.i("test", dataChild.getValue().toString() + "thiss");
                        }
                    }
                } else {
                    noOrder.setVisibility(View.VISIBLE);
                    pBar.setVisibility(View.GONE);
                    pendingRC.setVisibility(View.GONE);
                    gettingKeys = false;
                    Log.i("test", "Nothing");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                noOrder.setVisibility(View.GONE);
                pendingRC.setVisibility(View.GONE);
                pBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getData(final String key, final int childNumber, final long childrenCount) {
        orderDB.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                        Map dataMap = (Map<String, Object>) dataSnapshot.getValue();

                        //If it's Pending
                        if (String.valueOf(dataMap.get("Progress")).matches("0")) {
                            String details;
                            if (String.valueOf(dataMap.get("Fragrance")).matches("true")) {
                                details = "Fragranced Laundry | " + String.valueOf(dataMap.get("Vehicle")) + " Pickup";
                            } else {
                                details = "No Fragrance needed | " + String.valueOf(dataMap.get("Vehicle")) + " Pickup";
                            }
                            String name = String.valueOf(dataMap.
                                    get("FirstName")) + " " + String.valueOf(dataMap.get("LastName"));

                            if (childNumber == 1) {
                                pending_models.clear();
                            }
                            if (childNumber == childrenCount) {
                                gettingData = false;
                                gettingKeys = false;
                            }
                            pending_models.add(new Pending_Model(name , details,
                                    dataSnapshot.getKey(), String.valueOf(dataMap.get("CollectionTime")),
                                    String.valueOf(dataMap.get("PickupAddress")), String.valueOf(dataMap
                                    .get("Note")), String.valueOf(dataMap.get("PhoneNumber")),
                                    String.valueOf(dataMap.get("CollectionStamp")), String.valueOf(dataMap.get("DeliveryStamp"))));

                            pendingAdapter.notifyDataSetChanged();
                            pBar.setVisibility(View.GONE);
                            noOrder.setVisibility(View.GONE);
                            pendingRC.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                            Log.i("test", "Converted pending orders !!!");
                        } else {
                            dcPendingOrdersDB.child(key).removeValue();
                        }

                } else {
                   dcPendingOrdersDB.child(key).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                noOrder.setVisibility(View.GONE);
                pendingRC.setVisibility(View.GONE);
                pBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private class Pending_Adapter extends RecyclerView.Adapter<Pending_Adapter.MyViewHolder> {

        public Pending_Adapter() {}

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView orderName;
            TextView orderID;
            TextView details;
            TextView orderCoTime;
            TextView orderCoAdd;
            TextView notes;
            TextView notesHead;
            TextView pNumber;
            ImageView orderDp;
            Button button1;
            Button button2;
            Button button3;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.orderName = itemView.findViewById(R.id.orderName);
                this.orderID = itemView.findViewById(R.id.orderID);
                this.details = itemView.findViewById(R.id.details);
                this.orderCoTime = itemView.findViewById(R.id.orderCoTime);
                this.orderCoAdd = itemView.findViewById(R.id.orderCoAdd);
                this.notes = itemView.findViewById(R.id.notes);
                this.notesHead = itemView.findViewById(R.id.notesHeader);
                this.pNumber = itemView.findViewById(R.id.pNumber);
                this.orderDp = itemView.findViewById(R.id.orderDp);
                this.button1 = itemView.findViewById(R.id.button1);
                this.button2 = itemView.findViewById(R.id.button2);
                this.button3 = itemView.findViewById(R.id.button3);
            }
        }

        @NonNull
        @Override
        public Pending_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.pending_row, parent, false);
            Pending_Adapter.MyViewHolder myViewHolder = new Pending_Adapter.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull Pending_Adapter.MyViewHolder holder, final int position) {
            TextView orderName = holder.orderName;
            TextView orderID = holder.orderID;
            TextView details = holder.details;
            TextView orderCoTime = holder.orderCoTime;
            TextView orderCoAdd = holder.orderCoAdd;
            TextView notes = holder.notes;
            TextView notesHeader = holder.notesHead;
            TextView pNumber = holder.pNumber;
            ImageView orderDp = holder.orderDp;
            Button button1 = holder.button1;
            Button button2 = holder.button2;
            Button button3 = holder.button3;

            final String order_ID;
            order_ID = pending_models.get(position).getId();

            orderName.setText(pending_models.get(position).getOrderName());
            orderID.setText("ID: " + order_ID);
            details.setText(pending_models.get(position).getDetails());
            orderCoTime.setText("Time: " + pending_models.get(position).getCoTime());
            orderCoAdd.setText("Address: " + pending_models.get(position).getCoAdd());
            notes.setText(pending_models.get(position).getNote());
            orderDp.setImageResource(R.drawable.ic_contacts_black_24dp);
            final String collectionStamp = pending_models.get(position).getCollectionStamp();
            final String deliveryStamp = pending_models.get(position).getDeliveryStamp();

            if (pending_models.get(position).getNote().matches("")) {
                notesHeader.setVisibility(View.GONE);
                notes.setVisibility(View.GONE);
            }

            //Call shitt
            String toNumber = pending_models.get(position).getNumber();
            pNumber.setText("Phone no. :" + toNumber);

            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callNumber = pending_models.get(position).getNumber().replace("+23481", "081").replace("+23480", "080").replace("+23490", "090").replace(" ", "");
                    try {
                        callPhoneNumber(callNumber);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgressDialog("", "Accepting Order", (long) 10000);
                    dcDB.child("Orders").child("Collected").child(collectionStamp.substring(0, 8))
                            .child(order_ID).child("TimeStamp").setValue(collectionStamp);
                    dcDB.child("Orders").child("Delivery").child(deliveryStamp.substring(0, 8))
                            .child(order_ID).child("TimeStamp").setValue(deliveryStamp);
                    orderDB.child(order_ID).child("Progress").setValue("1", new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            hideProgressDialog();
                            Toast.makeText(context, "Order " + order_ID + " accepted.", Toast.LENGTH_SHORT).show();
                            pending_models.remove(position);
                            pendingAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });

            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProgressDialog("", "Cancelling Order", (long) 10000);
                    orderDB.child(order_ID).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            //It will remove itself if it dosen't find itself in the DB
                            hideProgressDialog();
                            Toast.makeText(context, "Order " + order_ID + " declined.", Toast.LENGTH_SHORT).show();
                            pending_models.remove(position);
                            pendingAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        /*
        orderText.setText(ordersSnap.get(position).getKey());
        isClosed[0] = true;
        orderText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);

        orderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClosed[0]) {
                    if (date_models.isEmpty()) {
                        //date model consist of Day:Time:OrderId
                        for (DataSnapshot child : ordersSnap.get(position).getChildren()) {
                            date_models.add(new DateModel(child.getKey(), child.getValue().toString(), ordersSnap.get(position).getKey()));
                        }
                    }

                    mapRecycler.setLayoutManager(new LinearLayoutManager(context));
                    mapRecycler.setVisibility(View.VISIBLE);
                    mapRecycler.setAdapter(new MapEditAdapter(date_models));
                    isClosed[0] = false;
                    orderText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
                } else {
                    orderText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
                    mapRecycler.setVisibility(View.GONE);
                    isClosed[0] = true;
                }

            }
        });
        */
        }

        @Override
        public int getItemCount() {
            return pending_models.size();
        }
    }

    /**
     * This method is responsible make a call and also
     * checking run time permissions for CALL_PHONE
     *
     * @param finalToNumber*/
    public void callPhoneNumber(String finalToNumber) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST);
                    return;
                }
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + finalToNumber));
            startActivity(callIntent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == CALL_REQUEST) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPhoneNumber(callNumber);
            }
            else {
                Toast.makeText(context,"Permission denied!", Toast.LENGTH_SHORT).show();
            }
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
