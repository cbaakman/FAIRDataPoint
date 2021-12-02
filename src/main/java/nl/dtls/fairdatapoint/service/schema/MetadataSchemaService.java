/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.dtls.fairdatapoint.service.schema;

import io.swagger.v3.oas.models.tags.Tag;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaBundleDTO;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaChangeDTO;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaDetailDTO;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaPublishDTO;
import nl.dtls.fairdatapoint.database.mongo.repository.MetadataSchemaRepository;
import nl.dtls.fairdatapoint.entity.exception.ResourceNotFoundException;
import nl.dtls.fairdatapoint.entity.schema.MetadataSchema;
import nl.dtls.fairdatapoint.entity.schema.MetadataSchemaChild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class MetadataSchemaService {

    @Autowired
    private MetadataSchemaRepository metadataSchemaRepository;

    @Autowired
    private MetadataSchemaMapper metadataSchemaMapper;

    private static final Comparator<MetadataSchema> SEMVER_COMPARATOR = Comparator.comparing(MetadataSchema::getVersion);

    public List<MetadataSchemaBundleDTO> getAllBundles() {
        List<MetadataSchema> schemas = metadataSchemaRepository.findAll();
        Map<String, List<MetadataSchema>> schemaBundles = schemas.stream().collect(Collectors.groupingBy(MetadataSchema::getUuid));
        return schemaBundles.values().stream().map(b ->
                metadataSchemaMapper.toBundleDTO(b.stream().sorted(SEMVER_COMPARATOR.reversed()).collect(Collectors.toList()))
        ).collect(Collectors.toList());
    }

    public Optional<MetadataSchemaBundleDTO> getBundle(String uuid) {
        List<MetadataSchema> schemas = metadataSchemaRepository.findByUuid(uuid);
        if (schemas.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(metadataSchemaMapper.toBundleDTO(schemas));
        }
    }

    public Optional<MetadataSchemaDetailDTO> getDraft(String uuid) {
        Optional<MetadataSchema> existingDraft = metadataSchemaRepository.findByUuidAndVersion(uuid, null);
        if (existingDraft.isPresent()) {
            return existingDraft.map(s -> metadataSchemaMapper.toDetailDTO(s));
        }
        Optional<MetadataSchema> mostRecent = getMostRecentVersion(uuid);
        return mostRecent.map(s -> metadataSchemaMapper.toDraftDetailDTO(s));
    }

    private Optional<MetadataSchema> getMostRecentVersion(String uuid) {
        return metadataSchemaRepository.findByUuid(uuid).stream().max(SEMVER_COMPARATOR);
    }

    public Optional<MetadataSchemaDetailDTO> getDetail(String uuid, String version) {
        return metadataSchemaRepository.findByUuidAndVersion(uuid, version).map(s -> metadataSchemaMapper.toDetailDTO(s));
    }

    public MetadataSchemaDetailDTO createDraft(MetadataSchemaChangeDTO dto) {
        // TODO: validate
        String newUuid = UUID.randomUUID().toString();
        List<MetadataSchema> extendsSchemas = extractExtendsSchemas(dto);
        List<MetadataSchemaChild> children = extractChildren(dto);
        MetadataSchema schema = metadataSchemaMapper.fromChangeDTO(dto, newUuid, extendsSchemas, children);
        metadataSchemaRepository.save(schema);
        return metadataSchemaMapper.toDetailDTO(schema);
    }

    public Optional<MetadataSchemaDetailDTO> updateDraft(String uuid, MetadataSchemaChangeDTO dto) {
        // TODO: validate
        Optional<MetadataSchema> oDraft = metadataSchemaRepository.findByUuidAndVersion(uuid, null);
        return oDraft.map(draft -> {
            Optional<MetadataSchema> previousVersion = getMostRecentVersion(uuid);
            List<MetadataSchema> extendsSchemas = extractExtendsSchemas(dto);
            List<MetadataSchemaChild> children = extractChildren(dto);
            MetadataSchema schema = metadataSchemaMapper.fromChangeDTO(draft, dto, previousVersion, extendsSchemas, children);
            metadataSchemaRepository.save(schema);
            return metadataSchemaMapper.toDetailDTO(schema);
        });
    }

    private List<MetadataSchema> extractExtendsSchemas(MetadataSchemaChangeDTO dto) {
        return dto.getExtendsSchemas().stream().map(coords -> {
            var schema = metadataSchemaRepository.findByUuidAndVersion(coords.getUuid(), coords.getVersion());
            if (schema.isEmpty()) {
                throw new ResourceNotFoundException(format("Metadata Schema '%s' with version '%s' not found", coords.getUuid(), coords.getVersion()));
            }
            return schema.get();
        }).collect(Collectors.toList());
    }

    private List<MetadataSchemaChild> extractChildren(MetadataSchemaChangeDTO dto) {
        return dto.getChildren().stream().map(child -> {
            var schema = metadataSchemaRepository.findByUuidAndVersion(child.getSchemaUuid(), child.getSchemaVersion());
            if (schema.isEmpty()) {
                throw new ResourceNotFoundException(format("Metadata Schema '%s' with version '%s' not found", child.getSchemaUuid(), child.getSchemaVersion()));
            }
            return metadataSchemaMapper.fromChildDTO(child, schema.get());
        }).collect(Collectors.toList());
    }

    public Optional<MetadataSchemaDetailDTO> publishDraft(String uuid, MetadataSchemaPublishDTO dto) {
        Optional<MetadataSchema> oDraft = metadataSchemaRepository.findByUuidAndVersion(uuid, null);
        return oDraft.map(draft -> {
            MetadataSchema schema = metadataSchemaMapper.fromPublishDTO(draft, dto);
            metadataSchemaRepository.save(schema);
            return metadataSchemaMapper.toDetailDTO(schema);
        });
    }

    public boolean deleteDraft(String uuid) {
        Optional<MetadataSchema> schema = metadataSchemaRepository.findByUuidAndVersion(uuid, null);
        schema.ifPresent(s -> metadataSchemaRepository.delete(s));
        return schema.isPresent();
    }
}
