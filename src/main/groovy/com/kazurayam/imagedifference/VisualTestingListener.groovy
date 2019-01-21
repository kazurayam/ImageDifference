package com.kazurayam.imagedifference

/**
 *
 * @author kazurayam
 *
 */
interface VisualTestingListener {

    void info(String message)

    void failed(String message)

    void fatal(String message)

}