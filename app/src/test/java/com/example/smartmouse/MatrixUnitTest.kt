package com.example.smartmouse

import android.database.MatrixCursor
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MatrixUnitTest {
    private val testCasesSquare: Array<Triple<Int, Int, FloatArray>> = arrayOf(
        Triple(1,1, floatArrayOf(5f)),
        Triple(1,1, floatArrayOf(2f)),
        Triple(2,2,floatArrayOf(1f,2f,3f,4f)),
        Triple(2,2,floatArrayOf(9f,8f,7f,6f)),
        Triple(3,3, floatArrayOf(10f,2f,3f,4f,5f,6f,7f,8f,9f)),
        Triple(3,3, floatArrayOf(19f,18f,17f,16f,15f,14f,13f,12f,11f)),
        Triple(4,4, floatArrayOf(1f,2f,3f,4f,5f,6f,7f,8f,9f,10f,11f,12f,13f,14f,15f,16f)),
        Triple(4,4, floatArrayOf(21f,19f,18f,17f,16f,15f,14f,13f,12f,11f,10f,9f,8f,7f,6f,1f))
    )

    private val testCases: Array<Triple<Int,Int,FloatArray>> = arrayOf(
        Triple(1,2, floatArrayOf(1f,2f)),
        Triple(1, 2, floatArrayOf(3f,4f)),
        Triple(2,1, floatArrayOf(9f,8f)),
        Triple(3,2, floatArrayOf(1f,2f,3f,4f,5f,6f)),
        Triple(3,2, floatArrayOf(9f,8f,7f,6f,5f,4f)),
        Triple(2,3, floatArrayOf(11f,12f,13f,14f,15f,16f))
    )

    private val testCasesErroneous: Array<Triple<Int,Int, FloatArray>> = arrayOf(
        Triple(2,2, floatArrayOf()),
        Triple(2,2, floatArrayOf(1f,2f,3f,4f,5f))
    )


    @Test
    fun copy_isCorrect() {
        for (copyFrom in testCases){
            var copyTo = Matrix(2,2)
            assertEquals(Matrix(copyFrom.first, copyFrom.second, copyFrom.third), copyTo.copy(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }

        for (copyFrom in testCasesSquare){
            var copyTo = Matrix(2,2)
            assertEquals(Matrix(copyFrom.first, copyFrom.second, copyFrom.third), copyTo.copy(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }

        for (copyFrom in testCasesErroneous){
            var copyTo = Matrix(2,2)
            assertEquals(Matrix(copyFrom.first, copyFrom.second, copyFrom.third), copyTo.copy(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }
    }

    @Test
    fun toFloatArray_isCorrect(){
        for (copyFrom in testCases){
            assertEquals(copyFrom.third, Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray())
        }

        for (copyFrom in testCasesSquare){
            assertEquals(copyFrom.third, Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray())
        }

        for (copyFrom in testCasesErroneous){
            assertEquals(copyFrom.third, Matrix(copyFrom.first, copyFrom.second, copyFrom.third).toFloatArray())
        }
    }

    @Test
    fun scaled_isCorrect(){
        assertEquals(Matrix(2,2, floatArrayOf(-0.5f, -1f, -1.5f, -2f)), Matrix.scaled(Matrix(testCasesSquare[2].first, testCasesSquare[2].second, testCasesSquare[2].third), -0.5f))
    }

    @Test
    fun addSubed_isCorrect(){
        assertEquals(Matrix(2,2, floatArrayOf(10f,10f,10f,10f)), Matrix.addSubed(Matrix(testCasesSquare[3].first, testCasesSquare[3].second, testCasesSquare[3].third), Matrix(testCasesSquare[2].first, testCasesSquare[2].second, testCasesSquare[2].third), '+'))
        assertEquals(Matrix(2,2, floatArrayOf(-8f,-6f,-4f,-2f)), Matrix.addSubed(Matrix(testCasesSquare[2].first, testCasesSquare[2].second, testCasesSquare[2].third), Matrix(testCasesSquare[3].first, testCasesSquare[3].second, testCasesSquare[3].third), '-'))
        assertEquals(Matrix(3,2, floatArrayOf(0f,0f,0f,0f,0f,0f)), Matrix.addSubed(Matrix(testCases[3].first, testCases[3].second, testCases[3].third), Matrix(testCases[4].first, testCases[4].second, testCases[4].third), '+'))
    }

    @Test
    fun multipled_isCorrect(){
        assertEquals(Matrix(3,3, floatArrayOf(261f,246f,231f,234f,219f,204f,378f,354f,330f)), Matrix.multipled(Matrix(testCasesSquare[4].first, testCasesSquare[4].second, testCasesSquare[4].third), Matrix(testCasesSquare[5].first, testCasesSquare[5].second, testCasesSquare[5].third)))
        assertEquals(Matrix(3,3, floatArrayOf(25f,59f,93f)), Matrix.multipled(Matrix(testCases[3].first, testCases[3].second, testCases[3].third), Matrix(testCases[2].first, testCases[2].second, testCases[2].third)))
        assertEquals(Matrix(2,2, floatArrayOf(0f,0f,0f,0f)), Matrix.multipled(Matrix(testCases[2].first, testCases[2].second, testCases[2].third), Matrix(testCases[3].first, testCases[3].second, testCases[3].third)))
    }

    @Test
    fun determinant_isCorrect(){
        for (copyFrom in testCases){
            assertEquals(null, Matrix.determinant(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }

        assertEquals(2f, Matrix.determinant(Matrix(testCasesSquare[1].first, testCasesSquare[1].second, testCasesSquare[1].third)))
        assertEquals(-2f, Matrix.determinant(Matrix(testCasesSquare[3].first, testCasesSquare[3].second, testCasesSquare[3].third)))
        assertEquals(0, Matrix.determinant(Matrix(testCasesSquare[5].first, testCasesSquare[5].second, testCasesSquare[5].third)))
        assertEquals(16, Matrix.determinant(Matrix(testCasesSquare[7].first, testCasesSquare[7].second, testCasesSquare[7].third)))

        for (copyFrom in testCasesErroneous){
            assertEquals(0f, Matrix.determinant(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }
    }

    @Test
    fun inverse_isCorrect(){
        for (copyFrom in testCases){
            assertEquals(null, Matrix.determinant(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }

        assertEquals(Matrix(2,2, floatArrayOf(-3f,4f,3.5f,-4.5f)), Matrix.determinant(Matrix(testCasesSquare[3].first, testCasesSquare[3].second, testCasesSquare[3].third)))
        assertEquals(null, Matrix.determinant(Matrix(testCasesSquare[5].first, testCasesSquare[5].second, testCasesSquare[5].third)))
        assertEquals(Matrix(4,4, floatArrayOf(1f,-2f,1f,0f,-2f,1.25f,2f,-0.25f,1f,1.25f,-3.75f,0.5f,0f,-0.25f,0.5f,-0.25f)), Matrix.determinant(Matrix(testCasesSquare[7].first, testCasesSquare[7].second, testCasesSquare[7].third)))


        for (copyFrom in testCasesErroneous){
            assertEquals(null, Matrix.determinant(Matrix(copyFrom.first, copyFrom.second, copyFrom.third)))
        }
    }
}