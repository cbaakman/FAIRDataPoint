package nl.dtls.fairdatapoint.api.dto.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.dtls.fairdatapoint.api.validator.ValidIri;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MetadataSchemaChildDTO {

    @NotBlank
    private String schemaUuid;

    @NotBlank
    private String schemaVersion;

    @NotBlank
    @ValidIri
    private String relationUri;

    @NotBlank
    private String title;

    @ValidIri
    private String tagsUri;

    @Valid
    @NotNull
    private List<MetadataSchemaChildMetadataDTO> metadata;
}
