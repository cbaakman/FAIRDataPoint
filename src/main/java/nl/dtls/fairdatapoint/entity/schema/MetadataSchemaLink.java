package nl.dtls.fairdatapoint.entity.schema;

import lombok.*;
import nl.dtls.fairdatapoint.api.validator.ValidIri;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MetadataSchemaLink {

    @NotBlank
    protected String title;

    @NotBlank
    @ValidIri
    protected String propertyUri;
}
