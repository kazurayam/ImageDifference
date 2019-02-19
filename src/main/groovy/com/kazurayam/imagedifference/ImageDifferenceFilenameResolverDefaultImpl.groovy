package com.kazurayam.imagedifference

import com.kazurayam.materials.ExecutionProfile
import com.kazurayam.materials.Material

/**
 *
 * @author kazurayam
 *
 */
class ImageDifferenceFilenameResolverDefaultImpl implements ImageDifferenceFilenameResolver {
    /**
     * Given with the following arguments:
     *     Material expMate:                 'Materials/Main/TS1/20181014_131314/CURA_Homepage' created by TSuiteResult with 'product' profile
     *     Material actMate:                 'Materials/Main/TS1/20181014_131315/CURA_Homepage' created by TSuiteResult with 'develop' profile
     *     ImageDifference:                  6.71
     *     Double criteriaPercent:           3.0
     *
     * @return 'CURA_Homepage.20181014_131314_product-20181014_131315_develop.(6.71)FAILED.png'
     */
    String resolveImageDifferenceFilename(
            Material expMate,
            Material actMate,
            ImageDifference diff,
            double criteriaPercent) {
        
        // FIXME: the depencency to the "Reports" directory here makes this method fragile
        //        should parameterize those ExecutionProfiles  
        ExecutionProfile profileExpected = expMate.getParent().getParent().getExecutionPropertiesWrapper().getExecutionProfile()
        ExecutionProfile profileActual   = actMate.getParent().getParent().getExecutionPropertiesWrapper().getExecutionProfile()
        //
        String fileName = expMate.getPath().getFileName().toString()
        String fileId = fileName.substring(0, fileName.lastIndexOf('.'))
        String expTimestamp = expMate.getParent().getParent().getTSuiteTimestamp().format()
        String actTimestamp = actMate.getParent().getParent().getTSuiteTimestamp().format()
        //
        StringBuilder sb = new StringBuilder()
        sb.append("${fileId}.")
        sb.append("${expTimestamp}_${profileExpected}")
        sb.append("-")
        sb.append("${actTimestamp}_${profileActual}")
        sb.append(".")
        sb.append("(${diff.getRatioAsString()})")
        sb.append("${(diff.imagesAreSimilar(criteriaPercent)) ? '.png' : 'FAILED.png'}")
        return sb.toString()
    }
}
