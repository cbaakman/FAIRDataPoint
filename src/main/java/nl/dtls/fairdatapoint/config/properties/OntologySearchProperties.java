package nl.dtls.fairdatapoint.config.properties;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ConfigurationProperties(prefix = "search")
public class OntologySearchProperties {
	
	private List<URL> ontologyUrls = new ArrayList<URL>();
	
	private double associationRelevanceThreshold;
}
