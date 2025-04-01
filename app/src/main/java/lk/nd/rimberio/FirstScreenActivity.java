package lk.nd.rimberio;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorKt;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FirstScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textViewIntro = findViewById(R.id.textViewIntro);

        String fullText = "The food at your \ndoorstep";
        String wordToColor = "food";

        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(wordToColor);
        int endIndex = startIndex + wordToColor.length();

        if(startIndex != -1){
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FAB043")),
                    startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textViewIntro.setText(spannableString);
        textViewIntro.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        Button button = findViewById(R.id.buttonGetStarted);

        Animation animation = AnimationUtils.loadAnimation(
                FirstScreenActivity.this, R.anim.bounce
        );

        button.startAnimation(animation);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(FirstScreenActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

    }
}