package com.kazurayam.imagedifference

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.apache.commons.io.FileUtils

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName

import spock.lang.Specification

class ImageCollectionDifferSpec extends Specification {

    private static Path fixtureDir
    private static Path specOutputDir

    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/resources/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(ImageCollectionDifferSpec.class.getName())
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * PNG file should end with "FAILED.png"
     */
    def test_makeImageCollectionDifferences_shouldCreatePngWithFAILED() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_shouldCreatePngWithFAILED")
        Path materials = caseOutputDir.resolve('Materials')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        when:
        boolean materialsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        boolean reportsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Reports'), reports)
        then:
        materialsCopyResult
        reportsCopyResult
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.putCurrentTestSuite('Test Suites/ImageDiff', '20181014_060501')
        List<MaterialPair> materialPairs =
        // we use Java 8 Stream API to filter entries
        mr.createMaterialPairs(new TSuiteName('Test Suites/Main/TS1')).stream().filter { mp ->
                mp.getLeft().getFileType() == FileType.PNG
            }.collect(Collectors.toList())

        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
            materialPairs,
            new TCaseName('Test Cases/ImageDiff'),
            5.0)          // specified value smaller than the actual diff ratio (6.72)
        //
        then:
        Files.exists(materials.resolve('ImageDiff/20181014_060501/ImageDiff/Main.Basic'))
        Files.exists(materials.resolve('ImageDiff/20181014_060501/ImageDiff/Main.Basic/' +
            'CURA_Homepage.20181014_060500_product-20181014_060501_develop.(6.72)FAILED.png'))
    }
    
    /**
     * PNG file should not end with "FAILED.png"
     */
    def test_makeImageCollectionDifferences_shouldCreatePngWithoutFAILED() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_shouldCreatePngWithoutFAILED")
        Path materials = caseOutputDir.resolve('Materials')
        Path reports = caseOutputDir.resolve('Reports')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        when:
        boolean materialsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        boolean reportsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Reports'), reports)
        then:
        materialsCopyResult
        reportsCopyResult
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.putCurrentTestSuite('Test Suites/ImageDiff', '20181014_060501')
        List<MaterialPair> materialPairs =
        // we use Java 8 Stream API to filter entries
        mr.createMaterialPairs(new TSuiteName('Test Suites/Main/TS1')).stream().filter { mp ->
                mp.getLeft().getFileType() == FileType.PNG
            }.collect(Collectors.toList())

        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
            materialPairs,
            new TCaseName('Test Cases/ImageDiff'),
            10.0)          // specified value larger than the actual diff ratio (6.72)
        //
        then:
        Files.exists(materials.resolve('ImageDiff/20181014_060501/ImageDiff/Main.Basic'))
        Files.exists(materials.resolve('ImageDiff/20181014_060501/ImageDiff/Main.Basic/' +
            'CURA_Homepage.20181014_060500_product-20181014_060501_develop.(6.72).png'))   
            // here we expect the file to be (6.72).png, rather than (6.72)FAILED.png
    }

}
