package com.example.smartmouse

import kotlin.math.pow
import kotlin.properties.Delegates

// From DataSamplingApp

class Matrix {
    lateinit var matrix: FloatArray
    var Row by Delegates.notNull<Int>()
    var Column by Delegates.notNull<Int>()
    var Size by Delegates.notNull<Int>()

    constructor(r: Int, c: Int, array: FloatArray? = null) {
        Row = r
        Column = c
        Size = Row*Column
        if (array != null){
            if(array.size == Size){
                matrix = array
            }else{
                matrix = FloatArray(Size)
                matrix.fill(0F)
            }
        }else{
            matrix = FloatArray(Size)
            matrix.fill(0F)
        }
    }

    fun copy(matrix: Matrix){
        if (Row == matrix.Row && Column == matrix.Column){
            for (x in 0 until Size){
                this.matrix[x] = matrix.matrix[x]
            }
        }
    }

    fun toFloatArray():FloatArray {
        return matrix
    }

    fun zero(){
        matrix.fill(0F)
    }

    companion object {
        fun scaled(matrix: Matrix, scalar: Float): Matrix {
            var result = FloatArray(matrix.Size)

            for (x in 0 until matrix.Size) {
                result[x] = scalar * matrix.matrix[x];
            }

            return Matrix(matrix.Row, matrix.Column, result)
        }

        fun addSubed(op1: Matrix, op2: Matrix, ope: Char): Matrix {
            var result = FloatArray(op1.Size)

            if (op1.Row == op2.Row && op2.Column == op2.Column) {
                if (ope == '+') {
                    for (x in 0 until op1.Size) {
                        result[x] = op1.matrix[x] + op2.matrix[x]
                    }
                } else if (ope == '-') {
                    for (x in 0 until op1.Size) {
                        result[x] = op1.matrix[x] - op2.matrix[x]
                    }
                }
            }

            return Matrix(op1.Row, op1.Column, result)
        }

        fun multipled(op1: Matrix, op2: Matrix): Matrix {
            var result = FloatArray(op1.Row * op2.Column)

            if (op1.Column == op2.Row) {
                for (x in 0 until op1.Row) {
                    for (y in 0 until op2.Column) {
                        for (z in 0 until op1.Column) {
                            result[op2.Column * x + y] += op1.matrix[z + op1.Row * x] * op2.matrix[z * op2.Column + y]
                        }
                    }
                }
            }

            return Matrix(op1.Row, op2.Column, result)
        }

        fun transpose(matrix: Matrix): Matrix {
            var result = FloatArray(matrix.Size)

            for (x in 0 until matrix.Row) {
                for (y in 0 until matrix.Column) {
                    result[x * matrix.Column + y] = matrix.matrix[x + y * matrix.Column]
                }
            }

            return Matrix(matrix.Row, matrix.Column, result)
        }

        fun determinant(matrix: Matrix): Float? {
            return if (matrix.Row != matrix.Column){
                null
            } else if (matrix.Size == 1) {
                matrix.matrix[0]
            } else {
                var det: Float = 0f
                for (x in 0 until matrix.Column) {
                    det += (-1f).pow(x) * matrix.matrix[x] * determinant(filter(matrix, 1, x + 1))!!
                }
                det
            }
        }

        private fun filter(matrix: Matrix, r: Int, c: Int): Matrix {
            var map = FloatArray((matrix.Row - 1) * (matrix.Column - 1))
            var index = 0


            for (x in 0 until matrix.Size) {
                if ((x - c + 1) % matrix.Column != 0 && (x < matrix.Column * (r - 1) || x > matrix.Column * r - 1)) {
                    map[index] = matrix.matrix[x]
                    index += 1
                }
            }

            return Matrix(matrix.Row - 1, matrix.Column - 1, map)
        }

        fun cofactor(matrix: Matrix): Matrix {
            var result = FloatArray(matrix.Size)
            var sign = 1
            for (x in 0 until matrix.Row) {
                for (y in 0 until matrix.Column) {
                    result[x * matrix.Column + y] = sign * (-1).toDouble().pow(y.toDouble())
                        .toFloat() * matrix.matrix[x * matrix.Column + y]
                }
                sign *= -1
            }

            return Matrix(matrix.Row, matrix.Column, result)
        }

        fun inverse(matrix: Matrix): Matrix? {
            var result = FloatArray(matrix.Size)

            return if (matrix.Row != matrix.Column || determinant(matrix) == 0f){
                null
            }else{
                for (r in 0 until matrix.Row) {
                    for (c in 0 until matrix.Column) {

                        result[r * matrix.Row + c] = determinant(filter(matrix, r + 1, c + 1))!!
                    }
                }

                scaled(
                    transpose(cofactor(Matrix(matrix.Row, matrix.Column, result))),
                    1 / determinant(matrix)!!
                )
            }
        }
    }
}