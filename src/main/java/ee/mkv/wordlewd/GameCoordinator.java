package ee.mkv.wordlewd;

import ee.mkv.wordlewd.game.GameStats;
import ee.mkv.wordlewd.game.WordleResult;
import ee.mkv.wordlewd.game.WordleService;
import ee.mkv.wordlewd.game.WordleState;
import ee.mkv.wordlewd.game.WordGuesser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class GameCoordinator {

    private final WordleService wordleService;
    private final WordGuesser wordGuesser;
    private final GameStats gameStats = new GameStats();

    public void runGame() {
        boolean gameOver = false;
        String nextGuess = "raise";
        do {
            WordleState state = wordleService.addGuess(nextGuess);
            if (state.getResult() != WordleResult.IN_PROGRESS) {
                gameOver = true;
            } else {
                gameStats.processState(state);
                nextGuess = wordGuesser.guessNextWord(gameStats);
            }
        } while (!gameOver);
    }

}
