package ee.mkv.wordlewd.guesser;

import ee.mkv.wordlewd.game.GameStats;
import ee.mkv.wordlewd.game.WordGuesser;
import ee.mkv.wordlewd.wikidata.QueryExecutor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.rdf4j.query.TupleQueryResult;

@RequiredArgsConstructor
public class WikidataGuesser implements WordGuesser {

    private final QueryExecutor queryExecutor;

    @Override
    @SneakyThrows
    public String guessNextWord(GameStats gameStats) {
        TupleQueryResult queryResult = queryExecutor.executeQuery(buildQuery(gameStats));
        return null;
    }

    private String buildQuery(GameStats gameStats) {
        return null;
    }
}
