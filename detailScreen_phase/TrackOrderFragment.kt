package com.myzonebuyer.fragment.detailScreen_phase

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.adapter.others.TrackStateAdapter
import com.myzonebuyer.databinding.DailogRatingBinding
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.loadFromUrl
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.Driver
import com.myzonebuyer.model.DriverDetail
import com.myzonebuyer.model.TrackData
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_track_order.*
import org.json.JSONObject

class TrackOrderFragment : BaseFragment() {
    var adapter: TrackStateAdapter? = null
    var id = ""
    var state_id = ""
    var arraList = ArrayList<TrackData>()
    var driverData: Driver? = null
    var alertDialog: AlertDialog? = null
    var driverID = ""
    var driverDetailss: DriverDetail? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null && args.containsKey("id")) {
            id = args.getString("id", "")
            state_id = args.getString("state", "")
            driverData = args.getParcelable("driver")
        } else if (args != null && args.containsKey("driverID")) {
            driverID = args.getString("driverID", "")
            state_id = args.getString("stateID", "")
            id = args.getString("orderItemID", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.track_order))
        return inflater.inflate(R.layout.fg_dsp_track_order, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        arraList.clear()
        trackRV.layoutManager = LinearLayoutManager(baseActivity)

        arraList.add(TrackData("Order Placed", "Your Order has been placed", 0))
        arraList.add(TrackData("In-Progress", "Your Order is In-Progress", 0))
        arraList.add(TrackData("Dispatched", "Your order has been Dispatched", 0))
        arraList.add(TrackData("Delivered", "Your order has been Delivered", 0))
        arraList.add(TrackData("Completed", "Your order has been Completed", 0))

        detailsBT.setOnClickListener(this)
        rateBT.setOnClickListener(this)
        trackNoTV.text = baseActivity.getString(R.string.order_id, id.toInt())

        setAdapter()

        if (state_id.toInt() == Const.STATE_COMPLETED) {
            rateBT.visibility = View.VISIBLE
        } else {
            rateBT.visibility = View.INVISIBLE
        }

        if (driverID.isNotEmpty()) {
            getDriverDetails()
        }


        when (state_id.toInt()) {
            Const.STATE_CANCELLED_BY_USER -> {
                stateTV.visibility = View.VISIBLE
                trackRV.visibility = View.INVISIBLE
                detailsBT.visibility = View.INVISIBLE
                stateTV.text = "Product is cancelled by user"
            }
            else -> {
                stateTV.visibility = View.GONE
                trackRV.visibility = View.VISIBLE
                detailsBT.visibility = View.VISIBLE
            }
        }
    }

    private fun getDriverDetails() {
        syncManager.sendToServer(Const.API_DRIVER_DETAILS + "/" + driverID, null, this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.detailsBT -> {
                if (driverData != null) {
                    val bundle = Bundle()
                    bundle.putParcelable("driver", driverData)
                    baseActivity.replaceFragWithArgs(DriverDetailFragment(), R.id.frame_container, bundle)
                } else {
                    showToastOne(getString(R.string.no_driver_assigned))
                }
            }

            R.id.rateBT -> {
                if (driverData != null) {
                    openRatingDialog()
                } else if (driverDetailss != null) {
                    openRateDai()
                } else {
                    showToastOne("No Details found")
                }
            }
        }
    }

    private fun openRateDai() {
        val alert = AlertDialog.Builder(baseActivity)
        alert.setTitle("")
        val binding: DailogRatingBinding = DataBindingUtil.inflate(baseActivity.layoutInflater, R.layout.dailog_rating, null, false)
        alert.setView(binding.root)
                .setCancelable(false)
        alertDialog = alert.create()
        binding.crossIV.setOnClickListener {
            alertDialog!!.dismiss()
        }

        binding.submitBT.setOnClickListener {
            if (binding.ratingRB.rating.toInt() == 0) {
                showToastOne(getString(R.string.please_give_rating))
            } else {
                val params = RequestParams()
                params.put("order_item_id", id)
                params.put("rating", binding.ratingRB.rating.toInt())
                syncManager.sendToServer(Const.DRIVER_RATING, params, this)
                alertDialog!!.dismiss()
            }
        }
        binding.nameTV.text = driverDetailss!!.fullName

        if (driverDetailss!!.profileFile != null) {
            binding.imgCIV.loadFromUrl(baseActivity, driverDetailss!!.profileFile!!, R.mipmap.default_image)
        }

        alertDialog!!.show()
    }

    private fun openRatingDialog() {
        val alert = AlertDialog.Builder(baseActivity)
        alert.setTitle("")
        val binding: DailogRatingBinding = DataBindingUtil.inflate(baseActivity.layoutInflater, R.layout.dailog_rating, null, false)
        alert.setView(binding.root)
                .setCancelable(false)
        alertDialog = alert.create()
        binding.crossIV.setOnClickListener {
            alertDialog!!.dismiss()
        }

        binding.submitBT.setOnClickListener {
            if (binding.ratingRB.rating.toInt() == 0) {
                showToastOne(getString(R.string.please_give_rating))
            } else {
                val params = RequestParams()
                params.put("order_item_id", id)
                params.put("rating", binding.ratingRB.rating.toInt())
                syncManager.sendToServer(Const.DRIVER_RATING, params, this)
                alertDialog!!.dismiss()
            }
        }
        binding.nameTV.text = driverData!!.driverName!!.fullName

        if (driverData!!.driverName!!.profileFile != null) {
            binding.imgCIV.loadFromUrl(baseActivity, driverData!!.driverName!!.profileFile!!, R.mipmap.default_image)
        }

        alertDialog!!.show()
    }


    private fun openRatingFragment() {
        val bundle = Bundle()
        bundle.putParcelable("driver", driverData)
        bundle.putInt("orderID", id.toInt())
        baseActivity.replaceFragWithArgs(RatingFragment(), R.id.frame_container, bundle)
    }

    private fun setAdapter() {
        adapter = TrackStateAdapter(baseActivity, arraList, this)
        trackRV.adapter = adapter
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.DRIVER_RATING) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                gotoHomeFragment()
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.API_DRIVER_DETAILS + "/" + driverID) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val driverDetail = Gson().fromJson(jsonObject.getJSONObject("driver").toString(), DriverDetail::class.java)
                setDriverData(driverDetail)
            }
        }
    }

    private fun setDriverData(driverDetail: DriverDetail?) {
        driverDetailss = driverDetail
    }
}