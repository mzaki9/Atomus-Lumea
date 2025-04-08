import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.content.res.AssetFileDescriptor
import com.example.lumea.domain.model.HeartRateResult
import java.io.IOException
import java.util.Arrays

class HealthScorePredictor(private val context: Context, private val modelFileName: String) {

    private var interpreter: Interpreter? = null
    private val inputTensorIndex = 0 // Biasanya input tensor pertama
    private val outputTensorIndex = 0 // Biasanya output tensor pertama
    private var inputShape: IntArray = intArrayOf(1, 5) // [batch_size, num_features] sesuai contoh
    private var outputShape: IntArray = intArrayOf(1, 1) // Contoh output: [batch_size, num_classes/output_dim]

    init {
        try {
            interpreter = Interpreter(loadModelFile())
            val inputTensor = interpreter?.getInputTensor(inputTensorIndex)
            inputShape = inputTensor?.shape() ?: inputShape
            val outputTensor = interpreter?.getOutputTensor(outputTensorIndex)
            outputShape = outputTensor?.shape() ?: outputShape
            Log.d("HealthPredictor", "Model loaded successfully. Input shape: ${Arrays.toString(inputShape)}, Output shape: ${Arrays.toString(outputShape)}")
        } catch (e: IOException) {
            Log.e("HealthPredictor", "Error loading model: ${e.message}")
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.getFileDescriptor())
        val fileChannel = inputStream.getChannel()
        val startOffset = fileDescriptor.getStartOffset()
        val declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    fun predict(heartRateResult: HeartRateResult): FloatArray? {
        interpreter?.let {
            // 1. Buat input ByteBuffer dari objek HeartRateResult
            val inputBuffer = ByteBuffer.allocateDirect(4 * inputShape.last()) // Float x number of features
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.putFloat(heartRateResult.heartRate.toFloat())
            inputBuffer.putFloat(heartRateResult.respiratoryRate)
            inputBuffer.putFloat(heartRateResult.spo2)
            inputBuffer.putFloat(50.toFloat())//Umur dummy
            // Anda mungkin perlu menambahkan 'age' jika model Anda membutuhkannya dan tersedia di tempat lain
            // Contoh (asumsi 'age' tersedia di ViewModel atau tempat lain):
            // inputBuffer.putFloat(age)
            inputBuffer.rewind()

            // 2. Buat output ByteBuffer (sesuaikan dengan output model Anda)
            val outputBuffer = ByteBuffer.allocateDirect(4 * outputShape.last()) // Float x number of output values
            outputBuffer.order(ByteOrder.nativeOrder())

            // 3. Jalankan inferensi
            it.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()

            // 4. Proses output
            val predictionResult = FloatArray(outputShape.last())
            for (i in 0 until outputShape.last()) {
                predictionResult[i] = outputBuffer.float
            }
            Log.d("HealthPredictor", "Prediction result: ${Arrays.toString(predictionResult)}")
            return predictionResult
        }
        return null
    }

    fun close() {
        interpreter?.close()
    }
}

