package ru.kot1.demo.activity.editors

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.kot1.demo.R
import ru.kot1.demo.databinding.FragmentNewPostBinding
import ru.kot1.demo.dto.Coords
import ru.kot1.demo.enumeration.AttachmentType
import ru.kot1.demo.util.AndroidUtils
import ru.kot1.demo.util.StringArg
import ru.kot1.demo.viewmodel.EditPostViewModel
import ru.kot1.demo.viewmodel.MediaWorkPostViewModel
import java.io.File
import java.util.regex.Pattern
import androidx.activity.result.ActivityResultCallback

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult


@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: EditPostViewModel by activityViewModels()
    private val mwPostViewModel: MediaWorkPostViewModel by activityViewModels()

    private lateinit var mapResultLauncher: ActivityResultLauncher<Intent>

    private val photoRequestCode = 1
    private val cameraRequestCode = 2
    private val musicRequestCode = 3
    private val videoRequestCode = 4

    private var fragmentBinding: FragmentNewPostBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_new_record, menu)
        menu.findItem(R.id.signout).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let {
                    if (it.edit.text.toString().trim().isBlank()) {
                        Toast.makeText(
                            requireContext(),
                            R.string.enter_anything,
                            Toast.LENGTH_LONG
                        )
                            .show()
                        return false
                    }


                    viewModel.preparePostText(it.edit.text.toString())
                    viewModel.save()
                    viewModel.edited.value?.let { post -> mwPostViewModel.deleteFile(post) }
                    AndroidUtils.hideKeyboard(requireView())
                    activity?.supportFragmentManager?.popBackStack()

                    Toast.makeText(
                        requireContext(), R.string.post_soon,
                        Toast.LENGTH_SHORT
                    ).show()

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initMapResult(){
        mapResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                   val data = result.data
                   val lat =  data?.getFloatExtra("lat", 0F)
                   val long = data?.getFloatExtra("long", 0F)

                    viewModel.preparePostCoords(Coords(lat, long))
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initMapResult()

        arguments?.getLong("post")?.let { id ->
            arguments?.getInt("postPosition")?.let { position ->
                if (id != 0L) {
                    (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.edit_post)
                    viewModel.loadUser(id, position)
                } else {
                    (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.new_post)
                    viewModel.prepareForNew()
                }
            }
        }



        viewModel.postText.observe(viewLifecycleOwner) { text ->
            binding.edit.setText(text)
        }

        viewModel.coords.observe(viewLifecycleOwner) {
            binding.placePanel.isVisible = it != null
        }

        viewModel.attach.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.previewPanel.visibility = View.GONE
            } else {
                binding.previewPanel.visibility = View.VISIBLE
                when (it.dataType) {
                    AttachmentType.IMAGE -> {
                        if (it.uri != null) {
                            binding.previewPhoto.setImageURI(it.uri)
                        } else {
                            binding.previewPhoto.setImageResource(R.drawable.ic_image)
                        }
                    }
                    AttachmentType.VIDEO -> binding.previewPhoto.setImageResource(R.drawable.ic_video)
                    AttachmentType.AUDIO -> binding.previewPhoto.setImageResource(R.drawable.ic_music)
                }
            }
        }


        binding.edit.requestFocus()

        binding.mp3File.setOnClickListener {
            MaterialFilePicker()
                .withSupportFragment(this@NewPostFragment)
                .withCloseMenu(true)
                .withPath(Environment.getExternalStorageDirectory().absolutePath)
                .withRootPath(Environment.getExternalStorageDirectory().absolutePath)
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.(mp3)$"))
                .withFilterDirectories(false)
                .withTitle("Select mp3 file")
                .withRequestCode(musicRequestCode)
                .start()
        }

        binding.mp4File.setOnClickListener {
            MaterialFilePicker()
                .withSupportFragment(this@NewPostFragment)
                .withCloseMenu(true)
                .withPath(Environment.getExternalStorageDirectory().absolutePath)
                .withRootPath(Environment.getExternalStorageDirectory().absolutePath)
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.(mp4)$"))
                .withFilterDirectories(false)
                .withTitle("Select mp4 file")
                .withRequestCode(videoRequestCode)
                .start()
        }


        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .galleryOnly()
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .start(photoRequestCode)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .cameraOnly()
                .start(cameraRequestCode)
        }

        binding.removeAttach.setOnClickListener {
            viewModel.prepareTargetAttach(null, null)
            binding.previewPanel.isVisible = false
        }


        binding.buttonPlace.setOnClickListener {
            binding.placePanel.isVisible = true
            val (lat, long) = viewModel.getPostCoords()

            val myIntent = Intent(requireActivity(), MapActivity::class.java)
            myIntent.putExtra("lat", lat ?: 0F)
            myIntent.putExtra("long", long ?: 0F)
            mapResultLauncher.launch(myIntent)

        }

        binding.removePlace.setOnClickListener {
            binding.placePanel.isVisible = false
            viewModel.removePostCoords()
        }




        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == ImagePicker.RESULT_ERROR) {
            fragmentBinding?.let {
                Snackbar.make(it.root, ImagePicker.getError(data), Snackbar.LENGTH_LONG).show()
            }
            return
        }
        if (resultCode == Activity.RESULT_OK &&
            (requestCode == photoRequestCode || requestCode == cameraRequestCode)
        ) {
            data?.let { viewModel.prepareTargetAttach(it.data, AttachmentType.IMAGE) }
            return
        }


        if (resultCode == Activity.RESULT_OK && requestCode == musicRequestCode) {
            data?.let {
                viewModel.prepareTargetAttach(
                    File(it.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)).toUri(),
                    AttachmentType.AUDIO
                )
            }
            return
        }

        if (resultCode == Activity.RESULT_OK && requestCode == videoRequestCode) {
            data?.let {
                viewModel.prepareTargetAttach(
                    File(it.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)).toUri(),
                    AttachmentType.VIDEO
                )
            }
            return
        }

    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}