package ee.mkv.wordlewd;

import ee.mkv.wordlewd.browser.BrowserWordleService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

    public static void main(String[] args) {
        Logger driverLogger = Logger.getLogger(RemoteWebDriver.class.getName());
        driverLogger.setLevel(Level.FINEST);
        try (BrowserWordleService wordleService = new BrowserWordleService()) {
            System.out.println(wordleService.addGuess("raise").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
