package ee.mkv.wordlewd.game;

import java.util.Optional;

public interface WordGuesser {
    Optional<String> guessNextWord(GameStats gameStats);
}
