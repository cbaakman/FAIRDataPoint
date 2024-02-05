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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import nl.dtls.fairdatapoint.database.mongo.repository.TermAssociationRepository;
import nl.dtls.fairdatapoint.entity.ontology.TermAssociation;


@Slf4j
public class OntologySearcher {
	
	/**
	 * Path to the directory where downloaded files are cached.
	 */
	private String cachePath;
	
	/**
	 * Whether or not to filter strings for punctuation characters. Like: . , { : etc.
	 */
	private boolean filterPunctuation = true;
	
	/**
	 * A list of words that should be ignored by the indexing.
	 */
	private List<String> stopWords = getStopWords();
	
	/**
	 * The list of urls that should be indexed.
	 */
	private List<URL> ontologyURLs = new ArrayList<URL>();
	

	
	/**
	 * A threshold value to determine which associations are relevant.
	 * Each association has a relevance value, that is precalculated for it.
	 * Associations which have their relevance below this setting will not be used to compute a result.
	 */
	private double relevanceThreshold;
	
	/**
	 * ontology manager, needed to index owl files.
	 */
	static private OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();

	/**
	 * ontology data factory, needed to index owl files.
	 */
	static private OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

	@Autowired
	TermAssociationRepository associationRepository;
	
	@PostConstruct
	private void init() {
		
		indexAllOntologies();
	}

	/**
	 * Setter for the relevance threshold.
	 * @param value the new value for the relevance threshold.
	 */
	public void setRelevanceThreshold(double value) {
		this.relevanceThreshold = value;
	}
	
	/**
	 * Tells whether a given ontology web language (OWL) file URL was already indexed by this class.
	 * 
	 * @param url, the url to the OWL file
	 * @return the answer it was already indexed or not
	 */
	private boolean alreadyIndexed(URL url) {
		
		return (associationRepository.findByUrl(url).size() > 0);
	}

	/**
	 * A setter for the cache path.
	 * 
	 * @param path, the path to the new cache directory
	 */
	public void setCachePath(String path) {
		cachePath = path;
	}

	/**
	 * A setter for the ontology url list.
	 * 
	 * @param ontologyUrls, the new list of ontology urls
	 */
	public void setOntologyUrls(List<URL> ontologyUrls) {
		
		this.ontologyURLs = ontologyUrls;
	}
	
	/**
	 * Get the full file path of the given file url, when it gets stored in the cache directory.
	 * 
	 * @param url to the file to store
	 * @return the full file path of the file in cache
	 */
	private File getCacheFilename(URL url) {
		
		File cached = new File(cachePath, new File(url.getFile()).getName());
		
		return cached;
	}
	
	/**
	 * Downloads the given file and stores it in cache.
	 * 
	 * @param url to the file to fetch
	 * @return the cached owl file
	 * @throws IOException, when there's a problem downloading the file
	 * @throws IOException, when there's a problem storing the file
	 */
	private File fetchOwl(URL url) throws IOException {
		
		// Download the owl file.
		File cached = getCacheFilename(url);
		if (!cached.isFile()) {

			log.info("downloading {}", cached.getName());
			
			FileUtils.copyURLToFile(url, cached);
		}
		
		return cached;
	}

	/**
	 * 	This method parses an ontology web language (OWL) file.
	 *  WARNING: It can put a strain on the memory, depending on the size of the input file.
	 *
	 * @param file the input OWL formatted file.
	 * @return the resulting ontology object from the file
	 * @throws OWLOntologyCreationException, if the document is not well formed
	 * @throws IOException, if the input file cannot be accessed
	 **/
	private static OWLOntology parseOwl(File file) throws OWLOntologyCreationException, IOException {

		
		OWLOntology ontology;
		InputStream input = new FileInputStream(file.toString());
		
		// Sometimes, OWL files are zipped.
		if (file.getName().endsWith(".zip")) {
		
			ZipInputStream zipInput = new ZipInputStream(input);
		
			zipInput.getNextEntry();
			
			ontology = ontologyManager.loadOntologyFromOntologyDocument(zipInput);
			
			zipInput.close();
		}
		else {
			ontology = ontologyManager.loadOntologyFromOntologyDocument(input);
			input.close();
		}
		
		return ontology;
	}
	
