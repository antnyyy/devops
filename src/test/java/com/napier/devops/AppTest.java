package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link App} class, focusing on output formatting and behavior
 * when no database connection is present. Uses output stream capture to verify
 * console-based reporting functionality.
 *
 * <p>All tests assume {@code App.con} is {@code null} by default (no DB connection),
 * which allows safe testing of report methods without requiring a live database.</p>
 */
class AppTest {

    /** Original System.out stream to restore after each test */
    private final PrintStream originalOut = System.out;

    /** Captures console output during test execution */
    private ByteArrayOutputStream outContent;

    /** Instance of the application under test */
    private App app;

    /**
     * Sets up the test environment before each test:
     * - Instantiates a fresh {@link App}
     * - Redirects {@code System.out} to a {@link ByteArrayOutputStream} for output capture
     */
    @BeforeEach
    void setUp() {
        app = new App();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Restores the original {@code System.out} after each test to prevent
     * interference with other tests or system behavior.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========================================================================
    // Tests for displayCity(App.City city)
    // ========================================================================

    /**
     * Tests that {@link App#displayCity} prints "City not found." when given a {@code null} city.
     * Verifies defensive programming against invalid input.
     */
    @Test
    void displayCity_null_printsNotFound() {
        app.displayCity(null);
        String out = outContent.toString();
        assertTrue(out.contains("City not found."));
    }

    /**
     * Tests that {@link App#displayCity} correctly formats and prints all fields
     * of a valid {@link App.City} object in the expected format.
     *
     * <p>Uses {@link Assertions#assertAll} to group multiple related assertions
     * for better failure reporting.</p>
     */
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

        assertAll("All city fields should be printed correctly",
                () -> assertTrue(out.contains("ID: 1"), "Should display city ID"),
                () -> assertTrue(out.contains("Name: Testville"), "Should display city name"),
                () -> assertTrue(out.contains("CountryCode: TST"), "Should display country code"),
                () -> assertTrue(out.contains("District: Central"), "Should display district"),
                () -> assertTrue(out.contains("Population: 123456"), "Should display population")
        );
    }

    // ========================================================================
    // Tests for displayCities(List<App.City> cities)
    // ========================================================================

    /**
     * Tests that {@link App#displayCities} prints "No cities." when given an empty list.
     * Ensures user-friendly feedback for empty result sets.
     */
    @Test
    void displayCities_empty_printsNoCities() {
        app.displayCities(new ArrayList<>());
        String out = outContent.toString();
        assertTrue(out.contains("No cities."));
    }

    /**
     * Tests that {@link App#displayCities} prints a formatted table header
     * and a single city row when given a list with one city.
     *
     * <p>Verifies both header labels and data values are present in output.</p>
     */
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

