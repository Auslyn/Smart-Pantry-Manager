package com.auslyn.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Pantry list: shows every ingredient and lets the user search, edit and delete. */
public class PantryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private View emptyState;
    private TextView tvSubtitle;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantry);

        db = new DatabaseHelper(this);
        emptyState = findViewById(R.id.emptyState);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        etSearch = findViewById(R.id.etSearch);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        Ui.applyInsets(findViewById(R.id.root), findViewById(R.id.header), nav);
        Ui.setupBottomNav(this, nav, R.id.nav_pantry);

        RecyclerView recycler = findViewById(R.id.recyclerViewPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new ArrayList<>(), new PantryAdapter.Listener() {
            @Override
            public void onEdit(PantryItem item) {
                Intent i = new Intent(PantryActivity.this, AddEditPantryActivity.class);
                i.putExtra(AddEditPantryActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(PantryItem item) {
                deleteWithUndo(item);
            }
        });
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddPantry);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditPantryActivity.class)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { loadItems(); }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> all = db.getAllPantryItems();
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);

        List<PantryItem> shown = new ArrayList<>();
        for (PantryItem item : all) {
            if (query.isEmpty() || item.getName().toLowerCase(Locale.ROOT).contains(query)) {
                shown.add(item);
            }
        }
        adapter.updateData(shown);
        emptyState.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        tvSubtitle.setText(getResources().getQuantityString(R.plurals.item_count, all.size(), all.size()));
    }

    private void deleteWithUndo(PantryItem item) {
        db.deletePantryItem(item.getId());
        loadItems();
        Snackbar.make(findViewById(R.id.contentArea), R.string.item_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    db.addPantryItem(item);
                    loadItems();
                })
                .show();
    }
}
