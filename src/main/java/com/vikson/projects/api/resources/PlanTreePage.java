package com.vikson.projects.api.resources;

import com.vikson.projects.model.PlanFolder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@ApiModel(value = "Plan Tree Page Resource")
public class PlanTreePage extends PageImpl<PlanTreeChildItem> {

    @ApiModelProperty(value = "Id of the project")
    private UUID id;
    @ApiModelProperty(value = "Id of the parent (Folder)")
    private UUID parentId;
    @ApiModelProperty(value = "Name of the project")
    private String name;
    @ApiModelProperty(value = "Number of plans in the project")
    private Long numberOfPlans;

    public PlanTreePage(PlanFolder parent, List<PlanTreeChildItem> content, Pageable pageable, long total,long numberOfPlans) {
        super(content, pageable, total);
        this.id = parent.getId();
        if(parent.getParent() != null)
            this.parentId = parent.getParent().getId();
        this.name = parent.getName();
        this.numberOfPlans = numberOfPlans;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public Long getNumberOfPlans() {
        return numberOfPlans;
    }
}
