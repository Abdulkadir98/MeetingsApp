package com.example.mitm.features.signup.signupscreen


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

import android.support.v4.app.Fragment

import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.example.mitm.R
import com.example.mitm.data.source.MeetingsRepository
import com.example.mitm.data.source.remote.MeetingsRemoteDataSource
import com.example.mitm.features.signup.SignUpFragmentActivityInteraction
import com.example.mitm.features.utils.AWSS3Util
import com.example.mitm.features.utils.Constants
import com.example.mitm.features.utils.Constants.Companion.RESULT_LOAD_IMAGE
import com.example.mitm.features.utils.Constants.Companion.TAKE_PHOTO_CODE
import com.example.mitm.features.utils.DelegateExt
import com.example.mitm.features.utils.Util
import org.jetbrains.anko.toast
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class SignUpFragment : Fragment(), SignUpContract.View, EasyPermissions.PermissionCallbacks {


    internal lateinit var FixBitmap: Bitmap
    private lateinit var firstname: EditText
    private lateinit var lastname: EditText
    private lateinit var progressbar: ProgressBar
    private lateinit var addpicture: TextView
    private lateinit var nextBtn: TextView
    private lateinit var profileimage: ImageView
    private lateinit var presenter: SignUpPresenter
    private var mCurrentPhotoPath: String = ""
    private var firstName: String = ""
    private var lastName: String = ""
    private var s3AvatarURL: String = ""

    private lateinit var transferUtility: TransferUtility
    private var userId = -1
    private var fileNameKey = ""


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private lateinit var callback: SignUpFragmentActivityInteraction

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val signView = inflater.inflate(R.layout.fragment_sign_up, container, false)

        firstname = signView.findViewById(com.example.mitm.R.id.firstname)
        lastname = signView.findViewById(com.example.mitm.R.id.lastname)
        addpicture = signView.findViewById(R.id.add_picture)
        progressbar = signView.findViewById(R.id.indeterminateBar)
        nextBtn = signView.findViewById(R.id.nextButton1)
        profileimage = signView.findViewById(R.id.profile_image)

        transferUtility = Util.getTransferUtility(activity?.applicationContext!!)!!
         val id: Int by DelegateExt.intPreference(activity?.applicationContext!!, Constants.USER_ID,
                                Constants.USER_ID_DEFAULT)
        userId = id
        firstname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.checkValiedText(firstname.text.toString().trim(),lastname.text.toString().trim())
                if(firstname.text.toString().length>=30) {
                    firstname.error = "First name should be within 30 letters"
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        lastname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.checkValiedText(firstname.text.toString().trim(), lastname.text.toString().trim())
                if(lastname.text.toString().length >= 30) {
                    lastname.error = "Last name should be within 30 letters"
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        return signView
    }

    override fun navigateToHomeScreen() {
        callback.navigateToHomeScreen()
        activity?.toast("Login successfully")
    }

    override fun displayLoginErrorMessage() {
        activity?.toast("Error logging in!")
        progressbar.visibility = View.GONE
    }

    override fun displayUserImage(url: String) {
        Glide.with(activity?.applicationContext!!)
                .load(url)
                .error(R.mipmap.user_6)
                .circleCrop()
                .into(profileimage)
        progressbar.visibility = View.GONE
    }

    override fun imageUploadSuccess() {

        val accessToken: String by DelegateExt.stringPreference(activity?.applicationContext!!,
                DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)
        presenter.signUp(accessToken)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = SignUpPresenter(MeetingsRepository(MeetingsRemoteDataSource(activity?.applicationContext!!)),this,context!!)
        addpicture.setOnClickListener {
            presenter.onAddPictureClicked()
        }
        nextBtn.setOnClickListener {

            progressbar.visibility = View.VISIBLE
            if (mCurrentPhotoPath.isNotEmpty())
            uploadToAWS(mCurrentPhotoPath)

            else {
                val accessToken: String by DelegateExt.stringPreference(activity?.applicationContext!!,
                        DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)
                presenter.signUp(accessToken)
            }

            if (isInternetOn()) {
                presenter.checkInternetConnection()
                progressbar.visibility = view.visibility

            } else {
                Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setOnNextMenuClickedListener(activity: Activity) {
        callback = activity as SignUpFragmentActivityInteraction
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun isInternetOn():Boolean {
            var connected = false
            try {
                val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val nInfo = cm.activeNetworkInfo
                connected = nInfo != null && nInfo.isAvailable && nInfo.isConnected
                return connected
            } catch (e: Exception) {
                Log.e("Connectivity Exception", e.message)
            }

            return connected

    }



    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {


    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        presenter.onPermissionGranted()

    }




    override fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, Constants.RESULT_LOAD_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            Constants.RESULT_LOAD_IMAGE ->
                if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null && data.data != null) {
                    mCurrentPhotoPath = getPath(data.data)
                    displayUserImage(mCurrentPhotoPath)
                    //uploadToAWS(mCurrentPhotoPath)
                }
            Constants.TAKE_PHOTO_CODE ->
                if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PHOTO_CODE ) {
                    Log.i(TAG, mCurrentPhotoPath)
                    progressbar.visibility = View.VISIBLE
                    displayUserImage(mCurrentPhotoPath)
                    //uploadToAWS(mCurrentPhotoPath )
                }
        }
    }

    override fun onSuccessText() {

        firstName = firstname.text.toString().trim()
        nextBtn.isEnabled = true
            nextBtn.alpha = 1f

    }

    override fun onFailedText() {
        firstname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        nextBtn.isEnabled = false
        nextBtn.alpha = 0.3f
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
        val uri = Uri.fromFile( File(path))
        var `in`: InputStream? = null
        try {
            `in` = context?.contentResolver?.openInputStream(uri)

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
            `in` =  context?.contentResolver?.openInputStream(uri)
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
            Log.e(TAG, "fileexcep $path not found")
        } catch (e: IOException) {
            Log.e(TAG, "ioexcep $path not found")
        }

        return null
    }

    companion object {

        private val TAG = "SignupFragment"
    }


    fun convertURLToBitmap(uri: Uri, mImagePath: String): Bitmap? {
        val parcelFileDescriptor = context!!.getContentResolver().openFileDescriptor(uri, "r")
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

    override fun showPickerDialog() {

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.take_photo)
                .setItems(
                        R.array.take_photo_array
                ) { dialog, which ->
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

    override fun takepic() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            context!!,
                            "com.example.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, Constants.TAKE_PHOTO_CODE)
                }
            }
        }

    }
    //@Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    override fun checkRuntimePermission() {
        val perms = Manifest.permission.WRITE_EXTERNAL_STORAGE       // Do not have permissions, request them now
        if (EasyPermissions.hasPermissions(context!!, perms)) {
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

    fun uploadToAWS(filePath: String?) {

        if (filePath == null) {
            activity?.toast("Could not find file path of image.")
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
                    s3AvatarURL = AWSS3Util().getS3Client(activity?.applicationContext).getResourceUrl(Constants.AWS_BUCKET, fileNameKey)
                    presenter.checkValidAvatarUrl(s3AvatarURL)
                    Log.i(TAG, s3AvatarURL)

                } catch (e: Exception) {

                    Log.e(TAG, e.message)
                }

            }
        }
        override fun onError(id: Int, ex: java.lang.Exception?) {
            Log.e(TAG, "Error during upload: $id", ex)
        }

    }
    fun getPath(uri: Uri): String {
        var uri = uri
        val needToCheckUri: Boolean = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs = mutableListOf<String>()
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(activity?.applicationContext, uri)) {
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
                cursor = activity?.contentResolver?.query(uri, projection.toTypedArray(), selection, selectionArgs.toTypedArray(), null)
                val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor?.moveToFirst()!!) {
                    return cursor.getString(columnIndex!!)
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


    override fun onResume() {
        super.onResume()
        val actionBar = (activity as AppCompatActivity).getSupportActionBar()
        actionBar?.title = ""
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
    }
}
