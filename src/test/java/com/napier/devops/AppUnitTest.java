package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppUnitTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    private App app;

    @BeforeEach
    void setUp() {
        app = new App();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // -------- displayCity --------

    @Test
    void displayCity_null_printsNotFound() {
        app.displayCity(null);
        String out = outContent.toString();
        assertTrue(out.contains("City not found."));
    }

    @Test
    void displayCity_valid_printsAllFields() {
        App.City c = new App.City();
        c.id = 1;
        c.name = "Testville";
        c.countryCode = "TST";
        c.district = "Central";
        c.population = 123456;

        app.displayCity(c);
        String out = outContent.toString();
        assertAll(
                () -> assertTrue(out.contains("ID: 1")),
                () -> assertTrue(out.contains("Name: Testville")),
                () -> assertTrue(out.contains("CountryCode: TST")),
                () -> assertTrue(out.contains("District: Central")),
                () -> assertTrue(out.contains("Population: 123456"))
        );
    }

    // -------- displayCities --------

    @Test
    void displayCities_empty_printsNoCities() {
        app.displayCities(new ArrayList<>());
        String out = outContent.toString();
        assertTrue(out.contains("No cities."));
    }

    @Test
    void displayCities_oneRow_printsHeaderAndRow() {
        App.City c = new App.City();
        c.id = 2;
        c.name = "Metro City";
        c.countryCode = "GBR";
        c.district = "England";
        c.population = 8900000;

        List<App.City> list = new ArrayList<>();
        list.add(c);

        app.displayCities(list);
        String out = outContent.toString();
        assertAll(
                () -> assertTrue(out.contains("ID")),
                () -> assertTrue(out.contains("Name")),
                () -> assertTrue(out.contains("Code")),
                () -> assertTrue(out.contains("District")),
                () -> assertTrue(out.contains("Population")),
                () -> assertTrue(out.contains("Metro City")),
                () -> assertTrue(out.contains("GBR")),
                () -> assertTrue(out.contains("England")),
                () -> assertTrue(out.contains("8900000"))
        );
    }

    // -------- displayCountries --------

    @Test
    void displayCountries_empty_printsNoCountries() {
        app.displayCountries(new ArrayList<>());
        String out = outContent.toString();
        assertTrue(out.contains("No countries."));
    }

    @Test
    void displayCountries_oneRow_printsHeaderAndRow() {
        App.Country country = new App.Country();
        country.code = "USA";
        country.name = "United States";
        country.population = 331002651L;

        List<App.Country> list = new ArrayList<>();
        list.add(country);

        app.displayCountries(list);
        String out = outContent.toString();
        assertAll(
                () -> assertTrue(out.contains("Code")),
                () -> assertTrue(out.contains("Name")),
                () -> assertTrue(out.contains("Population")),
                () -> assertTrue(out.contains("USA")),
                () -> assertTrue(out.contains("United States")),
                () -> assertTrue(out.contains("331002651"))
        );
    }

    // -------- displayContinentPops --------

    @Test
    void displayContinentPops_empty_printsNoData() {
        app.displayContinentPops(new ArrayList<>());
        String out = outContent.toString();
        assertTrue(out.contains("No data."));
    }

    @Test
    void displayContinentPops_oneRow_printsHeaderAndRow() {
        App.ContinentPop cp = new App.ContinentPop();
        cp.continent = "Europe";
        cp.population = 741000000L;

        List<App.ContinentPop> list = new ArrayList<>();
        list.add(cp);

        app.displayContinentPops(list);
        String out = outContent.toString();
        assertAll(
                () -> assertTrue(out.contains("Continent")),
                () -> assertTrue(out.contains("Population")),
                () -> assertTrue(out.contains("Europe")),
                () -> assertTrue(out.contains("741000000"))
        );
    }

    // -------- Report methods with no DB connection --------
    // App.con is null by default, so these calls should be safe and simple to test.

    @Test
    void getCity_noConnection_returnsNullAndWarns() {
        App.City c = app.getCity(1);
        String out = outContent.toString();
        assertNull(c);
        assertTrue(out.contains("No DB connection."));
    }

    @Test
    void getTopCitiesInCountry_noConnection_returnsEmptyAndWarns() {
        List<App.City> cities = app.getTopCitiesInCountry("GBR", 5);
        String out = outContent.toString();
        assertNotNull(cities);
        assertTrue(cities.isEmpty());
        assertTrue(out.contains("No DB connection."));
    }

    @Test
    void getTopCountriesByPopulation_noConnection_returnsEmptyAndWarns() {
        List<App.Country> countries = app.getTopCountriesByPopulation(3);
        String out = outContent.toString();
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
        assertTrue(out.contains("No DB connection."));
    }

    @Test
    void getPopulationByContinent_noConnection_returnsEmptyAndWarns() {
        List<App.ContinentPop> pops = app.getPopulationByContinent();
        String out = outContent.toString();
        assertNotNull(pops);
        assertTrue(pops.isEmpty());
        assertTrue(out.contains("No DB connection."));
    }
}