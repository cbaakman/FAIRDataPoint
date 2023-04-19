package nl.dtls.fairdatapoint.database.rdf.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class OntologySearcher {
	
	private boolean filterPunctuation;
	private List<String> stopWords;
	private Map<OWLOntology, Map<String, OWLEntity>> ontologySplits = new HashMap<OWLOntology, Map<String, OWLEntity>>();
	private Map<String, Integer> ontologyCount = new HashMap<String, Integer>();
	
	static private OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
	static private OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	
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
	
	private void cleanData() {
		
		ontologyCount.clear();
		for (Map<String, OWLEntity> v : ontologySplits.values())
			v.clear();
	}

	private static Map<OWLOntology, Map<String, OWLEntity>> loadOntologies(List<String> ontologyURIs) {
		
		Map<OWLOntology, Map<String, OWLEntity>> map = new HashMap<OWLOntology, Map<String, OWLEntity>>();
		
		for (String ontologyURI : ontologyURIs) {
			
			OWLOntology ontology = ontologyManager.getOntology(IRI.create(ontologyURI));
			
			map.put(ontology, new HashMap<String, OWLEntity>());
		}
		
		return map;
	}
	
	private List<String> processInput(String input) {
		
		List<String> result = new ArrayList<String>();
		
		for (String word : input.toLowerCase().split(",")) {
			
			word = wordWithoutPunctuation(word);
			
			if (!stopWords.contains(word) && word.length() > 0)
				result.add(word);
		}
		
		return result;
	}

	private static List<OWLIndividual> getIndividuals(OWLOntology ontology) {
		
		List<OWLIndividual> individuals = new ArrayList<OWLIndividual>();
		
		for (OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
			individuals.add(individual);
		}
		
		for (OWLAnonymousIndividual individual : ontology.getAnonymousIndividuals()) {
			individuals.add(individual);
		}
		
		return individuals;
	}
	
	private static List<OWLObject> getLabels(OWLEntity entity) {
		
		List<OWLObject> result = new ArrayList<OWLObject>();
		
		for(OWLClass owlClass: entity.getClassesInSignature()) {
			
			for (OWLAnnotationProperty property : owlClass.getAnnotationPropertiesInSignature()) {
				
				if (property.isLabel())
					result.add(property);
			}
		}
		
		return result;
	}
	
	private void addCountFor(String label) {
		
		label = label.toLowerCase();

		if (!ontologyCount.containsKey(label))	
			ontologyCount.put(label, 1);
	}
	
	private void incrementCountFor(String label) {
		label = label.toLowerCase();
		
		if (ontologyCount.containsKey(label))
			ontologyCount.put(label, ontologyCount.get(label) + 1);
		else
			ontologyCount.put(label, 1);
	}
	
	private void addConcepts(OWLObject result, OWLOntology ontology) {
		for (OWLClass relatedClass : getRelatedClasses(result)) {

			List<OWLObject> labels = getLabels(relatedClass);

			boolean hasObsolete = false;
			for (OWLObject label : labels) {
				hasObsolete = hasObsolete || isObsolete(label);
			}
			if (!hasObsolete && labels.size() > 0) {
				addConcept(labels.get(0), relatedClass, ontology);
			}
			else {
				OWLEntity value = getValue(relatedClass);
				
				labels = getLabels(value);
				if (labels.size() > 0)
					addConcept(labels.get(0), value, ontology);
			}
		}
	}
	
	private void addConcept(OWLObject label, OWLObject result, OWLOntology ontology) {
		
		String key = label.toString().toLowerCase();
		
		ontologySplits.get(ontology).put(key, result);
		
		incrementCountFor(key);
	}
	
	public OntologySearcher(List<String> ontologyURIs, boolean filterPunctuation) {
		filterPunctuation = filterPunctuation;
		stopWords = getStopWords();
		ontologySplits = loadOntologies(ontologyURIs);
	}
	
	public String search(String input) {
		
		cleanData();
		
		List<String> inputTerms = this.processInput(input);
		if (inputTerms.isEmpty())
			return "";
		
		for (OWLOntology ontology : ontologySplits.keySet()) {
			for (String term : inputTerms) {
				for (OWLIndividual individual : getIndividuals(ontology)) {
					for (OWLLiteral label : getLabels(individual)) {
						if (label.toString().contains(term)) {
							addConcepts(individual, ontology);
						}
					}
				}
				
				OWLIndividual concept = searchOneInOntology(term);
				if (concept != null) {
					addConcepts(concept, ontology);
				}
				else {
					addCountFor(term);
				}
			}
		}
		
		return null;
	}
}
