package ee.mkv.wordlewd.game;

import lombok.Value;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Value
public
class WordleRow {
    CharacterWithState[] states = new CharacterWithState[5];

    public WordleRow(Collection<CharacterWithState> states) {
        states.toArray(this.states);
    }

    public String getWord() {
        return Arrays.stream(states)
                .map(CharacterWithState::getCharacter)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
