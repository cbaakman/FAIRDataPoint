package nl.dtls.fairdatapoint.api.dto.schema;

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
public class MetadataSchemaBundleDTO {

    @NotBlank
    private String uuid;

    @NotBlank
    private String name;

    @NotNull
    private Boolean hasDraft;

    @Valid
    private List<MetadataSchemaBundleVersionDTO> versions;
}
