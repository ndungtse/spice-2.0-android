package com.medtroniclabs.spice.ui.member

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentMemberRegistrationBinding
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.listener.FormEventListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.ui.home.ToolsActivity

class MemberRegistrationFragment : Fragment(), FormEventListener, View.OnClickListener {

    private lateinit var binding: FragmentMemberRegistrationBinding

    private lateinit var formGenerator: FormGenerator


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemberRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setListener()
    }

    private fun setListener() {
        binding.btnSubmit.setOnClickListener(this)
        binding.btnStartAssessment.setOnClickListener(this)
    }

    private fun initializeView() {
        formGenerator = FormGenerator(
            requireContext(), binding.llForm, null, this, binding.scrollView,
            translate = false
        )
        val objectList = Gson().fromJson(
            CommonUtils.getStringFromAssets(
                "house_hold_member_registration.json",
                requireActivity().assets
            ),
            Array<FormLayout>::class.java
        ).asList()
        formGenerator.populateViews(objectList)
    }


    override fun loadLocalCache(id: String, localDataCache: Any, selectedParent: Long?) {
        TODO("Not yet implemented")
    }

    override fun onPopulate(targetId: String) {
        TODO("Not yet implemented")
    }

    override fun onCheckBoxDialogueClicked(
        id: String,
        serverViewModel: FormLayout,
        resultMap: Any?
    ) {
        TODO("Not yet implemented")
    }

    override fun onInstructionClicked(
        id: String,
        title: String,
        informationList: ArrayList<String>?,
        description: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onFormSubmit(resultMap: HashMap<String, Any>?) {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnStartAssessment -> {
                startActivity(Intent(requireActivity(),ToolsActivity::class.java))
            }
            R.id.btnSubmit -> {

            }
        }
    }
}