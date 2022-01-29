package com.application.memeshare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.number.NumberFormatter.with
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import android.os.Environment
import android.os.StrictMode
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import java.io.IOException
import com.bumptech.glide.request.target.Target as Target1
import androidx.core.content.ContextCompat.startActivity

import com.bumptech.glide.GenericTransitionOptions.with
import com.bumptech.glide.Glide.with
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.with
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.with
import com.squareup.picasso.Picasso.LoadedFrom


class MainActivity : AppCompatActivity() {

    var currentImageUrl : String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        loadMeme();

    }

    private fun loadMeme()
    {
        val queue = MySingleton.getInstance(this.applicationContext).requestQueue
        progressBarID.visibility = View.VISIBLE

       // val queue = Volley.newRequestQueue(this);
        val url = "https://meme-api.herokuapp.com/gimme"
        val memeimage = findViewById<ImageView>(R.id.memeimageviewid)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
             { response ->
                currentImageUrl = response.getString("url")
                Glide.with(this).load(currentImageUrl).listener(
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target1<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBarID.visibility = View.GONE
                            return false;
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target1<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBarID.visibility = View.GONE
                            return false;

                        }

                    }
                ).into(memeimageviewid)

            },
             { error ->
                Toast.makeText(this, "Soemthing went wrong", Toast.LENGTH_LONG).show()
            }
        )

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
        queue.add(jsonObjectRequest)

    }

    fun shareMeme(view: View) {
       //

        val memeimage = findViewById<ImageView>(R.id.memeimageviewid)
        val bmpUri: Uri? = getLocalBitmapUri(memeimage)

        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            shareIntent.type = "image/*"
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        }
        else
        {
            val intent = Intent(Intent.ACTION_SEND).setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT,  currentImageUrl);
            val chooser = Intent.createChooser(intent, "Share using ... ")
            startActivity(chooser)
        }

        /*
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpg"
        val photoFile = File(memeimageviewid.drawable.toBitmap().toString())
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile))
        startActivity(Intent.createChooser(shareIntent, "Share image using"))

         */

    }

    fun nextMeme(view: View) {
        loadMeme();
    }

    fun getLocalBitmapUri(imageView: ImageView): Uri? {
        // Extract Bitmap from ImageView drawable
        val drawable = imageView.drawable
        var bmp: Bitmap? = null
        bmp = if (drawable is BitmapDrawable) {
            (imageView.drawable as BitmapDrawable).bitmap
        } else {
            return null
        }
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    // Method when launching drawable within Glide
    fun getBitmapFromDrawable(bmp: Bitmap): Uri? {

        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(
                this,
                "com.codepath.fileprovider",
                file
            ) // use this version for API >= 24

            // **Note:** For API < 24, you may use bmpUri = Uri.fromFile(file);
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteCache(this)
    }
    fun deleteCache(context: Context) {
        try {
            val dir: File = context.getCacheDir()
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

}