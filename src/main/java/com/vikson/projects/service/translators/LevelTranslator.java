package com.vikson.projects.service.translators;

import com.vikson.projects.api.resources.PlanLevelDTO;
import com.vikson.projects.model.PlanLevel;
import com.vikson.projects.model.PlanRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@CacheConfig(cacheManager = "levelsCacheManager")
public class LevelTranslator {

    private static final Logger log = LoggerFactory.getLogger(LevelTranslator.class);

    private String baseUrl;
    @Value("${vikson.storage.s3.buckets.plans:vikson-prod-plans}")
    private String planBucketName;

    public LevelTranslator(@Value("${vikson.url:}") String baseUrl) {
        if (StringUtils.isEmpty(baseUrl)) {
            this.baseUrl = "http://localhost:8080";
            log.warn("Could not find property vikson.url, falling back to: {}", StringUtils.quote(this.baseUrl));
        } else {
            this.baseUrl = baseUrl;
        }
    }

    @Cacheable(value = "levels", key = "#root.args[0]")
    public List<PlanLevelDTO> translateLevels(UUID revisionId, PlanRevision revision) {
        List<PlanLevel> levelsSorted = revision.getLevels().stream()
                .sorted(Comparator.comparingInt((PlanLevel l) -> (l.getTilesX() * l.getTilesY())).reversed())
                .collect(Collectors.toList());
        List<PlanLevelDTO> levelDTOs = new ArrayList<>(levelsSorted.size());
        for(int i = levelsSorted.size() - 1, number = 1; i >= 0; i--, number++) {
            levelDTOs.add(translate(number, revision.getPlan().getProject().getId(), revision, levelsSorted.get(i)));
        }
        Collections.reverse(levelDTOs);
        return levelDTOs;
    }

    private PlanLevelDTO translate(int number, UUID projectId, PlanRevision revision, PlanLevel level) {
        String url = String.format("%s/api/v2/plans/revisions/%s/%d/", this.baseUrl, revision.getId(), number);
        String s3Url = String.format("https://s3.eu-central-1.amazonaws.com/%s/%s/%s/%s/%d/", planBucketName,
                projectId, revision.getPlan().getId(), revision.getId(), number);
        return new PlanLevelDTO(level, s3Url, url);
    }

}
