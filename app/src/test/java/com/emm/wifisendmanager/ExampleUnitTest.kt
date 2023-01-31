package com.emm.wifisendmanager

import com.emm.wifisendmanager.util.FileUtil
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val filename = "huvyyabv.dsadsf.gwbe.jpeg"
        val pair = FileUtil.getFileTypeByName(filename)
        println("$pair")
        assertEquals(4, 2 + 2)
    }
}