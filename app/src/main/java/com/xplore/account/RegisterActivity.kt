package com.xplore.account

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast

import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import com.xplore.General
import com.xplore.ImageUtil
import com.xplore.R

import java.util.HashMap

import com.xplore.TimeManager.Companion.globalTimeStamp
import com.xplore.TimeManager.Companion.refreshGlobalTimeStamp
import com.xplore.user.User
import kotlinx.android.synthetic.main.register_layout.*
import java.io.File
import java.io.IOException

/**
* Created by Nikaoto on 3/11/2017.
* TODO write description of this class - what it does and why.
*/

class RegisterActivity : Activity(), DatePickerDialog.OnDateSetListener {

    /* Request Codes */
    val NONE = 0
    val CAMERA_PERMISSION_REQUEST_CODE = 1;
    val GALLERY_PERMISSION_REQUEST_CODE = 2;
    val ACTION_SNAP_IMAGE = 3

    // File provider authority string
    val authority = "com.xplore.fileprovider"

    //TODO add age restriction constant to resources
    private val ageRestriction: Int = 16
    private var bYear: Int = 0
    private var bMonth: Int = 0
    private var bDay: Int = 0

    init {
        refreshGlobalTimeStamp()
    }

    internal val DBref = FirebaseDatabase.getInstance().reference

    companion object {
        @JvmStatic
        fun getStartIntent(context: Context, userId: String, fullName: String?, email: String?,
                           photoUrl: Uri?): Intent
                = Intent(context, RegisterActivity::class.java)
                    .putExtra("userId", userId)
                    .putExtra("fullName", fullName)
                    .putExtra("email", email)
                    .putExtra("photoUrl", photoUrl.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        //Getting User info from SignInActivity
        val userId = intent.getStringExtra("userId")
        val userFullName = intent.getStringExtra("fullName")
        Log.println(Log.INFO, "firebaseuser", "User FullName = $userFullName")

        val userEmail = intent.getStringExtra("email")
        val userProfilePicUrl = intent.getStringExtra("photoUrl")

        //Loading data into views
        fnameEditText.setText(separateFullName(userFullName, 0))
        lnameEditText.setText(separateFullName(userFullName, 1))
        emailEditText.setText(userEmail)

        //Birth date selector
        bdateTextView.setOnClickListener {
            //Creating new DialogFragment
            val fragment = com.xplore.CustomDatePicker(this, globalTimeStamp, ageRestriction)
            fragment.show(fragmentManager, "datePicker")
        }
        //TODO AI check for face in photo?

        if(userProfilePicUrl != "") {
            Picasso.with(this@RegisterActivity)
                    .load(userProfilePicUrl)
                    .transform(ImageUtil.mediumCircle(this))
                    .into(profileImageView)
        }

        profileImageView.setOnClickListener {
            //TODO string resources
            val dialog = AlertDialog.Builder(this).setTitle("Take picture from..")
                    .setNegativeButton("Camera", takeFromCamera)
                    .setPositiveButton("Gallery", takeFromGallery)
                    .setNeutralButton(R.string.cancel, null)
                    .create()
            dialog.show()
        }

        doneButton.setOnClickListener {
            if (checkFields()) {
                //TODO upload image to db if changed
                addUserEntryToDataBase(
                        UploadUser(userId, fnameEditText.str(), lnameEditText.str(),
                                numEditText.str(), userEmail,
                                General.getDateLong(bYear, bMonth, bDay), userProfilePicUrl)
                )
            }
        }
    }

    //Adds zero to Day or Month number if needed
    fun addZero(num: Int) = if(num < 10) "0$num" else "$num"

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        if (General.isNetConnected(this@RegisterActivity)) {

            //Checking if age is OK
            if (General.calculateAge(globalTimeStamp, year, month, day) >= ageRestriction) {
                bYear = year
                bMonth = month
                bDay = day

                bdateTextView.text = "$bYear/${addZero(bMonth)}/${addZero(bDay)}"
            } else {
                //TODO string resources
                Toast.makeText(this@RegisterActivity,
                        "You must be of age $ageRestriction to use Xplore",
                        Toast.LENGTH_SHORT).show()
            }
        } else
            General.createNetErrorDialog(this@RegisterActivity)
    }

    private fun addUserEntryToDataBase(user: UploadUser) {
        val userData = user.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/users/" + user.uid, userData)
        DBref.updateChildren(childUpdates)
        setResult(Activity.RESULT_OK)
        finish()
    }

