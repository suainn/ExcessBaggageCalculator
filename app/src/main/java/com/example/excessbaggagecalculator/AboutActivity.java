package com.example.excessbaggagecalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        // Keep content clear of the status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.aboutMain), (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Hide the default ActionBar if present.
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Toolbar toolbar = findViewById(R.id.aboutToolbar);

        toolbar.setTitle("About BaggageMate");
        toolbar.setTitleTextColor(0xFFFFFFFF);

        // Create a white back arrow.
        DrawerArrowDrawable backArrow = new DrawerArrowDrawable(this);
        backArrow.setColor(0xFFFFFFFF);
        backArrow.setProgress(1f);
        backArrow.setDirection(
                DrawerArrowDrawable.ARROW_DIRECTION_START
        );

        toolbar.setNavigationIcon(backArrow);
        toolbar.setNavigationContentDescription("Back to calculator");

        // Return to the existing calculator screen.
        toolbar.setNavigationOnClickListener(view -> finish());
    }
}