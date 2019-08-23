package com.vikson.projects.api.controllers;

import com.vikson.projects.api.resources.ProjectFolderDTO;
import com.vikson.projects.api.resources.ProjectFolderRestrictionDTO;
import com.vikson.projects.api.resources.ProjectTreeDTO;
import com.vikson.projects.api.resources.values.ExternalProjectFilter;
import com.vikson.projects.api.resources.values.ProjectExpandAttributes;
import com.vikson.projects.api.resources.values.ProjectFolderExclude;
import com.vikson.projects.api.resources.values.ProjectSort;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.service.ProjectService;
import com.vikson.projects.service.translators.ProjectTranslator;
import com.vikson.services.core.resources.SortDirection;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v2/projectFolders")
public class ProjectFolderController {

    private static final Logger log = LoggerFactory.getLogger(ProjectFolderController.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectTranslator projectTranslator;

    @ApiOperation(value = "Create Project Folder", notes = "Required Attributes: none (But body should not be null)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectFolderDTO createNewFolder(@RequestBody ProjectFolderDTO body) {
        log.debug("POST /projectFolders, Body: {}", body);
        return projectTranslator.translate(projectService.createFolder(body));
    }

    @ApiOperation(value = "Get Project Folder")
    @GetMapping(path = "/{folderId}")
    public ProjectFolderDTO getFolder(@PathVariable UUID folderId,
                                      @RequestParam(defaultValue = "") ProjectFolderExclude[] exclude) {
        log.debug("GET /projectFolders/{}", folderId);
        return projectTranslator.translate(projectService.getFolder(folderId), exclude);
    }

    @ApiOperation(value = "Get Project Tree")
    @GetMapping
    public ProjectTreeDTO getProjectTree(@RequestParam(defaultValue = "All") ExternalProjectFilter projectFilter,
                                         @RequestParam(defaultValue = "Active") StateFilter stateFilter,
                                         @RequestParam(defaultValue = "") String search,
                                         @RequestParam(defaultValue = "Categories,Permissions,LastModified") ProjectExpandAttributes[] expand,
                                         @RequestParam(defaultValue = "ASC") SortDirection sortDirection,
                                         @RequestParam(defaultValue = "Name") ProjectSort sortBy) {
        log.debug("GET /projectFolders?search={}&stateFilter={}", search, stateFilter);
        return projectService.getTree(search.toLowerCase(), stateFilter, projectFilter, expand, sortDirection, sortBy);
    }

    @ApiOperation(value = "Update Project Folder", notes = "Required Attributes: none (But body should not be null)")
    @PatchMapping(path = "/{folderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectFolderDTO updateFolder(@PathVariable UUID folderId, @RequestBody ProjectFolderDTO body) {
        log.debug("PATCH /projectFolders/{}, Body: {}", folderId, body);
        body.setId(folderId);
        return projectTranslator.translate(projectService.updateFolder(body));
    }

    @ApiOperation(value = "Move Project Folder to Root")
    @DeleteMapping(path = "/{folderId}/parent")
    public ProjectFolderDTO removeParent(@PathVariable UUID folderId) {
        log.debug("DELETE /projectFolders/{}", folderId);
        return projectTranslator.translate(projectService.setRootAsParent(folderId));
    }

    @ApiOperation(value = "Restrict User to a Folder", notes = "Required Attributes: none")
    @PutMapping(path = "/restrictions")
    public void restrict(@RequestBody ProjectFolderRestrictionDTO restriction) {
        log.debug("PUT /api/v2/projectFolders/restrictions Body: {}", restriction);
        projectService.restrictUserToFolder(restriction);
    }

    @ApiOperation(value = "Get Root Folder")
    @GetMapping(path = "/root")
    public ProjectFolderDTO getRootFolder(@RequestParam UUID userId) {
        log.debug("GET /api/v2/projectFolders/root?userId={}", userId);
        return projectTranslator.translate(projectService.getRootFolder(userId));
    }

    @ApiOperation(value = "Check if Project is in the restricted folder")
    @GetMapping(path = "/contains")
    public Boolean isProjectInRestrictedFolder(@RequestParam UUID projectId) {
        log.debug("GET /api/v2/projectFolders/contains?projectId={}", projectId);
        return projectService.isProjectInRestrictedFolder(projectId);
    }

    @ApiOperation(value = "Delete Empty Project Folder")
    @DeleteMapping(path = "/{folderId}")
    public void deleteEmptyFolder(@PathVariable UUID folderId){
        log.debug("DELETE /{}", folderId);
        projectService.deleteFolder(folderId);
    }

    @ApiOperation(value = "Get the project folder path of a project")
    @GetMapping(path = "/projects/{projectId}/path")
    public ProjectFolderDTO getProjectPath(@PathVariable UUID projectId){
        return projectTranslator.translate(projectService.getProjectPath(projectId));
    }

}
