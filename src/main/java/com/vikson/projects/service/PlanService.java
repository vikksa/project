package com.vikson.projects.service;

import com.vikson.notifications.NotificationCode;
import com.vikson.notifications.NotifyClient;
import com.vikson.projects.api.resources.MoveDto;
import com.vikson.projects.api.resources.PlanDTO;
import com.vikson.projects.api.resources.PlanFolderDTO;
import com.vikson.projects.api.resources.PlanLevelDTO;
import com.vikson.projects.api.resources.PlanQuickInfo;
import com.vikson.projects.api.resources.PlanRevisionDTO;
import com.vikson.projects.api.resources.PlanTreeDTO;
import com.vikson.projects.api.resources.BimplusPlanReferenceDto;
import com.vikson.projects.api.resources.values.PlanExpandAttribute;
import com.vikson.projects.api.resources.values.ProjectFilter;
import com.vikson.projects.api.resources.RevisionMetadataDTO;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.exceptions.ApiExceptionHelper;
import com.vikson.projects.exceptions.ErrorCodes;
import com.vikson.projects.util.DateTimeUtils;
import com.vikson.projects.model.Plan;
import com.vikson.projects.model.PlanFolder;
import com.vikson.projects.model.PlanLevel;
import com.vikson.projects.model.PlanRevision;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.values.BimplusPlanReference;
import com.vikson.projects.model.values.BimplusPlanState;
import com.vikson.projects.model.values.PlanType;
import com.vikson.projects.model.values.RevisionLocationDifference;
import com.vikson.projects.model.values.RevisionMetadata;
import com.vikson.projects.repositories.PlanFolderRepository;
import com.vikson.projects.repositories.PlanLevelRepository;
import com.vikson.projects.repositories.PlanRepository;
import com.vikson.projects.repositories.PlanRevisionRepository;
import com.vikson.projects.repositories.ProjectRepository;
import com.vikson.projects.service.translators.LevelSquarifier;
import com.vikson.projects.service.translators.PlanTranslator;
import com.vikson.services.issues.IssueRequestApiClient;
import com.vikson.services.issues.PinApiClient;
import com.vikson.services.issues.TaskApiClient;
import com.vikson.services.issues.resources.PostsFilter;
import com.vikson.services.issues.resources.SyncSizeDTO;
import com.vikson.services.projects.resources.PinRevisionLocationData;
import com.vikson.services.projects.resources.PlanRevisionLocationDifference;
import com.vikson.services.users.MembershipsApiClient;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.PrivilegeApiClient;
import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.resources.MembershipState;
import com.vikson.services.users.resources.Privilege;
import com.vikson.services.users.resources.PrivilegeCheck;
import com.vikson.services.users.resources.TeamMember;
import com.vikson.storage.FileType;
import com.vikson.storage.StorageAccessKey;
import com.vikson.storage.StorageEngine;
import com.vikson.storage.exception.StorageException;
import com.vikson.projects.util.SizeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vikson.apierrors.ApiErrors.apiError;
import static com.vikson.projects.exceptions.ApiExceptionHelper.newBadRequestError;
import static com.vikson.projects.exceptions.ApiExceptionHelper.newInternalServerError;
import static com.vikson.projects.exceptions.ApiExceptionHelper.newUnPrivilegedError;

@Service
@Transactional
public class PlanService {

    private static final Logger log = LoggerFactory.getLogger(PlanService.class);
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private PlanFolderRepository planFolderRepository;
    @Autowired
    private PlanRepository planRepository;
    @Autowired
    private PlanRevisionRepository planRevisionRepository;
    @Autowired
    private PlanLevelRepository planLevelRepository;

    @Autowired
    private PersonalApiClient personalApiClient;
    @Autowired
    private PrivilegeApiClient privilegeApiClient;
    @Autowired
    private UserPrivileges userPrivileges;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private StorageEngine storageEngine;

    @Autowired
    private PlanTranslator planTranslator;
    @Autowired
    private MembershipsApiClient membershipsApiClient;
    @Autowired
    private TaskApiClient taskApiClient;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private PinApiClient pinApiClient;
    @Autowired
    private IssueRequestApiClient issueRequestApiClient;

    public PlanFolder createFolder(UUID projectId, PlanFolderDTO planFolderDTO) {
        Assert.notNull(projectId, "Cannot create new PlanFolder new projectId is NULL!");
        Assert.notNull(planFolderDTO, "Cannot create new PlanFolder from NULL PlanFolderDTO!");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ApiExceptionHelper.newBadRequestError(ErrorCodes.CREATE_PLAN_FOLDER_IN_UNKNOWN_PROJECT, projectId.toString()));

        if (!userPrivileges.checkPrivilege(projectId, Arrays.asList(Privilege.CreatePlans, Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.ManagePlans, Privilege.CreatePlans);
        }

        if (planFolderDTO.getId() != null && planFolderRepository.existsById(planFolderDTO.getId())) {
            throw ApiExceptionHelper.newConflictError(ErrorCodes.PLAN_FOLDER_ALREADY_EXISTS, planFolderDTO.getId().toString());
        }

        if (StringUtils.isEmpty(planFolderDTO.getName())) {
            planFolderDTO.setName(String.format("New Folder (%s)", LocalDate.now().format(DATE_FORMATTER)));
        }

        PlanFolder parent = loadParent(planFolderDTO.getParentId(), project);

        PlanFolder newFolder = planFolderRepository.save(new PlanFolder(planFolderDTO.getId(), planFolderDTO.getName(), project, parent));
        log.debug("Created new Plan Folder: <{}>.", newFolder);
        return newFolder;
    }

