package nl.dtls.fairdatapoint.api.controller.schema;

import io.swagger.v3.oas.annotations.tags.Tag;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaBundleDTO;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaChangeDTO;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaDetailDTO;
import nl.dtls.fairdatapoint.api.dto.schema.MetadataSchemaPublishDTO;
import nl.dtls.fairdatapoint.entity.exception.ResourceNotFoundException;
import nl.dtls.fairdatapoint.service.schema.MetadataSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Tag(name = "Metadata Schema")
@RestController
@RequestMapping("/schemas")
public class MetadataSchemaController {

    @Autowired
    private MetadataSchemaService metadataSchemaService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MetadataSchemaBundleDTO>> getSchemaBundles() {
        List<MetadataSchemaBundleDTO> dto = metadataSchemaService.getAllBundles();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataSchemaDetailDTO> createSchemaDraft(@RequestBody @Valid MetadataSchemaChangeDTO reqDto) {
        MetadataSchemaDetailDTO dto = metadataSchemaService.createDraft(reqDto);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(path = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataSchemaBundleDTO> getSchemaBundle(@PathVariable final String uuid) throws ResourceNotFoundException {
        Optional<MetadataSchemaBundleDTO> oDto = metadataSchemaService.getBundle(uuid);
        if (oDto.isPresent()) {
            return new ResponseEntity<>(oDto.get(), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException(format("Metadata Schema '%s' doesn't exist", uuid));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{uuid}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataSchemaDetailDTO> getSchemaDraft(@PathVariable final String uuid) throws ResourceNotFoundException {
        Optional<MetadataSchemaDetailDTO> oDto = metadataSchemaService.getDraft(uuid);
        if (oDto.isPresent()) {
            return new ResponseEntity<>(oDto.get(), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException(format("Metadata Schema '%s' doesn't exist", uuid));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{uuid}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataSchemaDetailDTO> updateSchemaDraft(@PathVariable final String uuid, @RequestBody @Valid MetadataSchemaChangeDTO reqDto) throws ResourceNotFoundException {
        Optional<MetadataSchemaDetailDTO> oDto = metadataSchemaService.updateDraft(uuid, reqDto);
        if (oDto.isPresent()) {
            return new ResponseEntity<>(oDto.get(), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException(format("Metadata Schema '%s' doesn't exist", uuid));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{uuid}/draft", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteSchemaDraft(@PathVariable final String uuid) throws ResourceNotFoundException {
        boolean result = metadataSchemaService.deleteDraft(uuid);
        if (result) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException(format("Metadata Schema '%s' doesn't exist", uuid));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/{uuid}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataSchemaDetailDTO> publishSchemaVersion(@PathVariable final String uuid, @RequestBody @Valid MetadataSchemaPublishDTO reqDto) throws ResourceNotFoundException {
        Optional<MetadataSchemaDetailDTO> oDto = metadataSchemaService.publishDraft(uuid, reqDto);
        if (oDto.isPresent()) {
            return new ResponseEntity<>(oDto.get(), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException(format("Metadata Schema '%s' doesn't exist", uuid));
        }
    }

    @GetMapping(path = "/{uuid}/versions/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetadataSchemaDetailDTO> getSchemaVersion(@PathVariable final String uuid, @PathVariable final String version) throws ResourceNotFoundException {
        Optional<MetadataSchemaDetailDTO> oDto = metadataSchemaService.getDetail(uuid, version);
        if (oDto.isPresent()) {
            return new ResponseEntity<>(oDto.get(), HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException(format("Metadata Schema '%s' doesn't exist with version '%s'", uuid, version));
        }
    }
}
