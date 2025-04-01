package lk.nd.rimberio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import utils.UserPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hideSystemUI();
        ImageView imageView = findViewById(R.id.imageViewSplashLogo);
        imageView.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                imageView.setVisibility(View.VISIBLE);

                Animation animation = AnimationUtils.loadAnimation(
                        SplashActivity.this, R.anim.splash_logo_anim
                );
                imageView.startAnimation(animation);


            }
        }, 1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //checking if user in the app storage - if exists move to home else to get started

                UserPreferences userPreferences = new UserPreferences(SplashActivity.this);

                String userId = userPreferences.getUserId();

                if(userId != null && !userId.isEmpty()){
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Intent i = new Intent(SplashActivity.this, FirstScreenActivity.class);
                    startActivity(i);
                }

                finish();
            }
        }, 3000);
    }

    private void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        );
    }
}