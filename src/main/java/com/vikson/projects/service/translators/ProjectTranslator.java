package com.vikson.projects.service.translators;

import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.api.resources.ProjectFolderDTO;
import com.vikson.projects.api.resources.values.ProjectExpandAttributes;
import com.vikson.projects.api.resources.ProjectLocationDTO;
import com.vikson.projects.api.resources.values.ProjectFolderExclude;
import com.vikson.projects.api.resources.values.ProjectPermissions;
import com.vikson.projects.help.threading.ContextAwareThread;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.ProjectFolder;
import com.vikson.projects.model.ProjectLocation;
import com.vikson.projects.repositories.PlanRepository;
import com.vikson.projects.repositories.ProjectRepository;
import com.vikson.services.issues.CategoryApiClient;
import com.vikson.services.issues.IssueRequestApiClient;
import com.vikson.services.users.MembershipsApiClient;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.UsersApiClient;
import com.vikson.services.users.resources.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Component
public class ProjectTranslator {

    private static final Logger log = LoggerFactory.getLogger(ProjectTranslator.class);

    private static final List<Privilege> privileges = asList(
        Privilege.ManageProjectSettings,
        Privilege.ViewProjectSettings,
        Privilege.CreatePlans,
        Privilege.ManagePlans,
        Privilege.ViewTeam,
        Privilege.ManageTeam,
        Privilege.ViewPins,
        Privilege.CreatePins,
        Privilege.ManagePins,
        Privilege.ViewTasks,
        Privilege.CreateTasks,
        Privilege.ManageTasks,
        Privilege.ViewMedia,
        Privilege.CreateMedia,
        Privilege.ManageMedia,
        Privilege.CreateAudios,
        Privilege.CreateVideos,
        Privilege.CreatePhotos,
        Privilege.CreateText,
        Privilege.ViewDatasets,
        Privilege.ManageDatasets,
        Privilege.ViewGroups,
        Privilege.CreateGroups,
        Privilege.ManageGroups,
        Privilege.CreateReports,
        Privilege.CloseTasks,
        Privilege.DelegateTasks,
        Privilege.ViewComments,
        Privilege.CreateComments,

        Privilege.ManagePins,
        Privilege.ManageTasks,
        Privilege.ManageMedia,
        Privilege.ManageComments,
        Privilege.ManageGroups
    );

    private static final Map<ProjectPermissions, List<Privilege>> permissionToPrivilegesMap = builtPermissionToPrivilegesMapping();

    @Autowired
    private UserPrivileges userPrivileges;

    @Autowired
    private CategoryApiClient categoryApiClient;

    @Autowired
    private PersonalApiClient personalApiClient;

    @Autowired
    private IssueRequestApiClient issueRequestApiClient;

    @Autowired
    private UsersApiClient usersApiClient;

    @Autowired
    private MembershipsApiClient membershipsApiClient;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public ProjectDTO translate(Project project, boolean includeReferences) {
        return translate(project, includeReferences, ProjectExpandAttributes.values());
    }

    public ProjectDTO translate(Project project, boolean includeReferences, ProjectExpandAttributes[] expand) {
        if (includeReferences) {
            return translate(project, expand);
        } else {
            ProjectDTO projectDTO = new ProjectDTO(project);
            projectDTO.setExternal(isExternalProject(project));
            setLastModified(project,projectDTO);
            return projectDTO;
        }
    }

    public ProjectDTO translateMyProject(Project project) {
        return translateMyProject(project, ProjectExpandAttributes.values());
    }

    public ProjectDTO translateMyProject(Project project, ProjectExpandAttributes[] expand) {
        expand = checkExpand(expand);
        checkSaveOrganisationName(project);

        ProjectDTO projectDTO = translate(project, expand, true);

        Role role = membershipsApiClient.getMyRoleIn(project.getId());
        projectDTO.setRole(role);
        projectDTO.setPermissions(mapPrivileges(role.getPrivileges()));
        return projectDTO;
    }

    public List<ProjectDTO> translateProjectsBulk(List<Project> projects, ProjectExpandAttributes[] expand){
        expand = checkExpand(expand);

        List<RoleMap> roleMapList = membershipsApiClient.getUsersMembershipRoleMap();
        ProjectExpandAttributes[] finalExpand = expand;
        return projects.stream().map(p -> {
            checkSaveOrganisationName(p);
            ProjectDTO projectDTO = translate(p, finalExpand, true);
            Role role = getRoleFromMap(roleMapList, p.getId());
            projectDTO.setRole(role);
            projectDTO.setPermissions(mapPrivileges(role.getPrivileges()));
            return projectDTO;
        }).collect(Collectors.toList());
    }

