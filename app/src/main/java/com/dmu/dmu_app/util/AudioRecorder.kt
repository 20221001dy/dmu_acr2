package com.dmu.dmu_app.util // 본인의 패키지 경로에 맞게 수정하세요

import android.content.Context
import android.media.MediaRecorder
import java.io.File
import java.io.IOException

/**
 * MainActivity와 AudioRecorder가 소통할 규칙(인터페이스)을 정의합니다.
 * 녹음이 완료되거나 실패했을 때의 동작을 담고 있습니다.
 */
interface RecordingListener {
    fun onRecordingFinished(file: File)
    fun onRecordingFailed(exception: Exception)
}

/**
 * 오디오 녹음과 관련된 모든 로직을 담당하는 클래스입니다.
 * MediaRecorder의 생성, 설정, 시작, 중지, 해제 과정을 모두 관리합니다.
 * @param context 파일을 저장할 경로를 얻기 위해 필요합니다.
 */
class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    // isRecording 상태는 외부에서 읽을 수만 있도록 private set으로 설정합니다.
    var isRecording = false
        private set

    /**
     * 녹음을 시작하는 메서드입니다.
     * @param listener 녹음 결과를 통보받을 리스너 객체입니다.
     */
    fun start(listener: RecordingListener) {
        // 녹음 파일을 저장할 경로를 설정합니다.
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val audioFile = File(cacheDir, "audio_record.amr")
        if (!audioFile.parentFile.exists()) {
            audioFile.parentFile.mkdirs()
        }
        outputFile = audioFile

        // MediaRecorder를 설정하고 녹음을 시작합니다.
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                isRecording = false
                listener.onRecordingFailed(e) // 실패 시 리스너에 알립니다.
            }
        }
    }

    /**
     * 녹음을 중지하는 메서드입니다.
     * @param listener 녹음 결과를 통보받을 리스너 객체입니다.
     */
    fun stop(listener: RecordingListener) {
        if (!isRecording) return

        try {
            mediaRecorder?.stop()
        } catch (e: IllegalStateException) {
            // 녹음이 너무 짧을 때 등 stop()에서 오류가 발생할 수 있습니다.
            e.printStackTrace()
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false

            // 최종적으로 생성된 파일의 존재 여부를 확인하고 리스너에 알립니다.
            outputFile?.let {
                if (it.exists()) {
                    listener.onRecordingFinished(it)
                } else {
                    listener.onRecordingFailed(Exception("Recorded file not found."))
                }
            }
        }
    }
}