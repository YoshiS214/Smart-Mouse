package com.example.smartmouse

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test


class MatrixUnitTest {
    private val testCasesSquare: Array<Triple<Int, Int, FloatArray>> = arrayOf(
        Triple(1, 1, floatArrayOf(5f)),
        Triple(1, 1, floatArrayOf(2f)),
        Triple(2, 2, floatArrayOf(1f, 2f, 3f, 4f)),
        Triple(2, 2, floatArrayOf(9f, 8f, 7f, 6f)),
        Triple(3, 3, floatArrayOf(10f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)),
        Triple(3, 3, floatArrayOf(19f, 18f, 17f, 16f, 15f, 14f, 13f, 12f, 11f)),
        Triple(
            4,
            4,
            floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 16f)
        ),
        Triple(
            4,
            4,
            floatArrayOf(21f, 19f, 18f, 17f, 16f, 15f, 14f, 13f, 12f, 11f, 10f, 9f, 8f, 7f, 6f, 1f)
        )
    )

    private val testCases: Array<Triple<Int, Int, FloatArray>> = arrayOf(
        Triple(1, 2, floatArrayOf(1f, 2f)),
        Triple(1, 2, floatArrayOf(3f, 4f)),
        Triple(2, 1, floatArrayOf(9f, 8f)),
        Triple(3, 2, floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f)),
        Triple(3, 2, floatArrayOf(9f, 8f, 7f, 6f, 5f, 4f)),
        Triple(2, 3, floatArrayOf(11f, 12f, 13f, 14f, 15f, 16f))
    )

    private val testCasesErroneous: Array<Triple<Int, Int, FloatArray>> = arrayOf(
        Triple(2, 2, floatArrayOf()),
        Triple(2, 2, floatArrayOf(1f, 2f, 3f, 4f, 5f))
    )


    @Test
    fun copy_isCorrect() {
        var target: FloatArray
        var actual: FloatArray
        for (copyFrom in testCases) {
            var copyTo = Matrix(copyFrom.first, copyFrom.second)
            copyTo.copy(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            actual = copyTo.toFloatArray()
            target = Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray()
            assertArrayEquals(target, actual, 0f)
        }

        for (copyFrom in testCasesSquare) {
            var copyTo = Matrix(copyFrom.first, copyFrom.second)
            copyTo.copy(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            actual = copyTo.toFloatArray()
            target = Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray()
            assertArrayEquals(target, actual, 0f)
        }

        for (copyFrom in testCasesErroneous) {
            var copyTo = Matrix(copyFrom.first, copyFrom.second)
            copyTo.copy(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            actual = copyTo.toFloatArray()
            target = Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray()
            assertArrayEquals(target, actual, 0f)
        }
    }

    @Test
    fun toFloatArray_isCorrect() {
        var actual: FloatArray
        for (copyFrom in testCases) {
            actual = Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray()
            assertArrayEquals(copyFrom.third, actual, 0f)
        }

        for (copyFrom in testCasesSquare) {
            actual = Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray()
            assertArrayEquals(copyFrom.third, actual, 0f)
        }

        for (copyFrom in testCasesErroneous) {
            actual = Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray()
            assertArrayEquals(floatArrayOf(0f, 0f, 0f, 0f), actual, 0f)
        }
    }

    @Test
    fun scaled_isCorrect() {
        var actual: FloatArray = Matrix.scaled(
            Matrix(
                testCasesSquare[2].first,
                testCasesSquare[2].second,
                testCasesSquare[2].third
            ), -0.5f
        ).toFloatArray()
        assertArrayEquals(floatArrayOf(-0.5f, -1f, -1.5f, -2f), actual, 0f)
    }

    @Test
    fun addSubed_isCorrect() {
        var actual: FloatArray = Matrix.addSubed(
            Matrix(
                testCasesSquare[3].first,
                testCasesSquare[3].second,
                testCasesSquare[3].third
            ),
            Matrix(testCasesSquare[2].first, testCasesSquare[2].second, testCasesSquare[2].third),
            '+'
        ).toFloatArray()
        assertArrayEquals(floatArrayOf(10f, 10f, 10f, 10f), actual, 0f)
        actual = Matrix.addSubed(
            Matrix(
                testCasesSquare[2].first,
                testCasesSquare[2].second,
                testCasesSquare[2].third
            ),
            Matrix(testCasesSquare[3].first, testCasesSquare[3].second, testCasesSquare[3].third),
            '-'
        ).toFloatArray()
        assertArrayEquals(floatArrayOf(-8f, -6f, -4f, -2f), actual, 0f)
        actual = Matrix.addSubed(
            Matrix(
                testCasesSquare[3].first,
                testCasesSquare[3].second,
                testCasesSquare[3].third
            ),
            Matrix(testCasesSquare[4].first, testCasesSquare[4].second, testCasesSquare[4].third),
            '+'
        ).toFloatArray()
        assertArrayEquals(floatArrayOf(0f, 0f, 0f, 0f), actual, 0f)
    }

    @Test
    fun multipled_isCorrect() {
        var actual: FloatArray = Matrix.multipled(
            Matrix(
                testCasesSquare[4].first,
                testCasesSquare[4].second,
                testCasesSquare[4].third
            ), Matrix(testCasesSquare[5].first, testCasesSquare[5].second, testCasesSquare[5].third)
        ).toFloatArray()
        assertArrayEquals(
            floatArrayOf(261f, 246f, 231f, 234f, 219f, 204f, 378f, 354f, 330f),
            actual,
            0f
        )
        actual = Matrix.multipled(
            Matrix(testCases[3].first, testCases[3].second, testCases[3].third),
            Matrix(testCases[2].first, testCases[2].second, testCases[2].third)
        ).toFloatArray()
        assertArrayEquals(floatArrayOf(25f, 59f, 93f), actual, 0f)
        actual = Matrix.multipled(
            Matrix(testCases[2].first, testCases[2].second, testCases[2].third),
            Matrix(testCases[3].first, testCases[3].second, testCases[3].third)
        ).toFloatArray()
        assertArrayEquals(floatArrayOf(0f, 0f, 0f, 0f), actual, 0f)
    }

    @Test
    fun determinant_isCorrect() {
        var actual: Float?
        for (copyFrom in testCases) {
            actual = Matrix.determinant(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            assertEquals(null, actual)
        }

        actual = Matrix.determinant(
            Matrix(
                testCasesSquare[1].first,
                testCasesSquare[1].second,
                testCasesSquare[1].third
            )
        )
        assertEquals(2f, actual)
        actual = Matrix.determinant(
            Matrix(
                testCasesSquare[3].first,
                testCasesSquare[3].second,
                testCasesSquare[3].third
            )
        )
        assertEquals(-2f, actual)
        actual = Matrix.determinant(
            Matrix(
                testCasesSquare[5].first,
                testCasesSquare[5].second,
                testCasesSquare[5].third
            )
        )
        assertEquals(0f, actual)
        actual = Matrix.determinant(
            Matrix(
                testCasesSquare[7].first,
                testCasesSquare[7].second,
                testCasesSquare[7].third
            )
        )
        assertEquals(16f, actual)

        for (copyFrom in testCasesErroneous) {
            actual = Matrix.determinant(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            assertEquals(0f, actual)
        }
    }

    @Test
    fun inverse_isCorrect() {
        for (copyFrom in testCases) {
            assertEquals(
                null,
                Matrix.inverse(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            )
        }

        var actual: FloatArray = Matrix.inverse(
            Matrix(
                testCasesSquare[3].first,
                testCasesSquare[3].second,
                testCasesSquare[3].third
            )
        )!!.toFloatArray()
        assertArrayEquals(floatArrayOf(-3f, 4f, 3.5f, -4.5f), actual, 0f)
        assertEquals(
            null,
            Matrix.inverse(
                Matrix(
                    testCasesSquare[5].first,
                    testCasesSquare[5].second,
                    testCasesSquare[5].third
                )
            )
        )
        actual = Matrix.inverse(
            Matrix(
                testCasesSquare[7].first,
                testCasesSquare[7].second,
                testCasesSquare[7].third
            )
        )!!.toFloatArray()
        assertArrayEquals(
            floatArrayOf(
                1f,
                -2f,
                1f,
                0f,
                -2f,
                1.25f,
                2f,
                -0.25f,
                1f,
                1.25f,
                -3.75f,
                0.5f,
                0f,
                -0.25f,
                0.5f,
                -0.25f
            ), actual, 0f
        )


        for (copyFrom in testCasesErroneous) {
            assertEquals(
                null,
                Matrix.inverse(Matrix(copyFrom.first, copyFrom.second, copyFrom.third))
            )
        }
    }
}