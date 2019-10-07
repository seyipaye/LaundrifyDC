package ng.com.laundrifydc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static ng.com.laundrifydc.MainActivity.dcDB;
import static ng.com.laundrifydc.MainActivity.dcOrdersSnap;
import static ng.com.laundrifydc.MainActivity.gettingSnap;
import static ng.com.laundrifydc.MainActivity.mainRef;
import static ng.com.laundrifydc.MainActivity.ordersRef;


public class ActiveDeFrag extends DialogFragment {
    Context context;
    RecyclerView.Adapter activeDeAdapter;
    static List<String> deKeys;
    static String deDataDay;
    List<Active_Model> active_models;
    private final int CALL_REQUEST = 100;
    private String callNumber;
    TextView descText;
    RecyclerView activeDeRV;
    ProgressBar pBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    static ActiveDeFrag newInstance(ArrayList<String> data, String dataDay) {
        deKeys = data;
        deDataDay = dataDay;
        return new ActiveDeFrag();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.active_frag, container, false);

        context = v.getContext();
        activeDeRV = v.findViewById(R.id.activeRec);
        descText = v.findViewById(R.id.descText);
        descText.setText("This shows all deliveries for the selected day");
        activeDeRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        activeDeAdapter = new ActiveDeFrag.ActiveDe_Adapter();
        active_models = new ArrayList<>();
        activeDeRV.setAdapter(activeDeAdapter);
         pBar = v.findViewById(R.id.progressBar);
        ImageView backButton = v.findViewById(R.id.fragClose);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (deKeys.size() > 0) {
            if (!(active_models.size() > 0)){
                getOrders();
            }
        } else {
            dismiss();
        }
    }

    private void getOrders() {
        //Log.i("test", "Keys are " + deKeys);
        for (Iterator<String> it = deKeys.iterator(); it.hasNext(); ) {
            final String thisKey = it.next();
            Log.i("test", thisKey);
            mainRef.child("Orders").child(thisKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                //Log.i("test", "1 Order yay");
                                final Map dataMap = (Map<String, Object>) dataSnapshot.getValue();
                                final String newKey = dataSnapshot.getKey();
                                convertNadd(dataMap, newKey);
                            } else {
                                Log.i("test", "No Order");
                                dcDB.child("Orders").child("Delivery").child(deDataDay).child(thisKey).removeValue();
                                refreshDB();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void convertNadd(Map dataMap, String id) {

        //Delivery date matches
        if (chgS(dataMap.get("DeliveryStamp")).substring(0, 8).matches(deDataDay)) {
            String details;
            if (String.valueOf(dataMap.get("Fragrance")).matches("true")) {
                details = "Fragranced Laundry | " + (dataMap.get("Vehicle")) + " Pickup";
            } else {
                details = "No Fragrance needed | " + (dataMap.get("Vehicle")) + " Pickup";
            }
            String name = (dataMap.get("FirstName")) + " " + (dataMap.get("LastName"));


            active_models.add(new Active_Model(id, chgS(dataMap.get("UserID")), name, chgB(dataMap.get("WeeklyPickup")), details, chgS(dataMap.get("CollectionTime")),
                    chgS(dataMap.get("PickupAddress")), chgS(dataMap.get("DeliveryTime")), chgS(dataMap.get("PickupAddress")), chgS(dataMap.get("Note")), chgI(dataMap.get("Price")), chgB(dataMap.get("HavePaid")),
                    chgS(dataMap.get("PhoneNumber")), chgI(dataMap.get("Progress"))));
            activeDeAdapter.notifyDataSetChanged();

            Log.i("test", "Converted, remove PB");

            hideProgressDialog();
            activeDeRV.setVisibility(View.VISIBLE);
            pBar.setVisibility(View.GONE);
        } else {

            //Delivery day dosen't match change to a newer date
            dcDB.child("Orders").child("Delivery").child(chgS(dataMap.get("DeliveryStamp")).substring(0, 8))
                    .child(id).child("TimeStamp").setValue(chgS(dataMap.get("DeliveryStamp")));

            dcDB.child("Orders").child("Delivery").child(deDataDay).child(id).removeValue();
            informDC();
            refreshDB();
        }

        if ((chgI(dataMap.get("Progress")) == 4)) {

            // Waiting for confirmation
            Snackbar.make(activeDeRV, "Some orders need payment confirmation", Snackbar.LENGTH_SHORT);
        }
    }

    //Get snap again from database
    private void refreshDB() {
        gettingSnap = true;
        showProgressDialog("", "Refreshing data...", (long) 50000);
        if (ordersRef == null) {
            ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        }
        dcDB.child("Orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    gettingSnap = false;
                    dcOrdersSnap = dataSnapshot;
                    MainActivity.coRefreshed = true;
                    MainActivity.deRefreshed = true;
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

    private void informDC() {
        Toast.makeText(context, "User Changed Delivery time!", Toast.LENGTH_SHORT).show();
    }

    private String chgS(Object obj) {
        return String.valueOf(obj);
    }
    private boolean chgB(Object obj) {
        return Boolean.valueOf(String.valueOf(obj));
    }
    private int chgI(Object obj) {
        return Integer.valueOf(String.valueOf(obj));
    }
    public class ActiveDe_Adapter extends RecyclerView.Adapter<ActiveDe_Adapter.MyViewHolder> {

        public ActiveDe_Adapter() {}

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView orderName;
            TextView orderID;
            TextView details;
            TextView weeklyPickupT;
            TextView orderCoTime;
            TextView orderCoAdd;
            TextView orderDeTime;
            TextView orderDeAdd;
            TextView notesHeader;
            TextView notes;
            LinearLayout paymentLinear;
            Button priceText;
            TextView notPaidText;
            Switch paySwitch;
            TextView paidText;
            TextView pNumber;
            ImageView orderDp;
            Button startButton;
            Button callButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.orderName = itemView.findViewById(R.id.orderName);
                this.orderID = itemView.findViewById(R.id.orderID);
                this.details = itemView.findViewById(R.id.details);
                this.weeklyPickupT = itemView.findViewById(R.id.weeklyPickupT);
                this.orderCoTime = itemView.findViewById(R.id.orderCoTime);
                this.orderCoAdd = itemView.findViewById(R.id.orderCoAdd);
                this.orderDeTime = itemView.findViewById(R.id.orderDeTime);
                this.orderDeAdd = itemView.findViewById(R.id.orderDeAdd);
                this.notesHeader = itemView.findViewById(R.id.notesHeader);
                this.notes = itemView.findViewById(R.id.notes);
                this.paymentLinear = itemView.findViewById(R.id.paymentLinear);
                this.priceText = itemView.findViewById(R.id.priceText);
                this.notPaidText = itemView.findViewById(R.id.notPaidText);
                this.paySwitch = itemView.findViewById(R.id.payswitch);
                this.paidText = itemView.findViewById(R.id.paidText);
                this.pNumber = itemView.findViewById(R.id.pNumber);
                this.orderDp = itemView.findViewById(R.id.orderDp);
                this.startButton = itemView.findViewById(R.id.startButton);
                this.callButton = itemView.findViewById(R.id.callButton);
            }
        }

        @NonNull
        @Override
        public ActiveDe_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.active_row, parent, false);
            ActiveDe_Adapter.MyViewHolder myViewHolder = new ActiveDe_Adapter.MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ActiveDe_Adapter.MyViewHolder holder, final int position) {
            TextView orderName = holder.orderName;
            TextView orderID = holder.orderID;
            TextView details = holder.details;
            TextView weeklyPickupT = holder.weeklyPickupT;
            TextView orderCoTime = holder.orderCoTime;
            TextView orderCoAdd = holder.orderCoAdd;
            TextView orderDeTime = holder.orderDeTime;
            TextView orderDeAdd = holder.orderDeAdd;
            TextView notesHeader = holder.notesHeader;
            final TextView noteTV = holder.notes;
            LinearLayout paymentLinear = holder.paymentLinear;
            final Button priceText = holder.priceText;
            final TextView notPaidText = holder.notPaidText;
            final Switch paySwitch = holder.paySwitch;
            final TextView paidText = holder.paidText;
            TextView pNumber = holder.pNumber;
            ImageView orderDp = holder.orderDp;
            final Button startButton = holder.startButton;
            Button callButton = holder.callButton;

            final boolean[] isClosed = {true};

            final String order_ID = active_models.get(position).getOrderID();
            final boolean isWeekly = active_models.get(position).isWeeklyPickup();
            final boolean isPaid = active_models.get(position).isPayed();
            final int price = active_models.get(position).getPrice();
            final String noteT = active_models.get(position).getNote();
            final String orderNameT = active_models.get(position).getOrderName();
            final String deAdd = active_models.get(position).getDeAdd();
            final String isFragranced = active_models.get(position).getDeAdd();
            boolean hasStarted;

            //Reset flexible things to default
            orderDp.setImageResource(R.drawable.ic_contacts_black_24dp);
            orderName.setText(orderNameT);
            orderID.setText(order_ID);

            // Show Details
            details.setText(active_models.get(position).getDetails());

            // Show Weekly pickup floater
            if (isWeekly) {
                weeklyPickupT.setVisibility(View.VISIBLE);
            } else {
                weeklyPickupT.setVisibility(View.GONE);
            }

            orderCoTime.setText("Time: " + active_models.get(position).getCoTime());
            orderCoAdd.setText("Date: " + active_models.get(position).getCoAdd());
            orderDeTime.setText("Time: " + active_models.get(position).getDeTime());
            orderDeAdd.setText("Date: " + deAdd);

            // Assign Note
            if (noteT.matches("")) {
                notesHeader.setVisibility(View.GONE);
            } else {
                notesHeader.setVisibility(View.VISIBLE);
                noteTV.setVisibility(View.GONE);
                setUnderLineText(notesHeader, null, "Note");
                notesHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isClosed[0]) {
                            noteTV.setVisibility(View.VISIBLE);
                            noteTV.setText(noteT);
                            isClosed[0] = false;
                        } else {
                            noteTV.setVisibility(View.GONE);
                            isClosed[0] = true;
                        }

                    }
                });
            }

            // Configure Payment
            if (price > 0) {
                priceText.setText("₦" + price);
                if (isPaid) {
                    paySwitch.setChecked(true);
                    paySwitch.setClickable(false);
                    paidText.setTextSize(18);
                    notPaidText.setTextSize(10);
                } else {
                    paySwitch.setChecked(false);
                    notPaidText.setTextSize(18);
                    paidText.setTextSize(10);
                    paySwitch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeIfPaid("Confirm payment", "Are you sure " + orderNameT + " has paid ?", "HavePaid",
                                    paySwitch, order_ID, "true", position, startButton, deAdd);
                        }
                    });
                }
                paySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            paidText.setTextSize(18);
                            notPaidText.setTextSize(10);
                        } else {
                            notPaidText.setTextSize(18);
                            paidText.setTextSize(10);
                        }
                    }
                });
            } else {

                // config. switch
                paySwitch.setChecked(false);
                paySwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "Failed: ADD price first", Toast.LENGTH_SHORT).show();
                        paySwitch.setChecked(false);
                    }
                });

                //config. Price text
                priceText.setText("add Price");
                setUnderLineText(null, priceText, "add Price");
                priceText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputPrice(order_ID);
                    }
                });
            }

            pNumber.setText("Phone no.: " + active_models.get(position).getpNumber());

            //Call Button
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makeCall(active_models.get(position).getpNumber());
                }
            });

            if (active_models.get(position).getProgress() >= 3) {

                //Confirm the delivery
                startButton.setText("Confirm Delivery");
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveToHistory(position, order_ID);
                    }
                });
            } else {

                //Start Button
                startButton.setText("Confirm Payment");
                startButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (price > 0) {
                            startDelivery(deAdd, order_ID, startButton, position);
                        } else {
                            Toast.makeText(context, "Failed, add Price first", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            Log.i("test", String.valueOf(deKeys));
        }

        private void moveToHistory(final int position, String order_ID) {
            if (active_models.get(position).isPayed()) {
                Toast.makeText(context, "Storing to History", Toast.LENGTH_SHORT).show();
                showProgressDialog("Please wait", "Confirming delivering...", (long) 50000);

                //Order Payed
                if (active_models.get(position).getProgress() == 4){

                    //Payment uploaded by user Already
                    // Shake your ass ;)
                    //Remove from main DB
                    mainRef.child("Orders").child(order_ID)
                            .removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    Toast.makeText(getContext(), "Delivery Confirmed", Toast.LENGTH_SHORT).show();
                                    hideProgressDialog();
                                    active_models.remove(position);
                                    activeDeAdapter.notifyItemRemoved(position);
                                }
                            });
                } else {

                    //Get Order Snapshot
                    mainRef.child("Orders").child(active_models.get(position).getOrderID())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                    //Put snapshot in main history
                                    mainRef.child("History").child("Orders").child(dataSnapshot.getKey())
                                            .setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                    //Move to DC's history
                                                    dcDB.child("History").child("Orders").child(dataSnapshot.getKey()).child("TimeStamp")
                                                            .setValue(dataSnapshot.child("DeliveryStamp").getValue().toString(), new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                                    //Move in customer's DB
                                                                    DatabaseReference cusRef = mainRef.child("Users").child("Customers").child(dataSnapshot.child("UserID").getValue().toString());

                                                                    //Add to customer history with timestamp
                                                                    cusRef.child("History").child("Orders").child(dataSnapshot.getKey()).child("TimeStamp")
                                                                            .setValue(dataSnapshot.child("DeliveryStamp").getValue().toString(), new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                                                    //Remove from main DB
                                                                                    mainRef.child("Orders").child(dataSnapshot.getKey())
                                                                                            .removeValue(new DatabaseReference.CompletionListener() {
                                                                                                @Override
                                                                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                                                    Toast.makeText(getContext(), "Delivery Confirmed", Toast.LENGTH_SHORT).show();
                                                                                                    hideProgressDialog();
                                                                                                    active_models.remove(position);
                                                                                                    activeDeAdapter.notifyItemRemoved(position);
                                                                                                }
                                                                                            });
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                }
                                            });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            } else {
                Toast.makeText(context, "Failed, Confirm Payment first", Toast.LENGTH_SHORT).show();
            }
        }

        private void startDelivery(String add, final String orderID, final Button startButton, final int position) {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Confirm decision")
                    .setMessage("Are you sure you want to deliver to " + add + " now?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            showProgressDialog("Please wait", "Informing the customer", (long) 50000);
                            mainRef.child("Orders").child(orderID).child("Progress")
                                    .setValue(3, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            hideProgressDialog();
                                            Toast.makeText(context, "Successful update", Toast.LENGTH_SHORT).show();

                                            startButton.setText("Confirm Delivery");
                                            startButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    moveToHistory(position, orderID);
                                                }
                                            });
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(context,"Cancelled !!!",Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        }

         private void changeIfPaid(String title, String msg, final String child, final Switch paySwitch,
                                  final String orderID, final String value, final int position, final Button startButton, final String deAdd) {
             paySwitch.setChecked(false);
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            showProgressDialog("Please wait", "Uploading data... do not cancel", (long) 50000);
                            mainRef.child("Orders").child(orderID).child(child)
                                    .setValue(value, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            hideProgressDialog();
                                            Toast.makeText(context, "Successful update", Toast.LENGTH_SHORT).show();
                                                paySwitch.setChecked(true);
                                                paySwitch.setClickable(false);
                                                active_models.get(position).setPayed(true);
                                                dismiss();

                                        }
                                    });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(context,"Cancelled !!!",Toast.LENGTH_LONG).show();
                                paySwitch.setChecked(false);
                        }
                    })
                    .show();
        }

        private void inputPrice(final String orderID) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dView = inflater.inflate(R.layout.edittext_dialog, null);
            final EditText dEditText = dView.findViewById(R.id.dialogEditText);
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    new ContextThemeWrapper(context, R.style.DialogStyle));
            builder.setTitle("Warning")
                    .setMessage("This can never be changed again!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            showProgressDialog("Please wait", "Uploading data... do not cancel", (long) 50000);
                            mainRef.child("Orders").child(orderID).child("Price")
                                    .setValue(dEditText.getText().toString(), new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            hideProgressDialog();
                                            Toast.makeText(context, "Successful update", Toast.LENGTH_SHORT).show();
                                            dismiss();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(context,"Cancelled !!!",Toast.LENGTH_LONG).show();
                        }
                    });
            builder.setView(dView);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });
        }

        public void setUnderLineText(TextView tv, Button bt, String textToUnderLine) {
            if (bt == null) {
                String tvt = tv.getText().toString();
                int ofe = tvt.indexOf(textToUnderLine, 0);

                UnderlineSpan underlineSpan = new UnderlineSpan();
                SpannableString wordToSpan = new SpannableString(tv.getText());
                for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                    ofe = tvt.indexOf(textToUnderLine, ofs);
                    if (ofe == -1)
                        break;
                    else {
                        wordToSpan.setSpan(underlineSpan, ofe, ofe + textToUnderLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
                    }
                }
            } else {
                String tvt = bt.getText().toString();
                int ofe = tvt.indexOf(textToUnderLine, 0);

                UnderlineSpan underlineSpan = new UnderlineSpan();
                SpannableString wordToSpan = new SpannableString(bt.getText());
                for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                    ofe = tvt.indexOf(textToUnderLine, ofs);
                    if (ofe == -1)
                        break;
                    else {
                        wordToSpan.setSpan(underlineSpan, ofe, ofe + textToUnderLine.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        bt.setText(wordToSpan, Button.BufferType.SPANNABLE);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return active_models.size();
        }
    }

    private void makeCall(String getpNumber) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, CALL_REQUEST);
                    return;
                }
            }
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + getpNumber));
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
                makeCall(callNumber);
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
