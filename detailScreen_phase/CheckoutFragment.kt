package com.myzonebuyer.fragment.detailScreen_phase

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.myzonebuyer.R
import com.myzonebuyer.databinding.FgDspCheckoutBinding
import com.myzonebuyer.extensions.checkString
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.CheckoutData
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_checkout.*
import org.json.JSONObject

class CheckoutFragment : BaseFragment() {
    var binding: FgDspCheckoutBinding? = null
    var shipping_price = ""
    var sub_total = ""
    var checkData = CheckoutData()
    var pick_type = 0
    var latitude = 0.0
    var longitude = 0.0
    var address = ""
    var latTest = 0.0
    var lngTest = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments!!.let {
            shipping_price = it.getString("shipping_price", "0")
            sub_total = it.getString("sub_total", "")
            pick_type = it.getInt("pick_type", 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.checkout))
        binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_checkout, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding!!.paymentBT.setOnClickListener(this)

        if (pick_type == Const.PICKUP_DRIVER) {
            binding!!.addressET.setOnClickListener(this)
        }
        timeET.setOnClickListener(this)
        endTimeET.setOnClickListener(this)
        basePriceTV.text = baseActivity.getString(R.string.money_unit, sub_total)
        when (shipping_price) {
            "0" -> totalTV.text = baseActivity.getString(R.string.money_unit, sub_total)
            else -> {
                calculateSubTotal()
            }
        }
        shipPriceTV.text = baseActivity.getString(R.string.money_unit, shipping_price)

        getProfile()
    }

    private fun calculateSubTotal() {
        try {
            val args = (sub_total.toBigDecimal() + shipping_price.toBigDecimal()).toPlainString()
            sub_total = (sub_total.toBigDecimal() + shipping_price.toBigDecimal()).toPlainString()
            totalTV.text = baseActivity.getString(R.string.money_unit, args)
        } catch (ex: NumberFormatException) {
            ex.printStackTrace()
        }
    }

    private fun getProfile() {
        syncManager.sendToServer(Const.API_GET_PROFILE, null, this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.paymentBT -> {
                if (isValidate()) {
                    checkData.total_price = sub_total
                    checkData.order_name = nameET.checkString()
                    checkData.order_address = addressET.checkString()
                    checkData.order_from_time = timeET.checkString()
                    checkData.order_to_time = endTimeET.checkString()
                    checkData.latitude = latitude
                    checkData.longitude = longitude
                    checkData.pickup_type = pick_type
                    checkData.landmark = landmarkET.checkString()

                    val bundle = Bundle()
                    bundle.putParcelable("checkData", checkData)
                    baseActivity.replaceFragWithArgs(PaymentPageFragment(), R.id.frame_container, bundle)
                }
            }

            R.id.addressET -> {
                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(baseActivity)
                    baseActivity.startActivityForResult(intent, Const.PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: GooglePlayServicesRepairableException) {
                    log(e.message)
                } catch (e: GooglePlayServicesNotAvailableException) {
                    log(e.message)
                }
            }
        }
    }


    private fun isValidate(): Boolean {
        when {
            nameET.isBlank() -> showToastOne(getString(R.string.enter_order_name))
            addressET.isBlank() -> showToastOne(getString(R.string.fill_address))
            landmarkET.isBlank() -> showToastOne(getString(R.string.enter_landmark))
            else -> return true
        }
        return false
    }


    fun getResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val place = PlaceAutocomplete.getPlace(baseActivity, data)
            checkAddressAvail(place.latLng.latitude, place.latLng.longitude)

            address = place.address.toString()
            latTest = place.latLng.latitude
            lngTest = place.latLng.longitude

        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            val status = PlaceAutocomplete.getStatus(baseActivity, data)
            log(status.statusMessage)
        }

    }

    private fun checkAddressAvail(latitude: Double, longitude: Double) {
        val params = RequestParams()
        params.put("latitude", latitude)
        params.put("longitude", longitude)
        syncManager.sendToServer(Const.CHECK_DISTANCE, params, this)
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_GET_PROFILE) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val details = jsonObject.getJSONObject("details")
                if (details.getString("address") != null && details.getString("address") != "null") {
                    addressET.setText(details.getString("address"))
                }

                latitude = details.getDouble("latitude")
                longitude = details.getDouble("longitude")
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.CHECK_DISTANCE) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                addressET.setText(address)
                latitude = latTest
                longitude = lngTest
                shipping_price = jsonObject.getString("shipping_price")
                shipPriceTV.text = baseActivity.getString(R.string.money_unit, shipping_price)
                sub_total = arguments!!.getString("sub_total", "")
                calculateSubTotal()
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}