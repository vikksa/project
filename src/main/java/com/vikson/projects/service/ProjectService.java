package com.vikson.projects.service;

import com.vikson.notifications.NotificationCode;
import com.vikson.notifications.NotifyClient;
import com.vikson.projects.api.resources.AuditedDTO;
import com.vikson.projects.api.resources.CopyProjectDTO;
import com.vikson.projects.api.resources.MoveDto;
import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.api.resources.ProjectFolderDTO;
import com.vikson.projects.api.resources.ProjectFolderRestrictionDTO;
import com.vikson.projects.api.resources.ProjectTreeDTO;
import com.vikson.projects.api.resources.ScaleDefinitionDTO;
import com.vikson.projects.api.resources.BimplusProjectConfigDto;
import com.vikson.projects.api.resources.ConstructionTypeDTO;
import com.vikson.projects.api.resources.values.ExternalProjectFilter;
import com.vikson.projects.api.resources.ProjectAddressDTO;
import com.vikson.projects.api.resources.ProjectDurationDTO;
import com.vikson.projects.api.resources.values.ProjectExpandAttributes;
import com.vikson.projects.api.resources.values.ProjectSort;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.exceptions.ApiExceptionHelper;
import com.vikson.projects.exceptions.ErrorCodes;
import com.vikson.projects.lambda.LambdaService;
import com.vikson.projects.model.OrganisationRootFolder;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.ProjectFolder;
import com.vikson.projects.model.ProjectFolderRestriction;
import com.vikson.projects.model.ProjectLocation;
import com.vikson.projects.model.values.BimplusProjectConfig;
import com.vikson.projects.model.values.BimplusProjectState;
import com.vikson.projects.model.values.ProjectAddress;
import com.vikson.projects.model.values.ProjectDuration;
import com.vikson.projects.model.ProjectLogo;
import com.vikson.projects.model.ScaleDefinition;
import com.vikson.projects.repositories.OrganisationRootFolderRepository;
import com.vikson.projects.repositories.ProjectFolderRepository;
import com.vikson.projects.repositories.ProjectFolderRestrictionRepository;
import com.vikson.projects.repositories.ProjectLogoRepository;
import com.vikson.projects.repositories.ProjectRepository;
import com.vikson.projects.service.helpers.ProjectServiceHelper;
import com.vikson.projects.service.translators.ProjectTranslator;
import com.vikson.services.core.resources.SortDirection;
import com.vikson.services.datasetes.DataSetApi;
import com.vikson.services.datasetes.resources.DataSetCopy;
import com.vikson.services.internal.InternalApiClient;
import com.vikson.services.issues.CategoryApiClient;
import com.vikson.services.issues.IssueRequestApiClient;
import com.vikson.services.issues.resources.PostsFilter;
import com.vikson.services.issues.resources.SyncSizeDTO;
import com.vikson.services.reports.ReportsApiClient;
import com.vikson.services.users.ContactApiClient;
import com.vikson.services.users.MembershipsApiClient;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.UsersApiClient;
import com.vikson.services.users.resources.LicenseType;
import com.vikson.services.users.resources.MembershipState;
import com.vikson.services.users.resources.Organisation;
import com.vikson.services.users.resources.Privilege;
import com.vikson.services.users.resources.TeamMember;
import com.vikson.services.users.resources.UserProfile;
import com.vikson.storage.FileType;
import com.vikson.storage.StorageAccessKey;
import com.vikson.storage.StorageEngine;
import com.vikson.storage.exception.StorageException;
import com.vikson.projects.util.SizeUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vikson.projects.exceptions.ApiExceptionHelper.newBadRequestError;
import static com.vikson.projects.exceptions.ApiExceptionHelper.newInternalServerError;
import static com.vikson.projects.exceptions.ApiExceptionHelper.newUnPrivilegedError;

