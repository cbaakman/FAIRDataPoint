package nl.dtls.fairdatapoint.database.rdf.ontology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class OntologyParser {
	
	private List<OWLOntology> ontologies;
	private List<String> stopWords = readStopWords();
	private boolean filterPunctuations;
	
	private final Map<OWLOntology, Map<String, OWLIndividual>> ontologySplits =
		new HashMap<OWLOntology, Map<String, OWLIndividual>>();
	private final Map<String, Integer> ontologyCounts = new HashMap<String, Integer>();
	
	private String processedQuery = "";

	public OntologyParser(List<OWLOntology> ontologies) throws IOException {
		this(ontologies, false);
	}
	
	public OntologyParser(List<OWLOntology> ontologies, boolean filterPunctuations) throws IOException
	{
		this.ontologies = ontologies;
		this.filterPunctuations = filterPunctuations;
		
		for (OWLOntology ontology : this.ontologies) {
			ontologySplits.put(ontology, new HashMap());
		}
    }
	
	private void cleanData()
	{
		/// Cleans the keywords gathered from previous searches
		
		ontologyCounts.clear();
		for (OWLOntology ontology : ontologySplits.keySet())
			ontologySplits.get(ontology).clear();
	}
	
	private List<String> processConcepts()
	{
		/// This function converts the ontology output into a list sorted by most occurring concept
		
		List<Entry<String, Integer>> sorted = new ArrayList(ontologyCounts.entrySet());
		sorted.sort(Entry.comparingByValue());
						
		List<String> result = new ArrayList();
		for (Entry<String, Integer> entry : sorted) {
			result.add(entry.getKey());
		}
		
		return result;
	}
	
	private List<String> processInput(final String userInput)
	{
		List<String> result = new ArrayList();
		for (String word : userInput.split(",")) {
			word = word.toLowerCase();
			if (filterPunctuations)
				word = word.replaceAll("\\W+\\b", " ").replaceAll("\\W+$", "");
			
			if (!stopWords.contains(word.toLowerCase()))
				result.add(word);
		}
		return result;
	}
	
	public void search(final String userInput)
	{
		cleanData();
		List<String> processedInput = processInput(userInput);
		for (OWLOntology ontology : ontologySplits.keySet()) {
			for (String term : processedInput) {
				
				for (OWLAnonymousIndividual individual : ontology.getIndivi)
				{
				}
				
				for (OWLNamedIndividual individual : ontology.getIndividualsInSignature())
				{
				}
			}
		}
	}
	
	private String getLabel(final OWLIndividual individual)
	{
		for (OWLAnnotationProperty property : individual.getObjectPropertiesInSignature())
		{
			if(property.isLabel())
				return property.toString();
		}
		
		throw new RuntimeException("no label on " + individual.toString());
	}
	
	private void addConcepts(final OWLIndividual result, final OWLOntology ontology)
	{
		String label = getLabel(result);
		
		ontologySplits.get(ontology).put(label, result);
		addCount(label);
		
	}
	
	private void addCount(final String label)
	{
		/// Adds term to stored dict
		
		if (ontologyCounts.containsKey(label))
			ontologyCounts.put(label, ontologyCounts.get(label) + 1);
		else
			ontologyCounts.put(label, 1);
	}
	
	private static final String punctuations = "!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~";
	
	private static String replacePunctuations(final String s)
	{
		String r = s;
		for (int i = 0; i < punctuations.length(); i++) {
			
			char punctuation = punctuations.charAt(i);
			r = r.replace(punctuation, ' ');
		}
		return r;
	}

	private static List<String> readStopWords() throws IOException {
		
		List<String> words = new ArrayList();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("english-stopwords.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		for (String line; (line = reader.readLine()) != null;) {
			
			words.add(line.trim());
		}
		return words;
	}
}
