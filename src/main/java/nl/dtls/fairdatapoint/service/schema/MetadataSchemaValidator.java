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

import nl.dtls.fairdatapoint.database.mongo.repository.MetadataSchemaRepository;
import nl.dtls.fairdatapoint.entity.exception.ValidationException;
import nl.dtls.fairdatapoint.entity.schema.MetadataSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@Component
public class MetadataSchemaValidator {

    @Autowired
    private MetadataSchemaRepository metadataSchemaRepository;

    public void validate(MetadataSchema schema) {
        validateUrlPrefix(schema);

        validateVersion(schema);

        validateExtends(schema);

        validateChildren(schema);
    }

    private void validateUrlPrefix(MetadataSchema schema) {
        List<MetadataSchema> potentiallyConflictingSchemas = metadataSchemaRepository.findByUrlPrefix(schema.getUrlPrefix());
        potentiallyConflictingSchemas.forEach(s -> {
            if(!Objects.equals(s.getUuid(), schema.getUuid())) {
                throw new ValidationException(format("URL prefix '%s' is already used in schemas '%s'", s.getUrlPrefix(), s.getUuid()));
            }
        });
    }

    private void validateVersion(MetadataSchema schema) {
        if (schema.getPreviousVersion() != null) {
            MetadataSchema prev = schema.getPreviousVersion();
            if (prev.getVersion() == null) {
                throw new ValidationException(format("Cannot base draft on other draft in schemas '%s'", schema.getUuid()));
            }
            if (schema.getVersion() != null && !schema.getVersion().isSuccessor(prev.getVersion())) {
                throw new ValidationException(format("Version '%s' is lower then '%s' of previous version", schema.getVersion().toString(), prev.getVersion().toString()));
            }
        }
    }

    private void validateExtends(MetadataSchema schema) {
        // TODO: can have non-abstract parents if non-abstract?
        if (schema.getAbstractSchema()) {
            schema.getExtendsSchemas().forEach(parent -> {
                if (!parent.getAbstractSchema()) {
                    throw new ValidationException("Abstract schema can extend only abstract schemas");
                }
            });
        }
    }

    private void validateChildren(MetadataSchema schema) {
        // TODO: can have abstract schema children?
        schema.getChildren().forEach(child -> {
            if (child.getChildSchema().getAbstractSchema()) {
                throw new ValidationException("Abstract schema cannot be used as child");
            }
        });
    }
}
