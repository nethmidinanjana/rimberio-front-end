package lk.nd.rimberio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import adapter.SelectBranchViewAdapter;
import model.BranchItem;

public class BranchSelectionActivity extends AppCompatActivity {

    Button selectButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_branch_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.branchRecyclerView);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        selectButton = findViewById(R.id.buttonSelectBranch);
        selectButton.setEnabled(false);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("branch").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            QuerySnapshot querySnapshot = task.getResult();
                            List<BranchItem> branchItems = new ArrayList<>();

                            for(QueryDocumentSnapshot snapshot: querySnapshot){
                                String branchName = snapshot.getString("name");
                                Long id = snapshot.getLong("id");

                                branchItems.add(new BranchItem(id.intValue(), branchName, false));
                            }

                            recyclerView.setLayoutManager(new LinearLayoutManager(BranchSelectionActivity.this));


                            SelectBranchViewAdapter branchViewAdapter = new SelectBranchViewAdapter(branchItems, postion -> {
                                selectButton.setEnabled(true);
                                BranchItem clickedItem = branchItems.get(postion);
//                                Toast.makeText(BranchSelectionActivity.this, "Clicked: "+clickedItem.getId(), Toast.LENGTH_LONG).show();
                                String bid = String.valueOf(clickedItem.getId());

                                selectButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //TODO: Get the branch value and store in the database according to user
                                        DocumentReference documentReference = firestore.collection("user").document(userId);

                                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                if(task.isSuccessful()){
                                                    DocumentSnapshot snapshot = task.getResult();

                                                    if(snapshot.exists()){
                                                        documentReference.update("branch_id", bid)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Log.i("rimberioLogBranchSelect", "Success");
                                                                        Intent i = new Intent(BranchSelectionActivity.this, MainActivity.class);
                                                                        i.putExtra("userId", userId);
                                                                        startActivity(i);
                                                                    }
                                                                });
                                                    }
                                                }else{
                                                    Toast.makeText(BranchSelectionActivity.this, "Failed to fetch user!", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });


                                    }
                                });
                            });
                            recyclerView.setAdapter(branchViewAdapter);
                        }
                    }
                });






    }
}