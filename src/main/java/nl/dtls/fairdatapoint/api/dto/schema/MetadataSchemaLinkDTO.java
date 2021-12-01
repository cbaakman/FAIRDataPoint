package nl.dtls.fairdatapoint.api.dto.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.dtls.fairdatapoint.api.validator.ValidIri;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MetadataSchemaLinkDTO {

    @NotBlank
    private String title;

    @NotBlank
    @ValidIri
    private String propertyUri;
}