        assertAll("Table should include header and one data row",
                () -> assertTrue(out.contains("ID"), "Header should include 'ID'"),
                () -> assertTrue(out.contains("Name"), "Header should include 'Name'"),
                () -> assertTrue(out.contains("Code"), "Header should include 'Code'"),
                () -> assertTrue(out.contains("District"), "Header should include 'District'"),
                () -> assertTrue(out.contains("Population"), "Header should include 'Population'"),
                () -> assertTrue(out.contains("Metro City"), "Should display city name"),
                () -> assertTrue(out.contains("GBR"), "Should display country code"),
                () -> assertTrue(out.contains("England"), "Should display district"),
                () -> assertTrue(out.contains("8900000"), "Should display population")
        );
    }

    // ========================================================================
    // Tests for displayCountries(List<App.Country> countries)
    // ========================================================================

    /**
     * Tests that {@link App#displayCountries} prints "No countries." when given an empty list.
     */
    @Test
    void displayCountries_empty_printsNoCountries() {
        app.displayCountries(new ArrayList<>());
        String out = outContent.toString();
        assertTrue(out.contains("No countries."));
    }

    /**
     * Tests that {@link App#displayCountries} prints a header and one country row
     * with correct field formatting.
     */
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

        assertAll("Country table should display header and data",
                () -> assertTrue(out.contains("Code"), "Header should include 'Code'"),
                () -> assertTrue(out.contains("Name"), "Header should include 'Name'"),
                () -> assertTrue(out.contains("Population"), "Header should include 'Population'"),
                () -> assertTrue(out.contains("USA"), "Should display country code"),
                () -> assertTrue(out.contains("United States"), "Should display country name"),
                () -> assertTrue(out.contains("331002651"), "Should display population")
        );
    }

    // ========================================================================
    // Tests for displayContinentPops(List<App.ContinentPop> pops)
    // ========================================================================

    /**
     * Tests that {@link App#displayContinentPops} prints "No data." when given an empty list.
     */
    @Test
    void displayContinentPops_empty_printsNoData() {
        app.displayContinentPops(new ArrayList<>());
        String out = outContent.toString();
        assertTrue(out.contains("No data."));
    }

    /**
     * Tests that {@link App#displayContinentPops} prints a header and one continent population row.
     */
    @Test
    void displayContinentPops_oneRow_printsHeaderAndRow() {
        App.ContinentPop cp = new App.ContinentPop();
        cp.continent = "Europe";
        cp.population = 741000000L;

        List<App.ContinentPop> list = new ArrayList<>();
        list.add(cp);

        app.displayContinentPops(list);
        String out = outContent.toString();

        assertAll("Continent population table should show header and data",
                () -> assertTrue(out.contains("Continent"), "Header should include 'Continent'"),
                () -> assertTrue(out.contains("Population"), "Header should include 'Population'"),
                () -> assertTrue(out.contains("Europe"), "Should display continent name"),
                () -> assertTrue(out.contains("741000000"), "Should display population")
        );
    }

    // ========================================================================
    // Report Methods: Behavior with No Database Connection
    // ========================================================================

    /**
     * Tests that {@link App#getCity(int)} returns {@code null} and prints a warning
     * when no database connection exists ({@code App.con == null}).
     *
     * <p>This ensures the method fails gracefully without throwing exceptions.</p>
     */
    @Test
    void getCity_noConnection_returnsNullAndWarns() {
        App.City c = app.getCity(1);
        String out = outContent.toString();

        assertNull(c, "Should return null when no DB connection");
        assertTrue(out.contains("No DB connection."), "Should warn about missing connection");
    }

    /**
     * Tests that {@link App#getTopCitiesInCountry} returns an empty list and prints a warning
     * when no database connection is available.
     *
     * <p>Verifies the list is non-null but empty, maintaining API contract.</p>
     */
    @Test
    void getTopCitiesInCountry_noConnection_returnsEmptyAndWarns() {
        List<App.City> cities = app.getTopCitiesInCountry("GBR", 5);
        String out = outContent.toString();

        assertNotNull(cities, "Should return a non-null list");
        assertTrue(cities.isEmpty(), "List should be empty with no DB connection");
        assertTrue(out.contains("No DB connection."), "Should print connection warning");
    }

    /**
     * Tests that {@link App#getTopCountriesByPopulation} returns an empty list and warns
     * when no database connection exists.
     */
    @Test
    void getTopCountriesByPopulation_noConnection_returnsEmptyAndWarns() {
        List<App.Country> countries = app.getTopCountriesByPopulation(3);
        String out = outContent.toString();

        assertNotNull(countries, "Should return a non-null list");
        assertTrue(countries.isEmpty(), "List should be empty without DB");
        assertTrue(out.contains("No DB connection."), "Should warn about missing connection");
    }

    /**
     * Tests that {@link App#getPopulationByContinent} returns an empty list and prints
     * a warning when no database connection is present.
     */
    @Test
    void getPopulationByContinent_noConnection_returnsEmptyAndWarns() {
        List<App.ContinentPop> pops = app.getPopulationByContinent();
        String out = outContent.toString();

        assertNotNull(pops, "Should return a non-null list");
        assertTrue(pops.isEmpty(), "List should be empty without DB connection");
        assertTrue(out.contains("No DB connection."), "Should display connection warning");
    }
}