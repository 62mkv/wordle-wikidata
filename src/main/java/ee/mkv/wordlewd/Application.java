package ee.mkv.wordlewd;

import ee.mkv.wordlewd.browser.BrowserWordleService;
import ee.mkv.wordlewd.game.WordGuesser;
import ee.mkv.wordlewd.guesser.WikidataGuesser;
import ee.mkv.wordlewd.wikidata.QueryExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

    public static void main(String[] args) {
        Logger driverLogger = Logger.getLogger(RemoteWebDriver.class.getName());
        driverLogger.setLevel(Level.FINEST);
        try (BrowserWordleService wordleService = new BrowserWordleService(); QueryExecutor queryExecutor = new QueryExecutor()) {
            WordGuesser wordGuesser = new WikidataGuesser(queryExecutor);
            GameCoordinator coordinator = new GameCoordinator(wordleService, wordGuesser);
            coordinator.runGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
