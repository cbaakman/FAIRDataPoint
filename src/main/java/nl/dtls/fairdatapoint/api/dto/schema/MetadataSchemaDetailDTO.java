package nl.dtls.fairdatapoint.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MetadataSchemaDetailDTO {

    @NotNull
    protected String uuid;

    @NotNull
    protected String version;

    @NotNull
    protected Boolean draft;

    @NotNull
    protected String urlPrefix;

    @NotBlank
    protected String name;

    @NotNull
    protected String shapeDefinition;

    @NotBlank
    protected String description;

    @NotNull
    @JsonProperty("abstract")
    protected Boolean abstractSchema;

    @Valid
    @NotNull
    @JsonProperty("extends")
    private List<MetadataSchemaCoordsDTO> extendsSchemas;

    @Valid
    @NotNull
    private List<MetadataSchemaChildDTO> children;

    @Valid
    @NotNull
    private List<MetadataSchemaLinkDTO> externalLinks;
}
