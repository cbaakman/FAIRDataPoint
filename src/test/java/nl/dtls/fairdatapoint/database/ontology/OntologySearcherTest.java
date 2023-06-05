package nl.dtls.fairdatapoint.database.ontology;

import static nl.dtls.fairdatapoint.entity.metadata.MetadataGetter.getUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nl.dtls.fairdatapoint.WebIntegrationTest;
import nl.dtls.fairdatapoint.database.rdf.repository.exception.MetadataRepositoryException;

public class OntologySearcherTest extends WebIntegrationTest {

    @Test
    @DisplayName("'search' should find something")
    public void testSearch() throws MetadataRepositoryException {
    	
    	URL thesaurusURL = OntologySearcherTest.class.getClassLoader().getResource("ontologies/Thesaurus.owl");
    	
    	OntologySearcher searcher = new OntologySearcher(true);
		
    	searcher.indexOntologies(new URL[] {thesaurusURL});
    }
}