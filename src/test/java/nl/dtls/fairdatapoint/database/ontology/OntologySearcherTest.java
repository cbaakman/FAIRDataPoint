/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.dtls.fairdatapoint.database.ontology;


import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;

import nl.dtls.fairdatapoint.BaseIntegrationTest;
import nl.dtls.fairdatapoint.database.rdf.repository.exception.MetadataRepositoryException;
import nl.dtls.fairdatapoint.entity.ontology.TermAssociation;

@TestInstance(Lifecycle.PER_CLASS)
public class OntologySearcherTest extends BaseIntegrationTest {
		
	@Autowired
	OntologySearcher searcher;
	
	@BeforeAll
	public void setup() throws MalformedURLException {
		
		List<URL> urls = new ArrayList<URL>();
		urls.add(new URL("https://evs.nci.nih.gov/ftp1/NCI_Thesaurus/Thesaurus_23.09d.OWL.zip"));
		searcher.setOntologyUrls(urls);
		
		searcher.setRelevanceThreshold(0.0);
		
		searcher.indexAllOntologies();
	}
	
	@AfterAll
	public void teardown() {
		searcher.clearAllIndexes();
	}
	
    @Test
    @DisplayName("'OntologySearcher' should find additional terms with relevance above zero")
    public void testSearch() throws MetadataRepositoryException {
    	
    	long t0 = System.currentTimeMillis();
    	
    	List<TermAssociation> associations = searcher.getAssociations("disease");
    	
    	long t1 = System.currentTimeMillis();
    	double dt = ((double)(t1 - t0)) / 1000;
    	
    	// Must respond in less than a minute.
    	assertThat(dt < 60.0); 
    	
    	assertThat(associations.size() > 1);
    	
    	assertThat(associations.get(0).getRelevance() > 0.0);
    }
}