    //TODO change init after converting User class to kotlin
    private inner class UploadUser(val uid: String, fname: String, lname: String, tel_num: String,
                                   email: String, birth_date: Int,
                                   profile_picture_url: String? = "") : User() {

        init {
            this.id = uid
            this.fname = fname
            this.lname = lname
            this.tel_num = tel_num;
            this.email = email
            this.birth_date = birth_date

            //Setting profile picture URL
            if (profile_picture_url != null) {
                this.profile_picture_url = profile_picture_url
            }

            //Every new user starts with 0 reputation
            this.reputation = 0
        }

        @Exclude
        fun toMap(): Map<String, Any> {
            val result = HashMap<String, Any>()
            result.put("fname", this.fname)
            result.put("lname", this.lname)
            result.put("profile_picture_url", this.profile_picture_url)
            result.put("birth_date", this.birth_date)
            result.put("tel_num", this.tel_num)
            result.put("reputation", this.reputation)
            result.put("email", this.email)
            return result
        }
    }

    private fun checkFields(): Boolean {
        //TODO check fields
        return true
    }

    //Splits a full name and returns the i-th part of it. Returns fullName back if it has no space
    private fun separateFullName(fullName: String?, i: Int): String {
        if (fullName == null) {
            return ""
        } else {
            var name = arrayOf<String>(fullName,"")
            if (fullName.contains(" "))
                name = fullName.split(" ".toRegex(), 2).toTypedArray()

            return name[i]
        }
    }

    fun EditText.str() = this.text.toString() //TODO take this to general

    /* Everything below is code needed for profile picture choosing or taking functionality */

    var imagePath: Uri? = null

    //Requests permissions for given module with the passed request code and permissionType
    fun requestModulePermission(activity: Activity, permission: String, requestCode: Int,
                                module: () -> Unit) {
        //Checking if not allowed
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            //Open dialogue requesting permission
            Log.println(Log.INFO, "camera", "requesting permission")

            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        } else {
            module()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>?,
                                            grantResults: IntArray?) {
        if (grantResults != null && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            when (requestCode) {
                CAMERA_PERMISSION_REQUEST_CODE -> prepareCamera()
                GALLERY_PERMISSION_REQUEST_CODE -> Crop.pickImage(this)
            }
        }
    }

    val takeFromCamera = DialogInterface.OnClickListener {
        _, _ ->
        Log.println(Log.INFO, "camera", "write_ext_storage permission")
            requestModulePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    CAMERA_PERMISSION_REQUEST_CODE, { prepareCamera() })

    }

    // Prepares extra permissions for camera and then opens it(kind of hacky)
    fun prepareCamera() {
        Log.println(Log.INFO, "camera", "preparing Camera")
        Log.println(Log.INFO, "camera", "camera permission")
        requestModulePermission(this, Manifest.permission.CAMERA,
                CAMERA_PERMISSION_REQUEST_CODE, { openCamera() })
    }

    fun openCamera() {
        Log.println(Log.INFO, "camera", "opening Camera")

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File
            try {
                photoFile = createImageFile();
            } catch (ex: IOException) {
                throw ex
            }
            val photoURI = FileProvider.getUriForFile(this, authority, photoFile)
            imagePath = photoURI
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, ACTION_SNAP_IMAGE);
        }
    }

    val takeFromGallery = DialogInterface.OnClickListener {
        _, _ ->
            requestModulePermission(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                    GALLERY_PERMISSION_REQUEST_CODE, { Crop.pickImage(this) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            super.onActivityResult(requestCode, resultCode, data)
        }

        /* Picking or Taking picture */
        when (requestCode) {
            ACTION_SNAP_IMAGE -> {
                if(resultCode == Activity.RESULT_OK && imagePath != null) {
                    cropImage(imagePath as Uri, imagePath as Uri)
                    addPictureToGallery(imagePath.toString())
                }
            }
            Crop.REQUEST_PICK -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val photoURI = FileProvider.getUriForFile(this, authority, createImageFile())
                    cropImage(data.data, photoURI)
                }
            }
            Crop.REQUEST_CROP -> {
                if (data != null) {
                    Picasso.with(this)
                            .load(Crop.getOutput(data))
                            .transform(ImageUtil.mediumCircle(this))
                            .into(profileImageView)
                    imagePath = Crop.getOutput(data)
                }
            }
        }
    }

    //Starts cropping activity with code Crop.REQUEST_CROP
    fun cropImage(input: Uri, output: Uri) = Crop.of(input, output).asSquare().start(this)

    // Returns temporary file for storing cropped picture
    @Throws(IOException::class)
    fun createImageFile(): File {
        val imageFileName = "profile_pic_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return imageFile
    }

    /* add picture to gallery */
    private fun addPictureToGallery(picturePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(picturePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }
}