    private ProjectExpandAttributes[] checkExpand(ProjectExpandAttributes[] expand){
        if(expand != null && expand.length > 0) {
            Set<ProjectExpandAttributes> expandSet = Arrays.stream(expand).collect(Collectors.toSet());
            if(expandSet.remove(ProjectExpandAttributes.Permissions))
                expand = expandSet.toArray(new ProjectExpandAttributes[0]);
        }
        return expand;
    }

    private void checkSaveOrganisationName(Project project){
        // Load name of organisation, if not present.
        if(StringUtils.isEmpty(project.getOrganisationName())) {
            try {
                project.setOrganisationName(usersApiClient.getOrganisation(project.getOrganisationId()).getName());
                projectRepository.save(project);
            } catch (Exception e) {
                log.error(String.format("Could not load organisation name for project %s.", project.getId()), e);
            }
        }
    }

    private Role getRoleFromMap(List<RoleMap> roleMapList, UUID projectId){
        Role role = new Role();
        Optional<RoleMap> roleMap = roleMapList.stream().filter(r -> r.getProjectId().equals(projectId)).findFirst();
        if(roleMap.isPresent()) {
            role.setName(roleMap.get().getName());
            role.setType(roleMap.get().getRole());
        } else {
            role.setName(RoleType.SubContractor.toString());
            role.setType(RoleType.SubContractor);
            log.warn(String.format("Could not load proper role for project %s and user %s", projectId, personalApiClient.getMe().getUserId()));
        }
        role.setPrivileges(getPrivilegesForRoleType(role.getType()));
        return role;
    }

    private Set<Privilege> getPrivilegesForRoleType(RoleType roleType){
        Optional<DefaultRole> optionalRole = membershipsApiClient.getDefaultRoles().stream()
                .filter(role -> role.getRoleType().equals(roleType))
                .findFirst();
        if(!optionalRole.isPresent()){
            optionalRole = membershipsApiClient.getDefaultRoles().stream().filter(role -> role.getRoleType().equals(RoleType.SubContractor)).findFirst();
            if(!optionalRole.isPresent()){
                throw new RuntimeException("No roles found!");
            }
        }
        return optionalRole.get().getPrivileges();
    }

    public ProjectDTO translate(Project project) {
        return translate(project, ProjectExpandAttributes.values());
    }

    public ProjectDTO translate(Project project, ProjectExpandAttributes[] expand) {
        return translate(project, expand, true);
    }

    public ProjectDTO translate(Project project, ProjectExpandAttributes[] expand, boolean includePermissions) {
        Set<ProjectExpandAttributes> expandAttributes = expand != null?
                Arrays.stream(expand).collect(Collectors.toSet()) : Collections.emptyNavigableSet();

        ProjectDTO projectDTO = new ProjectDTO(project);


        if(expandAttributes.contains(ProjectExpandAttributes.Permissions))
            projectDTO.setPermissions(getPermissions(project.getId()));
        if(expandAttributes.contains(ProjectExpandAttributes.Categories))
            projectDTO.setCategoryList(categoryApiClient.getPinCategories(project.getId()));
        if(expandAttributes.contains(ProjectExpandAttributes.NumberOfPlans))
            projectDTO.setNumberOfPlans(planRepository.countPlansByProject(project.getId()));

        projectDTO.setExternal(isExternalProject(project));

        return projectDTO;
    }

    private void setLastModified(Project project, ProjectDTO projectDTO) {
        if(project.getLastModified() == null || project.getLastModified().isBefore(ZonedDateTime.now().minusMinutes(5))) {
            ContextAwareThread.runAsync(() -> {
                try {
                    ZonedDateTime lastActivity = issueRequestApiClient.getLastActivity(project.getId());
                    if (lastActivity != null) {
                        projectRepository.setLastModified(project.getId(), lastActivity);
                    }
                } catch (Exception e) {
                    //Ignore
                }
            });
        }
        if(project.getLastModified() != null)
            projectDTO.setLastModified(project.getLastModified().toInstant());
    }

    public ProjectDTO setOrganisationName(ProjectDTO projectDTO){
        Organisation organisation = usersApiClient.getOrganisation(projectDTO.getOrganisationId());
        projectDTO.setOrganisationName(organisation.getName());
        return projectDTO;
    }

    public ProjectFolderDTO translate(ProjectFolder folder) {
        return new ProjectFolderDTO(folder, this::translate, null);
    }
    public ProjectFolderDTO translate(ProjectFolder folder, ProjectFolderExclude[] excludes) {
        return new ProjectFolderDTO(folder, this::translate, null, excludes);
    }

    public ProjectLocationDTO translate(ProjectLocation projectLocation) {
        return new ProjectLocationDTO(projectLocation);
    }

    public List<ProjectPermissions> getPermissions(UUID projectId) {
        PrivilegeCheck check = userPrivileges.checkPrivilege(projectId, privileges, true);
        if (!check.isCheck()) {
            return new ArrayList<>();
        }
        List<Privilege> grantedPrivileges = Arrays.asList(check.getPrivileges());

        return mapPrivileges(grantedPrivileges);
    }

