package lk.nd.rimberio;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import interfaces.BackButtonHandler;

public class BaseActivity extends AppCompatActivity implements BackButtonHandler {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupBackBtn(View view){
        ImageView backButton = view.findViewById(R.id.imageViewBackBtn);

        if(backButton != null){
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } else {
            Log.e("BaseActivity", "Back button not found!");  // Debugging Log
        }
    }
}
