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
public class MetadataSchemaBundleVersionDTO {

    @NotBlank
    private String version;

    @NotBlank
    private String name;

    @NotNull
    @JsonProperty("abstract")
    private Boolean abstractSchema;
    
    @Valid
    @NotNull
    @JsonProperty("extends")
    private List<MetadataSchemaCoordsDTO> extendsSchemas;

    @Valid
    @NotNull
    private List<MetadataSchemaCoordsDTO> children;
}
