package nl.dtls.fairdatapoint.entity.schema;

import lombok.*;
import nl.dtls.fairdatapoint.api.validator.ValidIri;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class MetadataSchemaChild {

    @DBRef
    protected MetadataSchema childSchema;

    @NotBlank
    @ValidIri
    protected String relationUri;

    @NotBlank
    protected String title;

    @ValidIri
    protected String tagsUri;

    @Valid
    @NotNull
    protected List<MetadataSchemaChildMetadata> metadata = new ArrayList<>();
}
