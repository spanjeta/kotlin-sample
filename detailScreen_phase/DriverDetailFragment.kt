package com.myzonebuyer.fragment.detailScreen_phase

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.activity.BaseActivity
import com.myzonebuyer.databinding.FgDspDriverDetailBinding
import com.myzonebuyer.extensions.loadFromDrawable
import com.myzonebuyer.extensions.loadFromUrl
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.Driver
import com.myzonebuyer.model.DriverDetail
import com.myzonebuyer.utils.Const
import kotlinx.android.synthetic.main.fg_dsp_driver_detail.*
import kotlinx.android.synthetic.main.include_driver_details.*
import org.json.JSONObject


class DriverDetailFragment : BaseFragment(), BaseActivity.PermCallback {
    var binding: FgDspDriverDetailBinding? = null
    var driverData: Driver? = null
    var contact = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            driverData = args.getParcelable("driver")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.driver_detail))
        binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_driver_detail, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        phoneIV.setOnClickListener(this)

        getDriverDetail()

    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.phoneIV -> {
                onCall(contact)
            }
        }
    }


    private fun onCall(contact: String) {
        if (baseActivity.checkPermissions(arrayOf(Manifest.permission.CALL_PHONE), Const.CALL_STATUS, this)) {
            try {
                val intent = Intent(ACTION_CALL, Uri.parse(getString(R.string.tel) + contact))
                if (ActivityCompat.checkSelfPermission(baseActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                baseActivity.startActivity(intent)

            } catch (ex: android.content.ActivityNotFoundException) {
                showToast(baseActivity.getString(R.string.could_not_found_activity))
            }

        }

    }


    private fun getDriverDetail() {
        syncManager.sendToServer(Const.API_DRIVER_DETAILS + "/" + driverData!!.driverId, null, this)
    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_DRIVER_DETAILS + "/" + driverData!!.driverId) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val driverDetail = Gson().fromJson(jsonObject.getJSONObject("driver").toString(), DriverDetail::class.java)
                setDriverData(driverDetail)
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }

    private fun setDriverData(driverDetail: DriverDetail) {
        when {
            driverDetail.profileFile != null -> circleImageView.loadFromUrl(baseActivity, driverDetail.profileFile!!, R.mipmap.default_profile)
            else -> circleImageView.loadFromDrawable(baseActivity, R.mipmap.default_profile)
        }
        nameTV.text = driverDetail.fullName
        ratingRB.rating = driverDetail.rating!!.toFloat()
        contact = driverDetail.contactNo!!
        phoneET.setText(driverDetail.contactNo)

        vehicleET.setText(driverDetail.vehicleNumber.toString())

        typeET.setText(driverDetail.vehicleType!!.title)


    }


    override fun permGranted(resultCode: Int) {
        if (resultCode == Const.CALL_STATUS) {
            try {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(getString(R.string.tel) + contact))
                if (ActivityCompat.checkSelfPermission(baseActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                baseActivity.startActivity(intent)

            } catch (ex: android.content.ActivityNotFoundException) {
                showToast(baseActivity.getString(R.string.could_not_found_activity))
            }

        }


    }

    override fun permDenied(resultCode: Int) {
    }
}