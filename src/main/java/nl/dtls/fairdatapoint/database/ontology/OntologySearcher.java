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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.dtls.fairdatapoint.database.mongo.repository.TermAssociationRepository;
import nl.dtls.fairdatapoint.entity.ontology.TermAssociation;

@Service
public class OntologySearcher {
	
	private boolean filterPunctuation = true;
	private List<String> stopWords = getStopWords();
	
	static private OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
	static private OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	
	static private Logger log = LoggerFactory.getLogger(OntologySearcher.class);

	@Autowired
	TermAssociationRepository associationRepository;
	
	@PostConstruct
	private void init() {
		
		if (associationRepository.count() == 0) {
			
			indexAllOntologies();
		}
	}
	
	private static File getCacheDirectory() throws IOException {
		
		File directory = new File("./fdp-ontology-cache");
		
		if (!directory.isDirectory()) {
			directory.mkdir();
		}
		
		return directory;
	}
	
	private static OWLOntology fetchThesaurus() throws IOException, OWLOntologyCreationException {
		
		File cachePath = new File(getCacheDirectory(), "Thesaurus_23.05e.OWL.zip");
		
		if (!cachePath.isFile()) {

			log.info("downloading {}", cachePath.getName());
			
			URL ontologyURL = new URL("https://evs.nci.nih.gov/ftp1/NCI_Thesaurus/archive/23.05e_Release/Thesaurus_23.05e.OWL.zip");
			
			FileUtils.copyURLToFile(ontologyURL, cachePath);
		}

		log.info("parsing {}", cachePath.getName());
		
		InputStream input = new FileInputStream(cachePath.toString());
		
		ZipInputStream zipInput = new ZipInputStream(input);
		
		zipInput.getNextEntry();
		
		return ontologyManager.loadOntologyFromOntologyDocument(zipInput);
	}
	
	public void clearAllIndexes() {
		
		log.info("clearing all indexes");
		
		associationRepository.deleteAll();
	}

	public void indexAllOntologies() {
		
		List<OWLOntology> ontologies = new ArrayList<OWLOntology>();

		log.info("beginning to index all ontologies");
		try {
			OWLOntology thesaurus = fetchThesaurus();
			log.info("finished loading the thesaurus ontology");
			
			ontologies.add(thesaurus);
		} catch (IOException e) {
			log.error("I/O exception on indexing thesaurus: {}", e);
			
		} catch (OWLOntologyCreationException e) {
			log.error("ontology exception on indexing thesaurus: {}", e);
		}
		
		indexOntologies(ontologies);
	}
	
	private void indexOntologies(List<OWLOntology> ontologies) {

		log.debug("finding terms in classes");
		
		List<TermAssociation> associations = new ArrayList<TermAssociation>();
		
		for (OWLOntology ontology : ontologies) {
			
			for (OWLClass cls : ontology.getClassesInSignature()) {
				
				// get terms in class
				List<String> terms = getTermsInClass(ontology, cls);
				
				// convert to unique set
				Set<String> uniqueTermsInClass = new HashSet<String>(terms);
				
				// associate words with each other
				for (String term1 : uniqueTermsInClass) {
					for (String term2 : uniqueTermsInClass) {

						TermAssociation association = new TermAssociation();
						association.setKey(term1);
						association.setValue(term2);

						associations.add(association);
					}
				}
			}
		}
		
		log.debug("storing associations");
		
		associationRepository.insert(associations);
	}
	
	private static long ASSOCIATION_BUFFER_SIZE = 10000000;
	
	private List<String> getTermsInClass(OWLOntology ontology, OWLClass cls) {
		
		List<String> terms = new ArrayList<String>();
		
		OWLAnnotationProperty labelProperty = dataFactory.getRDFSLabel();
		
		Collection<OWLAnnotation> annotations =  EntitySearcher.getAnnotations(cls, ontology, labelProperty)
												 .collect(Collectors.toCollection(LinkedHashSet::new));
		
		for (OWLAnnotation annotation : annotations) {
				
			Optional<OWLLiteral> optionalLiteral = annotation.getValue().asLiteral();
			
			if (optionalLiteral.isPresent()) {
				
				Optional<String> optionalText = getStringFromLiteral(optionalLiteral.get());
				
				if (optionalText.isPresent()) {
					
					for (String word : getKeywordsFromString(optionalText.get())) {
						
						terms.add(word);
					}
				}
			}
		}
		
		return terms;	
	}
	
	static Pattern literalStringPattern = Pattern.compile("^\\\"(.*)\\\"\\^\\^xsd:string$", Pattern.CASE_INSENSITIVE);
	
	private static Optional<String> getStringFromLiteral(OWLLiteral literal) {
		
		Matcher matcher = literalStringPattern.matcher(literal.toString());
		
		if (matcher.find()) {
			// matched string pattern
			return Optional.of(matcher.group(1));
		}
		else {
			// this is not a string
			return Optional.empty();
		}
	}

	public List<String> getKeywordsFromString(String input) {
		
		List<String> result = new ArrayList<String>();
		
		for (String word : input.toLowerCase().split(" ")) {
			
			if (filterPunctuation)
				word = wordWithoutPunctuation(word);
			
			if (!stopWords.contains(word) && word.length() > 3)
				result.add(word);
		}
		
		return result;
	}

	private static String wordWithoutPunctuation(String s) {
		
		String r = "";
		for (Character c : s.toCharArray()) 
		{
			if(Character.isLetterOrDigit(c)) {
				r += c;
			}
		}
		
		return r;
	}

	private List<String> getStopWords() {
		
		URL resourceURL = OntologySearcher.class.getResource("../../../../../english-stopwords.txt");
		if (resourceURL == null) {
			throw new NullPointerException("Got a null pointer while accessing resource: english-stopwords.txt");
		}
		
		Path path = Path.of(resourceURL.getPath());
		
		try {
			return Files.readAllLines(path);
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot read from english-stopwords.txt: " + e);
		}
	}
	
	public List<TermAssociation> getAssociations(String input) {
		
		List<TermAssociation> associations = new ArrayList<TermAssociation>();
		
		for (String key : getKeywordsFromString(input)) {
			associations.addAll(associationRepository.findByKey(key));
		}
		
		log.debug("found {} associations for \"{}\"", associations.size(), input);
		
		return associations;
	}
}
