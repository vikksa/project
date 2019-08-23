package com.vikson.projects.api.resources;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(value = "Construction Type Resource")
public class ConstructionTypeDTO {

    @ApiModelProperty(value = "Id of the construction (to be deprecated)")
    private UUID id = UUID.randomUUID();
    @ApiModelProperty(value = "Type of the construction")
    private String type;

    public ConstructionTypeDTO() {
    }

    public ConstructionTypeDTO(String type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstructionTypeDTO that = (ConstructionTypeDTO) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
