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
package nl.dtls.fairdatapoint.service.index.settings;

import lombok.extern.slf4j.Slf4j;
import nl.dtls.fairdatapoint.api.dto.index.ping.PingDTO;
import nl.dtls.fairdatapoint.api.dto.index.settings.IndexSettingsDTO;
import nl.dtls.fairdatapoint.api.dto.index.settings.IndexSettingsUpdateDTO;
import nl.dtls.fairdatapoint.config.properties.InstanceProperties;
import nl.dtls.fairdatapoint.database.mongo.repository.IndexSettingsRepository;
import nl.dtls.fairdatapoint.entity.index.settings.IndexSettings;
import nl.dtls.fairdatapoint.entity.index.settings.IndexSettingsPing;
import nl.dtls.fairdatapoint.entity.index.settings.IndexSettingsRetrieval;
import nl.dtls.fairdatapoint.service.index.common.RequiredEnabledIndexFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class IndexSettingsService {

    @Autowired
    private IndexSettingsRepository repository;

    @Autowired
    private IndexSettingsMapper mapper;

    @Autowired
    private InstanceProperties instanceProperties;

    @RequiredEnabledIndexFeature
    public boolean isPingDenied(PingDTO ping) {
        log.info("Checking if ping.clientUrl is on deny list: " + ping.getClientUrl());
        return getOrDefaults()
                .getPing()
                .getDenyList()
                .parallelStream()
                .anyMatch(pattern -> Pattern.matches(pattern, ping.getClientUrl()));
    }

    @RequiredEnabledIndexFeature
    public IndexSettings getOrDefaults() {
        return repository.findFirstBy().orElse(getDefaults());
    }

    @RequiredEnabledIndexFeature
    public IndexSettingsDTO getCurrentSettings() {
        return mapper.toDTO(getOrDefaults(), getDefaults());
    }

    @RequiredEnabledIndexFeature
    public IndexSettings getDefaults() {
        final IndexSettings settings = new IndexSettings();
        settings.setPing(IndexSettingsPing.getDefault());
        settings.setRetrieval(IndexSettingsRetrieval.getDefault());
        settings.setAutoPermit(instanceProperties.isIndexAutoPermit());
        return settings;
    }

    @RequiredEnabledIndexFeature
    public IndexSettingsDTO updateSettings(IndexSettingsUpdateDTO dto) {
        return mapper.toDTO(
                repository.save(mapper.fromUpdateDTO(dto, getOrDefaults())),
                getDefaults()
        );
    }

    @RequiredEnabledIndexFeature
    public IndexSettingsDTO resetSettings() {
        return updateSettings(mapper.toUpdateDTO(getDefaults()));
    }
}
