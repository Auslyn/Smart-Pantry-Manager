package com.auslyn.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/** One form used for both adding a new pantry item and editing an existing one. */
public class AddEditPantryActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private static final String[] CATEGORIES = {
            "Vegetables", "Fruit", "Dairy", "Meat & Fish", "Grains & Pasta",
            "Bakery", "Pantry Staples", "Spices", "Other"};

    private DatabaseHelper db;
    private PantryItem editing; // null when adding

    private TextInputLayout tilName, tilQuantity, tilUnit, tilExpiry;
    private TextInputEditText etName, etQuantity, etExpiry;
    private AutoCompleteTextView actUnit, actCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);

        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilUnit = findViewById(R.id.tilUnit);
        tilExpiry = findViewById(R.id.tilExpiry);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        actUnit = findViewById(R.id.actUnit);
        actCategory = findViewById(R.id.actCategory);
        TextView tvTitle = findViewById(R.id.tvTitle);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        Ui.applyInsets(findViewById(R.id.root), findViewById(R.id.header), findViewById(R.id.bottomBar));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        actUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, UnitConverter.UNITS));
        actCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, CATEGORIES));
        actUnit.setText(UnitConverter.UNITS[0], false);

        int id = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (id != -1) {
            editing = db.getPantryItem(id);
        }
        if (editing != null) {
            tvTitle.setText(R.string.edit_item);
            etName.setText(editing.getName());
            etQuantity.setText(PantryAdapter.formatQuantity(editing.getQuantity()));
            actUnit.setText(editing.getUnit(), false);
            actCategory.setText(editing.getCategory(), false);
            etExpiry.setText(editing.getExpiryDate());
            btnSave.setText(R.string.save_changes);
        } else {
            tvTitle.setText(R.string.add_item);
        }

        // The date field opens a calendar instead of asking the user to type a date.
        etExpiry.setFocusable(false);
        etExpiry.setOnClickListener(v -> showDatePicker());
        tilExpiry.setEndIconOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            etExpiry.setText(ExpiryUtils.format(picked));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        // Lets the user remove an expiry date they no longer want.
        dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, getString(R.string.no_expiry),
                (d, which) -> etExpiry.setText(""));
        dialog.show();
    }

    private void save() {
        tilName.setError(null);
        tilQuantity.setError(null);
        tilUnit.setError(null);
        tilExpiry.setError(null);

        String name = text(etName);
        String qtyText = text(etQuantity);
        String unit = actUnit.getText().toString().trim();
        String category = actCategory.getText().toString().trim();
        String expiry = text(etExpiry);

        boolean ok = true;
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_required));
            ok = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            tilName.setError(getString(R.string.error_name_letters));
            ok = false;
        }

        double quantity = 0;
        if (qtyText.isEmpty()) {
            tilQuantity.setError(getString(R.string.error_quantity_required));
            ok = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    tilQuantity.setError(getString(R.string.error_quantity_positive));
                    ok = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.error_quantity_number));
                ok = false;
            }
        }

        if (!isKnownUnit(unit)) {
            tilUnit.setError(getString(R.string.error_unit));
            ok = false;
        }
        if (!ExpiryUtils.isValid(expiry)) {
            tilExpiry.setError(getString(R.string.error_date));
            ok = false;
        }
        if (!ok) return;

        if (editing == null) {
            db.addPantryItem(new PantryItem(name, quantity, unit, category, expiry));
            Toast.makeText(this, R.string.item_added, Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(new PantryItem(editing.getId(), name, quantity, unit, category, expiry));
            Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private boolean isKnownUnit(String unit) {
        for (String u : UnitConverter.UNITS) {
            if (u.equals(unit)) return true;
        }
        return false;
    }

    private String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
