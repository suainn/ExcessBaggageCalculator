package com.example.excessbaggagecalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL =
            "https://github.com/suainn/ExcessBaggageCalculator";

    private static final int MENU_SHARE = 1;
    private static final int MENU_ABOUT = 2;

    private EditText etWeight, etRate;
    private Spinner spinnerClass;
    private TextView tvExcessWeight;
    private TextView tvExcessCharge;
    private TextView tvTotalPayable;

    private ScrollView scrollMain;
    private View resultSection;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            View focusedView = getCurrentFocus();

            if (focusedView instanceof EditText) {

                Rect inputArea = new Rect();
                focusedView.getGlobalVisibleRect(inputArea);

                boolean tappedOutside = !inputArea.contains(
                        (int) event.getRawX(),
                        (int) event.getRawY()
                );

                if (tappedOutside) {
                    hideKeyboard();
                }
            }
        }

        // Allow buttons, dropdowns and other inputs to work normally.
        return super.dispatchTouchEvent(event);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Keep content clear of the status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (view, insets) -> {

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

        setupToolbar();

        // Connect XML components.
        etWeight = findViewById(R.id.etWeight);
        etRate = findViewById(R.id.etRate);
        spinnerClass = findViewById(R.id.spinnerClass);

        tvExcessWeight = findViewById(R.id.tvExcessWeight);
        tvExcessCharge = findViewById(R.id.tvExcessCharge);
        tvTotalPayable = findViewById(R.id.tvTotalPayable);

        scrollMain = findViewById(R.id.scrollMain);
        resultSection = findViewById(R.id.resultSection);

        Button btnCalculate = findViewById(R.id.btnCalculate);

        // Ticket class choices.
        String[] ticketClasses = {
                "Economy (20 kg)",
                "Business (40 kg)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                ticketClasses
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerClass.setAdapter(adapter);
        // Weight: Done closes the keyboard and shows ticket class.
        etWeight.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                scrollToSection(spinnerClass);
                return true;
            }

            return false;
        });

// Rate: Done closes the keyboard and shows Calculate Fee.
        etRate.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                scrollToSection(btnCalculate);
                return true;
            }

            return false;
        });

