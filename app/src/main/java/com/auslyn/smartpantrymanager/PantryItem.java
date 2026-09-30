package com.auslyn.smartpantrymanager;

/** One ingredient the user currently has at home. */
public class PantryItem {
    private int id;
    private String name;
    private double quantity;
    private String unit;
    private String category;
    private String expiryDate; // stored as yyyy-MM-dd, may be empty

    public PantryItem(int id, String name, double quantity, String unit, String category, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.expiryDate = expiryDate;
    }

    public PantryItem(String name, double quantity, String unit, String category, String expiryDate) {
        this(0, name, quantity, unit, category, expiryDate);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getCategory() { return category; }
    public String getExpiryDate() { return expiryDate; }
}
