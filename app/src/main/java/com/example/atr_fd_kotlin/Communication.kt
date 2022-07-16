package com.example.atr_fd_kotlin

import android.os.CountDownTimer
import android.util.Log
import com.example.atr_fd_kotlin.camerax.CameraManager
import com.example.atr_fd_kotlin.face_detection.FaceContourGraphic
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class Communication(){

    private val client_socket = CameraManager.socket

    fun sendData(data: String) {
        try {
            var jsondata = JSONObject()
            jsondata.put("TAG", "DATA")
            jsondata.put("data", data)
            var dataarray = ("^^^" + jsondata.toString() + "$")
            val thread = Thread {
                var currentSize = 0
                var currentCount = 0

                while (currentSize < dataarray.length) {
                    FaceContourGraphic.image_name = "Working"
                    try {
                        if (currentCount * 1024 + 1024 > dataarray.length) {
                            val dataToSend: String = dataarray.substring(currentCount * 1024)
                            client_socket!!.getOutputStream()
                                .write(dataToSend.toByteArray(StandardCharsets.UTF_8))
                            client_socket!!.getOutputStream().flush()
                            currentSize = dataarray.length
                            Log.d("ts", currentCount.toString())
                        } else {
                            val dataToSend: String =
                                dataarray.substring(currentCount * 1024, currentCount * 1024 + 1024)
                            client_socket!!.getOutputStream()
                                .write(dataToSend.toByteArray(StandardCharsets.UTF_8))
                            client_socket!!.getOutputStream().flush()
                            currentSize += 1024
                            Log.d("ts", currentCount.toString())
                        }
                        currentCount++
                    }
                    catch (e:Exception){

                    }
                }
            }
            thread.start()
        }
        catch (e:Exception){

        }
    }


    fun receiveData() {
        try {
            var stop = true
            val timer = object: CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d("Count Down", "111111111")
                }
                override fun onFinish() {
                    var thread = Thread {
                        var jsondata = JSONObject()
                        jsondata.put("TAG", "DATA")
                        jsondata.put("data", "SHUTDOWN")
                        var dataarray = ("^^^" + jsondata.toString() + "$")
                        if(FaceContourGraphic.image_name == "retrying"){
                            FaceContourGraphic.image_name = "UNKNOWN"
                            client_socket!!.getOutputStream().write(dataarray.toByteArray())
                            client_socket!!.getOutputStream().flush()
                            client_socket!!.getOutputStream().write("$^$".toByteArray())
                            client_socket!!.getOutputStream().flush()
                            stop = false
                        }
                    }
                    thread.start()
                }
            }
            val thread = Thread {
                try {
                    var timerstop = true
                    val `in` = BufferedReader(InputStreamReader(client_socket!!.getInputStream()))
                    var charsRead = 0
                    val buffer = CharArray(1024)
                    while (stop) {
                        charsRead = `in`.read(buffer)
                        val serverMessage = String(buffer).substring(0, charsRead)
                        if (serverMessage != null) {
                            Log.d("Message from Server", serverMessage)
                            FaceContourGraphic.image_name = serverMessage
                            if (serverMessage != "retrying"){
                                client_socket!!.getOutputStream().write("$^$".toByteArray())
                                client_socket!!.getOutputStream().flush()
                                client_socket.close()
                                break
                            }
                            else{
                                MainActivity.cameraManager.capture_image()
                                if(timerstop){
                                    timer.start()
                                    timerstop = false
                                }
                            }
                        }
                    }
                }
                catch (e:Exception){

                }
            }
            thread.start()
        } catch (e: Exception) {

        }
    }

}