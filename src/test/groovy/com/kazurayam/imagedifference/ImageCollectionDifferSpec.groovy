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

    private static Path projectDir = Paths.get(".")
    private static Path fixtureDir = projectDir.resolve('src/test/resources/fixture')
    private static Path testOutputDir = projectDir.resolve('build/tmp/testOutput')
    private static Path specOutputDir = testOutputDir.resolve(ImageCollectionDifferSpec.class.getName())

    def setupSpec() {}
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    def test_makeImageCollectionDifferences() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences")
        Path materials = caseOutputDir.resolve('Materials')
        Path reports   = caseOutputDir.resolve('Reports')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        when:
        boolean materialsCopyResult = Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        boolean reportsCopyResult   = Helpers.copyDirectory(fixtureDir.resolve('Reports'), reports)
        then:
        materialsCopyResult
        reportsCopyResult
        when:
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        mr.putCurrentTestSuite('Test Suites/ImageDiff', '20181014_060501')
        then:
        List<MaterialPair> materialPairs =
            // we use Java 8 Stream API to filter entries
            mr.createMaterialPairs(new TSuiteName('Test Suites/Main/TS1')).stream().filter { mp ->
                    mp.getLeft().getFileType() == FileType.PNG
                }.collect(Collectors.toList())

        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
                materialPairs,
                new TCaseName('Test Cases/ImageDiff'),
                7.0)
        //
        then:
        Files.exists(materials.resolve('ImageDiff/20181014_060501/ImageDiff/Main.Basic'))
        Files.exists(materials.resolve('ImageDiff/20181014_060501/ImageDiff/Main.Basic/CURA_Appointment.20181014_060500_product-20181014_060501_develop.(0.01).png'))
    }
}
