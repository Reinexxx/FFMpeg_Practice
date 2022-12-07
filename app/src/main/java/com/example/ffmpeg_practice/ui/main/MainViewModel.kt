package com.example.ffmpeg_practice.ui.main

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import java.io.IOException

private const val LOG_TAG = "AudioRecordTest"
class MainViewModel : ViewModel() {

    var recorder: MediaRecorder? = null
    var player: MediaPlayer? = null
    var fileName: String = ""

    fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    fun echoFilter(filteredFileName: String) {
        val mediaInformation = FFprobeKit.getMediaInformation(fileName)
        Log.d("VOICE INFO BEFORE", mediaInformation.logsAsString)
        val removeSilence = FFmpegKit.execute("-y -i  $fileName -af aecho=0.8:0.9:1000:0.3 $filteredFileName")
    }

    fun getDuration(): String {
        val mediaInformation = FFprobeKit.getMediaInformation(fileName)
        return mediaInformation.mediaInformation.duration
    }

    fun resetFileName(context: Context) {
        fileName = "${context.externalCacheDir?.absolutePath}/audiorecordtest.3gp"
    }
}