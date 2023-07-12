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
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import nl.dtls.fairdatapoint.BaseIntegrationTest;
import nl.dtls.fairdatapoint.WebIntegrationTest;
import nl.dtls.fairdatapoint.database.rdf.repository.exception.MetadataRepositoryException;

public class OntologySearcherTest extends BaseIntegrationTest {
	
	static private Logger log = LoggerFactory.getLogger(OntologySearcherTest.class);
	
	@Autowired
	OntologySearcher searcher;
	
    @Test
    @DisplayName("'getExtendedKeywords' should find something")
    public void testSearch() throws MetadataRepositoryException {
    	
    	Set<String> keywords = searcher.getExtendedKeywords("disease");
    	
		log.debug("got {} associated keywords", keywords.size());
    	
    	if (keywords.size() <= 1)
    		fail();
    	
    	// test the scoring of keywords
    	double totalScore = 0.0;
    	for (String keyword : keywords) {
    		if (!"disease".equals(keyword)) {
    			
    			double wordScore = searcher.getKeywordRankingScore(keyword);
    			
    			log.debug("{} has score {}", keyword, wordScore);
    			totalScore += wordScore;
    		}
    	}

    	if (totalScore <= 0.0)
    		fail();
    }
}