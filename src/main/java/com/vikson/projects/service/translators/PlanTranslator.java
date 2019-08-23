package com.vikson.projects.service.translators;

import com.vikson.projects.api.resources.PlanDTO;
import com.vikson.projects.api.resources.PlanFolderDTO;
import com.vikson.projects.api.resources.PlanLevelDTO;
import com.vikson.projects.api.resources.PlanRevisionDTO;
import com.vikson.projects.api.resources.PlanTreeDTO;
import com.vikson.projects.api.resources.values.PlanExpandAttribute;
import com.vikson.projects.api.resources.values.ResourcePermission;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.model.Plan;
import com.vikson.projects.model.PlanFolder;
import com.vikson.projects.model.PlanLevel;
import com.vikson.projects.model.PlanRevision;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.values.PlanType;
import com.vikson.projects.repositories.PlanRevisionRepository;
import com.vikson.projects.service.helpers.PlanServiceHelper;
import com.vikson.projects.service.TaskCountCacheComponent;
import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.UsersApiClient;
import com.vikson.services.users.resources.Privilege;
import com.vikson.services.users.resources.PrivilegeCheck;
import com.vikson.storage.FileType;
import com.vikson.storage.StorageAccessKey;
import com.vikson.storage.StorageEngine;
import com.vikson.storage.StorageScheme;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vikson.apierrors.ApiErrors.apiError;

@Component
public class PlanTranslator {

    private static final Logger log = LoggerFactory.getLogger(PlanTranslator.class);

    private String baseUrl;
    @Value("${vikson.storage.scheme:S3}")
    private StorageScheme storageScheme;
    @Value("${vikson.storage.s3.buckets.plans:vikson-prod-plans}")
    private String planBucketName;
    @Value("${vikson.plans.revisionDownloadUrlExpireryTimeMinutes:60}")
    private long revisionDownloadUrlExpireryTimeMinutes;
    @Value("${vikson.url:}")
    private String viksonUrl;

    @Autowired
    private UsersApiClient usersApiClient;
    @Autowired
    private UserPrivileges userPrivileges;

    @Autowired
    private StorageEngine storageEngine;
    @Autowired
    private PlanRevisionRepository revisionRepository;
    @Autowired
    private TaskCountCacheComponent taskCountCache;
    @Autowired
    private PlanServiceHelper planServiceHelper;
    @Autowired
    private LevelTranslator levelTranslator;

