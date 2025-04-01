package lk.nd.rimberio;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import api.ApiService;
import api.SignupRequest;
import api.BackendResponse;
import api.UserVerificationRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.Constants;
import utils.UserPreferences;
import utils.Validator;

public class SignUpActivity extends AppCompatActivity {

    private EditText username, email, password, reTypePassword;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_TIME = 30000;
    private TextView textViewSendVerification;
    private Dialog dialog;
    TextView errorTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        username = findViewById(R.id.usernameEditText);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        reTypePassword = findViewById(R.id.reTypePasswordEditText);
        textViewSendVerification = findViewById(R.id.textViewSendVerfication);
        textViewSendVerification.setVisibility(View.INVISIBLE);

        if(savedInstanceState != null){
            username.setText(savedInstanceState.getString("username"));
            email.setText(savedInstanceState.getString("email"));
            password.setText(savedInstanceState.getString("password"));
            reTypePassword.setText(savedInstanceState.getString("reTypePassword"));
        }

        dialog = new Dialog(SignUpActivity.this);



        //signup
        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Validations
                String uname = username.getText().toString();
                String em = email.getText().toString();
                String pw = password.getText().toString();
                String rtpw = reTypePassword.getText().toString();

                if(uname.isBlank()){
                    Toast.makeText(SignUpActivity.this, "Username cannot be empty or just spaces!", Toast.LENGTH_LONG).show();
                } else if (em.isBlank()) {
                    Toast.makeText(SignUpActivity.this, "Email cannot be empty or just spaces!", Toast.LENGTH_LONG).show();
                } else if (!Validator.isValidEmail(em)) {
                    Toast.makeText(SignUpActivity.this, "Invalid email address!", Toast.LENGTH_LONG).show();
                } else if (pw.isBlank()) {
                    Toast.makeText(SignUpActivity.this, "Password cannot be empty or just spaces!", Toast.LENGTH_LONG).show();
                } else if (!Validator.isValidPassword(pw)) {
                    Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters, with an uppercase letter and a number!", Toast.LENGTH_LONG).show();
                }else if (rtpw.isBlank()) {
                    Toast.makeText(SignUpActivity.this, "Please Re-Type your password!", Toast.LENGTH_LONG).show();
                } else if (!pw.equals(rtpw)) {
                    Toast.makeText(SignUpActivity.this, "Password and the Re-TyPassword does not match!", Toast.LENGTH_LONG).show();
                }else{
                    //signup and send verification code
                    startCountDown();
                    openDialog();
                    errorTxt = dialog.findViewById(R.id.errorTxt);
                    errorTxt.setVisibility(View.INVISIBLE);
                    //sending data to back-end
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.BACKEND_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);

                    SignupRequest request = new SignupRequest(uname, em, pw);

                    Call<BackendResponse> call = apiService.signup(request);

