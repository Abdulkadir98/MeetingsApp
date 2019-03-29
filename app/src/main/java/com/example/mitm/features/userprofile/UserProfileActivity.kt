package com.example.mitm.features.userprofile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.example.mitm.R
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.features.signup.SignUpActivity
import com.example.mitm.features.utils.AWSS3Util
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.DelegateExt
import com.example.mitm.features.utils.Util
import kotlinx.android.synthetic.main.activity_user_profile.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class UserProfileActivity : AppCompatActivity(), UserProfileContract.View {
    private var mCurrentPhotoPath: String = ""

    private lateinit var presenter: UserProfilePresenter

    private lateinit var transferUtility: TransferUtility

    var userId: Int by DelegateExt.intPreference(this, Constants.USER_ID, Constants.USER_ID_DEFAULT)
    private var avatarUrl: String by DelegateExt.stringPreference(this, Constants.USER_AVATAR_URL, Constants.USER_AVATAR_URL_DEFAULT)
    private var userFirstName: String by DelegateExt.stringPreference(this, Constants.USER_FIRST_NAME, Constants.USER_FIRST_NAME_DEFAULT)
    private var userLastName: String by DelegateExt.stringPreference(this, Constants.USER_LAST_NAME, Constants.USER_LAST_NAME_DEFAULT)


    var fileNameKey = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        presenter = UserProfilePresenter(MeetingsRepository(MeetingsRemoteDataSource(this)), this, this)
        transferUtility = Util.getTransferUtility(this)!!

        Glide.with(this)
                .load(avatarUrl)
                .error(R.mipmap.user_6)
                .circleCrop()
                .into(profileImageIv)

        userNameTv.text = "$userFirstName $userLastName"
        Log.i("User Profile", "$userFirstName")

        changePictureTv.setOnClickListener {
            presenter.onAddPictureClicked()
        }

        logoutBtn.setOnClickListener {
            presenter.logout()

        }


    }

    override fun navigateToPhoneAuth() {
        finishAffinity()
        startActivity<SignUpActivity>()

    }

    override fun displayErrorMessageForLoadingImage() {
        toast("Error in loading image")
    }

    override fun displayUserImage(url: String) {
        Glide.with(this)
                .load(url)
                .error(R.mipmap.user_6)
                .circleCrop()
                .into(profileImageIv)

        progressBar.visibility = View.GONE
    }

    @AfterPermissionGranted(Constants.RC_STORAGE)
    override fun checkRuntimePermission() {
        val perms = Manifest.permission.WRITE_EXTERNAL_STORAGE       // Do not have permissions, request them now
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            presenter.alreadyHasStoragePermission()
        } else {
            // Do not have permissions, request them now


            EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, Constants.RC_STORAGE, perms)
                            .setRationale(getString(R.string.storage_rationale))
                            .setPositiveButtonText(getString(R.string.rationale_ask_ok))
                            .setNegativeButtonText(getString(R.string.rationale_ask_cancel))
                            .build())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun isInternetOn(): Boolean {

        return true
    }

    override fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.RESULT_LOAD_IMAGE)
    }

    override fun showPickerDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.take_photo)
                .setItems(
                        R.array.take_photo_array
                ) { _, which ->
                    when (which) {
                        0 -> {
                            presenter.onCameraPicked()
                        }
                        1 -> {

                            presenter.onGalleryPicked()
                        }
                    }
                }
        builder.create()
        builder.create().show()
    }

    override fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(this.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, Constants.TAKE_PHOTO_CODE)
                }
            }
        }

    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.RESULT_LOAD_IMAGE ->
                if (resultCode == Activity.RESULT_OK && requestCode == Constants.RESULT_LOAD_IMAGE && data != null && data.data != null) {

                   // val bitmap = convertURLToBitmap(Uri.parse(data.data.toString()), data.data.toString())
                    //profileImageIv.setImageBitmap(bitmap)
                    mCurrentPhotoPath = getPath(data.data)
                    uploadToAWS(mCurrentPhotoPath)
                }
            Constants.TAKE_PHOTO_CODE ->
                if (resultCode == Activity.RESULT_OK && requestCode == Constants.TAKE_PHOTO_CODE) {
                    Log.i("User Profile", mCurrentPhotoPath)
                    progressBar.visibility = View.VISIBLE
                    uploadToAWS(mCurrentPhotoPath)
                }
        }
    }

    fun convertURLToBitmap(uri: Uri, mImagePath: String): Bitmap? {
        val parcelFileDescriptor = this.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor.getFileDescriptor()

        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(mImagePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var orientation = 0
        if (ei != null) {
            orientation = ei!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        }

        var rotatedBitmap = bitmap
        when (orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = TransformationUtils.rotateImage(bitmap, 90)

            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = TransformationUtils.rotateImage(bitmap, 180)

            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = TransformationUtils.rotateImage(bitmap, 270)

            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
            else -> rotatedBitmap = bitmap
        }

        return rotatedBitmap
    }

    private fun getBitmap(path: String): Bitmap? {
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var orientation = 0
        if (ei != null) {
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        }
        val uri = Uri.fromFile(File(path))
        var `in`: InputStream? = null
        try {
            `in` = this.contentResolver?.openInputStream(uri)

            //Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true

            BitmapFactory.decodeStream(`in`, null, o)
            `in`!!.close()

            var scale = 1
            if (o.outHeight > Constants.IMAGE_MAX_SIZE || o.outWidth > Constants.IMAGE_MAX_SIZE) {
                scale = Math.pow(2.0, Math.round(Math.log(Constants.IMAGE_MAX_SIZE / Math.max(o.outHeight, o.outWidth).toDouble()) / Math.log(0.5)).toInt().toDouble()).toInt()
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            `in` = this.contentResolver?.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(`in`, null, o2)
            `in`!!.close()
            var rotatedBitmap = bitmap
            rotatedBitmap = when (orientation) {

                ExifInterface.ORIENTATION_ROTATE_90 -> TransformationUtils.rotateImage(bitmap, 90)

                ExifInterface.ORIENTATION_ROTATE_180 -> TransformationUtils.rotateImage(bitmap, 180)

                ExifInterface.ORIENTATION_ROTATE_270 -> TransformationUtils.rotateImage(bitmap, 270)

                ExifInterface.ORIENTATION_NORMAL -> bitmap
                else -> bitmap
            }
            return rotatedBitmap
        } catch (e: FileNotFoundException) {
            Log.e("User Profile", "fileexcep $path not found")
        } catch (e: IOException) {
            Log.e("User Profile", "ioexcep $path not found")
        }
        return null
    }


    fun uploadToAWS(filePath: String?) {

        if (filePath == null) {
            toast("Could not find file path of image.")
            return
        }

        val file = File(filePath)
        val currentTimestamp = System.currentTimeMillis()
        fileNameKey = "${userId}_${currentTimestamp}.jpg"

        val observer = transferUtility.upload(
                fileNameKey, file
        )

        observer.setTransferListener(UploadListener())
    }

    inner class UploadListener : TransferListener {
        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

        }

        override fun onStateChanged(id: Int, state: TransferState?) {
            if (state!! == TransferState.COMPLETED) {

                try {
                    val s3AvatarURL = AWSS3Util().getS3Client(applicationContext).getResourceUrl(Constants.AWS_BUCKET, fileNameKey)
                    presenter.checkValidAvatarUrl(s3AvatarURL)
                    Log.i("User Profile", s3AvatarURL)
                } catch (e: Exception) {
//                        if (avatarURL != null) {
//                            s3AvatarURL = avatarURL!!
//                        }
                }

            }
        }

        override fun onError(id: Int, ex: java.lang.Exception?) {
            Log.e("User Profile", "Error during upload: $id", ex)
        }
    }

    fun getPath(uri: Uri): String {
        var uri = uri
        val needToCheckUri: Boolean = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs = mutableListOf<String>()
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    uri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), id.toLong())
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri);
                    val split = docId.split(":")
                    val type = split[0]
                    when (type) {
                        "image" -> uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs.add(split[1])
                }
            }
        }
        if ("content".equals(uri.scheme, true)) {
             val projection = listOf(MediaStore.Images.Media.DATA)

             var cursor: Cursor?
            try {
                cursor = contentResolver
                        .query(uri, projection.toTypedArray(), selection, selectionArgs.toTypedArray(), null)
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
                cursor.close()
            } catch (e: Exception) {
            }
        } else if ("file".equals(uri.scheme, true)) {
            return uri.path!!
        }

        return ""
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority

    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority

    }
}
