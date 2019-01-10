package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.extensions.checkString
import com.myzonebuyer.extensions.clearStack
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.ProfileData
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_requestmoney.*
import org.json.JSONObject

class RequestMoneyFragment : BaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, "Wallet Transaction")
        return inflater.inflate(R.layout.fg_dsp_requestmoney, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        if (store.getBoolean("is_seller", false)) {
            imgIV.setImageResource(R.mipmap.ic_wallet_red)
            submitBT.background = ContextCompat.getDrawable(baseActivity, R.drawable.background_red)
        }

        submitBT.setOnClickListener(this)

        getProfileApi()
    }

    private fun getProfileApi() {
        syncManager.sendToServer(Const.API_GET_PROFILE, null, this)
    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.submitBT -> {
                if (monetET.isBlank()) {
                    showToastOne("Please enter money to add to account")
                } else {
                    hitSubmitApi()
                }
            }
        }
    }

    private fun hitSubmitApi() {
        val params = RequestParams()
        params.put("amount", monetET.checkString())
        syncManager.sendToServer(Const.API_WITHDRAW_AMOUNT, params, this)
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_WITHDRAW_AMOUNT) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                gotoHomeFragment()
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.API_GET_PROFILE) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val profiledata = Gson().fromJson(jsonObject.getJSONObject("details").toString(), ProfileData::class.java)
                walletTV.text = getString(R.string.you_have_money, profiledata.wallet)
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}