package ee.mkv.wordlewd;

import ee.mkv.wordlewd.game.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class GameCoordinator {

    // see https://www.inverse.com/gaming/wordle-starting-words-best-using-math
    private static final String FIRST_GUESS = "slate";
    private final WordleService wordleService;
    private final WordGuesser wordGuesser;
    private final GameStats gameStats = new GameStats();

    public void runGame() {
        int tryCount = 0;
        boolean gameOver = false;
        WordleState latestState;

        String nextGuess = FIRST_GUESS;
        do {
            log.info("We'll try {} next", nextGuess);

            latestState = wordleService.addGuess(nextGuess);

            if (latestState.getResult() != WordleResult.IN_PROGRESS) {
                gameOver = true;
            } else {
                gameStats.processState(latestState);
                tryCount++;
                final Optional<String> optionalGuess = wordGuesser.guessNextWord(gameStats);
                if (optionalGuess.isEmpty()) {
                    log.warn("Sorry, no more guesses from me!");
                    gameOver = true;
                } else {
                    nextGuess = optionalGuess.get();
                }
            }
        } while (!gameOver);

        log.info("Completed after {} tries, with result of {}", tryCount, latestState.getResult());
    }

}
