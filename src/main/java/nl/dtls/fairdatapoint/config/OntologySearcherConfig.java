package nl.dtls.fairdatapoint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nl.dtls.fairdatapoint.config.properties.OntologySearchProperties;
import nl.dtls.fairdatapoint.database.ontology.OntologySearcher;

@Configuration
public class OntologySearcherConfig {

    @Bean("ontologySearcher")
    public OntologySearcher customOntologySearcher(OntologySearchProperties properties) {
    	
    	final OntologySearcher searcher = new OntologySearcher();
    	
    	searcher.setOntologyUrls(properties.getOntologyUrls());
    	searcher.setRelevanceThreshold(properties.getAssociationRelevanceThreshold());
    	
		return searcher;
    }
}
