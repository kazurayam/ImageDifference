package com.kazurayam.imagedifference

import java.nio.file.Path

import javax.imageio.ImageIO

import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.TCaseName

/**
 * This class is designed to implement the "Visual Testing in Katalon Studio" feature.
 *
 * This class uses the following 2 external libraries:
 * 1. AShot (https://github.com/yandex-qatools/ashot)
 * 2. Materials (https://github.com/kazurayam/Materials)
 *
 * The makeImageCollectionDifferences() method provides the core value of this class.
 * This method accepts Materials (image files) to compare them, make differences, and
 * store the diff-images into files.
 *
 * @author kazurayam
 */
class ImageCollectionDiffer {

    private MaterialRepository mr_
    private ImageDifferenceFilenameResolver idfResolver_
    private VisualTestingListener listener_ = new VisualTestingListenerDefaultImpl()
    

    /**
     * constructor
     *
     * @param mr
     * @author kazurayam
     */
    ImageCollectionDiffer(MaterialRepository mr) {
        mr_ = mr
        idfResolver_ = new ImageDifferenceFilenameResolverDefaultImpl()
    }

    /*
     * Non-argument constructor is required to pass "Test Cases/Test/Prologue"
     * which calls `CustomKeywords."${className}.getClass"().getName()`
     */
    ImageCollectionDiffer() {}

    void setImageDifferenceFilenameResolver(ImageDifferenceFilenameResolver idfResolver) {
        idfResolver_ = idfResolver
    }

    /**
     * set instance of VisualTestingListener, which will consume messages from ImageCollectionDiffer
     * 
     * @param listener
     */
    void setVTListener(VisualTestingListener listener) {
        listener_ = listener
    }

    /**
     * compare 2 Material files in each MaterialPair object,
     * create ImageDiff and store the diff image files under the directory
     * 
     * ./Materials/<tSuiteName>/yyyyMMdd_hhmmss/<tCaseName>.
     * 
     * The difference ratio is compared with the criteriaPercent given.
     * Will be marked FAILED if any of the pairs has greater difference.
     *
     * @param materialPairs created by
     *     com.kazurayam.materials.MaterialRpository#createMaterialPairs() method
     * @param tCaseName     created by com.kazurayam.materials.TCaseName(String)
     * @param criteriaPercent e.g. 3.00 percent. If the difference of
     *     a MaterialPair is greater than this,
     *     the MaterialPair is evaluated FAILED
     */
    void makeImageCollectionDifferences(
            List<MaterialPair> materialPairs,
            TCaseName tCaseName,
            Double criteriaPercent) {

        Statistics stats = new Statistics()

        // iterate over the list of Materials
        for (MaterialPair pair : materialPairs) {

            Material expMate = pair.getExpected()
            Material actMate = pair.getActual()

            // create ImageDifference of the 2 given images
            ImageDifference diff = new ImageDifference(
                    ImageIO.read(expMate.getPath().toFile()),
                    ImageIO.read(actMate.getPath().toFile()))

            // record this pair
            stats.add(diff)

            // resolve the name of output file to save the ImageDiff
            String fileName = idfResolver_.resolveImageDifferenceFilename(
                    expMate,
                    actMate,
                    diff,
                    criteriaPercent)

            // resolve the path of output file to save the ImageDiff
            Path pngFile = mr_.resolveMaterialPath(
                    tCaseName,
                    expMate.getDirpathRelativeToTSuiteResult(),
                    fileName)

            // write the ImageDiff into the output file
            ImageIO.write(diff.getDiffImage(), "PNG", pngFile.toFile())

            // verify the diffRatio, fail the test if the ratio is greater than criteria
            if (diff.getRatio() > criteriaPercent && listener_ != null) {
                listener_.failed(">>> diffRatio = ${diff.getRatio()} is exceeding criteria = ${criteriaPercent}")
            }

        }

        // show statistics for making debugging easier
        listener_.info(">>> #makeDiffs ${stats.toString()}")
        listener_.info(">>> #makeDiffs average of diffRatios is ${String.format('%.2f', stats.diffRatioAverage())}")
        listener_.info(">>> #makeDiffs standard deviation of diffRatio is ${String.format('%.2f', stats.evalStandardDeviation())}")
        listener_.info(">>> #makeDiffs recommended criteria is ${String.format('%.2f', stats.evalRecommendedCriteria(1.6))}")
    }

    /**
     *
     */
    class Statistics {

        private List<ImageDifference> list_

        Statistics() {
            list_ = new ArrayList<ImageDifference>()
        }

        void add(ImageDifference diff) {
            list_.add(diff)
        }

        String toString() {
            StringBuilder sb = new StringBuilder()
            sb.append(">>> # diffRatio: ")
            sb.append("[")
            def count = 0
            for (ImageDifference diff : list_) {
                if (count > 0) {
                    sb.append(", ")
                }
                sb.append(diff.getRatioAsString())
                count += 1
            }
            sb.append("] percent")
            return sb.toString()
        }

        Double diffRatioAverage() {
            Double sum = 0.0
            for (ImageDifference diff : list_) {
                sum += diff.getRatio()
            }
            return sum / list_.size()
        }

        Double evalStandardDeviation() {
            Double average = this.diffRatioAverage()
            Double s = 0.0
            for (ImageDifference diff : list_) {
                s += (average - diff.getRatio()) * (average - diff.getRatio())
            }
            return Math.sqrt(s / list_.size)
        }

        Double evalRecommendedCriteria(Double factor = 1.5) {
            Double average = this.diffRatioAverage()
            Double stddevi = this.evalStandardDeviation()
            return average + stddevi * factor
        }
    }

}
