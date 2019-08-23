package com.vikson.projects.service.translators;

import com.vikson.projects.api.resources.PlanDTO;
import com.vikson.projects.api.resources.PlanFolderDTO;
import com.vikson.projects.api.resources.PlanLevelDTO;
import com.vikson.projects.api.resources.PlanRevisionDTO;
import com.vikson.projects.api.resources.PlanTreeDTO;

public class LevelSquarifier {

    private LevelSquarifier(){}

    public static final int FACTOR = 4;

    public static PlanTreeDTO translate(PlanTreeDTO planTree) {
        planTree.getChildren().forEach(LevelSquarifier::translate);
        planTree.getSubFolder().forEach(LevelSquarifier::translate);
        return planTree;
    }

    public static PlanFolderDTO translate(PlanFolderDTO folder) {
        folder.getChildren().forEach(LevelSquarifier::translate);
        folder.getSubFolder().forEach(LevelSquarifier::translate);
        return folder;
    }

    public static PlanDTO translate(PlanDTO planDTO) {
        planDTO.getRevisions().forEach(LevelSquarifier::translate);
        translate(planDTO.getCurrentRevision());
        return planDTO;
    }

    public static PlanRevisionDTO translate(PlanRevisionDTO revisionDTO) {
        int factor = FACTOR;
        if(revisionDTO.getLevel().size() == 1) {
            factor = 8;
        }
        int theFactor = factor;
        revisionDTO.getLevel().forEach(level -> translate(level, theFactor));
        return revisionDTO;
    }

    public static PlanLevelDTO translate(PlanLevelDTO levelDTO) {
        int max = getBiggestSideTimesTwo(levelDTO.getTilesX(), levelDTO.getTilesY(), FACTOR);
        levelDTO.setTilesX(max);
        levelDTO.setTilesY(max);
        return levelDTO;
    }

    public static PlanLevelDTO translate(PlanLevelDTO levelDTO, int factor) {
        if(!levelDTO.isSquarified()) {
            int max = getBiggestSideTimesTwo(levelDTO.getTilesX(), levelDTO.getTilesY(), factor);
            levelDTO.setTilesX(max);
            levelDTO.setTilesY(max);
            levelDTO.setSquarified(true);
        }
        return levelDTO;
    }

    public static int getBiggestSideTimesTwo(int tilesX, int tilesY, int factor) {
        int max = tilesX;
        if(tilesY > tilesX) {
            max = tilesY;
        }

        return findNearestSuperiorPowerOfTwo(max) * factor;
    }

    public static int findNearestSuperiorPowerOfTwo(int a) {
        int exponent = a == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(a - 1);
        return (int)Math.pow(2, exponent);
    }

}
