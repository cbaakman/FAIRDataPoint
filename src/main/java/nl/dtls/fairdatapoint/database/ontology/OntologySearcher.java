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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologySearcher {
	
	private boolean filterPunctuation;
	private List<String> stopWords;
	
	static private OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
	static private OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	
	static private Logger log = LoggerFactory.getLogger(OntologySearcher.class);

	public OntologySearcher(boolean filterPunctuation) {
		filterPunctuation = filterPunctuation;
		
		stopWords = getStopWords();
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
	
	private Map<String, String> associations = new HashMap<String, String>();
	
	private void associate(String s1, String s2) {
		
		log.debug(String.format("associate %s with %s", s1, s2));
		
		associations.put(s1, s2);
		associations.put(s2, s1);
	}

	private static OWLOntology loadOntology(URL ontologyURL) throws IOException, OWLOntologyCreationException {
		
		InputStream input = ontologyURL.openStream();
		
		OWLOntology ontology = null;
		
		try {
			log.debug("loading ontology {}", ontologyURL.toString());
			
			ontology = ontologyManager.loadOntologyFromOntologyDocument(input);
		}
		finally {
			input.close();
		}
		
		if (ontology == null)
			throw new NullPointerException("Got null while trying to create an ontology from " + ontologyURL);
			
		return ontology;
	}

	public void indexOntologies(URL[] ontologyURLs) {
		
		for (URL ontologyURL : ontologyURLs) {
			
			OWLOntology ontology = null;
			try {
				ontology = loadOntology(ontologyURL);
			}
			catch (Exception e) {
				
				log.error("while loading {}: {}", ontologyURL.toString(), e.toString());
				
				continue;
			}
			
			indexProperties(ontology);
			indexClasses(ontology);
		}
	}

	private void indexClasses(OWLOntology ontology) {
		for (OWLClass cls : ontology.getClassesInSignature())
			indexClass(cls);
	}

	private void indexClass(OWLClass cls) {
		
		for (OWLClass clss : cls.getClassesInSignature()) {
			indexClass(clss);
		}
		
		List<String> terms = new ArrayList<String>();
		for (OWLAnonymousIndividual individual : cls.getAnonymousIndividuals()) {
			terms.addAll(getIndividualTerms(individual));
		}
		
		for (OWLNamedIndividual individual : cls.getIndividualsInSignature()) {
			terms.addAll(getIndividualTerms(individual));
		}
		
		for (String term1 : terms) {
			for (String term2 : terms) {
				
				associate(term1, term2);
			}
		}
	}

	private List<String> getIndividualTerms(OWLIndividual individual) {
		
		log.debug("getting terms from individual {}", individual.toString());
		
		if (individual.isOWLNamedIndividual()) {
			return getKeywordsFromString(individual.toString());
		}
		
		// empty list
		return new ArrayList<String>();
	}

	private void indexProperties(OWLOntology ontology) {

		indexAnnotationProperties(ontology);
		indexObjectProperties(ontology);
	}

	private void indexAnnotationProperties(OWLOntology ontology) {
	}

	private void indexObjectProperties(OWLOntology ontology) {
	}

	private List<String> getKeywordsFromString(String input) {
		
		List<String> result = new ArrayList<String>();
		
		for (String word : input.toLowerCase().split(",")) {
			
			if (filterPunctuation)
				word = wordWithoutPunctuation(word);
			
			if (!stopWords.contains(word) && word.length() > 0)
				result.add(word);
		}
		
		return result;
	}


	private List<String> getStopWords() {
		
		URL resourceURL = OntologySearcher.class.getResource("/english-stopwords.txt");
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
	
	public List<String> search(String input) {
		
			return new ArrayList<String>();
	}
}
