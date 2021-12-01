package nl.dtls.fairdatapoint.service.schema;

import nl.dtls.fairdatapoint.api.dto.schema.*;
import nl.dtls.fairdatapoint.entity.schema.*;
import org.springframework.data.mongodb.core.query.Meta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MetadataSchemaMapper {
    public MetadataSchemaBundleDTO toBundleDTO(List<MetadataSchema> schemas) {
        return null;
    }

    public MetadataSchemaDetailDTO toDetailDTO(MetadataSchema schema) {
        return new MetadataSchemaDetailDTO(
                schema.getUuid(),
                schema.getVersionString(),
                schema.isDraft(),
                schema.getUrlPrefix(),
                schema.getName(),
                schema.getShapeDefinition(),
                schema.getDescription(),
                schema.getAbstractSchema(),
                schema.getExtendsSchemas().stream().map(this::toCoordsDTO).collect(Collectors.toList()),
                schema.getChildren().stream().map(this::toChildDTO).collect(Collectors.toList()),
                schema.getLinks().stream().map(this::toLinkDTO).collect(Collectors.toList())
        );
    }

    private MetadataSchemaLinkDTO toLinkDTO(MetadataSchemaLink link) {
        return new MetadataSchemaLinkDTO(
                link.getTitle(),
                link.getPropertyUri()
        );
    }

    private MetadataSchemaChildDTO toChildDTO(MetadataSchemaChild child) {
        return new MetadataSchemaChildDTO(
                child.getChildSchema().getUuid(),
                child.getChildSchema().getVersionString(),
                child.getRelationUri(),
                child.getTitle(),
                child.getTagsUri(),
                child.getMetadata().stream().map(this::toChildMetadataDTO).collect(Collectors.toList())
        );
    }

    private MetadataSchemaChildMetadataDTO toChildMetadataDTO(MetadataSchemaChildMetadata childMetadata) {
        return new MetadataSchemaChildMetadataDTO(
                childMetadata.getTitle(),
                childMetadata.getPropertyUri()
        );
    }

    private MetadataSchemaCoordsDTO toCoordsDTO(MetadataSchema s) {
        return new MetadataSchemaCoordsDTO(
                s.getUuid(),
                s.getVersion().toString()
        );
    }

    public MetadataSchemaDetailDTO toDraftDetailDTO(MetadataSchema schema) {
        MetadataSchemaDetailDTO dto = toDetailDTO(schema);
        dto.setDraft(true);
        dto.setVersion(null);
        return dto;
    }

    public MetadataSchema fromChangeDTO(MetadataSchemaChangeDTO dto, String uuid, List<MetadataSchema> extendsSchemas, List<MetadataSchemaChild> children) {
        return MetadataSchema.builder()
                .uuid(uuid)
                .version(null)
                .versionString(null)
                .name(dto.getName())
                .description(dto.getDescription())
                .shapeDefinition(dto.getShapeDefinition())
                .urlPrefix(dto.getUrlPrefix())
                .abstractSchema(dto.getAbstractSchema())
                .previousVersion(null)
                .extendsSchemas(extendsSchemas)
                .children(children)
                .links(dto.getExternalLinks().stream().map(this::fromLinkDTO).collect(Collectors.toList()))
                .build();
    }

    public MetadataSchema fromChangeDTO(MetadataSchema draft, MetadataSchemaChangeDTO dto, Optional<MetadataSchema> previousVersion, List<MetadataSchema> extendsSchemas, List<MetadataSchemaChild> children) {
        return draft.toBuilder()
                .version(null)
                .versionString(null)
                .name(dto.getName())
                .shapeDefinition(dto.getShapeDefinition())
                .urlPrefix(dto.getUrlPrefix())
                .description(dto.getDescription())
                .abstractSchema(dto.getAbstractSchema())
                .extendsSchemas(extendsSchemas)
                .children(children)
                .previousVersion(previousVersion.orElse(null))
                .links(dto.getExternalLinks().stream().map(this::fromLinkDTO).collect(Collectors.toList()))
                .build();
    }

    private MetadataSchemaLink fromLinkDTO(MetadataSchemaLinkDTO metadataSchemaLinkDTO) {
        return new MetadataSchemaLink(
                metadataSchemaLinkDTO.getTitle(),
                metadataSchemaLinkDTO.getPropertyUri()
        );
    }

    public MetadataSchema fromPublishDTO(MetadataSchema draft, MetadataSchemaPublishDTO dto) {
        return draft.toBuilder()
                .version(new SemanticVersion(dto.getVersion()))
                .versionString(dto.getVersion())
                .description(dto.getDescription())
                .build();
    }

    public MetadataSchemaChild fromChildDTO(MetadataSchemaChildDTO dto, MetadataSchema metadataSchema) {
        return new MetadataSchemaChild(
                metadataSchema,
                dto.getRelationUri(),
                dto.getTitle(),
                dto.getTagsUri(),
                dto.getMetadata().stream().map(mm -> new MetadataSchemaChildMetadata(mm.getTitle(), mm.getPropertyUri())).collect(Collectors.toList())
        );
    }
}
