package lk.nd.rimberio;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.UserPreferences;
import utils.Validator;

public class ProfileActivity extends BaseActivity {

    FirebaseFirestore firestore;
    Spinner spinnerBranch;
    String userId;
    SQLiteDatabase database;
    TextView unameTextView, emailTextView, pointsTextView;
    EditText addressEditText, contactEditText;
    Button addressSaveButton;
    private String userBranchId = null;

    private HashMap<String, String> branchesMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        setupBackBtn(findViewById(android.R.id.content));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        unameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        addressEditText = findViewById(R.id.addressEditText);
        contactEditText = findViewById(R.id.contactEditText);
        pointsTextView = findViewById(R.id.pointsTextView);

        firestore = FirebaseFirestore.getInstance();

        UserPreferences userPreferences = new UserPreferences(ProfileActivity.this);
        userId = userPreferences.getUserId();

        //if userId isn't available in the device storage
        if(userId == null || userId.isEmpty()){
            Bundle extras = getIntent().getExtras();

            if(extras != null){
                userId = extras.getString("userId", "");
            }
        }

        if(userId != null){
            setupDatabase();
            loadUserDataFromSQLite();
            syncUserDataFromFirestore();
        }

        // Load branches from the database
        spinnerBranch = findViewById(R.id.spinnerBranch);


        loadData("branch", spinnerBranch, branchesMap);


        //save address
        addressSaveButton = findViewById(R.id.addressSaveButton);
        addressSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText addressEditText = findViewById(R.id.addressEditText);
                EditText contactEditText = findViewById(R.id.contactEditText);

                String address = addressEditText.getText().toString();
                String contact = contactEditText.getText().toString();