// Ignore the initial automatic selection when the page opens.
        spinnerClass.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    private boolean initialSelection = true;

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (initialSelection) {
                            initialSelection = false;
                            return;
                        }

                        hideKeyboard();
                        scrollToSection(etRate);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No action needed.
                    }
                }
        );

        btnCalculate.setOnClickListener(view -> calculateFee());
        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(view -> resetForm());
    }

    private void setupToolbar() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);

        // Logo and app name are provided by the XML.
        toolbar.setTitle("");
        toolbar.getMenu().clear();

        MenuItem shareItem = toolbar.getMenu().add(
                Menu.NONE,
                MENU_SHARE,
                0,
                "Share BaggageMate"
        );

        shareItem.setIcon(R.drawable.ic_share);
        shareItem.setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
        );

        MenuItem aboutItem = toolbar.getMenu().add(
                Menu.NONE,
                MENU_ABOUT,
                1,
                "About BaggageMate"
        );

        aboutItem.setIcon(R.drawable.ic_about);
        aboutItem.setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
        );

        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == MENU_SHARE) {
                shareApplication();
                return true;
            }

            if (item.getItemId() == MENU_ABOUT) {
                Intent intent = new Intent(
                        MainActivity.this,
                        AboutActivity.class
                );

                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void shareApplication() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "BaggageMate"
        );

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "BaggageMate – Your Baggage Fee Calculator\n"
                        + "Your companion for smarter packing.\n\n"
                        + APP_URL
        );

        startActivity(Intent.createChooser(
                shareIntent,
                "Share BaggageMate via"
        ));
    }

    private void calculateFee() {

        etWeight.setError(null);
        etRate.setError(null);
        resultSection.setVisibility(View.GONE);

        // Clear old results.
        tvExcessWeight.setText("—");
        tvExcessCharge.setText("—");
        tvTotalPayable.setText("—");

        BigDecimal weight = readNumber(
                etWeight,
                "Please enter the baggage weight."
        );

        BigDecimal rate = readNumber(
                etRate,
                "Please enter the rate per kg."
        );

        if (weight == null || rate == null) {
            if (weight == null) {
                etWeight.requestFocus();
            } else {
                etRate.requestFocus();
            }
            return;
        }

        if (weight.signum() < 0) {
            etWeight.setError("Weight cannot be negative.");
            etWeight.requestFocus();
            return;
        }

        if (rate.signum() <= 0) {
            etRate.setError("Rate must be greater than zero.");
            etRate.requestFocus();
            return;
        }

        // Economy: 20 kg. Business: 40 kg.
        BigDecimal allowance = new BigDecimal(
                spinnerClass.getSelectedItemPosition() == 0
                        ? "20" : "40"
        );

        BigDecimal excessWeight = weight.subtract(allowance)
                .max(BigDecimal.ZERO);

        BigDecimal excessCharge = excessWeight.multiply(rate);

        // Include the assignment's assumed 8% service tax.
        BigDecimal totalPayable = excessCharge.multiply(
                new BigDecimal("1.08")
        );

        tvExcessWeight.setText(String.format(
                Locale.US,
                "%s kg",
                formatNumber(excessWeight)
        ));

        tvExcessCharge.setText(String.format(
                Locale.US,
                "RM %s",
                formatNumber(excessCharge)
        ));

        tvTotalPayable.setText(String.format(
                Locale.US,
                "RM %s",
                formatNumber(totalPayable)
        ));

        // Scroll only after a successful calculation.
        showCalculationResult();
    }

    private void hideKeyboard() {

        View focusedView = getCurrentFocus();

        if (focusedView != null) {
            InputMethodManager keyboard =
                    (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE
                    );

            if (keyboard != null) {
                keyboard.hideSoftInputFromWindow(
                        focusedView.getWindowToken(),
                        0
                );
            }

            focusedView.clearFocus();
        }
    }

    private void scrollToSection(View target) {

        scrollMain.postDelayed(() -> {

            // Find the target's position inside the scrolling content.
            View content = scrollMain.getChildAt(0);
            Rect targetRect = new Rect();

            target.getDrawingRect(targetRect);

            ((android.view.ViewGroup) content)
                    .offsetDescendantRectToMyCoords(target, targetRect);

            // Leave some space above the next input for its label.
            int spacing = Math.round(
                    64 * getResources().getDisplayMetrics().density
            );

            scrollMain.smoothScrollTo(
                    0,
                    Math.max(0, targetRect.top - spacing)
            );

        }, 300);
    }

    private void showCalculationResult() {

        resultSection.setVisibility(View.VISIBLE);

        hideKeyboard();
        scrollToSection(resultSection);
    }

    private BigDecimal readNumber(
            EditText field,
            String emptyMessage
    ) {

        String input = field.getText().toString().trim();

        if (input.isEmpty()) {
            field.setError(emptyMessage);
            return null;
        }

        // Accept values such as 25, 42.5 and .5.
        if (!input.matches("(\\d+(\\.\\d*)?|\\.\\d+)")) {
            field.setError(
                    "Please enter a valid number, for example 25.5."
            );
            return null;
        }

        try {
            return new BigDecimal(input);
        } catch (NumberFormatException exception) {
            field.setError("Please enter a valid number.");
            return null;
        }
    }

    private void resetForm() {

        hideKeyboard();
        resultSection.setVisibility(View.GONE);

        // Clear inputs and validation errors.
        etWeight.setText("");
        etRate.setText("");
        etWeight.setError(null);
        etRate.setError(null);

        // Restore the default ticket class.
        spinnerClass.setSelection(0);

        // Reset results.
        tvExcessWeight.setText("0.00 kg");
        tvExcessCharge.setText("RM 0.00");
        tvTotalPayable.setText("RM 0.00");

        // Return to the beginning of the form.
        scrollMain.postDelayed(() -> {
            scrollMain.smoothScrollTo(0, 0);
        }, 350);
    }

    private String formatNumber(BigDecimal value) {
        return value.setScale(
                2,
                RoundingMode.HALF_UP
        ).toPlainString();
    }
}