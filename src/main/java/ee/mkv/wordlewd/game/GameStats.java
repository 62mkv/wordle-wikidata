package ee.mkv.wordlewd.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameStats {
    private final Set<Character> absentCharacters = new HashSet<>();
    private final Set<CharacterAtPosition> misplacedCharacters = new HashSet<>();
    private final Set<CharacterAtPosition> correctCharacters = new HashSet<>();
    private final List<String> alreadyTried = new ArrayList<>();
    private int lastProcessedRow = 0;

    public void processState(WordleState state) {
        final WordleRow[] rows = state.getRows();
        for (int i = lastProcessedRow; i < rows.length; i++) {
            processRow(rows[i]);
        }
    }

    private void processRow(WordleRow row) {
        int i = 0;
        for (CharacterWithState character : row.getStates()) {
            switch (character.getState()) {
                case PRESENT_IN_GIVEN_POSITION:
                    correctCharacters.add(getCharacterAtPosition(i, character));
                    break;
                case PRESENT_IN_OTHER_POSITION:
                    misplacedCharacters.add(getCharacterAtPosition(i, character));
                    break;
                case ABSENT:
                    absentCharacters.add(character.getCharacter());
            }

            i++;
        }

        alreadyTried.add(row.getWord());
    }

    private CharacterAtPosition getCharacterAtPosition(int i, CharacterWithState character) {
        return new CharacterAtPosition(character.getCharacter(), i);
    }
}
