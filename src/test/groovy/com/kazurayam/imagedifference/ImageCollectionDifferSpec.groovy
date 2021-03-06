package com.kazurayam.imagedifference

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.apache.commons.io.FileUtils

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.ImageDeltaStats
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.MaterialStorageFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.TSuiteResultIdImpl
import com.kazurayam.materials.stats.StorageScanner

import spock.lang.Specification

class ImageCollectionDifferSpec extends Specification {

    private static Path fixtureDir
    private static Path specOutputDir

    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(ImageCollectionDifferSpec.class.getName())
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * PNG file should end with "FAILED.png"
     */
    def test_makeImageCollectionDifferences_twins_shouldCreatePngWithFAILED() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_twins_shouldCreatePngWithFAILED")
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
    def test_makeImageCollectionDifferences_twins_shouldCreatePngWithoutFAILED() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_twins_shouldCreatePngWithoutFAILED")
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

    /**
     * run ImageCollectionDiffer#makeImageCollectionDifferences() with ImageDeletaStats object as an arugment
     * @return
     */
    def test_makeImageCollectionDifferences_chronos() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("test_makeImageCollectionDifferences_chronos")
        Path materials = caseOutputDir.resolve('Materials')
        Path storage = caseOutputDir.resolve('Storage')
        Files.createDirectories(materials)
        FileUtils.deleteQuietly(materials.toFile())
        assert Helpers.copyDirectory(fixtureDir.resolve('Storage'), storage)
        MaterialRepository mr = MaterialRepositoryFactory.createInstance(materials)
        MaterialStorage ms = MaterialStorageFactory.createInstance(storage)
        TSuiteName tsn = new TSuiteName('47News_chronos_capture')
        ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190216_204329')))
        ms.restore(mr, new TSuiteResultIdImpl(tsn, TSuiteTimestamp.newInstance('20190216_064354')))
        mr.scan()
        mr.putCurrentTestSuite('Test Suites/ImageDiff', '20190216_210203')
        when:
        // we use Java 8 Stream API to filter entries
        List<MaterialPair> materialPairs =
            mr.createMaterialPairs(tsn).stream().filter { mp ->
                mp.getLeft().getFileType() == FileType.PNG
            }.collect(Collectors.toList())
        StorageScanner storageScanner = new StorageScanner(ms, new StorageScanner.Options.Builder().build())
        ImageDeltaStats imageDeltaStats = storageScanner.scan(tsn)
        double ccp = imageDeltaStats.getCalculatedCriteriaPercentage(
                            new TSuiteName("47News_chronos_capture"),
                            Paths.get('main.TC_47News.visitSite').resolve('47NEWS_TOP.png'))
        then:
        15.0 < ccp && ccp < 16.0 // ccp == 15.197159598135954
        when:
        ImageCollectionDiffer icd = new ImageCollectionDiffer(mr)
        icd.makeImageCollectionDifferences(
            materialPairs,
            new TCaseName('Test Cases/ImageDiff'),
            imageDeltaStats)
        mr.scan()
        List<TSuiteResultId> tsriList = mr.getTSuiteResultIdList(new TSuiteName('Test Suites/ImageDiff'))
        assert tsriList.size() == 1
        TSuiteResultId tsri = tsriList.get(0)
        TSuiteResult tsr = mr.getTSuiteResult(tsri)
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName("Test Cases/ImageDiff"))
        List<Material> mateList = tcr.getMaterialList()
        assert mateList.size() == 1
        Material diffImage = mateList.get(0)
        then:
        diffImage.getPath().toString().endsWith('FAILED.png')
    }
}
