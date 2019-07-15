package ng.com.laundrifydc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static ng.com.laundrifydc.MainActivity.mainRef;
import static ng.com.laundrifydc.MainActivity.mainSnap;
import static ng.com.laundrifydc.MainActivity.orderDB;


public class ActiveDeFrag extends DialogFragment {
    Context context;
    RecyclerView.Adapter activeDeAdapter;
    List<Active_Model> activeModels;
    private final int CALL_REQUEST = 100;
    private String callNumber;
    TextView descText;
    RecyclerView activeDeRV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.active_frag, container, false);

        context = v.getContext();
        activeDeRV = v.findViewById(R.id.activeRec);
        descText = v.findViewById(R.id.descText);
        descText.setText("This shows all deliveries for the selected day");
        activeDeRV.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        activeDeAdapter = new ActiveDeFrag.ActiveDe_Adapter();
        activeModels = new ArrayList<>();
        activeDeRV.setAdapter(activeDeAdapter);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
        if (deKeys.size() > 0) {
            if (!(active_models.size() > 0)){
                getOrders();
            }
        } else {
            dismiss();
        }
        */
    }

    private void getSnapAgain() {
        showProgressDialog("Please wait...", "Fetching data...", (long) 50000);
        orderDB = mainRef.child("Orders");
        orderDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mainSnap = dataSnapshot;
                    //feedData();
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


    public class ActiveDe_Adapter extends RecyclerView.Adapter<ActiveDe_Adapter.MyViewHolder> {

        public ActiveDe_Adapter() {}

        public class MyViewHolder extends RecyclerView.ViewHolder {
            // TextView collectedTotal;
            //TextView collectedDay;
            // Button collectedButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // this.collectedTotal = itemView.findViewById(R.id.collectedTotal);
                // this.collectedDay = itemView.findViewById(R.id.collectedDay);
                // this.collectedButton = itemView.findViewById(R.id.collectedButton);
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
            // TextView collectedTotal = holder.collectedTotal;
            // TextView collectedDay = holder.collectedDay;
            // Button collectedButton = holder.collectedButton;

            /*
            final String order_ID;
            order_ID = activeCoModels.get(position).getKey();

            collectedTotal.setText(activeCoModels.get(position).getTotal());

            collectedDay.setText(activeCoModels.get(position).getDay());

            collectedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(context, "Swiping fragments", Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.sliding_in_left, R.anim.sliding_out_left, android.R.anim.slide_in_left
                                    , android.R.anim.slide_out_right );

                    DialogFragment newFrag = new ActiveCoFrag();
                    newFrag.show(fragmentManager, "tag");
                }
            });
            */
        }

        @Override
        public int getItemCount() {
            return 10;
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
