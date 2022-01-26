package ee.mkv.wordlewd.game;

import lombok.Value;

import java.util.Collection;

public interface WordleService {

    WordleState addGuess(String guess);

}
