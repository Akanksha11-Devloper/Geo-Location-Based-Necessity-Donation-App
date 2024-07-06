package com.example.necessity.HistoryActivities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.necessity.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstFragment extends Fragment {

    RecyclerView recyclerView;
    myadapter adapter;

    ArrayList<model> firstList;
    FirebaseFirestore db;

    public String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    boolean isNGO;

    public FirstFragment(boolean isNGO) {
        this.isNGO = isNGO;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root =  inflater.inflate(R.layout.fragment_first, container, false);

        recyclerView = root.findViewById(R.id.firstList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firstList = new ArrayList<>();
        adapter=new myadapter(getContext(), firstList, isNGO);
        recyclerView.setAdapter(adapter);


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
                                if (Long.parseLong(String.valueOf(d.get("expiry"))) > System.currentTimeMillis())
                                    firstList.add(obj);
                            } else {
                                if (Userid.equals(userID))
                                    if (Long.parseLong(String.valueOf(d.get("expiry"))) > System.currentTimeMillis())
                                        firstList.add(obj);
                            }

//                            if(Userid.equals(userID)) {
//                                if (Long.parseLong(String.valueOf(d.get("expiry"))) > System.currentTimeMillis())
//                                    firstList.add(obj);
//                            } else {
//                                if (Long.parseLong(String.valueOf(d.get("expiry"))) > System.currentTimeMillis())
//                                    firstList.add(obj);
//                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        adapter.setOnItemClickListener(new myadapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                db.collection("donations").document(firstList.get(position).getTimestamp())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                for (int i = 0; i < firstList.get(position).getLinks().size(); i++) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(firstList.get(position)
                                            .getLinks().get(i)).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                        }
                                    });
                                }

                                Toast.makeText(getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
                                firstList.remove(position);
                                adapter.notifyDataSetChanged();

                            }
                        });
            }

            @Override
            public void onNotifyClick(int position) {

                Map<String,Object> user = new HashMap<>();
                user.put("id", userID);


                db.collection("notifications").document(firstList.get(position).getUserid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Notified donor", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed tp notify donor", Toast.LENGTH_SHORT).show();
                    }
                });

                FirebaseDatabase.getInstance().getReference("notification").child(firstList.get(position).getUserid()).setValue(userID);

            }
        });

        return root;
    }

}