    public PlanFolder getPlanFolder(UUID folderId) {
        Assert.notNull(folderId, "Cannot retrieve PlanFolder when folderId is NULL!");
        PlanFolder folder = planFolderRepository.findById(folderId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Plan Folder", folderId));
        if (!privilegeApiClient.isMember(folder.getProject().getId())) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, folder.getProject().getId().toString());
        }
        return folder;
    }

    public PlanFolder updateFolder(UUID folderId, PlanFolderDTO updates) {
        Assert.notNull(updates, "Cannot update PlanFolder when PlanFodlerDTO is NULL!");

        PlanFolder folder = getPlanFolder(folderId);

        checkIfUserCanManagePlanFolder(folder.getProject().getId());

        if (!StringUtils.isEmpty(updates.getName())) {
            folder.setName(updates.getName());
        }

        if (updates.getParentId() != null) {
            if (folder.getId().equals(updates.getParentId()) || folder.isParentOf(updates.getParentId())) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_FOLDER_TO_CHILD_OR_ITSELF);
            }
            folder.setParent(loadParent(updates.getParentId(), folder.getProject()));
        }
        if (updates.getActive() != null) {
            folder.setActive(updates.getActive());
            if (updates.getActive()) {
                reactiveAllChildren(folder);
            }
        }

        PlanFolder updated = planFolderRepository.save(folder);
        log.debug("Updates Plan Folder: <{}>!", updated);
        return updated;
    }

    public void deleteFolder(UUID folderId) {
        PlanFolder folder = getPlanFolder(folderId);

        checkIfUserCanManagePlanFolder(folder.getProject().getId());

        if ((folder.getPlans() == null || folder.getPlans().isEmpty()) &&
                (folder.getSubFolders() == null || folder.getSubFolders().isEmpty())) {
            planFolderRepository.delete(folder);
        } else {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.PLAN_FOLDER_DELETE_WITH_CHILD);
        }
    }

    private void reactiveAllChildren(PlanFolder folder) {
        folder.getPlans().forEach(plan -> plan.setActive(true));
        folder.getSubFolders().forEach(subFolder -> {
            subFolder.setActive(true);
            reactiveAllChildren(subFolder);
        });
    }

    public PlanFolder removeParentFromFolder(UUID planFolderId) {
        Assert.notNull(planFolderId, "Cannot remove PlanFolder's Parent when ID is NULL!");
        PlanFolder folder = getPlanFolder(planFolderId);
        checkIfUserCanManagePlanFolder(folder.getProject().getId());
        folder.setParent(folder.getProject().getRootFolder());
        PlanFolder updated = planFolderRepository.save(folder);
        log.debug("Removed Parent from Plan Folder: <{}>!", folder);
        return updated;
    }

    private void checkIfUserCanManagePlanFolder(UUID projectId) {
        if (!userPrivileges.checkPrivilege(projectId, Collections.singletonList(Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.ManagePlans);
        }
    }

    public PlanTreeDTO getPlanTree(UUID projectId, String search, StateFilter stateFilter, ZonedDateTime since, PlanExpandAttribute[] expand) {
        Assert.notNull(projectId, "Cannot retrieve PlanTree for NULL projectId!");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ApiExceptionHelper.newBadRequestError(ErrorCodes.PLAN_TREE_FOR_UNKNOWN_PROJECT, projectId.toString()));

        if (privilegeApiClient.isMember(projectId)) {
            return planTranslator.translate(project, search, stateFilter, since, expand);
        } else {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, projectId.toString());
        }
    }

    public Plan createPlan(UUID projectId, PlanDTO planDTO) {
        Assert.notNull(projectId, "Cannot create new Plan when projectId is NULL!");
        Assert.notNull(planDTO, "Cannot create new Plan when PlanDTO is NULL!");

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ApiExceptionHelper.newBadRequestError(ErrorCodes.CREATE_PLAN_IN_UNKNOWN_PROJECT, projectId.toString()));

        if (!subscriptionService.canCreatePlans()) {
            throw ApiExceptionHelper.newLimitExceedError(ErrorCodes.PLAN_LIMIT_EXCEED);
        }

        if (!userPrivileges.checkPrivilege(projectId, Arrays.asList(Privilege.CreatePlans, Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.CreatePlans, Privilege.ManagePlans);
        }

        if (planDTO.getId() != null && planRepository.existsById(planDTO.getId())) {
            throw ApiExceptionHelper.newConflictError(ErrorCodes.PLAN_ALREADY_EXISTS, planDTO.getId().toString());
        }

        PlanRevisionDTO revisionDTO = planDTO.getRevision();
        if (revisionDTO == null) {
            if (planDTO.getRevisions() != null && !planDTO.getRevisions().isEmpty()) {
                revisionDTO = planDTO.getRevisions().get(0);
            } else {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.CREATE_PLAN_REVISION_NULL);
            }
        }

        ZonedDateTime created = ZonedDateTime.now(ZoneId.of("UTC"));
        if (revisionDTO.getCreated() != null) {
            created = revisionDTO.getCreated().atZone(ZoneId.of("UTC"));
        }

        PlanType planType;
        if (planDTO.getPlanType() == null) {
            if (planDTO.getMetadata() != null && planDTO.getMetadata().getPlanType() != null) {
                planType = planDTO.getMetadata().getPlanType();
            } else {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.CREATE_PLAN_PLANTYPE_NULL);
            }
        } else {
            planType = planDTO.getPlanType();
        }

        if (planDTO.getMetadata() == null) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.CREATE_PLAN_PLANMETADATA_NULL);
        }

        if (StringUtils.isEmpty(planDTO.getName())) {
            planDTO.setName(String.format("New Plan (%s)", LocalDate.now().format(DATE_FORMATTER)));
        }

        PlanFolder parent = loadParent(planDTO.getParentId(), project);

        Plan plan = new Plan(planDTO.getId(), planDTO.getName(), project, parent, created);

        PlanRevision revision = parseRevision(plan, planType, planDTO.getMetadata(), revisionDTO, created);
        plan.addNewRevision(revision);
        if (planDTO.getBimplusPlanReferenceDto() != null && planDTO.getBimplusPlanReferenceDto().getId() != null) {
            BimplusPlanReferenceDto bimplusPlanReferenceDto = planDTO.getBimplusPlanReferenceDto();
            if (bimplusPlanReferenceDto.getState() == null) {
                bimplusPlanReferenceDto.setState(BimplusPlanState.LINKED);
            }
            if (bimplusPlanReferenceDto.getName() == null || bimplusPlanReferenceDto.getName().trim().equals("")) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.BIMPLUS_ATTACHMENT_NAME_REQUIRED);
            }
            try {
                int bimplusRevision = bimplusPlanReferenceDto.getRevision() > 0 ? bimplusPlanReferenceDto.getRevision() : Integer.parseInt(revision.getVersion());
                plan.setBimplusPlanReference(new BimplusPlanReference(bimplusPlanReferenceDto.getId(), bimplusPlanReferenceDto.getName(), bimplusRevision, bimplusPlanReferenceDto.getState()));
            } catch (NumberFormatException nfe) {
                plan.setBimplusPlanReference(BimplusPlanReference.errorOccurred(bimplusPlanReferenceDto.getId(), bimplusPlanReferenceDto.getName(), "Bimplus revision is not an integer"));
            }
        }

        plan.setLastActivity(created);
        Plan save = planRepository.save(plan);
        if (project.swapLastActivityIfNewer(save.getLastActivity())) {
            projectRepository.save(project);
        }
        sendPlanNotification(save, NotificationCode.PLAN_NEW);
        return save;
    }

    public Plan getPlan(UUID planId) {
        Assert.notNull(planId, "Cannot load Plan when planId is NULL!");
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Plan", planId));

        if (!planTranslator.canViewPlan(plan.getProject().getId(), planId)) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, plan.getProject().getId().toString());
        }
        return plan;
    }

    public Plan getPlanByRevisionId(UUID revisionId) {
        Assert.notNull(revisionId, "Cannot load Plan when revisionId is NULL!");
        PlanRevision revision = planRevisionRepository.findById(revisionId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Plan Revision", revisionId));

        Plan plan = revision.getPlan();
        if (!planTranslator.canViewPlan(plan.getProject().getId(), plan.getId())) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, plan.getProject().getId().toString());
        }
        return plan;
    }

    @Transactional
    public List<Plan> doBulkUpdate(List<PlanDTO> resources) {
        Assert.notNull(resources, "Cannot do bulk update on NULL PlanDTO Collection!");
        return resources.stream()
                .filter(Objects::nonNull)
                .map(planDTO -> updatePlan(planDTO, false))
                .collect(Collectors.toList());
    }

    public Plan updatePlan(PlanDTO planDTO, boolean notify) {
        Assert.notNull(planDTO, "Cannot update Plan when PlanDTO is NULL!");
        Assert.notNull(planDTO.getId(), "Cannot update Plan when ID is NULL!");
        Plan plan = getPlan(planDTO.getId());
        PrivilegeCheck check = userPrivileges.checkPrivilege(plan.getProject().getId(), Arrays.asList(Privilege.CreatePlans, Privilege.ManagePlans), true);
        boolean privileged = check.isCheck() &&
                (check.hasPrivileges(Privilege.ManagePlans) || personalApiClient.getMe().getUserId().equals(plan.getCreatedBy()));
        if (!privileged) {
            if (planDTO.getLastActivity() != null) {
                updateLastActivity(plan, planDTO.getLastActivity());
                return planRepository.save(plan);
            }
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.CreatePlans, Privilege.ManagePlans);
        }

        if (planDTO.getActive() != null) {
            if (!plan.isActive() && planDTO.getActive() && !subscriptionService.canCreatePlans()) {
                throw ApiExceptionHelper.newLimitExceedError(ErrorCodes.PLAN_LIMIT_EXCEED);
            }

            plan.setActive(planDTO.getActive());
            if (plan.isActive()) {
                log.debug("Activated Plan <{}>.", plan.getId());
            } else {
                log.debug("Deactivated Plan <{}>.", plan.getId());
            }
        }
        if (planDTO.getCurrentRevisionId() != null) {
            if (getPlan(planDTO.getId()).isLock()) {
                throw apiError().locked()
                        .code(ErrorCodes.PLAN_REVISION_IN_PROCESS.getCode())
                        .info("id", plan.getId().toString())
                        .getApiException();
            }
            PlanRevision revision = plan.getRevisions().stream()
                    .filter(r -> r.getId().equals(planDTO.getCurrentRevisionId()))
                    .findAny()
                    .orElseThrow(() -> ApiExceptionHelper.newBadRequestError(ErrorCodes.CURRENT_REVISION_CHANGE, planDTO.getCurrentRevisionId().toString()));

            if (plan.getCurrentRevision().getMetadata().getImageWidth() != revision.getMetadata().getImageWidth() ||
                    plan.getCurrentRevision().getMetadata().getImageHeight() != revision.getMetadata().getImageHeight()) {
                throw apiError().conflict()
                        .code(ErrorCodes.PLAN_REVISION_DIFFERENT_SIZE.getCode())
                        .getApiException();
            }

            plan.setCurrentRevision(revision);
            log.debug("Set Plan <{}> Current Revision to <{}>.", plan.getId(), revision.getId());
        }
        if (!StringUtils.isEmpty(planDTO.getName())) {
            plan.setName(planDTO.getName());
            log.debug("Renamed Plan <{}> to {}.", plan.getId(), StringUtils.quote(plan.getName()));
            updatePinPlanName(plan);
        }
        // Change last activity field, if given.
        if (planDTO.getLastActivity() != null) {
            updateLastActivity(plan, planDTO.getLastActivity());
        }

        if (plan.getBimplusPlanReference().getId() != null && planDTO.getBimplusPlanReferenceDto() != null) {
            if (plan.getBimplusPlanReference().getState() == BimplusPlanState.NOT_LINKED
                    || plan.getBimplusPlanReference().getState() == BimplusPlanState.UNLINKED
                    || plan.getBimplusPlanReference().getState() == BimplusPlanState.ERROR) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.BIMPLUS_RESOURCE_CHANGE_STATE);
            }
            updateBimplusPlanReference(planDTO, plan);
        }

        Plan save = planRepository.save(plan);
        if (notify) {
            if (planDTO.getActive() != null) {
                sendPlanNotification(plan, planDTO.getActive() ? NotificationCode.PLAN_ACTIVE : NotificationCode.PLAN_INACTIVE);
            } else {
                sendPlanNotification(plan, NotificationCode.PLAN_REVISE);
            }
        }
        return save;
    }

    private void updateBimplusPlanReference(PlanDTO planDTO, Plan plan) {
        if (plan.getBimplusPlanReference() != null && planDTO.getBimplusPlanReferenceDto() != null &&
                plan.getBimplusPlanReference().getId().equals(planDTO.getBimplusPlanReferenceDto().getId())) {
            if (planDTO.getBimplusPlanReferenceDto().getState() != plan.getBimplusPlanReference().getState()) {
                checkBimplusPlanReferenceValidity(planDTO.getBimplusPlanReferenceDto(), plan);
            }
        } else {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.BIMPLUS_ATTACHMENT_ID_UPDATE_NOT_ALLOWED);
        }
    }

    private void updatePinPlanName(Plan plan) {
        pinApiClient.updatePlanName(plan.getId(), plan.getName());
    }

    private void checkBimplusPlanReferenceValidity(BimplusPlanReferenceDto bimplusPlanReferenceDto, Plan plan) {
        BimplusPlanReference bimplusPlanReference = plan.getBimplusPlanReference();
        if (bimplusPlanReferenceDto.getState() == BimplusPlanState.LINKED) {
            if (isStringInt(plan.getCurrentRevision().getVersion())) {
                plan.setBimplusPlanReference(new BimplusPlanReference()
                        .withId(bimplusPlanReferenceDto.getId())
                        .withName(bimplusPlanReferenceDto.getName())
                        .withRevision(Integer.parseInt(plan.getCurrentRevision().getVersion()))
                        .withState(BimplusPlanState.LINKED));
            } else {
                plan.setBimplusPlanReference(BimplusPlanReference.errorOccurred(bimplusPlanReference.getId(), bimplusPlanReference.getName(), "Bimplus revision is not an Integer"));
            }
        } else if (bimplusPlanReferenceDto.getState() == BimplusPlanState.ERROR) {
            plan.setBimplusPlanReference(BimplusPlanReference.errorOccurred(bimplusPlanReference.getId(), bimplusPlanReference.getName(), bimplusPlanReferenceDto.getErrorMessage()));
        } else {
            plan.getBimplusPlanReference().setState(bimplusPlanReferenceDto.getState());
        }
    }

    private void updateLastActivity(Plan plan, Instant lastActivity) {
        // Map Instant to UTC DateTime object.
        ZonedDateTime newLastActivity = lastActivity.atZone(ZoneId.of("UTC"));
        // Swap last activity if newer
        if (plan.swapLastActivityIfNewer(newLastActivity)) {
            // Also update projects lastActivity
            Project project = plan.getProject();
            if (project.swapLastActivityIfNewer(newLastActivity)) {
                projectRepository.save(project);
            }
        }
    }

    private void sendPlanNotification(Plan plan, NotificationCode code) {
        notifyClient.builder()
                .code(code)
                .plan(plan.getId(), plan.getName())
                .project(plan.getProject().getId(), plan.getProject().getName().getName())
                .build().sendAsync();
    }

    public Plan movePlan(UUID planId, UUID parentId) {
        Assert.notNull(parentId, "Cannot move plan to new PlanFolder when ID is NULL!");
        Plan plan = getPlan(planId);
        if (!userPrivileges.checkPrivilege(plan.getProject().getId(), Collections.singletonList(Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.ManagePlans);
        }
        PlanFolder newParent = loadParent(parentId, plan.getProject());
        plan.setParent(newParent);
        Plan movedPlan = planRepository.save(plan);
        sendPlanNotification(plan, NotificationCode.PLAN_REVISE);
        log.debug("Moved Plan <{}> to new Parent Folder <{}>!", movedPlan, newParent);
        return movedPlan;
    }

    public Plan removePlanFolder(UUID planId) {
        Plan plan = getPlan(planId);
        if (!userPrivileges.checkPrivilege(plan.getProject().getId(), Collections.singletonList(Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.ManagePlans);
        }
        plan.setParent(plan.getProject().getRootFolder());
        Plan movedPlan = planRepository.save(plan);
        sendPlanNotification(plan, NotificationCode.PLAN_REVISE);
        log.debug("Moved Plan <{}> to new Project's Root Folder!", movedPlan);
        return movedPlan;
    }

    public PlanRevision addNewPlanRevision(UUID planId, PlanRevisionDTO revisionDTO) {
        Assert.notNull(revisionDTO, "Cannot add new PlanRevision when PlanRevisionDTO is NULL!");

        Plan plan = getPlan(planId);
        if (plan.isLock()) {
            throw apiError().locked()
                    .code(ErrorCodes.PLAN_REVISION_IN_PROCESS.getCode())
                    .info("id", plan.getId().toString())
                    .getApiException();
        } else {
            planRepository.setLock(planId, true); // Lock plan to prevent actions on it
        }

        if (!subscriptionService.canCreatePlans()) {
            throw ApiExceptionHelper.newLimitExceedError(ErrorCodes.PLAN_LIMIT_EXCEED);
        }

        if (!userPrivileges.checkPrivilege(plan.getProject().getId(), Arrays.asList(Privilege.CreatePlans, Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.CreatePlans, Privilege.ManagePlans);
        }

        if (plan.getRevisions().stream().anyMatch(revision -> revision.getId().equals(revisionDTO.getId()))) {
            throw ApiExceptionHelper.newConflictError(ErrorCodes.PLAN_REVISION_ALREADY_EXISTS, revisionDTO.getId().toString());
        }

        PlanRevision newRevision = parseRevision(plan, plan.getCurrentRevision().getType(), revisionDTO.getMetadata(), revisionDTO,
                revisionDTO.getCreated() != null ? revisionDTO.getCreated().atZone(ZoneId.of("UTC")) : ZonedDateTime.now(ZoneId.of("UTC")));
        plan.addNewRevision(newRevision);

        if (plan.getBimplusPlanReference().getRevision() > 0
                && String.valueOf(plan.getBimplusPlanReference().getRevision()).equalsIgnoreCase(newRevision.getVersion())
                && isStringInt(newRevision.getVersion())) {
            plan.getBimplusPlanReference().setRevision(Integer.parseInt(newRevision.getVersion()));
        }

        if(revisionDTO.isOverwritePlanname()){
            plan.setName(removeExtension(revisionDTO.getMetadata().getFileName()));
            updatePinPlanName(plan);
        }

        PlanRevision revision = planRepository.save(plan).getCurrentRevision();

        //Update pin positions to new revision
        if (revisionDTO.getRevisionLocationDifference() != null && (revisionDTO.getRevisionLocationDifference().getFactor() != 0.0 ||
                revisionDTO.getRevisionLocationDifference().getMoveX() != 0 ||
                revisionDTO.getRevisionLocationDifference().getMoveY() != 0)) {
            pinApiClient.updatePinPositionsNewRevision(new PinRevisionLocationData()
                    .withPlanId(planId)
                    .withRevisionId(revisionDTO.getId())
                    .withPlanRevisionDifference(new PlanRevisionLocationDifference()
                            .withFactor(revisionDTO.getRevisionLocationDifference().getFactor())
                            .withMoveX(revisionDTO.getRevisionLocationDifference().getMoveX())
                            .withMoveY(revisionDTO.getRevisionLocationDifference().getMoveY())));
        }
        planRepository.setLock(planId, false);
        sendPlanNotification(plan, NotificationCode.PLAN_REVISE);
        return revision;
    }

    public static String removeExtension(String fileName){
        if (fileName.contains(".")) {
            int pntIndx = fileName.lastIndexOf('.');
            if (pntIndx > fileName.lastIndexOf('/')) {
                return fileName.substring(0, pntIndx);
            }
        }
        return fileName;
    }

    private boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public PlanRevision getRevision(UUID revisionId) {
        Assert.notNull(revisionId, "Cannot get PlanRevision when ID is NULL!");
        PlanRevision revision = planRevisionRepository.findById(revisionId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Plan Revision", revisionId));
        if (!privilegeApiClient.isMember(revision.getPlan().getProject().getId())) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, revision.getPlan().getProject().getId().toString());
        }
        return revision;
    }

    public Optional<InputStream> downloadThumbnail(Plan plan) {
        try {
            StorageAccessKey key = new StorageAccessKey(FileType.PlanThumbnail, String.format("%s/%s/%s/thumbnail.jpg",
                    plan.getProject().getId(), plan.getId(), plan.getCurrentRevision().getId()));
            if (!storageEngine.exists(key)) {
                return Optional.empty();
            }
            if (StringUtils.isEmpty(plan.getCurrentRevision().getMetadata().getThumbnailChecksum())) {
                generateThumbnailChecksum(plan, key);
            }
            return Optional.of(storageEngine.openStreamTo(key));
        } catch (StorageException e) {
            log.warn(String.format("Could not open Stream to Plan (<%s>) Thumbnail!", plan.getId()), e);
            return Optional.empty();
        }
    }

    private void generateThumbnailChecksum(Plan plan, StorageAccessKey key) {
        plan.getCurrentRevision().getMetadata().setThumbnailChecksum(storageEngine.md5AsHex(key));
        planRepository.save(plan);
    }

    public Optional<InputStream> downloadTile(UUID revisionId, int level, int y, int x, boolean alwaysReturn, boolean forAndroid) {
        PlanRevision revision = planRevisionRepository.findById(revisionId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Plan Revision", revisionId));
        if (!privilegeApiClient.isMember(revision.getPlan().getProject().getId())) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, revision.getPlan().getProject().getId().toString());
        }
        if (forAndroid) {
            PlanLevel l = revision.getLevels()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList())
                    .get(level - 1);
            int tilesX = l.getTilesX();
            int tilesY = l.getTilesY();
            if (tilesX < tilesY) {
                tilesX = tilesY;
            } else if (tilesY < tilesX) {
                tilesY = tilesX;
            }
            PlanLevel biggestLevel = revision.getLevels()
                    .stream()
                    .sorted(Comparator.reverseOrder())
                    .findFirst().orElseThrow(() -> new RuntimeException()); // TODO
            int factor = revision.getLevels().size() == 1 ? 8 : LevelSquarifier.FACTOR;
            int squareSide = LevelSquarifier.getBiggestSideTimesTwo(biggestLevel.getTilesX(), biggestLevel.getTilesY(), factor);
            int n = revision.getLevels().size() - level;
            for (int i = 0; i < n; i++)
                squareSide /= 2;
            int oddModifier = 0;
            PlanLevelDTO levelDTO = LevelSquarifier.translate(new PlanLevelDTO(l, "", ""));
            int tilesCont;
            if (levelDTO.getWidth() >= levelDTO.getHeight()) {
                tilesCont = levelDTO.getWidth() / 256;
            } else {
                tilesCont = levelDTO.getHeight() / 256;
            }

            if ((tilesCont % 2) > 0) {
                oddModifier = 1;
            }
            int xPaddingModifier = 0;
            if ((tilesX % 2) != 0) {
                tilesX += 1;
                xPaddingModifier = -1;
            }
            int yPaddingModifier = 0;
            if ((tilesY % 2) != 0) {
                tilesY += 1;
                yPaddingModifier = -1;
            }
            int paddingX = (squareSide - LevelSquarifier.findNearestSuperiorPowerOfTwo(tilesX)) / 2 + xPaddingModifier;// + oddModifier;
            int originalX = x, originalY = y;
            x = x - getBestPadding(squareSide);
            int paddingY = (squareSide - LevelSquarifier.findNearestSuperiorPowerOfTwo(tilesY)) / 2 + yPaddingModifier;// + oddModifier;
            y = y - getBestPadding(squareSide);
            log.debug("Download Tile forAndroid: tilesX={}, tilesY={}, squareSide={}, paddingX={}, paddingY={}, x={} (original={}), y={} (original={})",
                    tilesX, tilesY, squareSide, paddingX, paddingY, x, originalX, y, originalY);
        }
        StorageAccessKey key = new StorageAccessKey(FileType.PlanTile, String.format("%s/%s/%s/%d/%d_%d.jpg",
                revision.getPlan().getProject().getId(), revision.getPlan().getId(), revision.getId(), level, y, x));
        if (storageEngine.exists(key)) {
            return Optional.of(storageEngine.openStreamTo(key));
        } else if (alwaysReturn) {
            try {
                return Optional.of(new ClassPathResource("empty_tile.jpg").getInputStream());
            } catch (IOException e) {
                throw ApiExceptionHelper.newInternalServerError("Error in loading empty tile", e);
            }
        }
        return Optional.empty();
    }

    // numberOfTiles = 8 = 8x8
    // which means the original image file is 2x2 after squarified
    // perfect starting point must be 3x3
    private int getBestPadding(int numberOfTiles) {
        if (numberOfTiles == 1 || numberOfTiles == 2) {
            return 0;
        }

        if (numberOfTiles == 4) {
            return 1;
        }

        int originalNumberOfTiles = numberOfTiles / 4;
        return (numberOfTiles / 2) - (originalNumberOfTiles / 2);
    }

    public Optional<InputStream> downloadRevision(UUID revisionId) {
        PlanRevision revision = planRevisionRepository.findById(revisionId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Plan Revision", revisionId));
        if (!privilegeApiClient.isMember(revision.getPlan().getProject().getId())) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, revision.getPlan().getProject().getId().toString());
        }
        StorageAccessKey key = new StorageAccessKey(FileType.PlanTile, String.format("%s/%s/%s/revision.tar",
                revision.getPlan().getProject().getId(), revision.getPlan().getId(), revision.getId()));
        if (!storageEngine.exists(key)) {
            return Optional.empty();
        }
        return Optional.of(storageEngine.openStreamTo(key));
    }

    public List<Plan> searchPlans(UUID projectId, String search, int page, int size, ProjectFilter projectFilter) {
        Set<UUID> projects = null;
        if (projectFilter != null) {
            if (ProjectFilter.AllProjects == projectFilter) {
                projects = membershipsApiClient.listAllMemberships(personalApiClient.getMe().getUserId())
                        .stream()
                        .filter(m -> m.getState() == MembershipState.Active)
                        .map(TeamMember::getProjectId)
                        .collect(Collectors.toSet());
            } else {
                projects = projectRepository.findByOrganisationId(personalApiClient.getMe().getOrganisationId())
                        .map(Project::getId).collect(Collectors.toSet());
            }
        } else if (projectId != null && privilegeApiClient.isMember(projectId)) {
            projects = Collections.singleton(projectId);
        }
        if (projects != null && !projects.isEmpty()) {
            return planRepository.findByProjectIdsAndNameLike(projects, search)
                    .filter(plan -> planTranslator.canViewPlan(plan.getProject().getId(), plan.getId()))
                    .skip(page * size).limit(size)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private PlanFolder loadParent(UUID id, Project project) {
        if (id == null) {
            return project.getRootFolder();
        }
        PlanFolder parent = planFolderRepository.findById(id)
                .orElseThrow(() -> ApiExceptionHelper.newBadRequestError(ErrorCodes.ERROR_LOAD_PARENT_FOLDER, id.toString()));
        if (!parent.getProject().getId().equals(project.getId())) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.ERROR_CHOOSE_PARENT_FOLDER,
                    parent.getProject().getId().toString(), project.getId().toString());
        }
        return parent;
    }

    private PlanRevision parseRevision(Plan plan, PlanType planType, RevisionMetadataDTO metadataDTO, PlanRevisionDTO revisionDTO, ZonedDateTime created) {
        RevisionMetadata metadata = new RevisionMetadata();
        if (planType == PlanType.File) {
            if (metadataDTO.getImageWidth() == null) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.WIDTH_NOT_SPECIFIED);
            }
            metadata.setImageWidth(metadataDTO.getImageWidth());
            if (metadataDTO.getImageHeight() == null) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.HEIGHT_NOT_SPECIFIED);
            }
            metadata.setImageHeight(metadataDTO.getImageHeight());
            if (StringUtils.isEmpty(metadataDTO.getFileName())) {
                metadataDTO.setFileName("test-plan");
                //throw new BadRequestException("When importing a file revision metadata.fileName has to be specified!");
            }
            metadata.setFileName(metadataDTO.getFileName());
            if (StringUtils.isEmpty(metadataDTO.getContentType())) {
                // TODO throw new BadRequestException("When importing a file revision metadata.contentType has to be specified!");
                metadataDTO.setContentType("image/jpeg");
            }
            metadata.setContentType(metadataDTO.getContentType());

        } else if (planType == PlanType.Map) {
            if (StringUtils.isEmpty(metadataDTO.getNwLatLong())) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.NE_LATLONG_NOT_SPECIFIED);
            }
            String[] nwLatLong = metadataDTO.getNwLatLong().split(",");
            if (nwLatLong.length != 2) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.INVALID_NE_LATLONG, metadataDTO.getNwLatLong());
            }
            metadata.setNwLat(nwLatLong[0]);
            metadata.setNwLong(nwLatLong[1]);
            metadata.setImageHeight(metadataDTO.getImageHeight());
            metadata.setImageWidth(metadataDTO.getImageWidth());

            if (StringUtils.isEmpty(metadataDTO.getSeLatLong())) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.SE_LATLONG_NOT_SPECIFIED);
            }
            String[] seLatLong = metadataDTO.getSeLatLong().split(",");
            if (seLatLong.length != 2) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.INVALID_SE_LATLONG, metadataDTO.getSeLatLong());
            }
            metadata.setSeLat(seLatLong[0]);
            metadata.setSeLong(seLatLong[1]);
        }
        metadata.setArchiveChecksum(metadataDTO.getChecksum());
        metadata.setThumbnailChecksum(metadataDTO.getThumbnailChecksum());
        if (metadataDTO.getArchiveSize() == null) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.ARCHIVE_SIZE_NOT_SPECIFIED);
        }
        metadata.setArchiveSize(metadataDTO.getArchiveSize());
        metadata.setRotation(metadataDTO.getRotation() == null ? -1 : metadataDTO.getRotation());
        metadata.setPage(metadataDTO.getPage() == null ? -1 : metadataDTO.getPage());
        if (revisionDTO.getLevel() == null || revisionDTO.getLevel().size() == 0) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.EMPTY_PLAN_LEVEL);
        }
        if (StringUtils.isEmpty(revisionDTO.getVersion())) {
            revisionDTO.setVersion(LocalDate.now().format(DATE_FORMATTER));
        }
        RevisionLocationDifference revisionLocationDifference = new RevisionLocationDifference();
        if (revisionDTO.getRevisionLocationDifference() != null) {
            revisionLocationDifference.setMoveX(revisionDTO.getRevisionLocationDifference().getMoveX());
            revisionLocationDifference.setMoveY(revisionDTO.getRevisionLocationDifference().getMoveY());
            revisionLocationDifference.setFactor(revisionDTO.getRevisionLocationDifference().getFactor());
        }
        PlanRevision revision = new PlanRevision(revisionDTO.getId(), plan, revisionDTO.getVersion(), planType, metadata, created, revisionLocationDifference);
        revision.setLevels(revisionDTO.getLevel().stream()
                .map(levelDTO -> parseLevel(levelDTO, revision))
                .collect(Collectors.toList()));
        return revision;
    }

    private PlanLevel parseLevel(PlanLevelDTO levelDTO, PlanRevision revision) {
        if (levelDTO == null) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.PLAN_LEVEL_NULL);
        }
        return new PlanLevel(levelDTO.getId(), levelDTO.getTilesX(), levelDTO.getTilesY(), revision);
    }

    public PlanFolder moveFolderAndPlans(UUID parentId, MoveDto dto) {

        PlanFolder parent;
        if (parentId == null) {
            if (dto.getProjectId() == null) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_PLAN_PROJECT_ID_NULL);
            }
            parent = projectService.getProject(dto.getProjectId()).getRootFolder();
        } else {
            parent = planFolderRepository.findById(parentId)
                    .orElseThrow(() -> ApiExceptionHelper.newBadRequestError(ErrorCodes.ERROR_LOAD_PARENT_PLAN_FOLDER, parentId.toString()));
        }

        if (!userPrivileges.checkPrivilege(parent.getProject().getId(), Collections.singletonList(Privilege.ManagePlans), true).isCheck()) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.ManagePlans);
        }

        if (dto.getEntities() != null) {
            dto.getEntities().forEach(planId -> {
                Plan plan = getPlan(planId);
                if (!parent.getProject().getId().equals(plan.getProject().getId())) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_PLAN_OUTSIDE_PROJECT,
                            parent.getProject().getId().toString(), plan.getProject().getId().toString());
                }
                plan.setParent(parent);
                planRepository.save(plan);
                sendPlanNotification(plan, NotificationCode.PLAN_REVISE);
            });
        }
        if (dto.getFolders() != null) {
            dto.getFolders().forEach(folderId -> {
                PlanFolder planFolder = getPlanFolder(folderId);
                if (planFolder.getId().equals(parent.getId())) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_PLAN_FOLDER_TO_ITSELF);
                }
                if (isTryingToMoveInChild(planFolder, parent)) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_PLAN_FOLDER_TO_ITSELF);
                }
                if (!parent.getProject().getId().equals(planFolder.getProject().getId())) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_PLAN_OUTSIDE_PROJECT,
                            parent.getProject().getId().toString(), planFolder.getProject().getId().toString());
                }
                planFolder.setParent(parent);
                planFolderRepository.save(planFolder);
            });
        }
        return getPlanFolder(parent.getId());
    }

    private boolean isTryingToMoveInChild(PlanFolder source, PlanFolder destination) {
        if (destination.getParent() == null) {
            return false;
        }
        if (destination.getParent().getId().equals(source.getId())) {
            return true;
        }
        return isTryingToMoveInChild(source, destination.getParent());
    }

    public SyncSizeDTO getPlanSyncSize(UUID planId, String since, PostsFilter[] postsFilter) {
        long issueSize = issueRequestApiClient.getPlanIssueSyncSize(planId, since, Arrays.asList(postsFilter)).getSizeInBytes();
        long planSize = planRepository.findById(planId).get().getCurrentRevision().getMetadata().getArchiveSize();
        return new SyncSizeDTO(issueSize + planSize, SyncSizeDTO.SyncSizeType.Plan, SizeUtils.getHumanReadable(issueSize + planSize));
    }

    public Plan unlinkBimplus(UUID planId) {
        Optional<Plan> optional = planRepository.findById(planId);
        if (optional.isPresent()) {
            Plan plan = optional.get();
            plan.getBimplusPlanReference().setState(BimplusPlanState.UNLINKED);
            return planRepository.save(plan);
        } else {
            throw ApiExceptionHelper.newResourceNotFoundError("Plan", planId);
        }
    }

    public void unlinkBimplusAttachmentsForPlansInProject(UUID projectId) {
        planRepository.findByProjectId(projectId).forEach(plan -> unlinkBimplus(plan.getId()));
    }

    public Page<PlanQuickInfo> getPlanQuickInfo(UUID projectId, UUID folderId, List<UUID> planIds, String since, int page, int size) {
        if (planIds != null && !planIds.isEmpty()) {
            return planRepository.findByIdInAndActiveIsTrue(planIds, PageRequest.of(page, size))
                .map(plan -> new PlanQuickInfo(plan, DateTimeUtils.safelyParse(since)));
        }
        if (folderId != null) {
            return planRepository.findByParentIdAndActiveIsTrue(folderId, PageRequest.of(page, size))
                .map(plan -> new PlanQuickInfo(plan, DateTimeUtils.safelyParse(since)));
        }
        if (projectId != null) {
            return planRepository.findByProjectIdAndActiveIsTrue(projectId, PageRequest.of(page, size))
                .map(plan -> new PlanQuickInfo(plan, DateTimeUtils.safelyParse(since)));
        }
        log.warn("To Get plan quick info at least one of these required: ProjectId, FolderId or PlanIds");
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }
}