                    call.enqueue(new Callback<BackendResponse>() {
                        @Override
                        public void onResponse(Call<BackendResponse> call, Response<BackendResponse> response) {
                            if(response.isSuccessful()){
                                BackendResponse backendResponse = response.body();
                                Log.i("rimberioLog", backendResponse.getMessage()); //userId

                                String uId = backendResponse.getMessage();

                                if(backendResponse.isSuccess()){

                                    EditText verificationCode1 = dialog.findViewById(R.id.verificationCode1);
                                    EditText verificationCode2 = dialog.findViewById(R.id.verificationCode2);
                                    EditText verificationCode3 = dialog.findViewById(R.id.verificationCode3);
                                    EditText verificationCode4 = dialog.findViewById(R.id.verificationCode4);

                                    Button buttonVerify = dialog.findViewById(R.id.buttonVerify);
                                    buttonVerify.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Log.d("rimberioLog", "Verify Button Clicked");

                                            String v1 = verificationCode1.getText().toString();
                                            String v2 = verificationCode2.getText().toString();
                                            String v3 = verificationCode3.getText().toString();
                                            String v4 = verificationCode4.getText().toString();

                                            if (v1.isEmpty() || v2.isEmpty() || v3.isEmpty() || v4.isEmpty()) {
                                                Log.i("rimberioLog", "Please fill all the fields!");
                                                errorTxt.setText("Please fill all the fields!");
                                                errorTxt.setVisibility(View.VISIBLE);
                                            } else {

                                                errorTxt.setVisibility(View.INVISIBLE);
                                                buttonVerify.setEnabled(false);
                                                Log.d("rimberioLog", "Sending Verification Request...");

                                                UserVerificationRequest verificationRequest = new UserVerificationRequest(uId, v1 + v2 + v3 + v4);
                                                Call<BackendResponse> requestCall = apiService.verify(verificationRequest);

                                                requestCall.enqueue(new Callback<BackendResponse>() {
                                                    @Override
                                                    public void onResponse(Call<BackendResponse> call, Response<BackendResponse> response) {
                                                        buttonVerify.setEnabled(true);
                                                        Log.d("rimberioLog", "Server Response: " + response.body());
                                                        Log.d("rimberioLog", "Raw response: " + response.raw());


                                                        if (response.isSuccessful()) {
                                                            BackendResponse response1 = response.body();
                                                            Log.d("rimberioLog", "Server Response: " + response1.getMessage());

                                                            if (response1.isSuccess()) {
                                                                try {
                                                                    JSONObject userJson = new JSONObject(response1.getMessage());
                                                                    String userId = userJson.getString("userId");
                                                                    String username = userJson.getString("username");
                                                                    String email = userJson.getString("email");
                                                                    String password = userJson.getString("password");

                                                                    UserPreferences userPreferences = new UserPreferences(SignUpActivity.this);
                                                                    userPreferences.saveUserData(userId, username, email, password);

                                                                    Intent i = new Intent(view.getContext(), BranchSelectionActivity.class);
                                                                    i.putExtra("userId", userId);
                                                                    startActivity(i);
                                                                    finish();
                                                                } catch (JSONException e) {
                                                                    Log.e("rimberioLog", "JSON parsing error: " + e.getMessage());
                                                                }
                                                            } else {
                                                                errorTxt.setText("Invalid verification code");
                                                                errorTxt.setVisibility(View.VISIBLE);
                                                            }
                                                        } else {
                                                            //BUG: Server Response: null
                                                            errorTxt.setText("Invalid verification code");
                                                            errorTxt.setVisibility(View.VISIBLE);
                                                            Log.e("rimberioLog", "Response failed: " + response.message());
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<BackendResponse> call, Throwable t) {
                                                        buttonVerify.setEnabled(true);
                                                        errorTxt.setText("Something went wrong. Please try again later.");
                                                        errorTxt.setVisibility(View.VISIBLE);
                                                        Log.e("rimberioLog", "Request failed: " + t.getMessage());
                                                    }
                                                });
                                            }
                                        }
                                    });


                                }else{
                                    Toast.makeText(SignUpActivity.this, backendResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                try {
                                    // Debugging
                                    String errorResponse = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                    Log.e("SignupError", errorResponse);
                                    Toast.makeText(SignUpActivity.this, "Signup failed: " + errorResponse, Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<BackendResponse> call, Throwable t) {
                            Toast.makeText(SignUpActivity.this, "Network error. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    });



                }


            }
        });

        TextView textView = findViewById(R.id.textViewSignInLink);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(i);
            }
        });
    }

    private void startCountDown(){
        textViewSendVerification.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewSendVerification.setText(getResources().getString(R.string.send_verification_code_txt)+ " " + getResources().getString(R.string.in) + " " + millisUntilFinished / 1000+ "s");
            }

            @Override
            public void onFinish() {
                textViewSendVerification.setText(getResources().getString(R.string.send_verification_code_txt));
                textViewSendVerification.setOnClickListener(v -> {
                    openDialog();
                    startCountDown();
                });
            }
        }.start();
    }

    private void openDialog(){
        dialog.setContentView(R.layout.verification_alert_dialog);
        dialog.setCancelable(false);

        ImageView imageViewCancelIc = dialog.findViewById(R.id.imageViewCloseIc);
        imageViewCancelIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("username", username.getText().toString());
        outState.putString("email", email.getText().toString());
        outState.putString("password", password.getText().toString());
        outState.putString("reTypePassword", reTypePassword.getText().toString());
    }
}