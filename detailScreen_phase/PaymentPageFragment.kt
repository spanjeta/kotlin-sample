package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.replaceFragment
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.fragment.home_phase.MainHomeFragment
import com.myzonebuyer.model.CheckoutData
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_payment_page.*
import org.json.JSONObject

class PaymentPageFragment : BaseFragment() {
    var checkData: CheckoutData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null && bundle.containsKey("checkData")) {
            checkData = bundle.getParcelable("checkData")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.select_payment_method))
        return inflater.inflate(R.layout.fg_payment_page, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        continueBT.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.continueBT -> {
                if (checkData != null) {
                    hitCheckoutApi()
                }
            }
        }
    }

    private fun hitCheckoutApi() {
        val params = RequestParams()
        params.put("total_price", checkData!!.total_price)
        params.put("order_name", checkData!!.order_name)
        params.put("order_address", checkData!!.order_address)
        params.put("pickup_type", checkData!!.pickup_type)
        params.put("latitude", checkData!!.latitude)
        params.put("longitude", checkData!!.longitude)
        when {
            cashRB.isChecked -> params.put("payment_type", Const.PAYMENT_CASH)
            walletRB.isChecked -> params.put("payment_type", Const.PAYMENT_WALLET)
            else -> params.put("payment_type", Const.PAYMENT_CASH)
        }
        params.put("order_address_landmark", checkData!!.landmark)
        syncManager.sendToServer(Const.ORDER_SAVE, params, this)
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.ORDER_SAVE) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                store.setInt("cart_count", 0)
                baseActivity.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                baseActivity.replaceFragment(MainHomeFragment(), R.id.frame_container)

            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}