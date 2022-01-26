package ee.mkv.wordlewd.guesser;

import ee.mkv.wordlewd.game.CharacterAtPosition;
import ee.mkv.wordlewd.game.GameStats;
import ee.mkv.wordlewd.game.WordGuesser;
import ee.mkv.wordlewd.wikidata.QueryExecutor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class WikidataGuesser implements WordGuesser {

    private final QueryExecutor queryExecutor;

    @Override
    @SneakyThrows
    public Optional<String> guessNextWord(GameStats gameStats) {
        List<BindingSet> eagerList = queryExecutor.executeQuery(buildQuery(gameStats)).stream().collect(Collectors.toList());
        log.info("We received: {} items", eagerList.size());
        return eagerList
                .stream()
                .map(this::extractLemma)
                .map(String::toLowerCase)
                .findFirst();
    }

    private String extractLemma(BindingSet bindings) {
        log.info("Processing WD query result row: {}", bindings.getBinding("lemma"));
        return bindings.getBinding("lemma").getValue().stringValue();
    }

    private String buildQuery(GameStats gameStats) {
        return "SELECT ?lemma WHERE {\n" +
                "  ?lexeme dct:language wd:Q1860;\n" +
                "   wikibase:lemma ?lemma.\n" +
                "  FILTER(STRLEN(?lemma) = 5).\n" +
                exactPositionFilters(gameStats) +
                containsFilters(gameStats) +
                notContainsFilters(gameStats) +
                "}";
    }

    private String notContainsFilters(GameStats gameStats) {
        StringJoiner joiner = new StringJoiner(". ");
        for (Character character: gameStats.getAbsentCharacters()) {
            joiner.add(String.format("FILTER(! CONTAINS(?lemma, \"%s\"))", character));
        }
        return wrapWithDot(joiner.toString());
    }

    private String containsFilters(GameStats gameStats) {
        StringJoiner joiner = new StringJoiner(". ");
        for (CharacterAtPosition character : gameStats.getMisplacedCharacters()) {
            joiner.add(
                    String.format(
                            "FILTER(CONTAINS(?lemma, \"%s\"))",
                            character.getCharacter()
                    )
            );

            joiner.add(
                    String.format(
                            "FILTER(! STRSTARTS(SUBSTR(?lemma, %d, 1), \"%s\"))",
                            character.getIndex() + 1,
                            character.getCharacter()
                    )
            );
        }
        return wrapWithDot(joiner.toString());
    }

    private String exactPositionFilters(GameStats gameStats) {
        StringJoiner joiner = new StringJoiner(". ");
        for (CharacterAtPosition character : gameStats.getCorrectCharacters()) {
            joiner.add(
                    String.format(
                            "FILTER(STRSTARTS(SUBSTR(?lemma, %d, 1), \"%s\"))",
                            character.getIndex() + 1,
                            character.getCharacter()
                    )
            );
        }
        return wrapWithDot(joiner.toString());
    }

    private String wrapWithDot(String expression) {
        if (!expression.isEmpty()) {
            return expression + ". ";
        }
        return expression;
    }

}
