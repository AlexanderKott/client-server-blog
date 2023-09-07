package ru.kot1.demo.activity.editors

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.kot1.demo.R
import ru.kot1.demo.databinding.FragmentNewJobBinding
import ru.kot1.demo.util.AndroidUtils
import ru.kot1.demo.viewmodel.JobsViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NewJobFragment : Fragment() {
    private val viewModel: JobsViewModel by activityViewModels()
    private var _binding: FragmentNewJobBinding? = null
    private val binding get() = _binding!!
    private var jobId: Long = 0
    private var fromSet = false
    private var tillSet = false


    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewJobBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

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
                _binding?.let {
                    val cname = it.compName.text.toString()
                    val pos = it.position.text.toString()
                    val from = it.timeFromL.text.toString()
                    val till = it.timeTillL.text.toString()

                    if (cname.trim().isEmpty() || pos.trim().isEmpty() || !fromSet || !tillSet) {
                        Toast.makeText(requireContext(), R.string.fill_in_all_fileds,
                            Toast.LENGTH_LONG).show()
                        return false
                    }

                    val dateFormat: DateFormat = SimpleDateFormat("MM/dd/yyyy")

                    viewModel.postNewOrChangedJob(jobId, cname, pos,
                        dateFormat.parse(from).time,
                        dateFormat.parse(till).time)

                    Toast.makeText(requireContext(), R.string.job_soon,
                        Toast.LENGTH_SHORT).show()

                    AndroidUtils.hideKeyboard(requireView())
                    activity?.supportFragmentManager?.popBackStack()
                }

                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jobId = arguments?.getLong("jobId", 0L) ?: 0L
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.loadThisJobToUI(jobId)

        if (jobId == 0L) {
            (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.new_job)
        } else {
            (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.edit_job)
        }

        with(binding) {
            viewModel.loadJob.observe(viewLifecycleOwner) { uiObject ->
                compName.setText(uiObject.name)
                position.setText(uiObject.pos)

                val dateFormat: DateFormat = SimpleDateFormat("MM/dd/yyyy")
                val from = Date(uiObject.start)
                val till = Date(uiObject.fin)

                timeFromL.setText(dateFormat.format(from))
                timeTillL.setText(dateFormat.format(till))
            }

            setFrom.setOnClickListener {
                val newFragment = DatePickerFragment(DateType.FROM, binding, object : DateBack {
                    override fun setFrom(value: Boolean) {
                        fromSet = value
                    }

                    override fun setTill(value: Boolean) {
                        tillSet = value
                    }
                } )
                newFragment.show(requireActivity().supportFragmentManager, "datePicker")
            }

            setTill.setOnClickListener {
                val newFragment = DatePickerFragment(DateType.TILL, binding, object : DateBack {
                    override fun setFrom(value: Boolean) {
                        fromSet = value
                    }

                    override fun setTill(value: Boolean) {
                        tillSet = value
                    }
                })
                newFragment.show(requireActivity().supportFragmentManager, "datePicker")
            }



        }


    }

    enum class DateType {
        FROM, TILL
    }

    interface DateBack {
        fun setFrom(value : Boolean)
        fun setTill(value : Boolean)
    }

    class DatePickerFragment(val type: DateType, val binding: FragmentNewJobBinding,
    var backValue : DateBack) : DialogFragment(),
        DatePickerDialog.OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(requireContext(), this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val dateFormat: DateFormat = SimpleDateFormat("MM/dd/yyyy")
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_MONTH] = day
            calendar[Calendar.MONTH] = month
            calendar[Calendar.YEAR] = year

            val tempDate =  dateFormat.format(calendar.time)

            if (type == DateType.FROM) {
                binding.timeFromL.setText(tempDate)
                backValue.setFrom(true)
            }

            if (type == DateType.TILL) {
                binding.timeTillL.setText(tempDate)
                backValue.setTill(true)
            }

        }
    }


}


