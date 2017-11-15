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
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import com.xplore.ApiManager
import com.xplore.General
import com.xplore.util.ImageUtil
import com.xplore.R
import com.xplore.TimeManager
import com.xplore.TimeManager.globalTimeStamp
import com.xplore.TimeManager.refreshGlobalTimeStamp
import com.xplore.base.BaseAppCompatActivity
import com.xplore.user.UploadUser
import com.xplore.util.FirebaseUtil.DEFAULT_IMAGE_URL
import com.xplore.util.FirebaseUtil.MIN_AGE
import com.xplore.util.ImageUtil.FILE_PROVIDER_AUTHORITY
import com.xplore.util.ImageUtil.addPictureToGallery
import com.xplore.util.ImageUtil.createImageFile
import com.xplore.util.ImageUtil.getPicturePath
import com.xplore.util.ImageUtil.resizeAndCompressImage
import kotlinx.android.synthetic.main.register_layout.*
import java.io.File
import java.util.*

/*
* Created by Nikaoto on 3/11/2017.
*
* Handles full registration after authentication.
*
*/

open class RegisterActivity : BaseAppCompatActivity(), com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener {

    /* Request Codes */
    private val NONE = 0
    private val CAMERA_PERMISSION_REQUEST_CODE = 1;
    private val GALLERY_PERMISSION_REQUEST_CODE = 2;
    private val ACTION_SNAP_IMAGE = 3

    //
    private val PIC_KILOBYTE_LIMIT = 25

    companion object {
        // Args
        const val ARG_USER_ID = "userId"
        const val ARG_FULL_NAME = "fullName"
        const val ARG_EMAIL = "email"
        const val ARG_PHOTO_URL = "photoUrl"

        @JvmStatic
        fun getStartIntent(context: Context, userId: String, fullName: String?, email: String?,
                           photoUrl: String): Intent
                = Intent(context, RegisterActivity::class.java)
                .putExtra(ARG_USER_ID, userId)
                .putExtra(ARG_FULL_NAME, fullName)
                .putExtra(ARG_EMAIL, email)
                .putExtra(ARG_PHOTO_URL, photoUrl)
    }

    /* Google Api Client stuff (for logout) */
    private val googleApiClient: GoogleApiClient by lazy {
        ApiManager.getGoogleAuthApiClient(this)
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    //

    // Firebase Storage
    private val storageRef = FirebaseStorage.getInstance().reference
    private fun firebaseStorageProfilePicUri(userId: String) = "users/$userId/profile_picture.jpg"

    // Users profile image url
    var imagePath: Uri? = null

    var mobileNumberMessageShown = false

    var bYear: Int = 0
    var bMonth: Int = 0
    var bDay: Int = 0

    private val DBref = FirebaseDatabase.getInstance().reference

    // User data
    open val userId: String by lazy {
        intent.getStringExtra(ARG_USER_ID)
    }
    open val userProfilePicUrl: String by lazy {
        intent.getStringExtra(ARG_PHOTO_URL)
    }
    private val userFullName: String by lazy {
        intent.getStringExtra(ARG_FULL_NAME)
    }
    private val userEmail: String by lazy {
        intent.getStringExtra(ARG_EMAIL)
    }
    //

    init {
        refreshGlobalTimeStamp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        General.setRegistrationFinished(this, false);

        initLayout()
        fillFields()
        initClickEvents()
    }

    open fun initLayout() {
        setContentView(R.layout.register_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.register)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        //FirebaseUtil.logOut(this, googleApiClient)
        finish()
    }

    open fun fillFields() {
        // Loading data into views
        fnameEditText.safeSetText(separateFullName(userFullName, 0))
        lnameEditText.safeSetText(separateFullName(userFullName, 1))
        emailEditText.safeSetText(userEmail)
        emailEditText.isEnabled = false
        emailEditText.isFocusable = false
    }

    open fun initClickEvents() {
        initProfileImage(userProfilePicUrl)

        birthDateTextView.setOnClickListener {
            onBirthDateSelected(globalTimeStamp)
        }

        mobileNumberEditText.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                showMobileNumberReason()
            }
            false
        }