@Service
@Transactional
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    public static final ZoneId UTC = ZoneId.of("UTC");

    @Autowired
    private ProjectFolderRepository projectFolderRepository;
    @Autowired
    private ProjectFolderRestrictionRepository restrictionRepository;
    @Autowired
    private OrganisationRootFolderRepository organisationRootFolderRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectTranslator projectTranslator;

    @Autowired
    private PersonalApiClient personalApiClient;
    @Autowired
    private MembershipsApiClient membershipsApiClient;
    @Autowired
    private UserPrivileges userPrivileges;
    @Autowired
    private UsersApiClient usersApiClient;

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private ProjectServiceHelper serviceHelper;

    @Autowired
    private StorageEngine storageEngine;

    @Autowired
    private CategoryApiClient categoryApiClient;
    @Autowired
    private ContactApiClient contactApiClient;
    @Autowired
    private DataSetApi dataSetApi;
    @Autowired
    private ReportsApiClient reportsApiClient;
    @Autowired
    private NotifyClient notifyClient;
    @Autowired
    private IssueRequestApiClient issueRequestApiClient;
    @Autowired
    private ProjectLogoRepository projectLogoRepository;
    @Autowired
    private PlanService planService;
    @Autowired
    private LambdaService lambdaService;
    @Autowired
    private InternalApiClient internalApiClient;

    public ProjectFolder createFolder(ProjectFolderDTO folderDTO) {
        Assert.notNull(folderDTO, "Cannot create new ProjectFolder from NULL ProjectFolderDTO!");

        UserProfile me = personalApiClient.getMe();

        if (!(me.getSettings().isAdmin() || me.getSettings().isProjectCreator())) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.CREATE_PROJECT_FOLDER_PRIVILEGE);
        }

        if (StringUtils.isEmpty(folderDTO.getName())) {
            folderDTO.setName(String.format("New Folder (%s)", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }

        ProjectFolder parent;
        if (folderDTO.getParentId() != null) {
            parent = loadParent(folderDTO.getParentId(), me.getOrganisationId());
        } else {
            parent = loadRootFolder(me);
        }

        ProjectFolder newFolder = projectFolderRepository.save(new ProjectFolder(folderDTO.getId(), folderDTO.getName(), me.getOrganisationId(), parent));
        log.debug("Create new ProjectFolder: <%s>!", newFolder);
        return newFolder;
    }

    public ProjectFolder updateFolder(ProjectFolderDTO updates) {
        Assert.notNull(updates, "Cannot update ProjectFolder when ProjectFolderDTO is NULL!");
        Assert.notNull(updates.getId(), "Cannot update ProjectFolder when ProjectFolderDTO.id is NULL!");

        UserProfile me = personalApiClient.getMe();
        ProjectFolder folder = load(updates.getId());

        canManageProjectFolder(me, folder);

        if (!folder.getOrganisationId().equals(me.getOrganisationId())) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.FOLDER_UPDATE_OUTSIDE_ORGANIZATION,
                    me.getOrganisationId().toString(), folder.getOrganisationId().toString());
        }

        if (!StringUtils.isEmpty(updates.getName())) {
            folder.setName(updates.getName());
        }
        if (updates.getParentId() != null) {
            ProjectFolder parent = loadParent(updates.getParentId(), me.getOrganisationId());
            if (folder.getId().equals(updates.getParentId()) || isTryingToMoveInChild(folder, parent)) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_FOLDER_TO_CHILD_OR_ITSELF);
            }
            folder.setParent(parent);
        }
        if (updates.getActive() != null) {
            folder.setActive(updates.getActive());
            if (updates.getActive()) {
                reactiveAllChildren(folder);
            }
        }

        ProjectFolder updatedFolder = projectFolderRepository.save(folder);
        log.debug("Updated Project Folder: <%s>!", updatedFolder);
        return updatedFolder;
    }

    private void canManageProjectFolder(UserProfile me, ProjectFolder folder) {
        if (!(me.getSettings().isAdmin() ||
                (me.getSettings().isProjectCreator() && isProjectFolderIsOrInRestrictedFolder(folder, loadRootFolder(me))))) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.UPDATE_PROJECT_FOLDER_PRIVILEGE);
        }
    }

    private void reactiveAllChildren(ProjectFolder folder) {
        folder.getChildren().forEach(child -> child.setActive(true));
        folder.getSubFolders().forEach(subFolder -> {
            subFolder.setActive(true);
            reactiveAllChildren(subFolder);
        });
    }

    public ProjectFolder setRootAsParent(UUID folderId) {
        Assert.notNull(folderId, "Cannot set root folder as parent when folderId is NULL!");

        ProjectFolderDTO updates = new ProjectFolderDTO();
        updates.setId(folderId);
        updates.setParentId(loadRootFolder(personalApiClient.getMe()).getId());

        return updateFolder(updates);
    }

    public ProjectFolder getFolder(UUID folderId) {
        Assert.notNull(folderId, "Cannot get folder when folderId is NULL!");

        return projectFolderRepository.findById(folderId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("ProjectFolder", folderId));
    }

    public ProjectTreeDTO getTree(String search,
                                  StateFilter stateFilter,
                                  ExternalProjectFilter externalProjectFilter,
                                  ProjectExpandAttributes[] expand,
                                  SortDirection sortDirection,
                                  ProjectSort sort) {
        UserProfile me = personalApiClient.getMe();

        if (me.getSettings().isAdmin() || me.getSettings().isProjectCreator()) {
            ProjectFolder rootFolder = loadRootFolder(me);
            List<ProjectDTO> externalProjects = externalProjectFilter == ExternalProjectFilter.All ?
                    filterExternalProjects(membershipsApiClient.listAllMemberships(me.getUserId()), me, rootFolder, expand)
                    : Collections.emptyList();
            return sortTree(new ProjectTreeDTO(rootFolder, externalProjects, project -> projectTranslator.translate(project, expand), stateFilter, search), sortDirection, sort);
        } else {
            List<TeamMember> memberships = membershipsApiClient.listAllMemberships(me.getUserId());
            ProjectFolder rootFolder = loadRootFolder(me);
            ProjectTreeDTO tree = new ProjectTreeDTO(rootFolder, filterExternalProjects(memberships, me, rootFolder, expand), project -> projectTranslator.translate(project, expand), stateFilter, search);
            removeProjectsWhereNoMemberIn(tree, memberships.stream().map(TeamMember::getProjectId).collect(Collectors.toSet()));
            return sortTree(tree, sortDirection, sort);
        }
    }

    private ProjectTreeDTO sortTree(ProjectTreeDTO tree, SortDirection sortDirection, ProjectSort sort) {
        Comparator<ProjectDTO> projectComparator = sortDirection.equals(SortDirection.ASC) ? sort.getComparator() : sort.getComparator().reversed();
        Comparator<ProjectFolderDTO> folderComparator = getFolderComparator(sort, sortDirection);
        tree.getExternalProjects().sort(projectComparator);
        sortFolder(tree.getSubFolder(), folderComparator, projectComparator);
        sortProjects(tree.getChildren(), projectComparator);
        return tree;
    }

    private void sortFolder(List<ProjectFolderDTO> folders, Comparator<ProjectFolderDTO> folderComparator,
                            Comparator<ProjectDTO> projectComparator) {
        folders.sort(folderComparator);
        folders.forEach(folder -> {
            sortFolder(folder.getSubFolder(), folderComparator, projectComparator);
            sortProjects(folder.getChildren(), projectComparator);
        });
    }

    private void sortProjects(List<ProjectDTO> projects, Comparator<ProjectDTO> comparator) {
        projects.sort(comparator);
    }

    private Comparator<ProjectFolderDTO> getFolderComparator(ProjectSort sort, SortDirection direction) {
        Comparator<ProjectFolderDTO> folderComparator;
        switch (sort) {
            case Created:
                folderComparator = Comparator.comparing(AuditedDTO::getCreated);
                break;
            case LastModified:
                folderComparator = (p1, p2) -> {
                    if (p2.getLastModified() == null)
                        return 1;
                    if (p1.getLastModified() == null)
                        return -1;
                    return p1.getLastModified().compareTo(p2.getLastModified());
                };
                break;
            default:
            case Name:
                folderComparator = (p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName());
                break;
        }
        if (direction.equals(SortDirection.DESC)) {
            folderComparator = folderComparator.reversed();
        }
        return folderComparator;
    }

    private List<ProjectDTO> filterExternalProjects(List<TeamMember> memberships, UserProfile me, ProjectFolder rootFolder, ProjectExpandAttributes[] expand) {
        return memberships.stream()
                .filter(m -> m.getState() == MembershipState.Active)
                .filter(m -> {
                    Project project = projectRepository.findById(m.getProjectId())
                            .orElseThrow(() -> ApiExceptionHelper.newInternalServerError(String.format("Could not Find External Project <%s>.", m.getProjectId())));
                    return !project.getOrganisationId().equals(me.getOrganisationId()) || !isProjectFolderIsOrInRestrictedFolder(project.getParent(), rootFolder);
                })
                .map(m ->
                        projectRepository.findById(m.getProjectId())
                                .map(p -> projectTranslator.translate(p, expand))
                                .map(p -> {
                                    p.setMembership(m);
                                    return p;
                                })
                                .orElseThrow(() -> ApiExceptionHelper.newInternalServerError(String.format("Could not Find External Project <%s>.", m.getProjectId()))))
                .map(projectTranslator::setOrganisationName)
                .collect(Collectors.toList());
    }

    private ProjectFolderDTO removeProjectsWhereNoMemberIn(ProjectFolderDTO rootFolder, Set<UUID> memberships) {
        List<ProjectDTO> projects = new ArrayList<>(rootFolder.getChildren());
        projects.stream()
                .filter(p -> !memberships.contains(p.getId()))
                .forEach(rootFolder.getChildren()::remove);
        List<ProjectFolderDTO> subFolders = rootFolder.getSubFolder()
                .stream()
                .map(f -> removeProjectsWhereNoMemberIn(f, memberships))
                .collect(Collectors.toList());
        subFolders.stream()
                .filter(p -> p.getSubFolder().isEmpty() && p.getChildren().isEmpty())
                .forEach(rootFolder.getSubFolder()::remove);
        return rootFolder;
    }

    public void restrictUserToFolder(ProjectFolderRestrictionDTO restrictionDTO) {
        Assert.notNull(restrictionDTO, "restrictionDTO is required - must not be NULL!");
        UserProfile user = usersApiClient.getUser(restrictionDTO.getUserId());
        UserProfile me = personalApiClient.getMe();
        if (!user.getOrganisationId().equals(me.getOrganisationId())) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.USER_RESTRICTION_1);
        }
        if (!me.getSettings().isAdmin()) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.USER_RESTRICTION_2);
        }
        if (restrictionDTO.getFolderId() == null) {
            restrictionRepository.deleteByUserId(restrictionDTO.getUserId());
            return;
        }
        ProjectFolder folder = projectFolderRepository.findById(restrictionDTO.getFolderId())
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Project Folder", restrictionDTO.getFolderId()));
        if (!folder.getOrganisationId().equals(user.getOrganisationId())) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.SAME_ORGANIZATION);
        }
        ProjectFolderRestriction restriction = restrictionRepository.findById(user.getUserId())
                .orElseGet(() -> new ProjectFolderRestriction(user, folder));
        restriction.setFolder(folder);
        restrictionRepository.save(restriction);
    }

    public ProjectFolder getRootFolder() {
        return getRootFolder(personalApiClient.getMe().getUserId());
    }

    public UUID getRootFolderId() {
        return getRootFolder(personalApiClient.getMe().getUserId())
                .getId();
    }

    public ProjectFolder getRootFolder(UUID userId) {
        Assert.notNull(userId, "userId is required - must not be NULL!");
        UserProfile user = usersApiClient.getUser(userId);
        return restrictionRepository.findById(userId)
                .map(ProjectFolderRestriction::getFolder)
                .orElseGet(() -> organisationRootFolderRepository.findById(user.getOrganisationId())
                        .orElseGet(() -> organisationRootFolderRepository.save(
                                new OrganisationRootFolder(
                                        user.getOrganisationId(),
                                        new ProjectFolder(null,
                                                StringUtils.isEmpty(user.getOrganisationName()) ? "Root" : user.getOrganisationName(), user.getOrganisationId(), null)))
                        ).getFolder());
    }

    public Project createNewProject(ProjectDTO projectDTO) {
        Assert.notNull(projectDTO, "Cannot create new Project form NULL ProjectDTO!");

        Project project = serviceHelper.createNew(projectDTO);
        categoryApiClient.getPinCategories(project.getId());
        membershipsApiClient.clearMembershipCache(personalApiClient.getMe().getUserId());
        return project;
    }

    @NotNull
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Project createProject(ProjectDTO projectDTO) {
        UserProfile me = personalApiClient.getMe();

        if ((!me.getLicense().isPaid() && me.getLicense().getType() != LicenseType.Test)
                || (me.getLicense().getUntil() != null && me.getLicense().getUntil().isBefore(Instant.now()))) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.CREATE_PROJECT_PRIVILEGE_1);
        }

        if (!(me.getSettings().isAdmin() || me.getSettings().isProjectCreator())) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.CREATE_PROJECT_PRIVILEGE_2);
        }

        if (!subscriptionService.canCreateProjects()) {
            throw ApiExceptionHelper.newLimitExceedError(ErrorCodes.PROJECT_LIMIT_EXCEED);
        }

        if (StringUtils.isEmpty(projectDTO.getName())) {
            projectDTO.setName(findProjectName(me));
        } else if (!isProjectNameAvailable(projectDTO.getName())) {
            throw ApiExceptionHelper.newConflictError(ErrorCodes.PROJECT_WITH_NAME_ALREADY_EXISTS, projectDTO.getName());
        }

        ProjectFolder parent;
        if (projectDTO.getParentId() != null) {
            parent = loadParent(projectDTO.getParentId(), me.getOrganisationId());
        } else {
            parent = loadRootFolder(me);
        }

        ZonedDateTime created;
        if (projectDTO.getOccurred() != null) {
            created = projectDTO.getOccurred().atZone(UTC);
        } else if (projectDTO.getCreated() != null) {
            created = projectDTO.getCreated().atZone(UTC);
        } else {
            created = ZonedDateTime.now(UTC);
        }

        BimplusProjectConfig bimplusProjectConfig = new BimplusProjectConfig();
        if (projectDTO.getBimplusProjectConfigDto() != null && projectDTO.getBimplusProjectConfigDto().getId() != null) {
            BimplusProjectConfigDto bimplusProjectConfigDto = projectDTO.getBimplusProjectConfigDto();
            if (isBimplusProjectAlreadyLinked(bimplusProjectConfigDto.getId())) {
                throw ApiExceptionHelper.newConflictError(ErrorCodes.BIMPLUS_ALREADY_LINKED, String.valueOf(bimplusProjectConfigDto.getId()));
            }
            if (bimplusProjectConfigDto.getProjectName() == null || bimplusProjectConfigDto.getProjectName().trim().equals("")) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.BIMPLUS_PROJECT_NAME_REQUIRED);
            }
            if (bimplusProjectConfigDto.getState() == null) {
                bimplusProjectConfigDto.setState(BimplusProjectState.LINKED);
            }
            bimplusProjectConfig = new BimplusProjectConfig(bimplusProjectConfigDto.getId(), bimplusProjectConfigDto.getProjectName(), bimplusProjectConfigDto.getState(), bimplusProjectConfigDto.getTeamSlug());
        }

        Project project = projectRepository.saveAndFlush(new Project(projectDTO.getId(), projectDTO.getName(), me.getOrganisationId(), parent, created).withBimplusConfig(bimplusProjectConfig));
        log.debug("Created new Project: <%s>.", project);

        // Set Remaining Fields of the Project
        projectDTO.setName(null);
        projectDTO.setParentId(null);
        projectDTO.setActive(null);
        projectDTO.setId(project.getId());
        updateProject(projectDTO);

        return project;
    }

    public Project copyProject(CopyProjectDTO projectDTO) {
        Project project = getProject(projectDTO.getId());
        projectDTO.setId(UUID.randomUUID());
        Project newProject = serviceHelper.createNew(projectDTO);
        if (projectDTO.isCategories()) {
            categoryApiClient.copyCategories(newProject.getId(), project.getId());
        }
        if (projectDTO.isTeam() || !CollectionUtils.isEmpty(projectDTO.getTeamMembers())) {
            membershipsApiClient.copyTeam(project.getId(), newProject.getId(), projectDTO.getTeamMembers(), projectDTO.isTeam());
        }
        if (projectDTO.isContacts() || !CollectionUtils.isEmpty(projectDTO.getContactsList())) {
            contactApiClient.copy(newProject.getId(), project.getId(), projectDTO.getContactsList(), projectDTO.isContacts());
        }
        if (projectDTO.isScaleDefinitions()) {
            project.getScaleDefinitions()
                    .forEach(scaleDefinition -> newProject.getScaleDefinitions().add(new ScaleDefinition(newProject, scaleDefinition)));
        }
        if (projectDTO.isDatasets()) {
            dataSetApi.copyDataSets(new DataSetCopy(project.getId(), newProject.getId()));
        }
        if (projectDTO.isTemplates()) {
            reportsApiClient.copyTemplates(newProject.getId(), project.getId());
        }
        return projectRepository.save(newProject);
    }

    private String findProjectName(UserProfile userProfile) {
        try {
            String name = String.format("New Project (%s)", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if (isProjectNameAvailable(name)) {
                return name;
            }
            ZoneId zoneId = Optional.ofNullable(userProfile.getSettings().getTimeZone()).map(ZoneId::of).orElse(UTC);
            name = String.format("New Project (%s)", ZonedDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")));
            String nameTry = name;
            for (int n = 0; n < 100; n++) {
                if (isProjectNameAvailable(nameTry)) {
                    return nameTry;
                }
                nameTry = String.format("%s (%d)", name, n + 1);
            }
        } catch (Exception ignored) {
            log.warn("Caught Exception during Project Name Generation.", ignored);
        }
        return String.format("New Project (%s)", UUID.randomUUID());
    }

    public boolean isProjectNameAvailable(String name) {
        return !projectRepository.existsByNameNameIgnoreCaseAndOrganisationId(name, personalApiClient.getMe().getOrganisationId());
    }

    public boolean isBimplusProjectAlreadyLinked(UUID id) {
        return projectRepository.existsByBimplusConfigIdAndBimplusConfigStateNot(id, BimplusProjectState.LINKED);
    }

    public Project getProject(UUID id) {
        Assert.notNull(id, "Cannot get Project when id is NULL!");
        return projectRepository.findById(id)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Project", id));
    }

    public Project getProjectById(UUID id) {
        Assert.notNull(id, "Cannot get Project when id is NULL!");

        Project project = projectRepository.findById(id)
            .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("Project", id));

        UserProfile me = personalApiClient.getMe();

        if (me.getOrganisationId().equals(project.getOrganisationId()) && (me.getSettings().isAdmin() || isAdminForProject(me, project))) {
            return project;
        }
        if (internalApiClient.checkMember(me.getUserId(), project.getId())) {
            return project;
        }
        throw  ApiExceptionHelper.newUnPrivilegedError("User can't access project");
    }


    public List<Project> getProjectsInOrganisation() {
        UserProfile me = personalApiClient.getMe();

        if (me.getSettings().isAdmin() || me.getSettings().isProjectCreator()) {
            return projectRepository.findByOrganisationIdAndActiveIsTrue(me.getOrganisationId())
                    .collect(Collectors.toList());
        }

        return membershipsApiClient.listAllMemberships(me.getUserId())
                .stream()
                .map(m -> projectRepository.findById(m.getProjectId())
                        .orElseThrow(() -> ApiExceptionHelper.newInternalServerError(String.format("Could not Load Project <%s> from Membership: Not Found!", m.getProjectId()))))
                .collect(Collectors.toList());
    }

    public List<Project> getProjectsInOrganisation(int page, int size) {
        UserProfile me = personalApiClient.getMe();

        if (me.getSettings().isAdmin() || me.getSettings().isProjectCreator()) {
            return projectRepository.findByOrganisationIdAndActiveIsTrue(me.getOrganisationId(), PageRequest.of(page, size))
                    .collect(Collectors.toList());
        }

        return membershipsApiClient.listAllMemberships(me.getUserId(), page, size)
                .stream()
                .map(m -> projectRepository.findById(m.getProjectId())
                        .orElseThrow(() -> ApiExceptionHelper.newInternalServerError(String.format("Could not Load Project <%s> from Membership: Not Found!", m.getProjectId()))))
                .collect(Collectors.toList());
    }

    public List<Project> listMyProjects(String search, StateFilter stateFilter) {
        UserProfile me = personalApiClient.getMe();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Load Memberships...");
        List<UUID> memberships = membershipsApiClient.listAllMembershipProjectIds(me.getUserId());
        stopWatch.stop();
        log.info("Finished Loading Membership: {}ms", stopWatch.getLastTaskTimeMillis());
        return getProjectsByIdsAndSearchFilter(search, stateFilter, memberships);
    }

    public com.vikson.services.core.resources.Page<ProjectDTO> listMyProjectsPaged(int page, int size, ProjectExpandAttributes[] expand) {
        UserProfile me = personalApiClient.getMe();
        com.vikson.services.core.resources.Page<UUID> memberships = membershipsApiClient.listAllMembershipProjectIds(me.getUserId(), page, size);
        List<Project> projects = projectRepository.findAllByIdInAndActiveIsTrue(memberships.getContent());
        com.vikson.services.core.resources.Page<ProjectDTO> projectPage = new com.vikson.services.core.resources.Page<>();
        projectPage.setContent(projects.stream().map(project -> projectTranslator.translate(project, expand)).collect(Collectors.toList()));
        projectPage.setFirst(memberships.isFirst());
        projectPage.setLast(memberships.isLast());
        projectPage.setNumber(memberships.getNumber());
        projectPage.setSize(memberships.getSize());
        projectPage.setTotalElements(memberships.getTotalElements());
        projectPage.setTotalPages(memberships.getTotalPages());
        return projectPage;
    }

    public List<Project> listMyLeftProjects(String search, StateFilter stateFilter, int page, int size, String since) {
        UserProfile me = personalApiClient.getMe();
        List<UUID> membershipProjectIds = membershipsApiClient.listAllLeftMembershipProjectIds(me.getUserId(), page, size, since);
        return getProjectsByIdsAndSearchFilter(search, stateFilter, membershipProjectIds);
    }

    private List<Project> getProjectsByIdsAndSearchFilter(String search, StateFilter stateFilter, List<UUID> membershipProjectIds) {
        return projectRepository.findAllById(membershipProjectIds)
                .stream()
                .filter(project -> project.getName().getName().toUpperCase().contains(search.toUpperCase()))
                .filter(project -> {
                    switch (stateFilter) {
                        case Active:
                            return project.isActive();
                        case Inactive:
                            return !project.isActive();
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    public Page<Project> searchProjects(String search, int page, int size) {
        UserProfile me = personalApiClient.getMe();

        Pageable pageable = PageRequest.of(page, size);
        Set<UUID> memberships = membershipsApiClient.listAllMemberships(me.getUserId())
                .stream()
                .filter(m -> m.getState() == MembershipState.Active)
                .map(TeamMember::getProjectId)
                .collect(Collectors.toSet());
        String pattern = "%" + search + "%";
        if (me.getSettings().isAdmin() || me.getSettings().isProjectCreator()) {
            return projectRepository.findByNameNameContainingIgnoreCaseAndActiveIsTrueAndOrganisationIdOrIdIn(pattern, me.getOrganisationId(), memberships, pageable);
        } else {
            return projectRepository.findByNameNameContainingIgnoreCaseAndIdInAndActiveIsTrue(pattern, memberships, pageable);
        }
    }

    public Project updateProject(ProjectDTO projectDTO) {
        Assert.notNull(projectDTO, "Cannot update Project when ProjectDTO is NULL!");
        Project project = getProject(projectDTO.getId());

        UserProfile me = personalApiClient.getMe();

        canManageProject(me, project);

        if (!(StringUtils.isEmpty(projectDTO.getName()) || project.getName().getName().equalsIgnoreCase(projectDTO.getName()))) {
            if (!isProjectNameAvailable(projectDTO.getName())) {
                throw ApiExceptionHelper.newConflictError(ErrorCodes.PROJECT_WITH_NAME_ALREADY_EXISTS, projectDTO.getName());
            }
            project.getName().setName(projectDTO.getName());
        }

        if (projectDTO.getInitials() != null) {
            project.getName().setInitials(projectDTO.getInitials());
        }
        if (projectDTO.getActive() != null) {
            if (!project.isActive() && projectDTO.getActive() && !subscriptionService.canCreateProjects()) {
                throw ApiExceptionHelper.newLimitExceedError(ErrorCodes.PROJECT_LIMIT_EXCEED);
            }
            project.setActive(projectDTO.getActive());
        }
        if (projectDTO.getDuration() != null) {
            ProjectDuration duration = project.getDuration();
            ProjectDurationDTO durationDTO = projectDTO.getDuration();
            if (durationDTO.getStart() != null) {
                duration.setStart(durationDTO.getStart().atZone(UTC));
            }
            if (durationDTO.getEnd() != null) {
                duration.setEnd(durationDTO.getEnd().atZone(UTC));
            }
            if(durationDTO.isRemoveStart()){
                duration.setStart(null);
            }
            if(durationDTO.isRemoveEnd()){
                duration.setEnd(null);
            }
        }
        if (projectDTO.getAddress() != null) {
            ProjectAddress address = project.getAddress();
            ProjectAddressDTO addressDTO = projectDTO.getAddress();
            if (addressDTO.getStreetAddress() != null) {
                address.setStreetAddress(addressDTO.getStreetAddress());
            }
            if (addressDTO.getZipCode() != null) {
                address.setZipCode(addressDTO.getZipCode());
            }
            if (addressDTO.getCity() != null) {
                address.setCity(addressDTO.getCity());
            }
            if (addressDTO.getCc() != null) {
                address.setCc(addressDTO.getCc());
            }
        }
        if (projectDTO.getCostDefinition() != null) {
            ScaleDefinitionDTO costsDTO = projectDTO.getCostDefinition();
            ScaleDefinition costs = project.getScaleDefinitions().stream()
                    .filter(ScaleDefinition::isCosts)
                    .findAny()
                    .orElseGet(() -> {
                        ScaleDefinition newCosts = new ScaleDefinition(project);
                        newCosts.setCosts(true);
                        project.getScaleDefinitions().add(newCosts);
                        return newCosts;
                    });
            update(costs, costsDTO);
        }
        if (projectDTO.getPriorityDefinition() != null) {
            ScaleDefinitionDTO priorityDTO = projectDTO.getPriorityDefinition();
            ScaleDefinition priority = project.getScaleDefinitions().stream()
                    .filter(ScaleDefinition::isPriority)
                    .findAny()
                    .orElseGet(() -> {
                        ScaleDefinition newPriority = new ScaleDefinition(project);
                        newPriority.setPriority(true);
                        project.getScaleDefinitions().add(newPriority);
                        return newPriority;
                    });
            update(priority, priorityDTO);
        }
        if (projectDTO.getParentId() != null) {
            project.setParent(loadParent(projectDTO.getParentId(), project.getOrganisationId()));
        }
        if (projectDTO.getDescription() != null) {
            project.setDescription(projectDTO.getDescription());
        }
        if (projectDTO.getClient() != null) {
            project.getClient().setClientName(projectDTO.getClient());
        }
        if (projectDTO.getClientAssistant() != null) {
            project.getClient().setAssistantName(projectDTO.getClientAssistant());
        }
        if (projectDTO.getConstructionType() != null) {
            ConstructionTypeDTO constructionTypeDTO = projectDTO.getConstructionType();
            if (constructionTypeDTO.getType() != null) {
                project.setConstructionType(constructionTypeDTO.getType());
            }
        }
        if (projectDTO.getNotes() != null) {
            project.setNotes(projectDTO.getNotes());
        }
        if (projectDTO.getManager() != null) {
            project.setManager(projectDTO.getManager());
        }
        if (projectDTO.getManagerPhone() != null) {
            project.setManagerPhone(projectDTO.getManagerPhone());
        }

        Project updated = projectRepository.save(project);
        if (projectDTO.getActive() != null) {
            sendProjectNotification(project, projectDTO.getActive() ? NotificationCode.PROJECT_ACTIVE : NotificationCode.PROJECT_INACTIVE);
        }
        log.debug("Updated Project: <{}>!", updated);
        return updated;
    }

    public void deleteFolder(UUID folderId) {
        UserProfile me = personalApiClient.getMe();
        ProjectFolder folder = load(folderId);

        canManageProjectFolder(me, folder);

        if (!folder.getOrganisationId().equals(me.getOrganisationId())) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.DELETE_PROJECT_FOLDER_OUTSIDE_ORGANIZATION,
                    me.getOrganisationId().toString(), folder.getOrganisationId().toString());
        }

        if ((folder.getChildren() == null || folder.getChildren().isEmpty()) &&
                (folder.getSubFolders() == null || folder.getSubFolders().isEmpty())) {
            projectFolderRepository.delete(folder);
        } else {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.PROJECT_FOLDER_DELETE_WITH_CHILD);
        }
    }

    private void sendProjectNotification(Project project, NotificationCode code) {
        notifyClient.builder()
                .code(code)
                .project(project.getId(), project.getName().getName())
                .build().sendAsync();
    }

    public Project removeParentFolder(UUID id) {
        Project project = getProject(id);
        UserProfile me = personalApiClient.getMe();
        canManageProject(me, project);

        project.setParent(loadRootFolder(me));
        Project removedParentFolder = projectRepository.save(project);
        log.debug("Removed Parent folder From Project <{}>!", removedParentFolder);
        return removedParentFolder;
    }

    public void setProjectLogo(UUID projectId, MultipartFile file) {
        Project project = getProject(projectId);
        canManageProject(personalApiClient.getMe(), project);
        ProjectLogo newLogo = handleUploadLogo(file);
        if(project.getProjectLogo() != null) {
            deleteLogoSafely(project.getProjectLogo());
        }
        project.setProjectLogo(newLogo);
        projectRepository.save(project);
    }

    public void setProjectReportLogo(UUID projectId, MultipartFile file){
        Project project = getProject(projectId);
        canManageProject(personalApiClient.getMe(), project);
        ProjectLogo newLogo = handleUploadLogo(file);
        if(project.getReportLogo() != null) {
            deleteLogoSafely(project.getReportLogo());
        }
        project.setReportLogo(newLogo);
        projectRepository.save(project);
    }

    private ProjectLogo handleUploadLogo(MultipartFile file) {
        Assert.notNull(file, "Cannot set Project's Logo with MultipartFile NULL!");
        if (!Arrays.asList("image/jpeg", "image/png", "image/gif", "image/svg", "image/bmp").contains(file.getContentType())) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.AVATAR_FILE_TYPE, file.getContentType());
        }
        try {
            byte[] data = file.getBytes();
            if (data.length > 3 * 1024 * 1024) {
                throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MAX_LOGO_SIZE, String.valueOf(data.length / 1024));
            }
            String logoChecksum = DigestUtils.md5DigestAsHex(data);
            UUID storageId = UUID.randomUUID();
            StorageAccessKey storageAccessKey = new StorageAccessKey(file.getContentType(), null, FileType.Misc, storageId.toString());
            storageEngine.save(storageAccessKey, data);
            return projectLogoRepository.save(new ProjectLogo(storageId, file.getContentType(), file.getSize(), logoChecksum));
        } catch (IOException e) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.LOGO_SAVE, e);
        }
    }

    public void downloadProjectLogo(UUID projectId, HttpServletResponse response, HttpServletRequest request) {
        Project project = getProject(projectId);
        if (project.getProjectLogo() != null) {
            downloadLogo(project.getProjectLogo(), response, request);
        }
    }

    public void downloadReportLogo(UUID projectId, HttpServletResponse response, HttpServletRequest request) {
        Project project = getProject(projectId);
        if (project.getReportLogo() != null) {
            downloadLogo(project.getReportLogo(), response, request);
        }
    }

    private void downloadLogo(ProjectLogo logo, HttpServletResponse response, HttpServletRequest request){
        try {
            StorageAccessKey storageAccessKey = new StorageAccessKey(FileType.Misc, logo.getStorageId().toString());
            if (logo.getChecksum() == null || logo.getChecksum().isEmpty()) {
                String logoChecksum = storageEngine.md5AsHex(storageAccessKey);
                logo.setChecksum(logoChecksum);
                projectLogoRepository.save(logo);
            }
            String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
            if (!StringUtils.isEmpty(ifNoneMatch) && ifNoneMatch.equals(logo.getChecksum())) {
                response.setHeader(HttpHeaders.ETAG, logo.getChecksum());
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
                return;
            }
            if (storageEngine.exists(storageAccessKey)) {
                try (InputStream is = storageEngine.openStreamTo(storageAccessKey)) {
                    response.setStatus(HttpStatus.OK.value());
                    response.setContentType(logo.getMediaType());
                    response.setHeader(HttpHeaders.ETAG, logo.getChecksum());
                    StreamUtils.copy(is, response.getOutputStream());
                }
            }
        } catch (StorageException | IOException e) {
            throw ApiExceptionHelper.newInternalServerError(String.format("Could not open Stream to Project/Report Logo (Logo id:<%s>)!", logo.getStorageId()), e);
        }
    }

    public void removeLogo(UUID projectId) {
        Project project = getProject(projectId);
        canManageProject(personalApiClient.getMe(), project);
        if (project.getProjectLogo() != null) {
            deleteLogoSafely(project.getProjectLogo());
            project.setProjectLogo(null);
            projectRepository.save(project);
        }
    }

    public void removeReportLogo(UUID projectId) {
        Project project = getProject(projectId);
        canManageProject(personalApiClient.getMe(), project);
        if (project.getReportLogo() != null) {
            deleteLogoSafely(project.getReportLogo());
            project.setReportLogo(null);
            projectRepository.save(project);
        }
    }

    private void deleteLogoSafely(ProjectLogo logo) {
        try {
            StorageAccessKey oldKey = new StorageAccessKey(FileType.Misc, logo.getStorageId().toString());
            storageEngine.delete(oldKey);
            projectLogoRepository.delete(logo);
        } catch (StorageException e) {
            log.warn(String.format("Could not delete old Project Logo <%s>!", logo), e);
        }
    }

    private ScaleDefinition update(ScaleDefinition scaleDefinition, ScaleDefinitionDTO scaleDefinitionDTO) {
        if (scaleDefinitionDTO.getName() != null) {
            scaleDefinition.setName(scaleDefinitionDTO.getName());
        }
        if (scaleDefinitionDTO.getOneStarLabel() != null) {
            scaleDefinition.setOneStarLabel(scaleDefinitionDTO.getOneStarLabel());
        }
        if (scaleDefinitionDTO.getTwoStarLabel() != null) {
            scaleDefinition.setTwoStarLabel(scaleDefinitionDTO.getTwoStarLabel());
        }
        if (scaleDefinitionDTO.getThreeStarLabel() != null) {
            scaleDefinition.setThreeStarLabel(scaleDefinitionDTO.getThreeStarLabel());
        }
        if (scaleDefinitionDTO.getFourStarLabel() != null) {
            scaleDefinition.setFourStarLabel(scaleDefinitionDTO.getFourStarLabel());
        }
        if (scaleDefinitionDTO.getFiveStarLabel() != null) {
            scaleDefinition.setFiveStarLabel(scaleDefinitionDTO.getFiveStarLabel());
        }
        if (scaleDefinitionDTO.isActive() != null) {
            scaleDefinition.setActive(scaleDefinitionDTO.isActive());
        }
        return scaleDefinition;
    }

    private ProjectFolder loadParent(UUID parentId, UUID organisationId) {
        ProjectFolder parent = projectFolderRepository.findById(parentId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("ProjectFolder", parentId));
        if (!parent.getOrganisationId().equals(organisationId)) {
            log.debug("Cannot create Folder: Parent Folder belongs too different Organisation than current User (user: <%s>, folder: <%s>)!",
                    organisationId, parent.getOrganisationId());
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.PARENT_FOLDER_OUTSIDE_ORGANIZATION);
        }
        if (!isProjectFolderIsOrInRestrictedFolder(parent, loadRootFolder(personalApiClient.getMe()))) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.PARENT_FOLDER_OUTSIDE_ROOT);
        }
        return parent;
    }

    private OrganisationRootFolder createRootFolder(UUID organisationId, String organisationName) {
        return new OrganisationRootFolder(organisationId,
                new ProjectFolder(null,
                        StringUtils.isEmpty(organisationName) ? "Root" : organisationName,
                        organisationId,
                        null));
    }

    private ProjectFolder createGetRootFolder(UUID organisationId, String organisationName) {
        return organisationRootFolderRepository.findById(organisationId)
                .orElseGet(() -> organisationRootFolderRepository.save(
                        createRootFolder(organisationId, organisationName)))
                .getFolder();
    }

    public ProjectFolder loadRootFolder(UserProfile me) {
        if (!me.getSettings().isAdmin()) {
            ProjectFolder rootFolder = restrictionRepository.findById(me.getUserId())
                    .map(ProjectFolderRestriction::getFolder)
                    .orElse(null);
            if (rootFolder != null) {
                return rootFolder;
            }
        }
        return createGetRootFolder(me.getOrganisationId(), me.getOrganisationName());
    }

    public ProjectFolder loadRootFolder(Organisation organisation) {
        return createGetRootFolder(organisation.getId(), organisation.getName());
    }

    private ProjectFolder load(UUID folderId) {
        return projectFolderRepository.findById(folderId)
                .orElseThrow(() -> ApiExceptionHelper.newResourceNotFoundError("ProjectFolder", folderId));
    }

    public ProjectFolder moveFolderAndProjects(UUID parentId, MoveDto dto) {

        UserProfile me = personalApiClient.getMe();

        if (!(me.getSettings().isAdmin() || me.getSettings().isProjectCreator())) {
            ApiExceptionHelper.throwNewForbiddenError(ErrorCodes.MOVE_PROJECT_FOLDER_PRIVILEGE);
        }

        ProjectFolder parent;
        if (parentId == null) {
            parent = loadRootFolder(me);
        } else {
            parent = loadParent(parentId, me.getOrganisationId());
        }

        if (dto.getEntities() != null) {
            dto.getEntities().forEach(projectId -> {
                Project project = getProject(projectId);
                canManageProject(me, project);
                if (!parent.getOrganisationId().equals(project.getOrganisationId())) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_PROJECT_OUTSIDE_ORGANIZATION,
                            project.getOrganisationId().toString(), parent.getOrganisationId().toString());
                }
                project.setParent(parent);
                projectRepository.save(project);
            });
        }
        if (dto.getFolders() != null) {
            dto.getFolders().forEach(folderId -> {
                ProjectFolder projectFolder = getFolder(folderId);
                canManageProjectFolder(me, projectFolder);
                if (projectFolder.getId().equals(parent.getId())) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_FOLDER_TO_ITSELF);
                }
                if (isTryingToMoveInChild(projectFolder, parent)) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_FOLDER_TO_CHILD);
                }
                if (!parent.getOrganisationId().equals(projectFolder.getOrganisationId())) {
                    throw ApiExceptionHelper.newBadRequestError(ErrorCodes.MOVE_FOLDER_OUTSIDE_ORGANIZATION,
                            projectFolder.getOrganisationId().toString(), parent.getOrganisationId().toString());
                }
                projectFolder.setParent(parent);
                projectFolderRepository.save(projectFolder);
            });
        }
        return getFolder(parent.getId());
    }

    private boolean isTryingToMoveInChild(ProjectFolder source, ProjectFolder destination) {
        if (destination.getParent() == null) {
            return false;
        }
        if (destination.getParent().getId().equals(source.getId())) {
            return true;
        }
        return isTryingToMoveInChild(source, destination.getParent());
    }

    private boolean isProjectFolderIsOrInRestrictedFolder(ProjectFolder projectFolder, ProjectFolder restrictedFolder) {
        if (restrictedFolder.getId().equals(projectFolder.getId())) {
            return true;
        }
        if (projectFolder.getParent() == null) {
            return false;
        }
        return isProjectFolderIsOrInRestrictedFolder(projectFolder.getParent(), restrictedFolder);
    }

    private void canManageProject(UserProfile me, Project project) {
        if (!(me.getSettings().isAdmin() || isAdminForProject(me, project)
                || userPrivileges.hasPrivileges(project.getId(), Collections.singletonList(Privilege.ManageProjectSettings)))) {
            throw ApiExceptionHelper.newUnPrivilegedError(Privilege.ManageProjectSettings);
        }
    }

    private boolean isAdminForProject(UserProfile me, Project project) {
        return me.getSettings().isProjectCreator()
                && (project.getCreatedBy().equals(me.getUserId()) || isProjectInRestrictedFolder(project.getId()));
    }

    public Boolean isProjectInRestrictedFolder(UUID projectId) {
        Project project = getProject(projectId);
        UserProfile me = personalApiClient.getMe();
        ProjectFolder userRootFolder = loadRootFolder(me);
        return isProjectFolderIsOrInRestrictedFolder(project.getParent(), userRootFolder);
    }

    public ProjectFolder getProjectPath(UUID projectId){
        Project project = getProject(projectId);
        UserProfile me = personalApiClient.getMe();
        ProjectFolder rootFolder = loadRootFolder(me);

        // If the folder path is not in the restricted area of the user
        if(!me.getOrganisationId().equals(project.getOrganisationId()) ||
                !isProjectFolderIsOrInRestrictedFolder(project.getParent(), rootFolder)){
            rootFolder.setSubFolders(Collections.emptyList());
            rootFolder.setChildren(Collections.singletonList(project));
            return rootFolder;
        }

        ProjectFolder currentFolder = project.getParent();
        currentFolder.setChildren(Collections.singletonList(project));
        currentFolder.setSubFolders(Collections.emptyList());

        // Find the root folder from the project
        while(!currentFolder.getId().equals(rootFolder.getId())){
            ProjectFolder previousFolder = currentFolder;
            currentFolder = currentFolder.getParent();
            currentFolder.setChildren(Collections.emptyList());
            currentFolder.setSubFolders(Collections.singletonList(previousFolder));
        }

        return currentFolder;
    }

    public SyncSizeDTO getProjectSyncSize(UUID projectId, String since, PostsFilter[] postsFilter){
        long issueSize = issueRequestApiClient.getProjectIssueSyncSize(projectId, since, Arrays.asList(postsFilter)).getSizeInBytes();
        Long projectSizeNull = projectRepository.getProjectSyncSize(projectId);
        long projectSize = projectSizeNull != null ? projectSizeNull : 0;
        return new SyncSizeDTO(issueSize + projectSize, SyncSizeDTO.SyncSizeType.Project, SizeUtils.getHumanReadable(issueSize + projectSize));
    }

    public Project unlinkBimplus(UUID projectId) {
        Project project = getProject(projectId);
        project.getBimplusConfig().unlink();
        planService.unlinkBimplusAttachmentsForPlansInProject(projectId);
        return projectRepository.save(project);
    }

    private static final long DELAY_BETWEEN_OPENWEATHER_REQUESTS = 66000;
    private static final long BUFFER_BETWEEN_OPENWEATHER_REQUESTS = (long) (DELAY_BETWEEN_OPENWEATHER_REQUESTS * 0.1);
    private static final long TOTAL_REQUEST_DELAY = DELAY_BETWEEN_OPENWEATHER_REQUESTS + BUFFER_BETWEEN_OPENWEATHER_REQUESTS;
    private static final String EVERY_DAY_AT_NOON = "0 0 12 * * *";

    @Scheduled(cron = EVERY_DAY_AT_NOON)
    public void retrieveWeatherData() {
        log.info("Starting retrieving weather data");
        Page<ProjectLocation> projectLocations = getLocations(0, 60);
        triggerRetrieveWeatherDataWithDelay(projectLocations.getContent());
        while (projectLocations.hasNext()) {
            projectLocations = projectRepository.getLocationsDistinctByZipCode(projectLocations.nextPageable());
            triggerRetrieveWeatherDataWithDelay(projectLocations.getContent());
        }
    }

    public Page<ProjectLocation> getLocations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return projectRepository.getLocationsDistinctByZipCode(pageable);
    }

    private void triggerRetrieveWeatherDataWithDelay(List<ProjectLocation> projectLocations) {
        for (ProjectLocation projectLocation : projectLocations) {
            lambdaService.retrieveWeatherData(projectLocation);
        }
        try {
            log.info("Request page finished, sleeping");
            Thread.sleep(TOTAL_REQUEST_DELAY);
        } catch (InterruptedException e) {
            log.error("Sleep got interrupted {}", e.getMessage());
            throw ApiExceptionHelper.newInternalServerError(e.getLocalizedMessage());
        }
    }
}
