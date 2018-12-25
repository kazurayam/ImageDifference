ImageDifference
==================

by kazurayam

## What is this?

This project was developed to enhance the [aShot](https://github.com/pazone/ashot) library.
aShot is a set of WebDriver Screenshot utility. It takes take screenshots, crop, prettify, compare the screenshot of
web pages. Especially I was interested in the image comparison of aShot for my [`Visutal Testing in Katalon Studio` project](https://github.com/kazurayam/VisualTestingInKatalonStudio)

The [`ru.yandex.qatools.ashot.comparison.ImageDiff`](https://github.com/pazone/ashot/blob/master/src/main/java/ru/yandex/qatools/ashot/comparison/ImageDiff.java)
class is the core part of image difference support by aShot. I loved it but found I want some additional features:

1. I want to calcurate the rate of difference between 2 images in percentage. E.g, 1.35 means 1 point  plus 0.35 percent.
2. I want to get the ratio as string in the fixed "%1$.2f" format
3. I want to get boolean evaluation result if 2 images are different or similar. And the descision should be made against a criterio value specified.
   For example, if the 2 images are different for 4.3% and the given criteria is 4.0 then these are evaluated as "different enough",
   but if the given criteria is 5.0 then these are evaluated as "similar enough". This fussy evaluation rule would make this utility 
   applicable to many problem domains.
   
The `com.kazurayam.imagedifference.ImageDifference` class implements the core functionalities.

The accompanying class `com.kazurayam.imagedifference.ImageDifferenceSerializer` class is a utilitiy to
serialize 3 images into file: the expected image, the actual image and the difference image.

## API documentation

see [ImageDifference API](docs/index.html)