	/**
	 * Undoes all the work of indexAllOntologies.
	 * clears the database.
	 */
	public void clearAllIndexes() {
		
		log.info("clearing all associations from repository");
		
		associationRepository.deleteAll();
	}

	/**
	 * Fills the mongo repositories with association data, required for a fast response of the 'getAssociations' method.
	 */
	public void indexAllOntologies() {
		
		for (URL url : this.ontologyURLs) {

			// Don't index the same ontology at every reboot.
			if (alreadyIndexed(url))
				continue;
			
			try {
				File owlFile = fetchOwl(url);
				
				log.info("parsing {}", owlFile.getName());
				OWLOntology ontology = parseOwl(owlFile);
				
				log.info("beginning to index {}", url);
				indexOntology(ontology, url);
				
			} catch (IOException e) {
				log.error("I/O exception on indexing {}: {}", url, e);
				
			} catch (OWLOntologyCreationException e) {
				log.error("ontology exception on indexing {}: {}", url, e);
			}
		}
	}

	/**
	 * Counts the number of times that each term occurs in the given list.
	 * 
	 * @param terms the input list of encountered terms
	 * @return a map, holding the number of occurences per term
	 */
	private Map<String, Integer> countTerms(List<String> terms) {

		// Fill a map, containing the count of each string within the input list.
		Map<String, Integer> termCount = new HashMap<String, Integer>();
		for (String term : terms) {
			int count = 1;
			if (termCount.containsKey(term))
				count += termCount.get(term);
			termCount.put(term, count);
		}
		
		return termCount;
	}
	
	/**
	 * Indexes all the classes in one ontology's signature.
	 * For every class, get the terms in the description, count the occurrences,
	 * associate terms that occur together in one class and store in the database.
	 * 
	 * @param ontology the ontology to index
	 * @param url that the ontology came from, this will be stored with all associated data
	 */
	private void indexOntology(OWLOntology ontology, URL url) {
		
		// Count the frequency of the terms everywhere.
		List<String> allTerms = new ArrayList<String>();
		int classCount = 0;
		for (OWLClass cls : ontology.getClassesInSignature()) {
			allTerms.addAll(getTermsInClass(ontology, cls));
			classCount ++;
		}
		
		// Count for every term how often it occurs overall.
		Map<String, Integer> termCount = countTerms(allTerms);

		log.debug("finding associations from terms in classes");
		List<TermAssociation> associations = new ArrayList<TermAssociation>();
		for (OWLClass cls : ontology.getClassesInSignature()) {
			
			// get terms in class
			List<String> termsInClass = getTermsInClass(ontology, cls);
			int totalClassTermCount = termsInClass.size();
			
			// count the term frequency within the class
			Map<String, Integer> termCountInClass = countTerms(termsInClass);
			
			// See which words are together in this class.
			for (String term1 : termCountInClass.keySet()) {
				for (String term2 : termCountInClass.keySet()) {
					
					// Calculate the relevance for the association.
					double idf1 = Math.log((double)classCount / termCount.get(term1)),
						   idf2 = Math.log((double)classCount / termCount.get(term2)),
						   tf1 = (double)termCountInClass.get(term1) / totalClassTermCount,
						   tf2 = (double)termCountInClass.get(term2) / totalClassTermCount,
						   
						   relevance = tf1 * idf1 * tf2 * idf2;
					
					// store in object
					TermAssociation association = new TermAssociation();
					association.setKey(term1);
					association.setValue(term2);
					association.setRelevance(relevance);
					association.setUrl(url);
					
					associations.add(association);
				}
			}
		}
		
		log.debug("storing {} associations", associations.size());
		
		associationRepository.insert(associations);
	}
	
