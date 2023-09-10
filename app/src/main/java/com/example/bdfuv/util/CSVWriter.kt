package com.example.bdfuv.util

import android.content.Context
import com.example.bdfuv.model.MeasureDataModel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException


class CSVWriter {
    companion object {
        var writeListener =
            { _: Boolean ->

            }


        fun writeToCSV(
            datas: List<MeasureDataModel>,
            macAddress: String, questions: IntArray
            , content: Context
        ) {
            val fileName = Tools.getDateFileName(datas.get(0).date) + "_Sunny.csv"

            var fileWriter: FileWriter? = null

            var bw: BufferedWriter? = null

            val file = File(content.getExternalFilesDir(null), fileName)
            println(">>>>file.absolutePath = ${file.absolutePath}")

            var questionString = ""
            for (q in questions) {
                questionString += ",$q"
            }

            try {
                BufferedWriter(FileWriter(file, true))
                    .use { bw ->

                        for (data in datas) {
                            bw.append(Tools.getDateString(data.date))
                            bw.append(',')
                            bw.append(macAddress)
                            bw.append(',')
                            bw.append(data.area)
                            bw.append(',')
                            bw.append(data.UV1.toString())
                            bw.append(',')
                            bw.append(data.UV2.toString())
                            bw.append(',')
                            bw.append(data.redness.toString())
                            bw.append(',')
                            bw.append(data.melanin.toString())
                            bw.append(',')
                            bw.append(data.temperature.toString())
                            bw.append(',')
                            bw.append(data.humidity.toString())
                            bw.append(questionString)
                            bw.append('\n')
                        }

                        writeListener.invoke(true)
                        println("Write CSV successfully!")
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                writeListener.invoke(false)
            }

            //https://waylau.com/concise-try-with-resources-jdk9/
            //https://howtodoinjava.com/java/io/how-to-write-to-file-in-java-bufferedwriter-example/
        }
    }
}