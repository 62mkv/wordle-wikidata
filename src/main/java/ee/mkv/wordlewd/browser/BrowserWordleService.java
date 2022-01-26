package ee.mkv.wordlewd.browser;

import ee.mkv.wordlewd.browser.error.BrowserAppException;
import ee.mkv.wordlewd.game.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class BrowserWordleService implements WordleService, AutoCloseable {

    private static final String WORDLE_URL = "https://powerlanguage.co.uk/wordle/";

    private final ChromeDriver driver;
    private WordleState lastState;

    public BrowserWordleService() {
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();
    }

    @Override
    public WordleState addGuess(String guess) {
        if (isNotFinalState(lastState)) {
            tryAddGuess(guess);
            /* assuming we're always single-threaded */
            lastState = readCurrentState();
            return lastState;
        } else {
            throw new IllegalStateException("Application is already in final state, can not add guesses");
        }
    }

    private WordleState readCurrentState() {
        WebElement board = driver.findElement(By.tagName("game-app"))
                .getShadowRoot()
                .findElement(By.id("board"));

        List<WebElement> elements = board.findElements(By.tagName("game-row"));
        List<WordleRow> rows = elements
                .stream()
                .map(this::rowFromElement)
                .collect(Collectors.toList());
        return createStateFromRows(rows);
    }

    private WordleState createStateFromRows(List<WordleRow> rows) {
        int firstEmptyRowIndex = rows.indexOf(null);
        int lastActualRowIndex = firstEmptyRowIndex == -1
                ? rows.size() - 1
                : firstEmptyRowIndex - 1;
        if (lastActualRowIndex == -1) {
            // nothing has been provided yet
            return new WordleState(rows, WordleResult.IN_PROGRESS);
        }

        WordleRow lastRow = rows.get(lastActualRowIndex);
        WordleResult result = allCorrect(lastRow)
                ? WordleResult.SUCCESS
                : (firstEmptyRowIndex == -1
                ? WordleResult.FAILURE
                : WordleResult.IN_PROGRESS);

        return new WordleState(rows, result);
    }

    private boolean allCorrect(WordleRow lastRow) {
        final List<CharacterWithState> stateStream = Arrays.stream(lastRow.getStates())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return !stateStream.isEmpty()
                && stateStream.stream().allMatch(row -> row.getState() == CharState.PRESENT_IN_GIVEN_POSITION);
    }

    private WordleRow rowFromElement(WebElement webRow) {
        if (webRow.getAttribute("letters").isEmpty()) {
            return null;
        }

        List<WebElement> tiles = webRow
                .getShadowRoot()
                .findElements(By.cssSelector("game-tile"));
        List<CharacterWithState> charactersWithState = tiles
                .stream()
                .map(this::readStateFromTile)
                .collect(Collectors.toList());

        return new WordleRow(charactersWithState);
    }

    private CharacterWithState readStateFromTile(WebElement tile) {
        return new CharacterWithState(getLetter(tile), getLetterState(tile));
    }

    private CharState getLetterState(WebElement tile) {
        String evaluation = tile.getAttribute("evaluation");
        switch (evaluation) {
            case "correct":
                return CharState.PRESENT_IN_GIVEN_POSITION;
            case "present":
                return CharState.PRESENT_IN_OTHER_POSITION;
            case "absent":
                return CharState.ABSENT;
            default:
                throw new BrowserAppException("Unknown evaluation state for a tile: " + evaluation);
        }
    }

    private char getLetter(WebElement tile) {
        return tile.getAttribute("letter").charAt(0);
    }

    private void tryAddGuess(String guess) {
        if (!isReady()) {
            openApplication();
        }

        sendGuess(guess);
        sendNewLine();
    }

    private void sendNewLine() {
        WebElement gameKeyboard = driver.findElement(By.tagName("game-app"))
                .getShadowRoot()
                // see this: https://github.com/SeleniumHQ/selenium/issues/4971 - tagNames are not supported on shadow roots
                .findElement(By.cssSelector("game-keyboard"));

        WebElement lastRow = gameKeyboard
                .getShadowRoot()
                .findElements(By.cssSelector(".row"))
                .get(2);

        WebElement newLineButton = lastRow.findElement(By.cssSelector(":first-child"));

        clickElementAndWait(newLineButton, Duration.ofSeconds(2L));
    }

    private void sendGuess(String guess) {
        log.info("We're sending the keys: {}", guess);
        runWithWait(new Actions(driver)
                .click(driver.findElement(By.tagName("game-app")))
                .sendKeys(guess), Duration.ofMillis(500L));
    }

    private void runWithWait(Actions actions, Duration timeout) {
        actions.build().perform();
        try {
            new WebDriverWait(driver, timeout).until(a -> false);
        } catch (TimeoutException e) {
            // ignoring timeout exception
        }

    }

    private void openApplication() {
        driver.navigate().to(WORDLE_URL);
        // on first opening of the page, a popup, explaining the rules, is displayed,
        // we just need to be clicked anywhere - then the game board will be visible
        clickElementAndWait(driver.findElement(By.tagName("game-app")), Duration.ofMillis(500L));
    }

    private void clickElementAndWait(WebElement target, Duration timeout) {
        // see https://stackoverflow.com/a/44916498/2583044, Solution 1
        runWithWait(new Actions(driver).moveToElement(target).click(), timeout);
    }

    private boolean isNotFinalState(WordleState lastState) {
        if (lastState == null) {
            return true;
        }

        return lastState.getResult() == WordleResult.IN_PROGRESS;
    }

    private boolean isReady() {
        return WORDLE_URL.equals(driver.getCurrentUrl());
    }

    private void logDriver() {
        Logs logs = driver.manage().logs();
        for (LogEntry logEntry : logs.get(LogType.CLIENT)) {
            log.debug(logEntry.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        logDriver();
        if (this.driver != null) {
            this.driver.close();
        }
    }
}
