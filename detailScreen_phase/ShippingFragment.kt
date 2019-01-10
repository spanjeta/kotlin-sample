package com.myzonebuyer.fragment.detailScreen_phase

import android.app.AlertDialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.databinding.AlertShippingFgBinding
import com.myzonebuyer.databinding.FgDspShippingBinding
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import org.json.JSONObject

class ShippingFragment : BaseFragment() {
    var binding: FgDspShippingBinding? = null
    var cart_id = 0
    var shipping = "0"
    var sub_total = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null && args.containsKey("cart_id")) {
            cart_id = args.getInt("cart_id", 0)
            sub_total = args.getString("total", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.shipping))
        if (binding == null)
            binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_shipping, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding!!.adminBT.setOnClickListener(this)
        binding!!.myselfBT.setOnClickListener(this)

    }

    private fun getCalculateAmountApi() {
        val params = RequestParams()
        params.put("cart_id", cart_id)
        syncManager.sendToServer(Const.CAL_AMOUNT, params, this)
    }

    private fun sentShippingRequest() {
        val params = RequestParams()
        params.put("cart_id", cart_id)
        syncManager.sendToServer(Const.SHIPPING_REQUEST, params, this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.adminBT -> {
                getCalculateAmountApi()
            }

            R.id.myselfBT -> {
                val bundle = Bundle()
                bundle.putString("shipping_price", "0")
                bundle.putString("sub_total", sub_total)
                bundle.putInt("pick_type", Const.PICKUP_MYSELF)
                baseActivity.replaceFragWithArgs(CheckoutFragment(), R.id.frame_container, bundle)
            }
        }
    }

    private fun openAlertDialog() {
        val builder = AlertDialog.Builder(baseActivity)
        builder.setTitle("")
        val dialogBind: AlertShippingFgBinding = DataBindingUtil.inflate(baseActivity.layoutInflater, R.layout.alert_shipping_fg, null, false)
        builder.setView(dialogBind.root)

        dialogBind.priceTV.text = baseActivity.getString(R.string.money_unit, shipping)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            val bundle = Bundle()
            bundle.putString("shipping_price", shipping)
            bundle.putString("sub_total", sub_total)
            bundle.putInt("pick_type", Const.PICKUP_DRIVER)
            baseActivity.replaceFragWithArgs(CheckoutFragment(), R.id.frame_container, bundle)

        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.create().show()
    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.SHIPPING_REQUEST) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                val bundle = Bundle()
                bundle.putString("shipping_price", jsonObject.getString("shipping_price"))
                bundle.putString("sub_total", jsonObject.getString("sub_total"))
                bundle.putInt("pick_type", Const.PICKUP_DRIVER)
                baseActivity.replaceFragWithArgs(CheckoutFragment(), R.id.frame_container, bundle)
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.CAL_AMOUNT) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                shipping = jsonObject.getString("shipping_price")
                sub_total = jsonObject.getString("sub_total")
                openAlertDialog()
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}