package com.napier.sem;

import com.napier.sem.reports.CityReports;

public class App {
    public static void main(String[] args) {
        // Initialize the database connection
        Database db = new Database();
        try {
            System.out.println("Connecting to database...");
            // Provide the database URL, username, and password
            db.connect("jdbc:mysql:///world?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "your_new_root_password");
            System.out.println("Successfully connected");

            // Create CityReports instance
            CityReports cityReports = new CityReports(db);

            // 1. Print all cities in the world
            System.out.println("\n=== All Cities in the World ===");
            cityReports.getAllCities().forEach(System.out::println);

            // 2. Print cities for a specific country (e.g., Netherlands - NLD)
            String countryCode = "NLD";
            System.out.println("\n=== Cities in " + countryCode + " ===");
            cityReports.getCitiesByCountry(countryCode).forEach(System.out::println);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            System.out.println("Disconnecting...");
            db.disconnect();
            System.out.println("Disconnected.");
        }
    }
}