package com.vikson.projects.service;

import com.vikson.projects.model.ProjectFolder;
import com.vikson.projects.api.resources.ProjectTreeChildItem;
import com.vikson.projects.api.resources.ProjectTreePage;
import com.vikson.projects.api.resources.values.ProjectTreeSortAttributes;
import com.vikson.projects.api.resources.values.ProjectTreeStateFilter;
import com.vikson.projects.api.resources.values.ProjectTreeTypeFilter;
import com.vikson.projects.util.JDBCUtils;
import com.vikson.projects.util.ResourceUtils;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.resources.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectTreeService {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PersonalApiClient personalApiClient;

    private final String selectQueryPattern;
    private final String countQuery;
    private final String searchQueryPattern;
    private final String searchCountQuery;

    public ProjectTreeService() {
        selectQueryPattern = ResourceUtils.readResourceAsString("queries/projectree/select_query.sql");
        countQuery = ResourceUtils.readResourceAsString("queries/projectree/count_query.sql");
        searchQueryPattern = ResourceUtils.readResourceAsString("queries/projectree/search_query.sql");
        searchCountQuery = ResourceUtils.readResourceAsString("queries/projectree/search_count.sql");
    }

    @Transactional(readOnly = true)
    public ProjectTreePage getChildren(String search,
                                       ProjectTreeStateFilter filter,
                                       ProjectTreeTypeFilter typeFilter,
                                       int pageSize,
                                       int page,
                                       ProjectTreeSortAttributes sortBy,
                                       Sort.Direction sortDirection) {
        UserProfile me = personalApiClient.getMe();
        ProjectFolder rootFolder = projectService.getRootFolder();

        if (!(me.getSettings().isAdmin() || me.getSettings().isProjectCreator())) {
            return new ProjectTreePage(rootFolder, Collections.emptyList(), PageRequest.of(page, pageSize), 0);
        }
        if (StringUtils.isEmpty(search)) {
            return getChildren(rootFolder.getId(), filter, typeFilter, pageSize, page, sortBy, sortDirection);
        } else {
            return search(search.trim(), filter, typeFilter, pageSize, page, sortBy, sortDirection);
        }
    }

    private ProjectTreePage search(String search,
                                   ProjectTreeStateFilter filter,
                                   ProjectTreeTypeFilter typeFilter,
                                   int pageSize,
                                   int page,
                                   ProjectTreeSortAttributes sortBy,
                                   Sort.Direction sortDirection) {
        ProjectFolder rootFolder = projectService.getRootFolder();
        search = "%" + search + "%";
        return new ProjectTreePage(rootFolder,
                namedParameterJdbcTemplate.query(String.format(searchQueryPattern, sortBy.column(), sortDirection),
                        buildSetter(rootFolder.getId(), filter, typeFilter, search, page * pageSize, pageSize),
                        ProjectTreeChildItem::from),
                PageRequest.of(page, pageSize),
            namedParameterJdbcTemplate.query(searchCountQuery, buildSetter(rootFolder.getId(), filter, typeFilter, search,null,null), this::extractLong)
        );
    }

    @Transactional(readOnly = true)
    public ProjectTreePage getChildren(UUID folderId,
                                       ProjectTreeStateFilter filter,
                                       ProjectTreeTypeFilter typeFilter,
                                       int pageSize,
                                       int page,
                                       ProjectTreeSortAttributes sortBy,
                                       Sort.Direction sortDirection) {
        ProjectFolder folder = projectService.getFolder(folderId);
        return new ProjectTreePage(folder,
                namedParameterJdbcTemplate.query(String.format(selectQueryPattern, sortBy.column(), sortDirection),
                        buildSetter(folderId, filter, typeFilter, null,page * pageSize, pageSize), ProjectTreeChildItem::from),
                PageRequest.of(page, pageSize, sortDirection, sortBy.toString()),
            namedParameterJdbcTemplate.query(countQuery, buildSetter(folderId, filter, typeFilter, null,null, null), this::extractLong));
    }

    private Map<String,Object> buildSetter(UUID folderId, ProjectTreeStateFilter filter, ProjectTreeTypeFilter typeFilter, String search, Integer offset, Integer limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", folderId);
        params.put("search",search);
        setStateFilter(filter, params);
        try {
            params.put("typeFilter", JDBCUtils.createArray(namedParameterJdbcTemplate.getJdbcTemplate(), "varchar", (Object[]) typeFilter.allowedTypes()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        params.put("offsetValue", offset);
        params.put("limitValue", limit);
        return params;
    }

    private void setStateFilter(ProjectTreeStateFilter stateFilter, Map<String, Object> params) {
        boolean aBool = true, bBool = false;
        if (stateFilter == ProjectTreeStateFilter.Active)
            bBool = true;
        if (stateFilter == ProjectTreeStateFilter.Inactive)
            aBool = false;
        params.put("stateFilterActive", aBool);
        params.put("stateFilterInactive", bBool);
    }

    private long extractLong(ResultSet resultSet) throws SQLException {
        resultSet.next();
        return resultSet.getLong(1);
    }

}
