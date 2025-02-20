package com.medtroniclabs.spice.formgeneration.utility

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.CheckboxDialogLayoutBinding
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel

class CheckBoxDialog() : DialogFragment(), View.OnClickListener {
    private var callback: ((result: ArrayList<HashMap<String, Any>>) -> Unit)? = null
    var resultMap: Any? = null
    var title : String? = null
    constructor(
        callback: (result: ArrayList<HashMap<String, Any>>) -> Unit,
        resultMap: Any?,
        title:String?
    ) : this() {
        this.callback = callback
        this.resultMap = resultMap
        this.title = title
    }

    lateinit var binding: CheckboxDialogLayoutBinding
    private val viewModel: AssessmentViewModel by activityViewModels()

    companion object {
        const val TAG = "CheckBoxDialogComponent"
        private const val KEY_TYPE = "KEY_TYPE"
        fun newInstance(
            key: String,
            resultMap: Any?,
            title:String?=null,
            callback: (result: ArrayList<HashMap<String, Any>>) -> Unit
        ): CheckBoxDialog {
            val args = Bundle()
            args.putString(KEY_TYPE, key)
            val fragment = CheckBoxDialog(callback, resultMap,title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CheckboxDialogLayoutBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        getRespectiveList(requireArguments().getString(KEY_TYPE))
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.symptomTypeListResponse.observe(viewLifecycleOwner) { list ->
            binding.labelHeader.text = if(title.isNullOrEmpty()) {
                getString(R.string.symptoms)
            }else{
                getString(R.string.market_days)
            }
            if (list.isNotEmpty()) {
                if (resultMap != null && resultMap is ArrayList<*>) {
                    binding.rvItems.adapter = CheckboxDialogAdapter(
                        getSelectedSymptomList(
                            list,
                            resultMap as ArrayList<*>
                        ),
                        translate = SecuredPreference.getIsTranslationEnabled()
                    )
                } else {
                    binding.rvItems.adapter = CheckboxDialogAdapter(list, translate = SecuredPreference.getIsTranslationEnabled())
                }
            } else {
                binding.rvItems.adapter = null
            }
        }
    }

    private fun getSelectedSymptomList(
        list: List<SignsAndSymptomsEntity>,
        resultMap: ArrayList<*>
    ): List<SignsAndSymptomsEntity> {
        if (resultMap.isNotEmpty()) {
            resultMap.forEach { resultSymptom ->
                if (resultSymptom is Map<*, *>) {
                    val map = list.find { it._id == resultSymptom[DefinedParams.ID] }
                    map?.isSelected = true
                }
            }
            return list
        } else {
            return list
        }
    }

    private fun getRespectiveList(key: String?) {
        key?.let { viewModel.getSymptomListByType(it) }
    }

    private fun initializeView() {
        binding.rvItems.layoutManager = LinearLayoutManager(binding.root.context)
        binding.btnCancel.safeClickListener(this)
        binding.btnOkay.safeClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCancel -> {
                dismiss()
            }

            R.id.btnOkay -> {
                val adapter = binding.rvItems.adapter
                if (adapter != null && adapter is CheckboxDialogAdapter) {
                    callback?.invoke(
                        adapter.getSelectedItems()
                    )
                    dismiss()
                }
            }
        }
    }
}