package com.vikson.projects.api.controllers;

import com.vikson.projects.api.resources.CopyProjectDTO;
import com.vikson.projects.api.resources.MoveDto;
import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.api.resources.ProjectFolderDTO;
import com.vikson.projects.api.resources.ConstructionTypeDTO;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.service.ProjectService;
import com.vikson.projects.service.translators.ProjectTranslator;
import com.vikson.services.core.resources.Page;
import com.vikson.services.issues.resources.PostsFilter;
import com.vikson.services.issues.resources.SyncSizeDTO;
import com.vikson.projects.api.resources.values.ProjectExpandAttributes;
import com.vikson.projects.api.resources.values.ProjectPermissions;
import com.vikson.projects.api.resources.values.ProjectSort;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v2/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectTranslator translator;

    @ApiOperation(value = "Check Project Name Availability")
    @GetMapping(path = "/name")
    public boolean isProjectNameAvailable(@RequestParam(defaultValue = "") String name) {
        log.debug("GET /projects/name?name={}", name);
        return projectService.isProjectNameAvailable(name);
    }

    @ApiOperation(value = "Create Project", notes = "Required Attributes: none")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createProject(@RequestBody ProjectDTO body) {
        log.debug("POST /projects, Body: {}", body);
        return projectService.createNewProject(body).getId();
    }

    @ApiOperation(value = "Copy Project", notes = "Required Attributes: id")
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDTO copyProject(@RequestBody CopyProjectDTO body) {
        log.debug("PUT /projects, Body: {}", body);
        return translator.translate(projectService.copyProject(body));
    }

    @ApiOperation(value = "Get Project")
    @GetMapping(path = "/{projectId}")
    public ProjectDTO getProject(@PathVariable UUID projectId,
                                 @RequestParam(required = false, defaultValue = "true") boolean includeReferences,
                                 @RequestParam(defaultValue = "Categories,Permissions,LastModified") ProjectExpandAttributes[] expand) {
        log.debug("GET /projects/{}", projectId);
        return translator.translate(projectService.getProjectById(projectId), includeReferences, expand);
    }

    @ApiOperation(value = "List my Projects")
    @GetMapping
    public List<ProjectDTO> listMyProjects(@RequestParam(defaultValue = "Active") StateFilter stateFilter,
                                           @RequestParam(defaultValue = "") String search,
                                           @RequestParam(defaultValue = "Name") ProjectSort projectSort,
                                           @RequestParam(defaultValue = "Categories,Permissions,LastModified") ProjectExpandAttributes[] expand) {
        log.debug("GET /api/v2/projects");
        return translator.translateProjectsBulk(projectService.listMyProjects(search, stateFilter), expand)
                .stream()
                .sorted(projectSort.getComparator())
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "List my Projects paged")
    @GetMapping(path = "/paged")
    public Page<ProjectDTO> listMyProjectsPaged(@RequestParam(required = false, defaultValue = "0") int page,
                                                @RequestParam(required = false, defaultValue = "10") int size,
                                                @RequestParam(defaultValue = "Categories,Permissions,LastModified") ProjectExpandAttributes[] expand) {
        log.debug("GET /api/v2/projects/paged");
        return projectService.listMyProjectsPaged(page, size, expand);
    }

    @ApiOperation(value = "List my left projects")
    @GetMapping(path = "/left")
    public List<ProjectDTO> listMyLeftProjects(@RequestParam(defaultValue = "Active") StateFilter stateFilter,
                                               @RequestParam(defaultValue = "") String search,
                                               @RequestParam(defaultValue = "Name") ProjectSort projectSort,
                                               @RequestParam(defaultValue = "Categories,Permissions,LastModified") ProjectExpandAttributes[] expand,
                                               @RequestParam(required = false, defaultValue = "0") int page,
                                               @RequestParam(required = false, defaultValue = "10") int size,
                                               @RequestParam(required = false) String since) {
        log.debug("GET /api/v2/projects/left");
        return projectService.listMyLeftProjects(search, stateFilter, page, size, since)
                .stream()
                .map(project -> translator.translateMyProject(project, expand))
                .sorted(projectSort.getComparator())
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "List all Projects")
    @GetMapping(path = "/all")
    public List<ProjectDTO> listProjects() {
        log.debug("GET /projects/all");
        return projectService.getProjectsInOrganisation()
                .stream()
                .map(translator::translate)
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "List all Projects paged")
    @GetMapping(path = "/all/paged")
    public List<ProjectDTO> listProjectsPaged(@RequestParam(required = false, defaultValue = "0") int page,
                                              @RequestParam(required = false, defaultValue = "10") int size,
                                              @RequestParam(required = false, defaultValue = "Categories,Permissions,LastModified") ProjectExpandAttributes[] expand) {
        log.debug("GET /projects/all/paged");
        return projectService.getProjectsInOrganisation(page, size)
                .stream()
                .map(project -> translator.translate(project,expand))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Search Projects")
    @GetMapping(path = "/search")
    public List<ProjectDTO> searchProjects(@RequestParam(name = "search") String search,
                                           @RequestParam(required = false, defaultValue = "0") int page,
                                           @RequestParam(required = false, defaultValue = "4") int size) {
        log.debug("GET /projects/search?search={}", search);
        return projectService.searchProjects(search, page, size)
                .map(translator::translate)
                .getContent();
    }

    @ApiOperation(value = "Update Project", notes = "Required Attributes: none")
    @PatchMapping(path = "/{projectId}")
    public ProjectDTO update(@PathVariable UUID projectId, @RequestBody ProjectDTO body) {
        log.debug("PATCH /projects/{projectId}, Body: {}", projectId, body);
        body.setId(projectId);
        return translator.translate(projectService.updateProject(body));
    }

    @ApiOperation(value = "Unlink bimplus project")
    @DeleteMapping(path = "/bimplus/{projectId}/unlink")
    public ProjectDTO unlink(@PathVariable UUID projectId) {
        return translator.translate(projectService.unlinkBimplus(projectId));
    }

    @ApiOperation(value = "Move Project to Root")
    @DeleteMapping(path = "/{projectId}/parent")
    public void removeProjectParent(@PathVariable UUID projectId) {
        log.debug("DELETE /projects/{projectId}/parent", projectId);
        projectService.removeParentFolder(projectId);
    }

    @ApiOperation(value = "Download Project Logo")
    @GetMapping(path = "/{projectId}/logo")
    public void downloadLogo(@PathVariable UUID projectId, HttpServletResponse response, HttpServletRequest request) {
        log.debug("GET /projects/{}/logo", projectId);

        // In Case there is no Logo set!
        response.setStatus(HttpStatus.NO_CONTENT.value());
        projectService.downloadProjectLogo(projectId, response, request);
    }

    @ApiOperation(value = "Update Project Logo")
    @PutMapping(path = "/{projectId}/logo")
    public void changeLogo(@PathVariable UUID projectId, MultipartFile logo) {
        log.debug("PUT /projects/{}/logo, Body: {}", projectId, logo);
        projectService.setProjectLogo(projectId, logo);
    }

    @ApiOperation(value = "Remove Project Logo")
    @DeleteMapping(path = "/{projectId}/logo")
    public void removeLogo(@PathVariable UUID projectId) {
        log.debug("DELETE /projects/{}/logo", projectId);
        projectService.removeLogo(projectId);
    }

    @GetMapping(path = "/{projectId}/reportLogo")
    public void downloadReportLogo(@PathVariable UUID projectId, HttpServletResponse response, HttpServletRequest request) {
        // In Case there is no Logo set!
        response.setStatus(HttpStatus.NO_CONTENT.value());
        projectService.downloadReportLogo(projectId, response, request);
    }

    @PutMapping(path = "/{projectId}/reportLogo")
    public void changeReportLogo(@PathVariable UUID projectId, MultipartFile logo) {
        projectService.setProjectReportLogo(projectId, logo);
    }

    @DeleteMapping(path = "/{projectId}/reportLogo")
    public void removeReportLogo(@PathVariable UUID projectId) {
        projectService.removeReportLogo(projectId);
    }

    @ApiOperation(value = "Search Construction Types")
    @GetMapping(path = "/constructionTypes")
    public List<ConstructionTypeDTO> searchConstructionTypes(@RequestParam String search) {
        log.info("GET /projects/constructionTypes?search={}", search);
        return Collections.emptyList();
    }

    @ApiOperation(value = "Get User Permissions of a Project")
    @GetMapping(path = "/{projectId}/permissions")
    public List<ProjectPermissions> getUsersProjectPermissions(@PathVariable UUID projectId) {
        log.debug("GET /api/v2/{}/permissions", projectId);
        return translator.getPermissions(projectId);
    }

    @ApiOperation(value = "Move Project and Folders", notes = "Required Attributes: none")
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/folders/{folderId}")
    public ProjectFolderDTO moveProjectAndFolders(@PathVariable UUID folderId,
                                                  @RequestBody MoveDto dto) {
        log.debug("POST /projects/folders/{}", folderId);
        Assert.notNull(folderId, "Cannot move Projects/ProjectFolders to new ProjectFolder when ID is NULL!");
        return translator.translate(projectService.moveFolderAndProjects(folderId, dto));
    }

    @ApiOperation(value = "Move Projects and Folders to Root", notes = "Required Attributes: none")
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/folders")
    public ProjectFolderDTO removeProjectAndFolders(@RequestBody MoveDto dto) {
        log.debug("DELETE /projects/folders");
        log.info("Move project and folders to root folder");
        return translator.translate(projectService.moveFolderAndProjects(null, dto));
    }

    @ApiOperation(value = "Get Project Sync Size")
    @GetMapping(path = "/{projectId}/syncSize")
    public SyncSizeDTO getProject(@PathVariable UUID projectId,
                                  @RequestParam(required = false) String since,
                                  @RequestParam(required = false) PostsFilter[] filter) {
        if(filter == null){
            filter = new PostsFilter[0];
        }
        return projectService.getProjectSyncSize(projectId, since, filter);
    }

}
