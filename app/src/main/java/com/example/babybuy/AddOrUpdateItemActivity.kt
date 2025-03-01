package com.example.babybuy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.babybuy.model.Product
import com.example.babybuy.databinding.ActivityAddOrUpdateItemBinding
import com.example.babybuy.model.FirestoreProduct
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException

class AddOrUpdateItemActivity : AppCompatActivity() {
    private lateinit var addOrUpdateItemBinding: ActivityAddOrUpdateItemBinding
    private var isForUpdate = false
    private var imageUriPath = ""
    private var productLocation = ""

    private lateinit var startCustomCameraActivityForResult: ActivityResultLauncher<Intent>
    private lateinit var startGalleryActivityForResult: ActivityResultLauncher<Array<String>>
    private lateinit var startMapActivityForResult: ActivityResultLauncher<Intent>

    companion object{
        const val RESULT_CODE_COMPLETE = 1001
        const val RESULT_CODE_CANCEL = 1002
        const val GALLERY_PERMISSION_REQUEST_CODE = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOrUpdateItemBinding = ActivityAddOrUpdateItemBinding.inflate(layoutInflater)
        setContentView(addOrUpdateItemBinding.root)
        bindCustomCameraActivityForResult()
        bindGalleryActivityForResult()
        bindMapsActivityForResult()

        val db = FirebaseFirestore.getInstance()
        val productsCollection = db.collection("products")

        val receivedIntent = intent;
        val receivedProduct = receivedIntent.getParcelableExtra<Product>(AppConstant.KEY_PRODUCT)
        receivedProduct?.apply {
            isForUpdate = true
            addOrUpdateItemBinding.tieTitle.setText(this.title)
            addOrUpdateItemBinding.tiePrice.setText(this.price)
            addOrUpdateItemBinding.tieDescription.setText(this.description)
            addOrUpdateItemBinding.cbPurchased.isChecked = (this.isPurchased)
            imageUriPath = this.image
            loadThumbnailImage(this.image)
            //TODO for location
        }

        addOrUpdateItemBinding.ibBack.setOnClickListener {
            setResultWithFinish(RESULT_CODE_CANCEL, null)
        }

        addOrUpdateItemBinding.ivAddImage.setOnClickListener {
            handleImageAddButtonClicked()
        }

        addOrUpdateItemBinding.mbLocation.setOnClickListener {
            startMapActivity()
        }

        addOrUpdateItemBinding.mbSubmit.setOnClickListener {
            val title = addOrUpdateItemBinding.tieTitle.text.toString().trim()
            val price = addOrUpdateItemBinding.tiePrice.text.toString().trim()
            val description = addOrUpdateItemBinding.tieDescription.text.toString().trim()
            val isPurchased = addOrUpdateItemBinding.cbPurchased.isChecked

            // Create a flag to track if any validation errors occur
            var hasValidationError = false

            // Validate Title
            if (title.isEmpty()) {
                addOrUpdateItemBinding.tieTitle.error = "Title is required"
                hasValidationError = true
            }

            // Validate Price
            if (price.isEmpty()) {
                addOrUpdateItemBinding.tiePrice.error = "Price is required"
                hasValidationError = true
            }

            // Validate Description
            if (description.isEmpty()) {
                addOrUpdateItemBinding.tieDescription.error = "Description is required"
                hasValidationError = true
            }

            // Check if an image is selected (assuming imageUriPath is a non-empty string)
            if (imageUriPath.isEmpty()) {
                Toast.makeText(this, "Please select an image before proceeding", Toast.LENGTH_SHORT).show()
                hasValidationError = true
            }

            // If any validation error occurred, do not proceed with further actions
            if (hasValidationError) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = Product(
                title = title,
                price = price,
                description = description,
                image = imageUriPath,
                location = productLocation,
                isPurchased = isPurchased
            )

            if (isForUpdate) {
                product.id = receivedProduct!!.id
                productsCollection.document(product.id.toString())
                    .set(product)
                    .addOnSuccessListener {
                        clearFieldsData()
                        showToast("Product updated successfully...")
                        setResultWithFinish(RESULT_CODE_COMPLETE, product)
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        showToast("Couldn't update product. Try again...")
                    }
            } else {
                // Add a new product to Firestore
                productsCollection.add(product)
                    .addOnSuccessListener { documentReference ->
                        product.id = documentReference.id

                        documentReference.update("id", documentReference.id)
                        clearFieldsData()
                        showToast("Product added successfully...")
                        setResultWithFinish(RESULT_CODE_COMPLETE, product)
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        showToast("Couldn't add product. Try again...")
                    }
            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
    private fun clearFieldsData() {
        addOrUpdateItemBinding.tieTitle.text?.clear()
        addOrUpdateItemBinding.tiePrice.text?.clear()
        addOrUpdateItemBinding.tieDescription.text?.clear()
    }

    private fun setResultWithFinish(resultCode: Int, product: Product?) {
        val intent = Intent()
        product?.let {
            intent.putExtra(AppConstant.KEY_PRODUCT, it)
        }
        setResult(resultCode, intent)
        finish()
    }

    private fun handleImageAddButtonClicked() {
        val pickImageBottomSheetDialog = BottomSheetDialog(this)
        pickImageBottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_pick_image)

        val linearLayoutPickByCamera: LinearLayout = pickImageBottomSheetDialog
            .findViewById(R.id.ll_pick_by_camera)!!
        val linearLayoutPickByGallery: LinearLayout = pickImageBottomSheetDialog
            .findViewById(R.id.ll_pick_by_gallery)!!

        linearLayoutPickByCamera.setOnClickListener {
            pickImageBottomSheetDialog.dismiss()
            startCameraActivity()
        }
        linearLayoutPickByGallery.setOnClickListener {
            pickImageBottomSheetDialog.dismiss()
            startGalleryToPickImage()
        }

        pickImageBottomSheetDialog.setCancelable(true)
        pickImageBottomSheetDialog.show()
    }

    private fun startCameraActivity() {
        val intent = Intent(this, CustomCameraActivity::class.java)
        startCustomCameraActivityForResult.launch(intent)
    }

    private fun allPermissionForGalleryGranted(): Boolean {
        var granted = false
        for (permission in getPermissionsRequiredForCamera()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                granted = true
            }
        }
        return granted
    }

    private fun getPermissionsRequiredForCamera(): List<String> {
        val permissions: MutableList<String> = ArrayList()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return permissions
    }

    private fun startGalleryToPickImage() {
        if (allPermissionForGalleryGranted()) {
            startActivityForResultFromGalleryToPickImage()
        } else {
            requestPermissions(
                getPermissionsRequiredForCamera().toTypedArray(),
                GALLERY_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startActivityForResultFromGalleryToPickImage() {
        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
//        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startGalleryActivityForResult.launch(arrayOf("image/*", "video/*"))
    }

    private fun loadThumbnailImage(imageUriPath: String) {
        addOrUpdateItemBinding.ivAddImage.post(Runnable {
            var bitmap: Bitmap?
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    Uri.parse(imageUriPath)
                )
                bitmap = BitmapScalar.stretchToFill(
                    bitmap,
                    addOrUpdateItemBinding.ivAddImage.getWidth(),
                    addOrUpdateItemBinding.ivAddImage.getHeight()
                )
                addOrUpdateItemBinding.ivAddImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    private fun startMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startMapActivityForResult.launch(intent)
    }
    private fun onLocationDataFetched() {
        if (productLocation.isBlank()) {
            return
        }

        try {
            val lat = productLocation.split(",")[0]
            val lng = productLocation.split(",")[1]
            val geoCodedAddress = GeoCoding.reverseTheGeoCodeToAddress(this, lat, lng)
            addOrUpdateItemBinding.mbLocation.text = geoCodedAddress
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        productLocation.isNotBlank().apply {

        }
    }

    private fun bindCustomCameraActivityForResult() {
        startCustomCameraActivityForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CustomCameraActivity.CAMERA_ACTIVITY_SUCCESS_RESULT_CODE) {
                imageUriPath = it.data?.getStringExtra(CustomCameraActivity.CAMERA_ACTIVITY_OUTPUT_FILE_PATH)!!
                loadThumbnailImage(imageUriPath)
            } else {
                imageUriPath = "";
                addOrUpdateItemBinding.ivAddImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun bindGalleryActivityForResult() {
        startGalleryActivityForResult = registerForActivityResult(
            ActivityResultContracts.OpenDocument()) {
            if (it != null) {
                imageUriPath = it.toString()
                contentResolver.takePersistableUriPermission(
                    Uri.parse(imageUriPath),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                loadThumbnailImage(imageUriPath)
            } else {
                imageUriPath = "";
                addOrUpdateItemBinding.ivAddImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun bindMapsActivityForResult() {
        startMapActivityForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == MapsActivity.MAPS_ACTIVITY_SUCCESS_RESULT_CODE) {
                productLocation = it.data?.getStringExtra(AppConstant.KEY_PRODUCT_LOCATION).toString()
                onLocationDataFetched()
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        setResultWithFinish(RESULT_CODE_CANCEL, null)
    }
}