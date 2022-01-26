package ee.mkv.wordlewd.game;

import lombok.Value;

import java.util.Collection;

@Value
public
class WordleState {
    WordleRow[] rows = new WordleRow[6];
    WordleResult result;

    public WordleState(Collection<WordleRow> rows, WordleResult result) {
        rows.toArray(this.rows);
        this.result = result;
    }
}