    @NotNull
    private List<ProjectPermissions> mapPrivileges(Collection<Privilege> grantedPrivileges) {
        return Arrays.stream(ProjectPermissions.values())
            .filter(p -> containsAny(grantedPrivileges, permissionToPrivilegesMap.getOrDefault(p, Collections.emptyList())))
            .collect(Collectors.toList());
    }


    private static final boolean containsAny(Collection<Privilege> base, List<Privilege> check) {
        return base.stream().anyMatch(check::contains);
    }

    private static Map<ProjectPermissions, List<Privilege>> builtPermissionToPrivilegesMapping() {
        Map<ProjectPermissions, List<Privilege>> map = new EnumMap<>(ProjectPermissions.class);
        map.put(ProjectPermissions.ViewSettings, asList(Privilege.ViewProjectSettings, Privilege.ManageProjectSettings));
        map.put(ProjectPermissions.EditSettings, singletonList(Privilege.ManageProjectSettings));
        map.put(ProjectPermissions.CreatePlan, asList(Privilege.CreatePlans, Privilege.ManagePlans));
        map.put(ProjectPermissions.CreatePlanFolder,asList(Privilege.CreatePlans, Privilege.ManagePlans));
        map.put(ProjectPermissions.ViewTeamMember, asList(Privilege.ViewTeam, Privilege.ManageTeam));
        map.put(ProjectPermissions.EditTeamMember, singletonList(Privilege.ManageTeam));
        map.put(ProjectPermissions.ViewPins, asList(Privilege.ViewPins, Privilege.ManagePins, Privilege.CreatePins));
        map.put(ProjectPermissions.CreatePins, asList(Privilege.CreatePins, Privilege.ManagePins));
        map.put(ProjectPermissions.ViewTasks, asList(Privilege.ViewTasks, Privilege.ManageTasks, Privilege.CreateTasks));
        map.put(ProjectPermissions.CreateTasks, asList(Privilege.ManageTasks, Privilege.CreateTasks));
        map.put(ProjectPermissions.ViewComments, asList(Privilege.ViewComments, Privilege.CreateComments, Privilege.ManageComments));
        map.put(ProjectPermissions.CreateComments, asList(Privilege.CreateComments, Privilege.ManageComments));
        map.put(ProjectPermissions.ViewMedia, asList(Privilege.ViewMedia, Privilege.ViewPhotos, Privilege.ViewAudios, Privilege.ViewVideos, Privilege.ViewText, Privilege.ViewVideos, Privilege.ManageMedia, Privilege.CreateMedia, Privilege.CreateAudios, Privilege.CreateVideos, Privilege.CreateText, Privilege.CreatePhotos));
        map.put(ProjectPermissions.CreateVisuals, asList(Privilege.CreateMedia, Privilege.CreatePhotos, Privilege.CreateVideos, Privilege.ManageMedia));
        map.put(ProjectPermissions.CreateAudios, asList(Privilege.CreateAudios, Privilege.CreateMedia, Privilege.ManageMedia));
        map.put(ProjectPermissions.CreateNotes, asList(Privilege.ManageMedia, Privilege.CreateMedia, Privilege.CreateText));
        map.put(ProjectPermissions.CreateDocuments, asList(Privilege.CreateMedia, Privilege.ManageMedia));
        map.put(ProjectPermissions.ViewDataSets, asList(Privilege.ManageDatasets, Privilege.CreateDatasets, Privilege.ViewDatasets));
        map.put(ProjectPermissions.CreateDataSets, asList(Privilege.CreateDatasets, Privilege.ManageDatasets));
        map.put(ProjectPermissions.ViewGroups, asList(Privilege.ManageGroups, Privilege.CreateGroups, Privilege.ViewGroups));
        map.put(ProjectPermissions.CreateGroups, asList(Privilege.ManageGroups, Privilege.CreateGroups));
        map.put(ProjectPermissions.CreateReports, asList(Privilege.CreateReports, Privilege.ManageReports));

        map.put(ProjectPermissions.ManagePins, singletonList(Privilege.ManagePins));
        map.put(ProjectPermissions.ManageTasks, singletonList(Privilege.ManageTasks));
        map.put(ProjectPermissions.ManagePosts, singletonList(Privilege.ManageMedia));
        map.put(ProjectPermissions.ManageComments, singletonList(Privilege.ManageComments));
        map.put(ProjectPermissions.ManageGroups, singletonList(Privilege.ManageGroups));
        return map;
    }

    private boolean isExternalProject(Project project) {
        return !project.getOrganisationId().equals(personalApiClient.getMe().getOrganisationId());
    }

}
