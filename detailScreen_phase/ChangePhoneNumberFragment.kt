package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.extensions.checkString
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.fragment.login_phase.OtpFragment
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_change_phone.*
import org.json.JSONObject

class ChangePhoneNumberFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.change_phone))
        return inflater.inflate(R.layout.fg_dsp_change_phone, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        submitBT.setOnClickListener(this)

        if (store.getBoolean("is_seller", false)) {
            submitBT.background = ContextCompat.getDrawable(baseActivity, R.drawable.background_red)
        }
    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.submitBT -> {
                if (isValidate()) {
                    hitCheckPhoneApi()
                }
            }
        }
    }

    private fun gotoOtpFragment() {
        val args = Bundle()
        args.putString("phone", pickerCP.selectedCountryCode + contactET.checkString())
        args.putBoolean("islogin", true)
        baseActivity.replaceFragWithArgs(OtpFragment(), R.id.frame_container, args)
    }

    private fun hitCheckPhoneApi() {
        val args = RequestParams()
        args.put("contact_no", pickerCP.selectedCountryCode + contactET.checkString())
        syncManager.sendToServer(Const.CHECK_PHONE, args, this)

    }

    private fun isValidate(): Boolean {
        when {
            contactET.isBlank() -> showToastOne(getString(R.string.enter_phone_number))
            contactET.length() < 5 -> showToastOne(getString(R.string.enter_valid_phone))
            else -> return true
        }
        return false
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.CHECK_PHONE) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                gotoOtpFragment()
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}