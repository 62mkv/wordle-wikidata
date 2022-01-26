package ee.mkv.wordlewd.game;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class GameStats {
    private final Set<Character> absentCharacters = new HashSet<>();
    private final Set<CharacterAtPosition> misplacedCharacters = new HashSet<>();
    private final Set<CharacterAtPosition> correctCharacters = new HashSet<>();
    private final List<String> alreadyTried = new ArrayList<>();

    public void processState(WordleState state) {
        log.trace("Current state: {}", state);
        for (WordleRow row : state.getRows()) {
            if (row != null && !alreadyTried.contains(row.getWord())) {
                log.trace("Processing row: {}", row);
                processRow(row);
            }
        }

        cleanupState();
    }

    /**
     * This is need because if our guess contained some character twice, we need to exclude it from "absent" if it was
     * present in some other position
     */
    private void cleanupState() {
        Set<Character> presentCharacters = new HashSet<>();
        presentCharacters.addAll(correctCharacters.stream().map(CharacterAtPosition::getCharacter).collect(Collectors.toList()));
        presentCharacters.addAll(misplacedCharacters.stream().map(CharacterAtPosition::getCharacter).collect(Collectors.toList()));
        for (Character character : presentCharacters) {
            absentCharacters.remove(character);
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
