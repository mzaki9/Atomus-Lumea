package com.example.lumea.data.sensor

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.lumea.domain.model.PpgReading
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class PpgAnalyzer(
    private val onPpgReading: (PpgReading) -> Unit
) : ImageAnalysis.Analyzer {
    private val TAG = "PpgAnalyzer"
    
    // Supported formats are YUV_420_888, YUV_422_888, YUV_444_888, RGBA_8888
    override fun analyze(image: ImageProxy) {
        try {
            // Skip unsupported formats
            if (image.format != ImageFormat.YUV_420_888 &&
                image.format != ImageFormat.YUV_422_888 &&
                image.format != ImageFormat.YUV_444_888) {
                Log.e(TAG, "Unsupported format: ${image.format}")
                image.close()
                return
            }
            
            // Get timestamp for this frame
            val timestamp = System.currentTimeMillis()
            
            // Calculate means for each color channel
            val (redMean, greenMean, blueMean, avgIntensity) = extractColorData(image)
            
            // Create PPG reading object
            val reading = PpgReading(
                timestamp = timestamp,
                redMean = redMean,
                greenMean = greenMean,
                blueMean = blueMean,
                intensity = avgIntensity
            )
            
            // Pass the reading to callback
            onPpgReading(reading)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
        } finally {
            // Always close the image when done
            image.close()
        }
    }
    
    private fun extractColorData(image: ImageProxy): ColorData {
        // For PPG, we're most interested in the center region of the image
        // where the finger is likely to be pressed
        
        // Define a region of interest (ROI) in the center (e.g., 30% of the image)
        val centerWidth = (image.width * 0.3).toInt()
        val centerHeight = (image.height * 0.3).toInt()
        val startX = (image.width - centerWidth) / 2
        val startY = (image.height - centerHeight) / 2
        
        var redSum = 0.0f
        var greenSum = 0.0f
        var blueSum = 0.0f
        var pixelCount = 0
        
        // Extract the image data - approach depends on the format
        when (image.format) {
            ImageFormat.YUV_420_888, 
            ImageFormat.YUV_422_888,
            ImageFormat.YUV_444_888 -> {
                // YUV format - common for camera preview
                // For PPG, the green channel is often most sensitive to blood volume changes
                val yBuffer = image.planes[0].buffer
                val uBuffer = image.planes[1].buffer
                val vBuffer = image.planes[2].buffer
                
                val yPixelStride = image.planes[0].pixelStride
                val yRowStride = image.planes[0].rowStride
                
                // We'll simplify and just use the Y (luminance) channel for processing
                // For more accurate RGB, you'd need proper YUV to RGB conversion
                // This is a simplified approach for PPG
                for (y in startY until startY + centerHeight) {
                    for (x in startX until startX + centerWidth) {
                        val yIndex = y * yRowStride + x * yPixelStride
                        if (yIndex < yBuffer.capacity()) {
                            // In YUV, Y is brightness, U and V are color components
                            val yValue = yBuffer.get(yIndex).toInt() and 0xFF
                            
                            // For simplicity, we'll use Y as our main signal
                            // But assign it to green channel which is most sensitive for PPG
                            greenSum += yValue
                            redSum += yValue * 0.7f  // Approximate, less sensitive
                            blueSum += yValue * 0.5f // Approximate, least sensitive
                            pixelCount++
                        }
                    }
                }
            }
        }
        
        // Calculate means
        val redMean = if (pixelCount > 0) redSum / pixelCount else 0f
        val greenMean = if (pixelCount > 0) greenSum / pixelCount else 0f
        val blueMean = if (pixelCount > 0) blueSum / pixelCount else 0f
        
        // For PPG, often the green channel is most relevant as it's absorbed by blood
        // But we'll calculate an average intensity across all channels
        val intensity = (redMean + greenMean + blueMean) / 3f
        
        return ColorData(redMean, greenMean, blueMean, intensity)
    }
    
    private data class ColorData(
        val redMean: Float,
        val greenMean: Float,
        val blueMean: Float,
        val intensity: Float
    )
}