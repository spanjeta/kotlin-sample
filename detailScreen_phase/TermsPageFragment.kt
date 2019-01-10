package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.utils.Const
import kotlinx.android.synthetic.main.fg_dsp_terms_page.*
import org.json.JSONObject

class TermsPageFragment : BaseFragment() {
    var type = 0
    var isLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        args?.let {
            type = args.getInt("type")
            isLogin = args.getBoolean("isLogin", false)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        when {
            isLogin -> setToolbarLogin(true, "")
            else -> setToolbarMain(true, "")
        }
        return inflater.inflate(R.layout.fg_dsp_terms_page, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        when (type) {
            Const.TYPE_ABOUT -> {
                syncManager.sendToServer(Const.API_PAGES + "/${Const.TYPE_ABOUT}", null, this)
            }
            Const.TYPE_TERMS -> {
                syncManager.sendToServer(Const.API_PAGES + "/${Const.TYPE_TERMS}", null, this)
            }
            Const.TYPE_PRIVACY_POLICY -> {
                syncManager.sendToServer(Const.API_PAGES + "/${Const.TYPE_PRIVACY_POLICY}", null, this)
            }
        }

    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getInt("status") == Const.STATUS_OK) {
            val title: String? = jsonObject.getJSONObject("page").getString("title")
            val description: String? = jsonObject.getJSONObject("page").getString("description")
            when {
                isLogin -> setToolbarLogin(true, title)
                else -> setToolbarMain(true, title)
            }
            descTV.text = description
        } else {
            elseErrorMsg(jsonObject)
        }

    }
}