package com.vikson.projects.api.controllers;

import com.vikson.projects.api.resources.PlanDTO;
import com.vikson.projects.api.resources.PlanTreePage;
import com.vikson.projects.api.resources.values.PlanTreeSortAttributes;
import com.vikson.projects.api.resources.values.PlanTreeStateFilter;
import com.vikson.projects.api.resources.values.PlanTreeTypeFilter;
import com.vikson.projects.service.PlanService;
import com.vikson.projects.service.PlanTreeService;
import com.vikson.projects.service.translators.PlanTranslator;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/plans-api/v3")
public class PlanTreeController {

    @Autowired
    private PlanTreeService service;

    @Autowired
    private PlanService planService;
    @Autowired
    private PlanTranslator planTranslator;

    @ApiOperation(value = "Get Root Plans, Folders in a Project")
    @GetMapping(path = "/projects/{projectId}/children")
    public PlanTreePage getChildren(@PathVariable UUID projectId,
                                    @RequestParam(required = false, defaultValue = "Active") PlanTreeStateFilter stateFilter,
                                    @RequestParam(required = false, defaultValue = "All") PlanTreeTypeFilter typeFilter,
                                    @RequestParam(required = false) Instant since,
                                    @RequestParam(required = false, defaultValue = "") String search,
                                    @RequestParam(required = false, defaultValue = "50") int size,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "Title") PlanTreeSortAttributes sortBy,
                                    @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection) {
        return service.getRootChildren(projectId, search, stateFilter, typeFilter, since, page, size, sortBy, sortDirection);
    }

    @ApiOperation(value = "Get Root Plans, Folders in a Folder")
    @GetMapping(path = "/folders/{folderId}/children")
    public PlanTreePage getChildren(@PathVariable UUID folderId,
                                    @RequestParam(required = false, defaultValue = "Active") PlanTreeStateFilter stateFilter,
                                    @RequestParam(required = false, defaultValue = "All") PlanTreeTypeFilter typeFilter,
                                    @RequestParam(required = false) Instant since,
                                    @RequestParam(required = false, defaultValue = "50") int size,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "Title") PlanTreeSortAttributes sortBy,
                                    @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection) {
        return service.getChildren(folderId, stateFilter, typeFilter, since, page, size, sortBy, sortDirection);
    }


    @ApiOperation(value = "Update Plan in Bulk", notes = "Required Attributes: id")
    @PatchMapping(path = "/plans", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<PlanDTO> updatePlans(@RequestBody List<PlanDTO> resources) {
        return planService.doBulkUpdate(resources)
            .stream()
            .map(planTranslator::translate)
            .collect(Collectors.toList());
    }

}
