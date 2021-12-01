package nl.dtls.fairdatapoint.entity.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class MetadataSchema {

    private static final String CATALOG_PREFIX = "catalog";

    @Id
    @JsonIgnore
    protected ObjectId id;

    @Indexed
    protected String uuid;

    @Indexed
    protected String versionString;

    @NotNull
    protected SemanticVersion version;

    @NotNull
    protected String urlPrefix;

    @NotBlank
    protected String name;

    @NotNull
    protected String shapeDefinition;

    @NotBlank
    protected String description;

    @NotNull
    protected Boolean abstractSchema;

    @DBRef
    protected MetadataSchema previousVersion = null;

    @DBRef
    protected List<MetadataSchema> extendsSchemas = new ArrayList<>();

    @Valid
    @NotNull
    protected List<MetadataSchemaChild> children = new ArrayList<>();

    @Valid
    @NotNull
    protected List<MetadataSchemaLink> links = new ArrayList<>();

    public boolean isRoot() {
        return urlPrefix.isEmpty();
    }

    public boolean isCatalog() {
        return name.equals(CATALOG_PREFIX);
    }

    public boolean isDraft() {
        return version == null;
    }
}
