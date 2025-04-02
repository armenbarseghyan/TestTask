package todoapp.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages configuration settings for test environment.
 * Loads properties from configuration file with fallback to default values.
 */
@Slf4j
public class TestConfig {
    private static final String PROPERTIES_FILE = "test.properties";
    private final Properties properties = new Properties();
    private static TestConfig instance;

    /**
     * Private constructor to enforce singleton pattern and load properties.
     */
    private TestConfig() {
        loadProperties();
    }

    /**
     * Provides thread-safe singleton instance of TestConfig.
     *
     * @return Singleton TestConfig instance
     */
    public static synchronized TestConfig getInstance() {
        if (instance == null) {
            instance = new TestConfig();
        }
        return instance;
    }

    /**
     * Loads configuration properties from file or sets default values.
     */
    private void loadProperties() {
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                log.warn("Unable to find {}, using default configuration", PROPERTIES_FILE);
                setDefaultProperties();
                return;
            }

            properties.load(input);
        } catch (IOException ex) {
            log.error("Error loading properties", ex);
            setDefaultProperties();
        }
    }

    /**
     * Sets default configuration values when properties file is unavailable.
     */
    private void setDefaultProperties() {
        properties.setProperty("base.url", "http://localhost:8080");
        properties.setProperty("ws.url", "ws://localhost:4242/ws");
        properties.setProperty("admin.username", "admin");
        properties.setProperty("admin.password", "admin");
    }

    /**
     * Retrieves base URL for API testing.
     *
     * @return Configured or default base URL
     */
    public String getBaseUrl() {
        return properties.getProperty("base.url", "http://localhost:9090");
    }

    /**
     * Retrieves WebSocket URL for testing.
     *
     * @return Configured or default WebSocket URL
     */
    public String getWsUrl() {
        return properties.getProperty("ws.url", "ws://localhost:9090/ws");
    }

    /**
     * Retrieves admin username for authentication.
     *
     * @return Configured or default admin username
     */
    public String getAdminUsername() {
        return properties.getProperty("admin.username", "admin");
    }

    /**
     * Retrieves admin password for authentication.
     *
     * @return Configured or default admin password
     */
    public String getAdminPassword() {
        return properties.getProperty("admin.password", "admin");
    }
}