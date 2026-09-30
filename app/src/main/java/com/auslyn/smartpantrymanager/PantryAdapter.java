package com.auslyn.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/** Binds pantry items from the database to the cards in the list. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items;
    private final Listener listener;

    public PantryAdapter(List<PantryItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Context ctx = h.itemView.getContext();
        PantryItem item = items.get(position);

        h.tvInitial.setText(item.getName().substring(0, 1).toUpperCase(Locale.ROOT));
        h.tvName.setText(item.getName());
        h.tvQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        String category = item.getCategory();
        h.tvCategory.setText(category == null || category.isEmpty()
                ? ctx.getString(R.string.uncategorised) : category);

        Integer days = ExpiryUtils.daysUntil(item.getExpiryDate());
        if (days == null) {
            h.tvExpiry.setText(R.string.no_expiry);
            h.tvExpiry.setTextColor(ctx.getColor(R.color.text_secondary));
        } else if (days < 0) {
            h.tvExpiry.setText(R.string.expired);
            h.tvExpiry.setTextColor(ctx.getColor(R.color.danger));
        } else if (days <= ExpiryUtils.SOON_DAYS) {
            h.tvExpiry.setText(days == 0
                    ? ctx.getString(R.string.expires_today)
                    : ctx.getResources().getQuantityString(R.plurals.expires_in_days, days, days));
            h.tvExpiry.setTextColor(ctx.getColor(R.color.warning));
        } else {
            h.tvExpiry.setText(ctx.getString(R.string.expires_on, item.getExpiryDate()));
            h.tvExpiry.setTextColor(ctx.getColor(R.color.text_secondary));
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<PantryItem> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    /** 2.0 -> "2", 1.5 -> "1.5" */
    static String formatQuantity(double q) {
        if (q == Math.floor(q)) return String.valueOf((long) q);
        return String.format(Locale.US, "%.1f", q);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitial, tvName, tvCategory, tvExpiry, tvQuantity;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View v) {
            super(v);
            tvInitial = v.findViewById(R.id.tvInitial);
            tvName = v.findViewById(R.id.tvItemName);
            tvCategory = v.findViewById(R.id.tvItemCategory);
            tvExpiry = v.findViewById(R.id.tvItemExpiry);
            tvQuantity = v.findViewById(R.id.tvItemQuantity);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
