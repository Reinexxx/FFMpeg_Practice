package com.example.ffmpeg_practice.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.example.ffmpeg_practice.R
import com.example.ffmpeg_practice.databinding.FragmentMainBinding
import java.io.File
import java.io.IOException

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var viewModel = MainViewModel()
    private lateinit var binding: FragmentMainBinding

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // Record to the external cache directory for visibility

        activity?.let { ActivityCompat.requestPermissions(it, permissions, REQUEST_RECORD_AUDIO_PERMISSION) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mStartRecording = true
        var mStartPlaying = true
        context?.let { context -> viewModel.resetFileName(context) }

        binding.message.text = "音声保存先:${viewModel.fileName}"

        binding.recStartBtn.setOnClickListener {
            context?.let { context -> viewModel.resetFileName(context) }
            viewModel.onRecord(mStartRecording)
            when (mStartRecording) {
                true -> {
                    binding.recStartBtn.text = "収録停止"
                    binding.playBtn.isEnabled = false
                }
                false -> {
                    binding.recStartBtn.text = "収録開始"
                    binding.playBtn.isEnabled = true
                }
            }
            mStartRecording = !mStartRecording
        }

        binding.playBtn.setOnClickListener {
            viewModel.onPlay(mStartPlaying)
            when (mStartPlaying) {
                true -> {
                    binding.playBtn.text = "停止"
                    binding.recStartBtn.isEnabled = false
                }
                false -> {
                    binding.playBtn.text ="再生"
                    binding.recStartBtn.isEnabled = true
                }
            }

            mStartPlaying = !mStartPlaying
        }

        binding.analyzeBtn.setOnClickListener {
            val duration = viewModel.getDuration()
            binding.analyzeMessage.text = "音声の長さ:${duration}"
        }

        binding.echoBtn.setOnClickListener {
            val filteredFileName = "${context?.externalCacheDir?.absolutePath}/filteredAudio.3gp"
            viewModel.echoFilter(filteredFileName)
            viewModel.fileName = filteredFileName
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) activity?.finish()
    }

    override fun onStop() {
        super.onStop()
        viewModel.recorder?.release()
        viewModel.recorder = null
        viewModel.player?.release()
        viewModel.player = null
    }

}