package twetailer.selenium.widget;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestStoreMap {

    private static WebDriver driver;

    @BeforeClass
    public static void setUpBeforeClass() {
        driver =  new FirefoxDriver();
        // driver.get("http://localhost:9999/widget/maps/stores.jsp");
        driver.get("http://twetailer.appspot.com/widget/maps/stores.jsp");
        // driver.get("http://anothersocialeconomy.appspot.com/widget/maps/stores.jsp");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        driver.quit(); // Close the browser
    }

    @Before
    public void setUp() throws Exception {
        new WebDriverWait(driver, 2000).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                // Return Boolean.TRUE only if the overlay 'introFlash' has disappeared, which means that the application is ready
                return (Boolean) ((JavascriptExecutor) driver).executeScript("return dojo.byId('introFlash').style.display == 'none';");
            }
        });
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void loadStoreI() {
        // Set the postal code and get the map
        driver.findElement(By.id("postalCode")).sendKeys("h8p3r8");
        driver.findElement(By.id("showMapButton")).click();

        new WebDriverWait(driver, 5000).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                // Return Boolean.TRUE only if the overlay 'introFlash' has disappeared, which means that the application is ready
                return (Boolean) ((JavascriptExecutor) driver).executeScript("return localModule._mapFetchedWithData;");
            }
        });
    }

    @Test
    public void loadStoreII() {
        // Reset the postal code field content
        WebElement postalCodeField = driver.findElement(By.id("postalCode"));
        postalCodeField.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        postalCodeField.sendKeys(Keys.DELETE);

        // Set the postal code and get the map
        postalCodeField.sendKeys("h9b1x9");
        driver.findElement(By.id("showMapButton")).click();

        new WebDriverWait(driver, 5000).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                // Return Boolean.TRUE only if the overlay 'introFlash' has disappeared, which means that the application is ready
                return (Boolean) ((JavascriptExecutor) driver).executeScript("return localModule._mapFetchedWithData;");
            }
        });
    }
}
