package de.mcella.spring.learntool.learn.algorithm

import de.mcella.spring.learntool.UnitTest
import de.mcella.spring.learntool.learn.exceptions.InputValuesNotAcceptableException
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
@Category(UnitTest::class)
class Sm2AlgorithmTest {

    fun parametersForTest() = arrayOf(
        arrayOf("5", "0", "2.5", "1", "1", "1", "2.6"),
        arrayOf("4", "1", "2.0", "1", "6", "2", "2.0"),
        arrayOf("5", "2", "2.0", "6", "12", "3", "2.1"),
        arrayOf("3", "3", "1.8", "6", "11", "4", "1.66"),
        arrayOf("2", "0", "2.5", "1", "1", "0", "2.5"),
        arrayOf("1", "1", "2.0", "1", "1", "0", "2.0"),
        arrayOf("0", "1", "2.0", "1", "1", "0", "2.0")
    )

    @Test(expected = InputValuesNotAcceptableException::class)
    fun `given the algorithm input values with quality equals to -1, when validating the input values, then throw InputValuesNotAcceptableException`() {
        val inputValues = InputValues(-1, 0, MIN_EASE_FACTOR, 0)
        Sm2Algorithm.validate(inputValues)
    }

    @Test(expected = InputValuesNotAcceptableException::class)
    fun `given the algorithm input values with quality equals to 6, when validating the input values, then throw InputValuesNotAcceptableException`() {
        val inputValues = InputValues(6, 0, MIN_EASE_FACTOR, 0)
        Sm2Algorithm.validate(inputValues)
    }

    @Test
    @Parameters
    fun test(quality: String, previousRepetitions: String, previousEaseFactor: String, previousInterval: String, interval: String, repetitions: String, easeFactor: String) {
        val inputValues = InputValues(
            quality.toInt(),
            previousRepetitions.toInt(),
            previousEaseFactor.toFloat(),
            previousInterval.toInt()
        )

        val outputValues = Sm2Algorithm.evaluate(inputValues)

        assertEquals(interval.toInt(), outputValues.interval)
        assertEquals(repetitions.toInt(), outputValues.repetitions)
        assertEquals(easeFactor.toFloat(), outputValues.easeFactor)
    }
}
