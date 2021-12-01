package nl.dtls.fairdatapoint.api.dto.schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MetadataSchemaCoordsDTO {
    @NotBlank
    private String uuid;

    @NotBlank
    private String version;
}
