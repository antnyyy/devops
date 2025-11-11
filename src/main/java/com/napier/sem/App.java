package com.napier.sem;

import com.napier.sem.reports.CityReports;
import com.napier.sem.reports.CountryReports;
import com.napier.sem.reports.LanguageReports;

public class App {
    public static void main(String[] args) {
        Database db = new Database();
        String url = "jdbc:mysql://localhost:33060/world?allowPublicKeyRetrieval=true&useSSL=false";
        db.connect(url, "root", "example");

        CountryReports countryReports = new CountryReports(db);
        CityReports cityReports = new CityReports(db);
        LanguageReports languageReports = new LanguageReports(db);

        // ---- Example calls ----
        var topCities = cityReports.getTopCitiesWorldwide(10);
        cityReports.printCities(topCities);

        var topCountries = countryReports.getTopCountriesWorldwide(10);
        countryReports.printCountries(topCountries);

        var big5 = languageReports.getTopFiveLanguagesBySpeakers();
        languageReports.printLanguages(big5);
        // -----------------------

        db.disconnect();
    }
}
