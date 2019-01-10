package com.myzonebuyer.fragment.detailScreen_phase

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.databinding.FgDspChangePasswordBinding
import com.myzonebuyer.extensions.checkString
import com.myzonebuyer.extensions.getLength
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_change_password.*
import org.json.JSONObject

class ChangePasswordFragment : BaseFragment() {
    var binding: FgDspChangePasswordBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.change_password))
        binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_change_password, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding!!.submitBT.setOnClickListener(this)

        if (store.getBoolean("is_seller", false)) {
            binding!!.submitBT.background = ContextCompat.getDrawable(baseActivity, R.drawable.background_red)
        }

    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.submitBT -> {
                if (isValidate()) {
                    val params = RequestParams()
                    params.put("password", newET.checkString())
                    syncManager.sendToServer(Const.API_CHANGE_PASSWORD, params, this)
                }

            }
        }
    }

    private fun isValidate(): Boolean {
        if (newET.isBlank()) {
            showToastOne(baseActivity.getString(R.string.enter_new_password))
        } else if (!baseActivity.isValidPassword(newET.checkString())) {
            showToastOne(baseActivity.getString(R.string.enter_valid_password))
        } else if (newET.getLength() < 8) {
            showToastOne(baseActivity.getString(R.string.pass_8_chars))
        } else if (confirmET.isBlank()) {
            showToastOne(baseActivity.getString(R.string.enter_confirm_password))
        } else if (confirmET.checkString() != newET.checkString()) {
            showToastOne(baseActivity.getString(R.string.verify_password_mismatch))
        } else {
            return true
        }
        return false
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_CHANGE_PASSWORD) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                baseActivity.supportFragmentManager.popBackStack()
                showToastOne(baseActivity.getString(R.string.password_changes_successfully))
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}