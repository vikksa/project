package com.vikson.projects.api.resources;

import com.vikson.projects.model.ProjectLocation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Project Location Resource")
public class ProjectLocationDTO {

    @ApiModelProperty(value = "Zip Code")
    private String zipCode;
    @ApiModelProperty(value = "Country Code")
    private String cc;

    public ProjectLocationDTO() {
    }

    public ProjectLocationDTO(ProjectLocation location) {
        this.zipCode = location.getZipCode();
        this.cc = location.getCc();
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectLocationDTO that = (ProjectLocationDTO) o;

        if (zipCode != null ? !zipCode.equals(that.zipCode) : that.zipCode != null) return false;
        return cc != null ? cc.equals(that.cc) : that.cc == null;
    }

    @Override
    public int hashCode() {
        int result = zipCode != null ? zipCode.hashCode() : 0;
        result = 31 * result + (cc != null ? cc.hashCode() : 0);
        return result;
    }
}
