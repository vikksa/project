package com.vikson.projects.api.controllers;

import com.vikson.projects.api.resources.ProjectTreePage;
import com.vikson.projects.api.resources.values.ProjectTreeSortAttributes;
import com.vikson.projects.api.resources.values.ProjectTreeStateFilter;
import com.vikson.projects.api.resources.values.ProjectTreeTypeFilter;
import com.vikson.projects.service.ProjectTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/projects-api/v3/folders")
public class ProjectTreeController {

    @Autowired
    private ProjectTreeService projectTreeService;

    @GetMapping("/children")
    public ProjectTreePage getChildren(@RequestParam(required = false, defaultValue = "") String search,
                                       @RequestParam(required = false, defaultValue = "Active") ProjectTreeStateFilter stateFilter,
                                       @RequestParam(required = false, defaultValue = "All") ProjectTreeTypeFilter typeFilter,
                                       @RequestParam(required = false, defaultValue = "50") int size,
                                       @RequestParam(required = false, defaultValue = "0") int page,
                                       @RequestParam(required = false, defaultValue = "Title") ProjectTreeSortAttributes sortBy,
                                       @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection) {
        return projectTreeService.getChildren(search, stateFilter, typeFilter, size, page, sortBy, sortDirection);
    }

    @GetMapping("/{folderId}/children")
    public ProjectTreePage getChildren(@PathVariable UUID folderId,
                                       @RequestParam(required = false, defaultValue = "Active") ProjectTreeStateFilter stateFilter,
                                       @RequestParam(required = false, defaultValue = "All") ProjectTreeTypeFilter typeFilter,
                                       @RequestParam(required = false, defaultValue = "50") int size,
                                       @RequestParam(required = false, defaultValue = "0") int page,
                                       @RequestParam(required = false, defaultValue = "Title") ProjectTreeSortAttributes sortBy,
                                       @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection) {
        return projectTreeService.getChildren(folderId, stateFilter, typeFilter, size, page, sortBy, sortDirection);
    }

}