                //save address on database
                if(address.isBlank()){
                    Toast.makeText(ProfileActivity.this, "Please enter an address", Toast.LENGTH_SHORT).show();
                } else if (contact.isBlank()) {
                    Toast.makeText(ProfileActivity.this, "Please enter a contact number", Toast.LENGTH_SHORT).show();
                }else if(!Validator.isValidContact(contact)){
                    Toast.makeText(ProfileActivity.this, "Invalid mobile number.", Toast.LENGTH_SHORT).show();
                }else{
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("address", address);
                    userData.put("contact", contact);

                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Confirm Address Change").setMessage("Are you sure you want to update your address?")
                            .setPositiveButton("Yes", (dialog, which)->{
                                firestore.collection("user").document(userId)
                                        .set(userData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid->{
                                            Toast.makeText(ProfileActivity.this, "Address saved successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e ->{
                                            Toast.makeText(ProfileActivity.this, "Failed to save address", Toast.LENGTH_SHORT).show();
                                        });
                            }).setNegativeButton("Cancel", null).show();

                    addressEditText.clearFocus();
                    contactEditText.clearFocus();

                }
            }
        });


        //update branch
        Button saveBranchButton = findViewById(R.id.saveBranchButton);
        saveBranchButton.setOnClickListener(view->{

            String selectedBranch = spinnerBranch.getSelectedItem().toString();
            String selectedBranchId = branchesMap.get(selectedBranch);

            if(!selectedBranchId.equals(userBranchId)){
                if(spinnerBranch.getSelectedItemPosition() == -1){
                    Toast.makeText(this, "Please select a branch", Toast.LENGTH_SHORT).show();
                } else{
                    new AlertDialog.Builder(this)
                            .setTitle("Confirm Branch Change").setMessage("Are you sure you want to update your branch?")
                            .setPositiveButton("Yes", (dialog, which)->{
                                firestore.collection("user").document(userId).update("branch_id",selectedBranchId)
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(this, "Branch updated successfully!", Toast.LENGTH_SHORT).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Failed to update branch", Toast.LENGTH_SHORT).show()
                                        );
                            }).setNegativeButton("Cancel", null).show();


                }
            }


        });

        //load user and store in the database

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(view ->{
            userPreferences.clearUserData();

            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }

    private void loadData(String collectionName, Spinner spinner, HashMap<String, String> dataMap) {
        firestore.collection(collectionName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> namesList = new ArrayList<>();

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String id = String.valueOf(snapshot.getLong("id").intValue());
                        String name = snapshot.getString("name");

                        if (name != null) {
                            namesList.add(name);
                            dataMap.put(name, id);
                        }
                    }

                    // Create and set adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namesList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    if (collectionName.equals("branch") && userBranchId != null && !userBranchId.isEmpty()) {
                        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                            if (entry.getValue().equals(userBranchId)) {
                                int position = adapter.getPosition(entry.getKey());
                                spinner.setSelection(position);
                                break;
                            }
                        }
                    }

                    // Handle selection
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                            String selectedName = adapterView.getItemAtPosition(position).toString();
                            String selectedId = dataMap.get(selectedName);
                            Log.d("SelectedItem", "Name: " + selectedName + ", ID: " + selectedId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });

                }).addOnFailureListener(e -> Log.e("FirestoreError", "Error loading " + collectionName, e));
    }

    private void setupDatabase(){
        database = openOrCreateDatabase("rimberioDb", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS user ("
                + "id TEXT PRIMARY KEY, "
                + "name TEXT, "
                + "email TEXT, "
                + "branch_id TEXT, "
                + "points INTEGER, "
                + "address TEXT, "
                + "contact TEXT)"
        );
    }

    private void loadUserDataFromSQLite(){
        Cursor cursor = database.rawQuery("SELECT * FROM user WHERE id = ?", new String[]{userId});

        if(cursor.moveToFirst()){
            int nameIndex = cursor.getColumnIndex("name");
            int emailIndex = cursor.getColumnIndex("email");
            int branchIdIndex = cursor.getColumnIndex("branch_id");
            int pointsIndex = cursor.getColumnIndex("points");
            int addressIndex = cursor.getColumnIndex("address");
            int contactIndex = cursor.getColumnIndex("contact");

            if (nameIndex == -1 || emailIndex == -1 || branchIdIndex == -1 || pointsIndex == -1) {
                Log.e("SQLiteError", "Some columns are missing!");
            }else{
                String name = cursor.getString(nameIndex);
                String email = cursor.getString(emailIndex);
                String branchId = cursor.getString(branchIdIndex);
                userBranchId = cursor.getString(branchIdIndex);

                Log.i("RimberioLogProfile", branchId);
                Log.i("RimberioLogProfile", userBranchId);

                int points = cursor.getInt(pointsIndex);

                String address = addressIndex == -1 ? "" : cursor.getString(addressIndex);
                String contact = contactIndex == -1 ? "" : cursor.getString(contactIndex);

                updateUi(name, email, points, branchId, address, contact);
            }
        }

        cursor.close();
    }

    private void updateUi(String name, String email, int points, String branchId, String address, String contact){
        unameTextView.setText(name);
        emailTextView.setText(email);
        pointsTextView.setText(String.valueOf(points));
        addressEditText.setText(address != null ? address : "");
        contactEditText.setText(contact != null ? contact : "");
    }

    private void syncUserDataFromFirestore(){
        DocumentReference docref = firestore.collection("user").document(userId);
        docref.addSnapshotListener((snapshot, e)-> {
            if (e != null) {
                Log.e("FirestoreError", "Error listening to Firestore updates", e);
                return;
            }

            if(snapshot != null && snapshot.exists()){
                String name = snapshot.getString("username");
                String email = snapshot.getString("email");
                String branchId = snapshot.getString("branch_id");
                int points = snapshot.getLong("points").intValue();

                String address = snapshot.contains("address") ? snapshot.getString("address") : null;
                String contact = snapshot.contains("contact") ? snapshot.getString("contact") : null;

                updateUi(name, email, points, branchId, address, contact);

                saveUserDataToSQLite(name, email, branchId, points, address, contact);
            }
        });
    }

    private void saveUserDataToSQLite(String name, String email, String branchId, int points, @Nullable String address, @Nullable String contact){

        ContentValues values = new ContentValues();
        values.put("id", userId);
        values.put("name", name);
        values.put("email", email);
        values.put("branch_id", branchId);
        values.put("points", points);

        if (address != null) {
            values.put("address", address);
        }
        if (contact != null) {
            values.put("contact", contact);
        }

        database.insertWithOnConflict("user", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d("SQLiteUpdate", "User data updated in SQLite");
    }
}

