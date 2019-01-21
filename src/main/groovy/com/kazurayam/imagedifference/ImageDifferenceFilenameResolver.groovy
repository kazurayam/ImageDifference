package com.kazurayam.imagedifference

import com.kazurayam.materials.Material

/**
 *
 * @author kazurayam
 *
 */
interface ImageDifferenceFilenameResolver {

    String resolveImageDifferenceFilename(
            Material expectedMaterial,
            Material actualMaterial,
            ImageDifference imageDifference,
            Double criteriaPercent)
}