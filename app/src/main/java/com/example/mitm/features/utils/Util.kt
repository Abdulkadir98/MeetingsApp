package com.example.mitm.features.utils

import android.content.Context
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.mobile.client.AWSMobileClient
import android.R.attr.countDown
import android.net.Uri
import android.util.Log
import com.amazonaws.mobile.client.Callback
import com.google.android.libraries.places.internal.e
import com.amazonaws.mobile.client.UserStateDetails
import java.util.concurrent.CountDownLatch
import org.json.JSONException
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.regions.Region


object Util {

    private var sS3Client: AmazonS3Client? = null
    private var sMobileClient: AWSCredentialsProvider? = null
    private var sTransferUtility: TransferUtility? = null

    fun getCredProvider(context: Context): AWSCredentialsProvider? {
        if (sMobileClient == null) {
            val latch = CountDownLatch(1)
            AWSMobileClient.getInstance().initialize(context, object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails) {
                    latch.countDown()
                }

                override fun onError(e: Exception) {
                    Log.e("AWS cred provider", "onError: ", e)
                    latch.countDown()
                }
            })
            try {
                latch.await()
                sMobileClient = AWSMobileClient.getInstance()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        return sMobileClient
    }

    fun getS3Client(context: Context): AmazonS3Client? {
        if (sS3Client == null) {
            sS3Client = AmazonS3Client(getCredProvider(context))
            try {
                val regionString = AWSConfiguration(context)
                        .optJsonObject("S3TransferUtility")
                        .getString("Region")
                sS3Client?.setRegion(Region.getRegion(regionString))
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return sS3Client
    }

    fun getTransferUtility(context: Context): TransferUtility? {
        if (sTransferUtility == null) {
            sTransferUtility = TransferUtility.builder()
                    .context(context)
                    .s3Client(getS3Client(context))
                    .awsConfiguration(AWSConfiguration(context))
                    .build()
        }

        return sTransferUtility
    }

}