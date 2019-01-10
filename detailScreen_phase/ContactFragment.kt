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
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_contact.*
import org.json.JSONObject

class ContactFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, "Contact Us")
        return inflater.inflate(R.layout.fg_dsp_contact, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        if (store.getBoolean("is_seller", false)) {
            subBT.background = ContextCompat.getDrawable(baseActivity, R.drawable.background_red)
        }

        subBT.setOnClickListener {
            when {
                msgET.isBlank() -> showToastOne(getString(R.string.please_enter_msg))
                else -> hitContactApi()
            }
        }
    }

    private fun hitContactApi() {
        val parms=RequestParams()
        parms.put("message",msgET.checkString())
        syncManager.sendToServer(Const.API_CONTACT_US, parms, this)
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_CONTACT_US) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                baseActivity.supportFragmentManager.popBackStack()
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}