    public PlanTranslator(@Value("${vikson.url:}") String baseUrl) {
        if (StringUtils.isEmpty(baseUrl)) {
            this.baseUrl = "http://localhost:8080";
            log.warn("Could not find property vikson.url, falling back to: {}", StringUtils.quote(this.baseUrl));
        } else {
            this.baseUrl = baseUrl;
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PlanTreeDTO translate(Project project, String search, StateFilter stateFilter, ZonedDateTime sinceFilter, PlanExpandAttribute[] expand) {
        List<PlanFolderDTO> subFolder = project.getRootFolder().getSubFolders().stream()
            .filter(getPlanFolderSinceFilter(sinceFilter))
            .filter(planFolder -> planFolder.getName().toLowerCase().contains(search))
            .filter(getPlanFolderActiveFilter(stateFilter))
            .map(planFolder -> translate(planFolder, search, stateFilter, null, sinceFilter, expand))
            .peek(this::removeEmptySubFolders)
            .filter(this::filterEmptyFolder)
            .collect(Collectors.toList());
        List<PlanDTO> children = project.getRootFolder().getPlans().stream()
            .filter(plan -> canViewPlan(project.getId(), plan.getId()))
            .filter(plan -> plan.getName().toLowerCase().contains(search))
            .filter(getPlanActiveFilter(stateFilter))
            .map(plan -> translate(plan, expand))
            .filter(getPlanSinceFilter(sinceFilter))
            .collect(Collectors.toList());
        return new PlanTreeDTO(project.getName().getName(), project.getRootFolder(), subFolder, children);
    }

    private void removeEmptySubFolders(PlanFolderDTO planFolderDTO) {
        planFolderDTO.setSubFolder(planFolderDTO.getSubFolder()
            .stream()
            .peek(this::removeEmptySubFolders)
            .filter(this::filterEmptyFolder).collect(Collectors.toList()));
    }

    private boolean filterEmptyFolder(PlanFolderDTO planFolderDTO) {
        boolean hasAnyPrivileges = userPrivileges.hasAnyPrivileges(planFolderDTO.getProjectId(),
            Arrays.asList(Privilege.ManagePins, Privilege.CreatePins, Privilege.ViewPins, Privilege.ManagePlans, Privilege.CreatePlans));
        return hasAnyPrivileges || isFolderNotEmpty(planFolderDTO);
    }

    private boolean isFolderNotEmpty(PlanFolderDTO planFolder) {
        if (!planFolder.getChildren().isEmpty()) {
            return true;
        }
        for (PlanFolderDTO folder : planFolder.getSubFolder()) {
            return isFolderNotEmpty(folder);
        }
        return false;
    }

    private PlanFolderDTO translate(PlanFolder planFolder, String search, StateFilter stateFilter, Boolean state, ZonedDateTime sinceFilter, PlanExpandAttribute[] expand) {
        List<PlanFolderDTO> subFolder = planFolder.getSubFolders().stream()
            .filter(getPlanFolderSinceFilter(sinceFilter))
            .filter(subPlanFolder -> subPlanFolder.getName().toLowerCase().contains(search))
            .peek(folderConsumer -> {
                if (state != null) folderConsumer.setActive(state);
            })
            .filter(getPlanFolderActiveFilter(stateFilter))
            .map(subPlanFolder -> translate(subPlanFolder, search, stateFilter, planFolder.isActive() ? null : false, sinceFilter, expand))
            .collect(Collectors.toList());
        List<PlanDTO> children = planFolder.getPlans().stream()
            .filter(plan -> canViewPlan(plan.getProject().getId(), plan.getId()))
            .filter(plan -> plan.getName().toLowerCase().contains(search))
            .peek(plan -> {
                if (state != null) plan.setActive(state);
            })
            .filter(getPlanActiveFilter(stateFilter))
            .map(plan -> translate(plan, expand))
            .filter(getPlanSinceFilter(sinceFilter))
            .collect(Collectors.toList());
        return new PlanFolderDTO(planFolder, subFolder, children);
    }

    private Predicate<PlanFolder> getPlanFolderActiveFilter(StateFilter filter) {
        if (filter == StateFilter.Active) return PlanFolder::isActive;
        if (filter == StateFilter.Inactive) return planFolder -> !planFolder.isActive();
        return planFolder -> true;
    }

    private Predicate<Plan> getPlanActiveFilter(StateFilter filter) {
        if (filter == StateFilter.Active) return Plan::isActive;
        if (filter == StateFilter.Inactive) return plan -> !plan.isActive();
        return plan -> true;
    }

    private Predicate<PlanFolder> getPlanFolderSinceFilter(ZonedDateTime sinceFilter) {
        //if (sinceFilter == null) return planFolder -> true;
        //return planFolder -> planFolder.getCreated().isAfter(sinceFilter);
        // TODO we allways sync plan folders, regardless if there is a since filter or not
        return planFolder -> true;
    }

    private Predicate<PlanDTO> getPlanSinceFilter(ZonedDateTime sinceFilter) {
        if (sinceFilter == null) return plan -> true;
        return plan -> (plan.getLastModified() != null && plan.getLastModified().isAfter(sinceFilter.toInstant())) ||
                plan.getCreated().isAfter(sinceFilter.toInstant());
    }

    public PlanFolderDTO translate(PlanFolder folder) {
        return translate(folder, null);
    }


    private PlanFolderDTO translate(PlanFolder folder, Boolean state) {
        List<PlanFolderDTO> subFolder = folder.getSubFolders().stream()
            .map(planFolder -> this.translate(planFolder, folder.isActive() ? null : false))
            .collect(Collectors.toList());
        List<PlanDTO> children = folder.getPlans().stream()
            .filter(plan -> canViewPlan(plan.getProject().getId(), plan.getId()))
            .map(this::translate)
            .peek(planDTO -> {
                if (state != null) planDTO.setActive(state);
            })
            .collect(Collectors.toList());
        return new PlanFolderDTO(folder, subFolder, children);
    }

    public PlanDTO translate(Plan plan) {
        return translate(plan, PlanExpandAttribute.values());
    }

    public PlanDTO translate(Plan plan, PlanExpandAttribute[] expand) {
        Set<PlanExpandAttribute> expandSet = expand != null?
                Arrays.stream(expand).collect(Collectors.toSet())
                : Collections.emptySet();
        List<PlanRevisionDTO> revisions = (expandSet.contains(PlanExpandAttribute.Revisions))?
                plan.getRevisions().stream()
                    .map(revision -> translate(revision, plan.getCurrentRevision()))
                    .collect(Collectors.toList())
                : Collections.emptyList();
        PlanRevisionDTO currentRevision = translate(plan.getCurrentRevision(), expandSet.contains(PlanExpandAttribute.ExcludeLevels));
        PlanDTO planDTO = new PlanDTO(plan, revisions, currentRevision);
        PrivilegeCheck check = userPrivileges.checkPrivilege(plan.getProject().getId(), Arrays.asList(Privilege.CreatePlans, Privilege.ManagePlans), true);
        if (check.hasPrivileges(Privilege.ManagePlans) || (check.hasPrivileges(Privilege.CreatePlans) && plan.getCreatedBy().equals(check.getCurrentUserId()))) {
            planDTO.setPermissions(Collections.singletonList(ResourcePermission.Edit));
        }
        if(expandSet.contains(PlanExpandAttribute.TaskCount)) {
            planDTO.setOpenTaskCount(0);
            //planDTO.setOpenTaskCount(taskCountCache.getTaskCount(plan.getId()).getOpenCount());
        }
        return planDTO;
    }

    @Transactional
    private ZonedDateTime fetchLastActivity(Plan plan) {
        if(plan.requiresLastModifiedUpdate()) {
            try {
                planServiceHelper.doFetchLastActivity(plan.getId(), plan.getLastModified().toInstant(),
                        SecurityContextHolder.getContext());
            } catch (Exception e) {
                log.warn("Could not load lastActivity for Plan", e);
            }
        }
        return plan.getLastModified();
    }

    public PlanRevisionDTO translate(PlanRevision revision) {
        return translate(revision, false);
    }

    public PlanRevisionDTO translate(PlanRevision revision, PlanRevision currentPlanRevision) {
        if (revision.getMetadata().getImageHeight() != currentPlanRevision.getMetadata().getImageHeight() ||
                revision.getMetadata().getImageWidth() != currentPlanRevision.getMetadata().getImageWidth()) {
            return translate(revision, false).withAllowSetActive(false);
        }
        return translate(revision, false);
    }

    public PlanRevisionDTO translate(PlanRevision revision, boolean excludeLevels) {
        try {
            if (!revision.getMetadata().hasChecksum()) {
                StorageAccessKey key = getRevisionKey(revision);
                String checksum = storageEngine.md5AsHex(key);
                revision.getMetadata().setArchiveChecksum(checksum);
                revisionRepository.save(revision);
            }
        } catch (Exception e) {
            log.warn("Could not create Checksum for Revision.", e);
        }
        try {
            if (revision.getMetadata().getArchiveSize() == null) {
                StorageAccessKey key = getRevisionKey(revision);
                Long archiveSize = storageEngine.size(key);
                revision.getMetadata().setArchiveSize(archiveSize);
                revisionRepository.save(revision);
            }
        } catch (Exception e) {
            log.warn("Could not load size of Revision.", e);
        }
        if (revision.getType() == PlanType.File && (revision.getMetadata().getFileName() == null || revision.getMetadata().getFileName().equals("test-plan"))) {
            String revisionRoot = String.format("%s/%s/%s/", revision.getPlan().getProject().getId(), revision.getPlan().getId(), revision.getId());
            Stream<StorageAccessKey> all = storageEngine.findAll(revisionRoot, FileType.OriginalPlan);
            Optional<String> fileName = all.map(storageAccessKey -> storageAccessKey.getKey().substring(revisionRoot.length())).
                filter(key -> !(key.contains("/") || key.equals("thumbnail.jpg") || key.equals("revision.tar")))
                .findFirst();
            fileName.ifPresent(f -> {
                revision.getMetadata().setFileName(f);
                revisionRepository.save(revision);
            });
        }
        String username = usersApiClient.getUser(revision.getCreatedBy()).getCompleteName();
        PlanRevisionDTO revisionDTO = new PlanRevisionDTO(revision, excludeLevels? Collections.emptyList() : levelTranslator.translateLevels(revision.getId(), revision),username);
        applyPreSignedURLs(revisionDTO, getRevisionKey(revision));
        applyOriginalFileUrl(revision, revisionDTO);
        return revisionDTO;
    }

    @NotNull
    private StorageAccessKey getRevisionKey(PlanRevision revision) {
        return new StorageAccessKey(FileType.PlanTile, String.format("%s/%s/%s/revision.tar",
                            revision.getPlan().getProject().getId(), revision.getPlan().getId(), revision.getId()));
    }

    private void applyPreSignedURLs(PlanRevisionDTO revisionDTO, StorageAccessKey storageAccessKey) {
        try {
            // Revision Archive URL
            URL url = storageEngine.generatePreSignedURL(storageAccessKey, revisionDownloadUrlExpireryTimeMinutes, TimeUnit.MINUTES);
            revisionDTO.setArchiveDownloadUrl(url);
        } catch (IllegalArgumentException e) {
            log.warn("Something went wrong generating the AWS Pre-Signed URL! Falling back.", e);
            try {
                URL url = new URL(String.format("%s/api/v2/planRevisions/%s/revision.tar", viksonUrl, revisionDTO.getId()));
                revisionDTO.setArchiveDownloadUrl(url);
            } catch (MalformedURLException mue) {
                throw apiError().internalServerError()
                        .cause(mue)
                        .getApiException();
            }
        }
    }

    private void applyOriginalFileUrl(PlanRevision revision, PlanRevisionDTO revisionDTO) {
        try {
            StorageAccessKey key = new StorageAccessKey(FileType.OriginalPlan,
                    String.format("%s/%s/%s/%s", revision.getPlan().getProject().getId(),
                    revision.getPlan().getId(), revision.getId(), revision.getOriginalFileName().orElse("")));
            if (storageEngine.exists(key)) {
                URL url = storageEngine.generatePreSignedURL(key, revisionDownloadUrlExpireryTimeMinutes, TimeUnit.MINUTES);
                revisionDTO.setOriginalDownloadUrl(url);
            }
        } catch (Exception e) {
            log.warn("Could not generate Original Download URL.", e);
        }
    }

    public List<PlanLevelDTO> translateLevels(PlanRevision revision) {
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

    public PlanLevelDTO translate(int number, UUID projectId, PlanRevision revision, PlanLevel level) {
        String url = String.format("%s/api/v2/plans/revisions/%s/%d/", this.baseUrl, revision.getId(), number);
        String s3Url = String.format("https://s3.eu-central-1.amazonaws.com/%s/%s/%s/%s/%d/", planBucketName,
            projectId, revision.getPlan().getId(), revision.getId(), number);
        return new PlanLevelDTO(level, s3Url, url);
    }

    public boolean canViewPlan(UUID projectId, UUID planId) {
        boolean hasAnyPrivileges = userPrivileges.hasAnyPrivileges(projectId,
            Arrays.asList(Privilege.ManagePins, Privilege.CreatePins, Privilege.ViewPins, Privilege.ManagePlans, Privilege.CreatePlans));
        if (hasAnyPrivileges) {
            return true;
        } else {
            return taskCountCache.getTaskCount(planId).getTotal() > 0;
        }
    }

}
