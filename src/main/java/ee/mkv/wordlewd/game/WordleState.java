package ee.mkv.wordlewd.game;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
public
class WordleState {
    List<WordleRow> rows;
    WordleResult result;

    public WordleState(Collection<WordleRow> rows, WordleResult result) {
        this.rows = new ArrayList<>(rows);
        this.result = result;
    }
}
