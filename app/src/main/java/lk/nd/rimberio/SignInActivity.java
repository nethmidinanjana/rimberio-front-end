package lk.nd.rimberio;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import api.ApiService;
import api.BackendResponse;
import api.SignInRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.Constants;
import utils.UserPreferences;
import utils.Validator;

public class SignInActivity extends AppCompatActivity {

    EditText signInEmailEditText, signInPasswordEditText;
    CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        signInEmailEditText = findViewById(R.id.signInEmailEditText);
        signInPasswordEditText = findViewById(R.id.signInPasswordEditText);
        rememberMeCheckBox = findViewById(R.id.checkBoxRememberMe);

        if(savedInstanceState != null){
            signInEmailEditText.setText(savedInstanceState.getString("email"));
            signInPasswordEditText.setText(savedInstanceState.getString("password"));
            rememberMeCheckBox.setChecked(savedInstanceState.getBoolean("isChecked", false));
        }

        int checkedColor = ContextCompat.getColor(this, R.color.main_yellow);
        int uncheckedColor = ContextCompat.getColor(this, R.color.placeholder_text_gray);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, // checked state
                        new int[]{} // unchecked state
                },
                new int[]{
                        checkedColor,    // color when checked
                        uncheckedColor   // color when unchecked
                }
        );

        rememberMeCheckBox.setButtonTintList(colorStateList);


        TextView textView = findViewById(R.id.textViewSignInLink);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        // sign in
        Button button = findViewById(R.id.buttonSignIn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String em = signInEmailEditText.getText().toString();
                String pw = signInPasswordEditText.getText().toString();
                Boolean ic = rememberMeCheckBox.isChecked();

                if (em.isBlank()) {
                    Toast.makeText(SignInActivity.this, "Email cannot be empty!", Toast.LENGTH_LONG).show();
                } else if (!Validator.isValidEmail(em)) {
                    Toast.makeText(SignInActivity.this, "Invalid email address!", Toast.LENGTH_LONG).show();
                } else if (pw.isBlank()) {
                    Toast.makeText(SignInActivity.this, "Password cannot be empty!", Toast.LENGTH_LONG).show();
                }else{
                    button.setEnabled(false);
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.BACKEND_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);

                    SignInRequest signInRequest = new SignInRequest(em, pw);

                    Call<BackendResponse> call = apiService.signin(signInRequest);

                    call.enqueue(new Callback<BackendResponse>() {
                        @Override
                        public void onResponse(Call<BackendResponse> call, Response<BackendResponse> response) {
                            Log.d("rimberioLogSignIn", response.raw().toString());

                            if(response.isSuccessful() && response.body() != null){
                                BackendResponse backendResponse = response.body();
                                Log.d("rimberioLogSignIn", "Server Response: " + backendResponse.getMessage());

                                if(backendResponse.isSuccess()){

                                    try {
                                        JSONObject userJson = new JSONObject(backendResponse.getMessage());
                                        String userId = userJson.getString("userId");
                                        String username = userJson.getString("username");
                                        String password = userJson.getString("password");

                                        if(ic){
                                            UserPreferences userPreferences = new UserPreferences(SignInActivity.this);
                                            userPreferences.saveUserData(userId, username, em, password);
                                        }

                                        Intent i = new Intent(SignInActivity.this, MainActivity.class);
                                        i.putExtra("userId", userId);
                                        startActivity(i);
                                        finish();
                                    } catch (JSONException e) {
                                        Log.e("rimberioLogSignIn", "JSON parsing error: " + e.getMessage());
                                    }

                                }else{
                                    button.setEnabled(true);
                                    Toast.makeText(SignInActivity.this, backendResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }else{
                                button.setEnabled(true);
                                try {
                                    String errorResponse = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                    Log.e("rimberioLogSignIn", "Raw Error Response: " + errorResponse);

                                    JSONObject jsonObject = new JSONObject(errorResponse);
                                    String errorMessage = jsonObject.optString("message", "Something went wrong!");

                                    Log.e("rimberioLogSignIn", "Extracted Error Message: " + errorMessage);

                                    new AlertDialog.Builder(SignInActivity.this)
                                            .setTitle("Error")
                                            .setMessage(errorMessage)
                                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                            .show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<BackendResponse> call, Throwable t) {
                            Log.e("rimberioLogSignIn", "Request failed: " + t.getMessage());
                            Toast.makeText(SignInActivity.this, "Network error. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });


                }

            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState); // Explicitly call AppCompatActivity's onSaveInstanceState

        outState.putString("email", signInEmailEditText.getText().toString());
        outState.putString("password", signInPasswordEditText.getText().toString());
        outState.putBoolean("isChecked", rememberMeCheckBox.isChecked());
    }

}