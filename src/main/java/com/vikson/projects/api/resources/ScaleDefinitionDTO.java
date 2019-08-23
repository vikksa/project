package com.vikson.projects.api.resources;

import com.vikson.projects.model.ScaleDefinition;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Scale Definition Resource")
public class ScaleDefinitionDTO {

    @ApiModelProperty(value = "Name of the scale")
    private String name;
    @ApiModelProperty(value = "String of the one star label")
    private String oneStarLabel = "";
    @ApiModelProperty(value = "String of the two star label")
    private String twoStarLabel = "";
    @ApiModelProperty(value = "String of the three star label")
    private String threeStarLabel = "";
    @ApiModelProperty(value = "String of the four star label")
    private String fourStarLabel = "";
    @ApiModelProperty(value = "String of the five star label")
    private String fiveStarLabel = "";
    @ApiModelProperty(value = "Whether the scale definition is active or not")
    private Boolean active = true;

    public ScaleDefinitionDTO() {
    }

    public ScaleDefinitionDTO(ScaleDefinition scaleDefinition) {
        this.name = scaleDefinition.getName();
        this.oneStarLabel = scaleDefinition.getOneStarLabel();
        this.twoStarLabel = scaleDefinition.getTwoStarLabel();
        this.threeStarLabel = scaleDefinition.getThreeStarLabel();
        this.fourStarLabel = scaleDefinition.getFourStarLabel();
        this.fiveStarLabel = scaleDefinition.getFiveStarLabel();
        this.active = scaleDefinition.isActive();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOneStarLabel() {
        return oneStarLabel;
    }

    public void setOneStarLabel(String oneStarLabel) {
        this.oneStarLabel = oneStarLabel;
    }

    public String getTwoStarLabel() {
        return twoStarLabel;
    }

    public void setTwoStarLabel(String twoStarLabel) {
        this.twoStarLabel = twoStarLabel;
    }

    public String getThreeStarLabel() {
        return threeStarLabel;
    }

    public void setThreeStarLabel(String threeStarLabel) {
        this.threeStarLabel = threeStarLabel;
    }

    public String getFourStarLabel() {
        return fourStarLabel;
    }

    public void setFourStarLabel(String fourStarLabel) {
        this.fourStarLabel = fourStarLabel;
    }

    public String getFiveStarLabel() {
        return fiveStarLabel;
    }

    public void setFiveStarLabel(String fiveStarLabel) {
        this.fiveStarLabel = fiveStarLabel;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScaleDefinitionDTO that = (ScaleDefinitionDTO) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (oneStarLabel != null ? !oneStarLabel.equals(that.oneStarLabel) : that.oneStarLabel != null) return false;
        if (twoStarLabel != null ? !twoStarLabel.equals(that.twoStarLabel) : that.twoStarLabel != null) return false;
        if (threeStarLabel != null ? !threeStarLabel.equals(that.threeStarLabel) : that.threeStarLabel != null)
            return false;
        if (fourStarLabel != null ? !fourStarLabel.equals(that.fourStarLabel) : that.fourStarLabel != null)
            return false;
        if (fiveStarLabel != null ? !fiveStarLabel.equals(that.fiveStarLabel) : that.fiveStarLabel != null)
            return false;
        return active != null ? active.equals(that.active) : that.active == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (oneStarLabel != null ? oneStarLabel.hashCode() : 0);
        result = 31 * result + (twoStarLabel != null ? twoStarLabel.hashCode() : 0);
        result = 31 * result + (threeStarLabel != null ? threeStarLabel.hashCode() : 0);
        result = 31 * result + (fourStarLabel != null ? fourStarLabel.hashCode() : 0);
        result = 31 * result + (fiveStarLabel != null ? fiveStarLabel.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        return result;
    }
}
