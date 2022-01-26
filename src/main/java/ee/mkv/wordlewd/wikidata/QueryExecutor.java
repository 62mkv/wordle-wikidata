package ee.mkv.wordlewd.wikidata;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.http.client.RDF4JProtocolSession;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class QueryExecutor implements AutoCloseable {
    private static final String BASE_URI = "https://query.wikidata.org/sparql";

    private final HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(
                    RequestConfig
                            .custom()
                            .setCookieSpec("standard")
                            .build()
            )
            .build();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final RDF4JProtocolSession session = new RDF4JProtocolSession(httpClient, executorService);
    private final Dataset dataset = new SimpleDataset();

    public QueryExecutor() {
        session.setRepository(BASE_URI);
    }

    public TupleQueryResult executeQuery(String query) throws IOException {
        log.info("WD Query: {}", query);
        return session.sendTupleQuery(QueryLanguage.SPARQL, query, BASE_URI, dataset, false, 10);
    }

    public void close() throws Exception {
        session.close();
        ((CloseableHttpClient) httpClient).close();
        executorService.shutdown();
    }
}