	/**
	 * Get all the terms from the class.
	 * 1. Get the RDFS label of the class.
	 * 2. Return all the keywords in this label as terms.
	 * 
	 * @param ontology the ontology that the class is in
	 * @param cls the class to take the terms from
	 * @return a list of all the terms in the class, it may included the same term twice.
	 */
	private List<String> getTermsInClass(OWLOntology ontology, OWLClass cls) {
		
		// Retrieve the RDFS labels from the classes:
		OWLAnnotationProperty labelProperty = dataFactory.getRDFSLabel();
		Collection<OWLAnnotation> annotations =  EntitySearcher.getAnnotations(cls, ontology, labelProperty)
												 .collect(Collectors.toCollection(LinkedHashSet::new));

		List<String> terms = new ArrayList<String>();
		for (OWLAnnotation annotation : annotations) {
				
			// get the label's value as a literal
			Optional<OWLLiteral> optionalLiteral = annotation.getValue().asLiteral();
			if (optionalLiteral.isPresent()) {
				
				// get string from literal
				Optional<String> optionalText = getStringFromLiteral(optionalLiteral.get());
				if (optionalText.isPresent()) {
					
					// Get the keywords from the label.
					for (String word : getKeywordsFromString(optionalText.get())) {
						
						terms.add(word);
					}
				}
			}
		}
		
		return terms;	
	}
	
	static final Pattern literalStringPattern = Pattern.compile("^\\\"(.*)\\\"\\^\\^xsd:string$", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Find out whether a literal is a string and return the string if so.
	 * 
	 * @param literal the literal to test on and get the string from
	 * @return the resulting string, or nothing if it wasn't a string
	 */
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

	/**
	 * Breaks a string apart into separate keywords, skipping punctuation and stop words.
	 * 
	 * @return the list of resulting stop words
	 */
	public List<String> getKeywordsFromString(String input) {
		
		List<String> result = new ArrayList<String>();
		for (String word : input.toLowerCase().split(" ")) {
			
			// remove punctuation, if that's the setting
			if (filterPunctuation)
				word = wordWithoutPunctuation(word);
			
			// remove stopwords, according to settings
			if (!stopWords.contains(word) && word.length() > 3)
				result.add(word);
		}
		
		return result;
	}

	/**
	 * Removes punctuation characters from the string and return it.
	 * Every non-alphanumeric character is considered punctuation.
	 * 
	 * @param s input string
	 * @return the input string without punctuation characters.
	 */
	private static String wordWithoutPunctuation(String s) {
		
		// Return this input string, but without punctuation characters.
		String r = "";
		for (Character c : s.toCharArray()) 
		{
			if(Character.isLetterOrDigit(c)) {
				r += c;
			}
		}
		
		return r;
	}

	/**
	 * Get a list of stop words from a config file.
	 * 
	 * @return all the stop words in a list.
	 */
	private List<String> getStopWords() {
		
		// Get the english stop words from the config file.
		InputStream input = OntologySearcher.class.getResourceAsStream("english-stopwords.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		
		List<String> lines = new ArrayList<String>();
		String line;
		
		try {
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
			
		} catch (IOException e) {
			throw new RuntimeException("Cannot read from english-stopwords.txt: " + e);
		}

		// each line has one word
		return lines;
	}
	
	/**
	 * Look up all the stored associations for a given input string.
	 * This method will get all the keywords in the string and then retrieve all associations with it.
	 * Associations that lie under the relevance threshold setting will be skipped.
	 * 
	 * @param input the input string with keywords
	 * @return all associations that are relevant enough.
	 */
	public List<TermAssociation> getAssociations(String input) {

        log.info("getting associations for '{}'", input);
		
		// Get associations from mongo, that have words from 'input' as key.
		List<TermAssociation> associations = new ArrayList<TermAssociation>();
		
		for (TermAssociation a : associationRepository.findByKeysAndUrls(getKeywordsFromString(input), this.ontologyURLs)) {
			
            log.info("comparing '{}' association '{}' with relevance {} to {}",
                     a.getKey(), a.getValue(), a.getRelevance(), this.relevanceThreshold);

			if (a.getRelevance() > this.relevanceThreshold)
				associations.add(a);
		}
		
		log.info("found {} associations for \"{}\"", associations.size(), input);
		
		return associations;
	}
}
