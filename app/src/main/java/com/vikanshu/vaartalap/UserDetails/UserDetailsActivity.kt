package com.vikanshu.vaartalap.UserDetails

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.vikanshu.vaartalap.HomeActivity.HomeActivity
import com.vikanshu.vaartalap.R
import dmax.dialog.SpotsDialog
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var userImage: ImageView
    private lateinit var userName: TextView
    private lateinit var proceed: Button
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var FINAL_USER_IMAGE_URI = "default"
    private lateinit var FINAL_USER_IMAGE_BITMAP: Bitmap
    private var FINAL_USER_NAME = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        userImage = findViewById(R.id.userImageDetails)
        userName = findViewById(R.id.personNameDetails)
        proceed = findViewById(R.id.proceedDetails)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()


        userImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "select image from"), 2102)
        }

        proceed.setOnClickListener { proceed() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2102) {
            CropImage
                .activity(data?.data)
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val compressed =
                    Compressor(this)
                        .setQuality(70)
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .compressToBitmap(File(resultUri.path.toString()))
                userImage.setImageBitmap(compressed)
                FINAL_USER_IMAGE_BITMAP = compressed
                FINAL_USER_IMAGE_URI = "custom"
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                showToast(error.message.toString())
            }
        }
    }


    private fun proceed() {
        val name = userName.text.toString().trim()
        if (name.isNullOrEmpty()) {
            showToast("Please enter your name")
        } else {
            FINAL_USER_NAME = name.trim()
            if (FINAL_USER_IMAGE_URI == "default") {
                val dialog: AlertDialog =
                    SpotsDialog.Builder().setContext(this).setCancelable(false)
                        .setMessage("Please Wait....").build().apply { show() }
                val data = HashMap<String, Any>()
                data["name"] = FINAL_USER_NAME
                data["image"] = FINAL_USER_IMAGE_URI
                data["number"] = auth.currentUser?.phoneNumber.toString()
                firestore.collection("users").document(auth.uid.toString()).set(data)
                    .addOnCompleteListener { t ->
                        dialog.dismiss()
                        if (t.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            this.finish()
                        } else {
                            showToast(t.exception?.localizedMessage.toString())
                        }
                    }
            } else {
                val baos = ByteArrayOutputStream()
                FINAL_USER_IMAGE_BITMAP.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val array: ByteArray = baos.toByteArray() as ByteArray
                val dialog: AlertDialog =
                    SpotsDialog.Builder().setContext(this).setCancelable(false)
                        .setMessage("Please Wait....").build().apply { show() }
                val task = firebaseStorage.reference.child("profile_images")
                    .child(auth.currentUser?.uid.toString() + ".jpeg").putBytes(array)
                task.addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.storage?.downloadUrl?.addOnCompleteListener { v ->
                            if (v.isSuccessful) {
                                showToast("Image Uploaded")
                                FINAL_USER_IMAGE_URI = v.result.toString()
                            } else {
                                showToast("Error saving image, saving default image")
                                FINAL_USER_IMAGE_URI = "default"
                            }
                            val data = HashMap<String, Any>()
                            data["name"] = FINAL_USER_NAME
                            data["image"] = FINAL_USER_IMAGE_URI
                            data["number"] = auth.currentUser?.phoneNumber.toString()
                            firestore.collection("users").document(auth.uid.toString())
                                .set(data)
                                .addOnCompleteListener { t ->
                                    dialog.dismiss()
                                    if (t.isSuccessful) {
                                        startActivity(
                                            Intent(
                                                this,
                                                HomeActivity::class.java
                                            )
                                        )
                                        this.finish()
                                    } else {
                                        showToast(t.exception?.localizedMessage.toString())
                                    }
                                }
                        }
                    } else {
                        showToast("Unable to upload image")
                        showToast(it.exception?.localizedMessage.toString())
                    }

                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}