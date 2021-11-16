package com.magtonic.magtonicwipposition.api

import android.util.Log
import com.google.gson.Gson
import com.magtonic.magtonicwipposition.model.send.HttpGetPositionPara
import com.magtonic.magtonicwipposition.model.send.HttpUpdatePositionPara
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiFunc {
    private val mTAG = ApiFunc::class.java.name
    private val baseIP = "http://192.1.1.121/webs.asmx/"

    private val apiStrGetPosition = baseIP + "webs_app_tc_pmn01"

    private val apiStrSetPositionUpdate = baseIP + "webs_app_tc_pmo01"

    private object ContentType {

        const val title = "Content-Type"
        const val xxxForm = "application/x-www-form-urlencoded"

    }//ContentType

    fun getPosition(para: HttpGetPositionPara, callback: Callback) {
        Log.e(mTAG, "ApiFunc->getPosition")
        postWithParaPJsonStrandTimeOut(apiStrGetPosition, Gson().toJson(para), callback)

    }

    fun updatePositionDate(para: HttpUpdatePositionPara, callback: Callback) {
        Log.e(mTAG, "ApiFunc->updatePositionDate")
        postWithParaPJsonStrandTimeOut(apiStrSetPositionUpdate, Gson().toJson(para), callback)

    }

    private fun postWithParaPJsonStr(url: String, jsonStr: String, callback: Callback) {
        Log.e(mTAG, "->postWithParaPJsonStr")
        Log.e(mTAG, "send jsonStr = $jsonStr")
        val body = FormBody.Builder()
            .add("p_json", jsonStr)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(ContentType.title, ContentType.xxxForm)
            .build()

        val client = OkHttpClient().newBuilder()
            .retryOnConnectionFailure(false)
            .build()

        try {
            val response = client.newCall(request).enqueue(callback)
            Log.d("postWithParaPJsonStr", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()

        } catch (e: IOException) {



            e.printStackTrace()
        }

    }

    private fun postWithParaPJsonStrandTimeOut(url: String, jsonStr: String, callback: Callback) {
        Log.e(mTAG, "->postWithParaPJsonStrandTimeOutOutSource")


        val body = FormBody.Builder()
            .add("p_json", jsonStr)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader(ContentType.title, ContentType.xxxForm)
            .build()



        val client = OkHttpClient().newBuilder()
            //.connectTimeout(5000, TimeUnit.MILLISECONDS) //5 secs
            //.readTimeout(5000, TimeUnit.MILLISECONDS) //5 secs
            //.writeTimeout(5000, TimeUnit.MILLISECONDS) //5 secs
            .connectTimeout(10, TimeUnit.SECONDS) //5 secs
            .readTimeout(10, TimeUnit.SECONDS) //5 secs
            .writeTimeout(10, TimeUnit.SECONDS) //5 secs
            .retryOnConnectionFailure(false)
            .build()


        try {
            val response = client.newCall(request).enqueue(callback)



            Log.d("pPara_pjson_timeout", "response = $response")

            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
            client.cache?.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }


    }
}