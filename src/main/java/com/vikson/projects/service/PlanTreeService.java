package com.vikson.projects.service;

import com.vikson.projects.exceptions.ErrorCodes;
import com.vikson.projects.model.PlanFolder;
import com.vikson.projects.api.resources.PlanTreeChildItem;
import com.vikson.projects.api.resources.PlanTreePage;
import com.vikson.projects.api.resources.values.PlanTreeSortAttributes;
import com.vikson.projects.api.resources.values.PlanTreeStateFilter;
import com.vikson.projects.api.resources.values.PlanTreeTypeFilter;
import com.vikson.projects.util.JDBCUtils;
import com.vikson.projects.util.ResourceUtils;
import com.vikson.services.issues.IssueRequestApiClient;
import com.vikson.services.issues.TaskApiClient;
import com.vikson.services.issues.resources.PlanPins;
import com.vikson.services.issues.resources.PlanTaskDTO;
import com.vikson.services.users.PrivilegeApiClient;
import com.vikson.projects.exceptions.ApiExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlanTreeService {
    private static final Logger log = LoggerFactory.getLogger(PlanTreeService.class);

    private final String selectQuery;
    private final String countQuery;
    private final String searchQuery;
    private final String countSearchQuery;
    private final String subcontractorsQuery;
    private final String subcontractorsCount;
    private final String plansOnlyQuery;
    private final String plansOnlyCount;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ProjectService projectService;
    @Autowired
    private PlanService planService;
    @Autowired
    private PrivilegeUtils privilegeUtils;
    @Autowired
    private PrivilegeApiClient privilegeApiClient;

    @Autowired
    private TaskApiClient taskApiClient;

    @Autowired
    private IssueRequestApiClient issueApiClient;

    public PlanTreeService() {
        selectQuery = ResourceUtils.readResourceAsString("queries/plantree/select_query.sql");
        countQuery = ResourceUtils.readResourceAsString("queries/plantree/count_query.sql");
        searchQuery = ResourceUtils.readResourceAsString("queries/plantree/search_query.sql");
        countSearchQuery = ResourceUtils.readResourceAsString("queries/plantree/search_count_query.sql");
        subcontractorsQuery = ResourceUtils.readResourceAsString("queries/plantree/subcontractors_select.sql");
        subcontractorsCount = ResourceUtils.readResourceAsString("queries/plantree/subcontractors_count.sql");
        plansOnlyQuery = ResourceUtils.readResourceAsString("queries/plantree/plans_only_query.sql");
        plansOnlyCount = ResourceUtils.readResourceAsString("queries/plantree/plans_only_count.sql");
    }

    @Transactional(readOnly = true)
    public PlanTreePage getRootChildren(UUID projectId,
                                        String search,
                                        PlanTreeStateFilter stateFilter,
                                        PlanTreeTypeFilter typeFilter,
                                        Instant sinceFilter,
                                        int page,
                                        int pageSize,
                                        PlanTreeSortAttributes sortBy,
                                        Sort.Direction sortDirection) {
        if (!privilegeApiClient.isMember(projectId)) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, projectId.toString());
        }
        if(!privilegeUtils.canViewAllPlans(projectId)) {
            return loadPlansForSubcontractor(projectId, stateFilter, search, sinceFilter, page, pageSize, sortBy, sortDirection);
        }
        if(typeFilter == PlanTreeTypeFilter.Plans) {
            return loadPlansOnly(projectId, stateFilter, search, sinceFilter, page, pageSize, sortBy, sortDirection);
        }
        if (StringUtils.isEmpty(search)) {
            UUID rootFolderId = projectService.getProject(projectId).getRootFolder().getId();
            return getChildren(rootFolderId, stateFilter, typeFilter, sinceFilter, page, pageSize, sortBy, sortDirection);
        }
        return search(projectId, search, stateFilter, typeFilter, sinceFilter, page, pageSize, sortBy, sortDirection);
    }

    private PlanTreePage search(UUID projectId,
                                String search,
                                PlanTreeStateFilter stateFilter,
                                PlanTreeTypeFilter typeFilter,
                                Instant sinceFilter,
                                int page,
                                int pageSize,
                                PlanTreeSortAttributes sortBy,
                                Sort.Direction sortDirection) {
        search = "%" + search + "%";
        PlanFolder folder = projectService.getProject(projectId).getRootFolder();
        Map<String,Integer> taskCounts = new HashMap<>();
        getPlanTaskListWithFallback(projectId)
                .forEach(planTaskDTO -> taskCounts.put(planTaskDTO.getPlanId().toString(), planTaskDTO.getOpenTasks()));

        // Get Open Pin List
        List<PlanPins> planPins = getPlanPinListWithFallback(folder.getProject().getId());
        Map<String,Integer> pinsCounts = new HashMap<>();
        planPins.forEach(planPinsDTO -> pinsCounts.put(planPinsDTO.getPlanId().toString(), planPinsDTO.getOpenPins()));

        boolean canManagePlans = privilegeUtils.canManagePlans(projectId);

        return new PlanTreePage(folder,
                namedParameterJdbcTemplate.query(String.format(searchQuery, sortBy.colum(), sortDirection),
                        buildSetter(folder.getId(), stateFilter, typeFilter, sinceFilter, search, page * pageSize, pageSize),
                        (resultSet, i) -> PlanTreeChildItem.from(resultSet, taskCounts.getOrDefault(resultSet.getString("id"),0),
                                pinsCounts.getOrDefault(resultSet.getString("id"),0), canManagePlans)),
                PageRequest.of(page, pageSize, sortDirection, sortBy.toString()),
                namedParameterJdbcTemplate.query(countSearchQuery,
                        buildSetter(folder.getId(), stateFilter, typeFilter, sinceFilter, search, null, null),
                        this::extractLong), planCount(folder.getId(), search, stateFilter, sinceFilter));
    }

    private Long planCount(UUID folderId, String search,
                           PlanTreeStateFilter stateFilter,
                           Instant sinceFilter) {
        return namedParameterJdbcTemplate.query(countSearchQuery,
            buildSetter(folderId, stateFilter, PlanTreeTypeFilter.Plans, sinceFilter, search, null, null),
            this::extractLong);
    }

    @Transactional(readOnly = true)
    public PlanTreePage getChildren(UUID folderId,
                                    PlanTreeStateFilter stateFilter,
                                    PlanTreeTypeFilter typeFilter,
                                    Instant sinceFilter,
                                    int page,
                                    int pageSize,
                                    PlanTreeSortAttributes sortBy,
                                    Sort.Direction sortDirection) {
        PlanFolder folder = planService.getPlanFolder(folderId);
        if (!privilegeApiClient.isMember(folder.getProject().getId())) {
            throw ApiExceptionHelper.newForbiddenError(ErrorCodes.NOT_A_PROJECT_MEMBER, folder.getProject().getId().toString());
        }

        Map<String, Integer> taskCounts = new HashMap<>();
        getPlanTaskListWithFallback(folder.getProject().getId())
                .forEach(planTaskDTO -> taskCounts.put(planTaskDTO.getPlanId().toString(), planTaskDTO.getOpenTasks()));

        // Get Open Pin List
        List<PlanPins> planPins = getPlanPinListWithFallback(folder.getProject().getId());
        Map<String,Integer> pinsCounts = new HashMap<>();
        planPins.forEach(planPinsDTO -> pinsCounts.put(planPinsDTO.getPlanId().toString(), planPinsDTO.getOpenPins()));

        boolean canManagePlans = privilegeUtils.canManagePlans(folder.getProject().getId());

        return new PlanTreePage(folder,
                namedParameterJdbcTemplate.query(String.format(selectQuery, sortBy.colum(), sortDirection),
                        buildSetter(folderId, stateFilter, typeFilter, sinceFilter, null, page * pageSize, pageSize),
                        (resultSet, i) -> PlanTreeChildItem.from(resultSet, taskCounts.getOrDefault(resultSet.getString("id"),0),
                                pinsCounts.getOrDefault(resultSet.getString("id"),0), canManagePlans)),
                PageRequest.of(page, pageSize, sortDirection, sortBy.colum()),
                namedParameterJdbcTemplate.query(countQuery, buildSetter(folderId, stateFilter, typeFilter, sinceFilter, null, null, null), this::extractLong),
                planCount(folderId, "%%", stateFilter, sinceFilter));
    }

    private PlanTreePage loadPlansForSubcontractor(UUID projectId,
                                                   PlanTreeStateFilter stateFilter,
                                                   String search,
                                                   Instant sinceFilter,
                                                   int page,
                                                   int pageSize,
                                                   PlanTreeSortAttributes sortBy,
                                                   Sort.Direction sortDirection) {
        // Get Open Task List
        List<PlanTaskDTO> planTasks = getPlanTaskListWithFallback(projectId);
        // Map Plan Id to Number of Open Tasks
        Map<String,Integer> taskCounts = new HashMap<>();
        planTasks.forEach(planTaskDTO -> taskCounts.put(planTaskDTO.getPlanId().toString(), planTaskDTO.getOpenTasks()));

        // Get Open Pin List
        List<PlanPins> planPins = getPlanPinListWithFallback(projectId);
        Map<String,Integer> pinsCounts = new HashMap<>();
        planPins.forEach(planPinsDTO -> pinsCounts.put(planPinsDTO.getPlanId().toString(), planPinsDTO.getOpenPins()));

        // Get permission check
        boolean canManagePlans = privilegeUtils.canManagePlans(projectId);

        // Get Root Folder
        PlanFolder planFolder = projectService.getProject(projectId).getRootFolder();
        // Build Parameter map with everything
        Map<String, Object> paramMap = buildParameterMap(projectId,
                planTasks.stream().map(PlanTaskDTO::getPlanId).collect(Collectors.toList()),
                stateFilter, sinceFilter, search, page * pageSize, pageSize);
        // Build new Page with the help of the parameter list
        Long planCount = namedParameterJdbcTemplate.query(subcontractorsCount, paramMap, this::extractLong);
        return new PlanTreePage(planFolder,
                namedParameterJdbcTemplate.query(
                        String.format(subcontractorsQuery, sortBy.colum(), sortDirection),
                        paramMap, (resultSet, i) -> PlanTreeChildItem.from(resultSet, taskCounts.getOrDefault(resultSet.getString("id"),0),
                                pinsCounts.getOrDefault(resultSet.getString("id"),0), canManagePlans)),
                PageRequest.of(page, pageSize, sortDirection, sortBy.toString()),
            planCount, planCount);
    }

    private PlanTreePage loadPlansOnly(UUID projectId,
                                       PlanTreeStateFilter stateFilter,
                                       String search,
                                       Instant sinceFilter,
                                       int page,
                                       int pageSize,
                                       PlanTreeSortAttributes sortBy,
                                       Sort.Direction sortDirection) {
        PlanFolder planFolder = projectService.getProject(projectId).getRootFolder();
        Map<String,Integer> taskCounts = new HashMap<>();
        getPlanTaskListWithFallback(projectId)
                .forEach(planTaskDTO -> taskCounts.put(planTaskDTO.getPlanId().toString(), planTaskDTO.getOpenTasks()));
        boolean canManagePlans = privilegeUtils.canManagePlans(projectId);

        // Get Open Pin List
        List<PlanPins> planPins = getPlanPinListWithFallback(projectId);
        Map<String,Integer> pinsCounts = new HashMap<>();
        planPins.forEach(planPinsDTO -> pinsCounts.put(planPinsDTO.getPlanId().toString(), planPinsDTO.getOpenPins()));

        Map<String, Object> paramMap = buildParameterMap(projectId, Collections.emptyList(),
                stateFilter, sinceFilter, search, page * pageSize, pageSize);
        //Plan Count
        Long totalCount = namedParameterJdbcTemplate.query(plansOnlyCount, paramMap, this::extractLong);
        return new PlanTreePage(planFolder,
                namedParameterJdbcTemplate.query(String.format(plansOnlyQuery, sortBy.colum(), sortDirection),
                        paramMap, (resultSet, i) -> PlanTreeChildItem.from(resultSet, taskCounts.getOrDefault(resultSet.getString("id"),0),
                                pinsCounts.getOrDefault(resultSet.getString("id"),0), canManagePlans)),
                PageRequest.of(page, pageSize, sortDirection, sortBy.colum()),
            totalCount,totalCount);
    }

    private List<PlanTaskDTO> getPlanTaskListWithFallback(UUID projectId){
        try {
            return taskApiClient.getPlansWithOpenTasks(projectId);
        } catch (Exception e){
            log.error(String.format("Get Open Tasks Count of a plan failed. %s", e.getMessage()));
            return Collections.emptyList();
        }
    }

    private List<PlanPins> getPlanPinListWithFallback(UUID projectId){
        try {
            return issueApiClient.getPlansWithOpenPins(projectId);
        } catch (Exception e){
            log.error(String.format("Get Open Pins Count of a plan failed. %s", e.getMessage()));
            return Collections.emptyList();
        }
    }

    private Map<String, Object> buildSetter(UUID parentId, PlanTreeStateFilter stateFilter, PlanTreeTypeFilter typeFilter, Instant sinceFilter, String search, Integer offset, Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", parentId);
        params.put("search", search);
        setStateFilter(stateFilter, params);
        setSinceFilter(sinceFilter, params);
        try {
            params.put("typeFilter", JDBCUtils.createArray(namedParameterJdbcTemplate.getJdbcTemplate(), "varchar", (Object[]) typeFilter.allowedTypes()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        params.put("offsetValue", offset);
        params.put("limitValue", limit);
        return params;
    }

    private void setStateFilter(PlanTreeStateFilter stateFilter, Map<String, Object> params) {
        boolean aBool = true, bBool = false;
        if (stateFilter == PlanTreeStateFilter.Active)
            bBool = true;
        if (stateFilter == PlanTreeStateFilter.Inactive)
            aBool = false;
        params.put("stateFilterActive", aBool);
        params.put("stateFilterInactive", bBool);
    }

    private Map<String,Object> buildParameterMap(UUID projectId, List<UUID> planIds, PlanTreeStateFilter stateFilter, Instant sinceFilter, String search, Integer offset, Integer limit) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("project", projectId);
            params.put("planIds", JDBCUtils.createArray(namedParameterJdbcTemplate, "uuid", planIds.toArray()));
            if(stateFilter == PlanTreeStateFilter.Active)
                params.put("active", JDBCUtils.createArray(namedParameterJdbcTemplate, "bool", true, true));
            else if(stateFilter == PlanTreeStateFilter.Inactive)
                params.put("active", JDBCUtils.createArray(namedParameterJdbcTemplate, "bool", false, false));
            else params.put("active", JDBCUtils.createArray(namedParameterJdbcTemplate, "bool", true, false));
            params.put("since", sinceFilter != null? Timestamp.from(sinceFilter) : Timestamp.valueOf("2000-01-01 00:00:00.000"));
            params.put("offset", offset);
            params.put("limit", limit);
            params.put("search", "%" + search + "%");
            return params;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setSinceFilter(Instant sinceFilter, Map<String, Object> params) {
        if (sinceFilter == null)
            sinceFilter = Instant.now().minusSeconds(60 * 60 * 24 * 365 * 10);
        Timestamp timestamp = Timestamp.from(sinceFilter);
        params.put("sinceFilter", timestamp);
    }

    private long extractLong(ResultSet resultSet) throws SQLException {
        resultSet.next();
        return resultSet.getLong(1);
    }
}