        doneButton.setOnClickListener {
            onDoneButtonClick()
        }
    }

    open fun showMobileNumberReason() {
        if (!mobileNumberMessageShown) {
            mobileNumberMessageShown = true
            AlertDialog.Builder(this)
                    .setTitle(R.string.mobile_number_reason_title)
                    .setMessage(R.string.mobile_number_reason_message)
                    .setPositiveButton(R.string.okay, null)
                    .create().show()
        }
    }

    open fun onBirthDateSelected(timeStamp: Long, offSet: Int = MIN_AGE) {
        val c = Calendar.getInstance()
        c.time = Date(TimeManager.globalTimeStamp)
        val year = c.get(Calendar.YEAR) - offSet
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        SpinnerDatePickerDialogBuilder()
                .context(this)
                .callback(this)
                .spinnerTheme(R.style.NumberPickerStyle)
                .year(year)
                .monthOfYear(month)
                .dayOfMonth(day)
                .build()
                .show();
    }

    open fun initProfileImage(userProfilePicUrl: String?) {
        Picasso.with(this@RegisterActivity).invalidate(userProfilePicUrl)

        if (userProfilePicUrl == null || userProfilePicUrl == "null"
                || userProfilePicUrl.isEmpty()) {
            Picasso.with(this@RegisterActivity)
                    .load(R.drawable.user_default_profile_image)
                    .transform(ImageUtil.mediumCircle(this))
                    .into(profileImageView)
        } else {
            Picasso.with(this@RegisterActivity)
                    .load(userProfilePicUrl)
                    .transform(ImageUtil.mediumCircle(this))
                    .into(profileImageView)
        }

        profileImageView.setOnClickListener {
            val dialog = AlertDialog.Builder(this).setTitle(R.string.take_picture_from)
                    .setNegativeButton(R.string.camera, takeFromCamera)
                    .setPositiveButton(R.string.gallery, takeFromGallery)
                    .setNeutralButton(R.string.cancel, null)
                    .create()
            dialog.show()
        }
    }

    open fun onDoneButtonClick() {
        if (fieldsValid()) {
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show()
            val ref = storageRef.child(firebaseStorageProfilePicUri(userId))
            val newUser = UploadUser(
                    userId,
                    fnameEditText.str(),
                    lnameEditText.str(),
                    mobileNumberEditText.str(),
                    emailEditText.str(),
                    userProfilePicUrl,
                    0,
                    General.getDateInt(bYear, bMonth, bDay)
            )
            if (imagePath != null) {
                uploadUserData(newUser, imagePath as Uri, ref)
            } else {
                addUserEntryToDataBase(newUser)
            }
        }
    }

    // Uploads user profile pic and saves new link. Finally, uploads the user data to db
    private fun uploadUserData(user: UploadUser, input: Uri, output: StorageReference) {
        output.putFile(input)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                        user.profile_picture_url = taskSnapshot.downloadUrl.toString()
                        addUserEntryToDataBase(user)
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.fail_profile_picture_upload, Toast.LENGTH_SHORT)
                            .show()
                }
    }

    // Adds zero to Day or Month number if needed
    fun addZero(num: Int) = if(num < 10) "0$num" else "$num"

    override fun onDateSet(view: com.tsongkha.spinnerdatepicker.DatePicker?, year: Int,
                           receivedMonth: Int, day: Int) {

        val month  = receivedMonth + 1 //+1 is necessary because 0 is January
        if (General.isNetConnected(this@RegisterActivity)) {

            //Checking if age is OK
            if (General.calculateAge(globalTimeStamp, year, month, day) >= MIN_AGE) {
                bYear = year
                bMonth = month
                bDay = day

                birthDateTextView.text = "$bYear/${addZero(bMonth)}/${addZero(bDay)}"
            } else {
                val res = this@RegisterActivity.resources
                Toast.makeText(this@RegisterActivity, res.getString(R.string.you_must_be_at_least) +
                        " " + MIN_AGE + " " + res.getString(R.string.years_to_use_xplore),
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            General.createNetErrorDialog(this@RegisterActivity)
        }
    }

    // Uploads all textual user data to Firebase
    open fun addUserEntryToDataBase(user: UploadUser) {
        if (user.profile_picture_url == "null" || user.profile_picture_url.isEmpty()) {
            user.profile_picture_url = DEFAULT_IMAGE_URL
        }
        val userData = user.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates.put("/users/" + user.id, userData)
        DBref.updateChildren(childUpdates)
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun makeBorderGreen(v: View) =  v.setBackgroundResource(R.drawable.edit_text_border)
    fun makeBorderRed(v: View) =  v.setBackgroundResource(R.drawable.edit_text_border_red)

    private fun fieldsValid(): Boolean {
        makeBorderGreen(fnameEditText)
        makeBorderGreen(lnameEditText)
        makeBorderGreen(emailEditText)
        makeBorderGreen(mobileNumberEditText)
        makeBorderGreen(birthDateTextView)

        if (fnameEditText.text.isEmpty()) {
            return fieldError(fnameEditText)
        } else if (lnameEditText.text.isEmpty()) {
            return fieldError(lnameEditText)
        } else if (emailEditText.text.isEmpty()) {
            return fieldError(emailEditText)
        } else if (!General.isValidEmail(emailEditText.text)) {
            makeBorderRed(emailEditText)
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            return false
        } else if (mobileNumberEditText.text.isEmpty()) {
            return fieldError(mobileNumberEditText)
        } else if (birthDateTextView.text.isEmpty()) {
            return fieldError(birthDateTextView)
        } else if (bYear == 0 || bMonth == 0 || bDay == 0) {
            return fieldError(birthDateTextView)
        }
        return true
    }

    private fun scrollToView(v: View) {
        scrollView.post{
            scrollView.smoothScrollTo(0, v.bottom)
        }
    }

    private fun fieldError(v: View): Boolean {
        makeBorderRed(v)
        scrollToView(v)

        Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show()
        return false
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

    fun EditText.str() = this.text.trim().toString()

    //
    //
    //
    /* Everything below is code needed for profile picture choosing/taking functionality */
    //
    //
    //

    // Requests permissions for given module with the passed request code and permissionType
    fun requestModulePermission(activity: Activity, permission: String, requestCode: Int,
                                module: () -> Unit) {
        //Checking if not allowed
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            //Open dialogue and request the permission
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        } else {
            module()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                CAMERA_PERMISSION_REQUEST_CODE -> prepareCamera()
                GALLERY_PERMISSION_REQUEST_CODE -> Crop.pickImage(this)
            }
        }
    }

    val takeFromCamera = DialogInterface.OnClickListener {
        _, _ ->
            requestModulePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    CAMERA_PERMISSION_REQUEST_CODE, { prepareCamera() })

    }

    // Prepares extra permissions for camera and then opens it (kind of hacky)
    fun prepareCamera() {
        requestModulePermission(this, Manifest.permission.CAMERA,
                CAMERA_PERMISSION_REQUEST_CODE, { openCamera() })
    }

    fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoURI = createImageFile(this).getUri()
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

        /* Picking or Snapping picture */
        when (requestCode) {
            ACTION_SNAP_IMAGE -> {
                if(resultCode == Activity.RESULT_OK && imagePath != null) {
                    val temp = createImageFile(this)
                    cropImage(imagePath as Uri, temp.getUri())
                    imagePath = Uri.parse(getPicturePath(this, temp.name))
                    addPictureToGallery(this, imagePath.toString())
                }
            }
            Crop.REQUEST_PICK -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val tempFile = createImageFile(this)
                    imagePath = Uri.parse(getPicturePath(this, tempFile.name))
                    cropImage(data.data, tempFile.getUri())
                }
            }
            Crop.REQUEST_CROP -> {
                if (data != null) {
                    val tempPath = resizeAndCompressImage(this, imagePath.toString())
                    val tempFile = File(tempPath)
                    imagePath = tempFile.getUri()
                    Picasso.with(this)
                            .load(tempFile)
                            .transform(ImageUtil.mediumCircle(this))
                            .into(profileImageView)
                }
            }
        }
    }

    private fun File.getUri(): Uri {
        return FileProvider.getUriForFile(this@RegisterActivity, FILE_PROVIDER_AUTHORITY, this)
    }

    // Starts cropping activity with code Crop.REQUEST_CROP
    private fun cropImage(input: Uri, output: Uri) {
        return Crop.of(input, output).asSquare().start(this)
    }

    private fun TextView.safeSetText(s: String?) {
        if (s != null) {
            this.text = s
        } else {
            this.text = ""
        }
    }
}
