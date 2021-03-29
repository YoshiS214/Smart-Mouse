package com.example.smartmouse

import kotlin.math.pow
import kotlin.properties.Delegates

// From DataSamplingApp

class Matrix {
    var matrix: FloatArray
    var row by Delegates.notNull<Int>()
    var column by Delegates.notNull<Int>()
    var size by Delegates.notNull<Int>()

    constructor(r: Int, c: Int, array: FloatArray? = null) {
        row = r
        column = c
        size = row * column
        if (array != null) {
            if (array.size == size) {
                matrix = array
            } else {
                matrix = FloatArray(size)
                matrix.fill(0F)
            }
        } else {
            matrix = FloatArray(size)
            matrix.fill(0F)
        }
    }

    fun copy(matrix: Matrix) {
        if (row == matrix.row && column == matrix.column) {
            for (x in 0 until size) {
                this.matrix[x] = matrix.matrix[x]
            }
        }
    }

    fun toFloatArray(): FloatArray {
        return matrix
    }

    fun zero() {
        matrix.fill(0F)
    }

    companion object {
        fun scaled(matrix: Matrix, scalar: Float): Matrix {
            var result = FloatArray(matrix.size)

            for (x in 0 until matrix.size) {
                result[x] = scalar * matrix.matrix[x]
            }

            return Matrix(matrix.row, matrix.column, result)
        }

        fun addSubed(op1: Matrix, op2: Matrix, ope: Char): Matrix {
            var result = FloatArray(op1.size)

            if (op1.row == op2.row && op2.column == op2.column) {
                if (ope == '+') {
                    for (x in 0 until op1.size) {
                        result[x] = op1.matrix[x] + op2.matrix[x]
                    }
                } else if (ope == '-') {
                    for (x in 0 until op1.size) {
                        result[x] = op1.matrix[x] - op2.matrix[x]
                    }
                }
            }

            return Matrix(op1.row, op1.column, result)
        }

        fun multipled(op1: Matrix, op2: Matrix): Matrix {
            var result = FloatArray(op1.row * op2.column)

            if (op1.column == op2.row) {
                for (x in 0 until op1.row) {
                    for (y in 0 until op2.column) {
                        for (z in 0 until op1.column) {
                            result[op2.column * x + y] += op1.matrix[z + op1.column * x] * op2.matrix[z * op2.column + y]
                        }
                    }
                }
            }

            return Matrix(op1.row, op2.column, result)
        }

        private fun transpose(matrix: Matrix): Matrix { // Flip the row number and column number of each element
            var result = FloatArray(matrix.size)

            for (x in 0 until matrix.row) {
                for (y in 0 until matrix.column) {
                    result[x * matrix.column + y] = matrix.matrix[x + y * matrix.column]
                }
            }

            return Matrix(matrix.row, matrix.column, result)
        }

        fun determinant(matrix: Matrix): Float? { // Recursion with base case that matrix is 1 by 1
            return if (matrix.row != matrix.column) {
                null
            } else if (matrix.size == 1) {
                matrix.matrix[0]
            } else {
                var det: Float = 0f
                for (x in 0 until matrix.column) {
                    det += (-1f).pow(x) * matrix.matrix[x] * determinant(filter(matrix, 1, x + 1))!!
                }
                det
            }
        }

        private fun filter(matrix: Matrix, r: Int, c: Int): Matrix { // Return matrix of elements in input matrix whose row number and column number are different from ones that specified
            var map = FloatArray((matrix.row - 1) * (matrix.column - 1))
            var index = 0


            for (x in 0 until matrix.size) {
                if ((x - c + 1) % matrix.column != 0 && (x < matrix.column * (r - 1) || x > matrix.column * r - 1)) {
                    map[index] = matrix.matrix[x]
                    index += 1
                }
            }

            return Matrix(matrix.row - 1, matrix.column - 1, map)
        }

        private fun cofactor(matrix: Matrix): Matrix { // Return a matrix whose elements are multipled with 1 or -1
            var result = FloatArray(matrix.size)
            var sign = 1
            for (x in 0 until matrix.row) {
                for (y in 0 until matrix.column) {
                    result[x * matrix.column + y] = sign * (-1).toDouble().pow(y.toDouble())
                        .toFloat() * matrix.matrix[x * matrix.column + y]
                }
                sign *= -1
            }

            return Matrix(matrix.row, matrix.column, result)
        }

        fun inverse(matrix: Matrix): Matrix? {
            var result = FloatArray(matrix.size)

            return if (matrix.row != matrix.column || determinant(matrix) == 0f) {
                null
            } else {
                for (r in 0 until matrix.row) {
                    for (c in 0 until matrix.column) {

                        result[r * matrix.row + c] = determinant(filter(matrix, r + 1, c + 1))!!
                    }
                }

                scaled(
                    transpose(cofactor(Matrix(matrix.row, matrix.column, result))),
                    1 / determinant(matrix)!!
                )
            }
        }
    }
}