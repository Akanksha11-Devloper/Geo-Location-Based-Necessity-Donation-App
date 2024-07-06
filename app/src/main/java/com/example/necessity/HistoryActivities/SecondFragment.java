package com.example.necessity.HistoryActivities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.necessity.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    RecyclerView recyclerView2;
    myadapter adapter2;

    ArrayList<model> secondList;
    FirebaseFirestore db;

    public String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    boolean isNGO;

    public SecondFragment(boolean isNGO) {
        this.isNGO = isNGO;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root =  inflater.inflate(R.layout.fragment_second, container, false);

        recyclerView2 = root.findViewById(R.id.secondList);
        recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        secondList = new ArrayList<>();
        adapter2=new myadapter(getContext(), secondList, isNGO);
        recyclerView2.setAdapter(adapter2);


        db=FirebaseFirestore.getInstance();

        db.collection("donations").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list=queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot d:list)
                        {
                            model obj=d.toObject(model.class);
                            String Userid = (String) d.get("userid");

                            if (isNGO) {
                                if (Long.parseLong(String.valueOf(d.get("expiry"))) < System.currentTimeMillis())
                                    secondList.add(obj);
                            } else {
                                if (Userid.equals(userID))
                                    if (Long.parseLong(String.valueOf(d.get("expiry"))) < System.currentTimeMillis())
                                        secondList.add(obj);
                            }

//                            if(Userid.equals(userID)) {
//                                if (Long.parseLong(String.valueOf(d.get("expiry"))) < System.currentTimeMillis())
//                                    secondList.add(obj);
//                            }
                        }
                        adapter2.notifyDataSetChanged();
                    }
                });

        adapter2.setOnItemClickListener(new myadapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                db.collection("donations").document(secondList.get(position).getTimestamp())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                for (int i = 0; i < secondList.get(position).getLinks().size(); i++) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(secondList.get(position)
                                            .getLinks().get(i)).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                        }
                                    });
                                }

                                Toast.makeText(getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
                                secondList.remove(position);
                                adapter2.notifyDataSetChanged();

                            }
                        });
            }

            @Override
            public void onNotifyClick(int position) {

            }
        });

        return root;
    }

}