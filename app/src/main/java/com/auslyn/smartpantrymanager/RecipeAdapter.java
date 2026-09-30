package com.auslyn.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Lists recipe cards. In "almost there" mode the missing ingredient is shown. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClick {
        void onClick(MatchResult result);
    }

    private List<MatchResult> results;
    private boolean showMissing;
    private final OnRecipeClick listener;

    public RecipeAdapter(List<MatchResult> results, OnRecipeClick listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MatchResult result = results.get(position);
        Recipe recipe = result.getRecipe();

        h.tvName.setText(recipe.getName());
        h.tvSummary.setText(h.itemView.getResources().getQuantityString(
                R.plurals.ingredient_count, recipe.getIngredients().size(), recipe.getIngredients().size()));
        h.tvIngredients.setText(ingredientNames(recipe));

        if (showMissing && !result.getMissing().isEmpty()) {
            h.tvBadge.setText(h.itemView.getContext().getString(
                    R.string.missing_one, result.getMissing().get(0).getName()));
            h.tvBadge.setBackgroundResource(R.drawable.bg_badge_warning);
            h.tvBadge.setTextColor(h.itemView.getContext().getColor(R.color.warning));
        } else {
            h.tvBadge.setText(R.string.ready_to_cook);
            h.tvBadge.setBackgroundResource(R.drawable.bg_badge_success);
            h.tvBadge.setTextColor(h.itemView.getContext().getColor(R.color.success));
        }

        h.itemView.setOnClickListener(v -> listener.onClick(result));
    }

    private String ingredientNames(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            if (sb.length() > 0) sb.append("  \u2022  ");
            sb.append(capitalise(ing.getName()));
        }
        return sb.toString();
    }

    static String capitalise(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public void updateData(List<MatchResult> newResults, boolean showMissing) {
        this.results = newResults;
        this.showMissing = showMissing;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvSummary, tvIngredients, tvBadge;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRecipeName);
            tvSummary = v.findViewById(R.id.tvRecipeSummary);
            tvIngredients = v.findViewById(R.id.tvRecipeIngredients);
            tvBadge = v.findViewById(R.id.tvBadge);
        }
    }
}
