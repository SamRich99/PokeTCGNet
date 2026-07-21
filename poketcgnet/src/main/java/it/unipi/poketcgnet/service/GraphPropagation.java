package it.unipi.poketcgnet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Gestione della propagazione fallita nel dual-write Mongo-first -> Neo4j (scelta
// AP/consistenza eventuale,
@Component
public class GraphPropagation {

    private static final Logger log = LoggerFactory.getLogger(GraphPropagation.class);

    public void propagate(String operation, Runnable graphWrite) {
        try {
            graphWrite.run();
        } catch (RuntimeException first) {
            try {
                graphWrite.run();
                log.warn("Propagazione Neo4j riuscita al retry [{}] (primo tentativo: {})",
                        operation, first.getMessage());
            } catch (RuntimeException second) {
                log.error("GRAPH_PROPAGATION_FAILED [{}]: {} — Mongo aggiornato, grafo NO: "
                        + "da riconciliare in batch", operation, second.getMessage());
            }
        }
    }
}
