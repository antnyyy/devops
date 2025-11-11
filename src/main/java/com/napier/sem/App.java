/**
 * App entry point for the World database project.
 * Connects to MySQL → prepares report classes → disconnects.
 */
package com.napier.sem;

import com.napier.sem.reports.LanguageReports;
import com.napier.sem.reports.CityReports;
import com.napier.sem.reports.CountryReports;


public class App
{
    public static void main(String[] args)
    {
        // Create a Database instance
        Database db = new Database();

        // Connect to the world database
        String url = "jdbc:mysql://localhost:33060/world?allowPublicKeyRetrieval=true&useSSL=false";
        db.connect(url, "root", "example");

        // Create report class instances, passing the database connection
        CountryReports countryReports = new CountryReports(db);
        CityReports cityReports = new CityReports(db);
        LanguageReports languageReports = new LanguageReports(db);

        // ------------------------------------------------------------
        // TODO: Add your report calls here later
        // Example (once you add methods):
        // var countries = countryReports.getTopCountriesWorldwide(10);
        // countryReports.printCountries(countries);
        // ------------------------------------------------------------

        // Disconnect when done
        db.disconnect();
    }
}
