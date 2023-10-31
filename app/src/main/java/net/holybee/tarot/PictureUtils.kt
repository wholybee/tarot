package net.holybee.tarot

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.widget.ImageButton
import androidx.core.view.doOnLayout
import kotlin.math.roundToInt


private const val TAG="PictureUtils"

fun getScaledBitmap(assetManager: AssetManager?, fileName: String, destWidth: Int, destHeight: Int): Bitmap? {


    val inputStream = assetManager?.open(fileName)
    // Read in the dimensions of the image on disk

    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeStream(inputStream, null, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()


    // Figure out how much to scale down by
    val sampleSize = if (srcHeight <= destHeight && srcWidth <= destWidth) {
        1
    } else {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        minOf(heightScale, widthScale).roundToInt()
    }
    // Read in and create final bitmap

    val is2 = assetManager?.open(fileName)
    return BitmapFactory.decodeStream(is2, null, BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    })

}

fun setCardPicture(context:Context, imageButton: ImageButton, fileName: String?) {
    val assetManager = context.assets


    if ((imageButton.tag != fileName) && (fileName != null)) {
        imageButton.doOnLayout { measuredView ->
            val scaledBitmap = getScaledBitmap(
                assetManager,
                fileName,
                measuredView.width,
                measuredView.height
            )
            if (scaledBitmap != null) {
                imageButton.setImageBitmap(context.let { toRoundCorner(it, scaledBitmap, 6f) })
                imageButton.tag = fileName
            } else {
                imageButton.setImageBitmap(null)
                imageButton.tag = null
            }
        }
    }
}
fun toRoundCorner(context: Context, bitmap: Bitmap, dp: Float): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)
    val roundPx = convertDpToPx(context, dp)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun convertDpToPx(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}