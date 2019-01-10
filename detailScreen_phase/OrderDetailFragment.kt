package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.adapter.others.OrderDetailsAdapter
import com.myzonebuyer.extensions.changeDateFormat
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.getPaymentType
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.OrderDetail
import com.myzonebuyer.model.OrderItemData
import com.myzonebuyer.utils.Const
import kotlinx.android.synthetic.main.fg_history_detail.*
import kotlinx.android.synthetic.main.include_order_details.*
import org.json.JSONObject

class OrderDetailFragment : BaseFragment() {
    var order_id = 0
    var arrayList = ArrayList<OrderItemData>()
    var orderAdapter: OrderDetailsAdapter? = null
    var order_by: OrderDetail? = null
    var new_id = 0
    var itemID = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            order_id = args.getInt("order_id", 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.order_details))
        return inflater.inflate(R.layout.fg_history_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        resetData()

        listRV.layoutManager = LinearLayoutManager(baseActivity)
        trackBT.setOnClickListener(this)

        getHistoryDetail()
    }

    private fun resetData() {
        orderAdapter = null
        arrayList.clear()

    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.trackBT -> {

            }
        }
    }

    private fun getHistoryDetail() {
        syncManager.sendToServer(Const.ORDER_DETAILS + "/" + order_id, null, this)
    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.ORDER_DETAILS + "/" + order_id) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val array = jsonObject.getJSONObject("order").getJSONArray("order_item")
                for (i in 0 until array.length()) {
                    val orderItem = Gson().fromJson(array.getJSONObject(i).toString(), OrderItemData::class.java)
                    arrayList.add(orderItem)
                }
                order_by = Gson().fromJson(jsonObject.getJSONObject("order").toString(), OrderDetail::class.java)
                setOrderData()
                setAdapter()
            } else {
                elseErrorMsg(jsonObject)
                setAdapter()
            }
        } else if (jsonObject?.getString("url") == Const.API_ORDER_CANCEL + "/" + new_id) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                gotoHomeFragment()
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.API_ORDER_REFUND + "/" + itemID) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                val args = Bundle()
                args.putInt("orderID", itemID)
                baseActivity.replaceFragWithArgs(RefundOrderFragment(), R.id.frame_container, args)
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }

    private fun setOrderData() {
        nameTV.text = order_by!!.createdBy!!.fullName
        contactTV.text = baseActivity.getString(R.string.phone_plus, order_by!!.createdBy!!.contactNo)
        addressTV.text = order_by!!.orderAddress

        orderIDTV.text = baseActivity.getString(R.string.order_id, order_by!!.id)
        placedTV.text = baseActivity.getString(R.string.placed_on, changeDateFormat(order_by!!.createdAt!!))


        paidOnTV.visibility = View.GONE
//        if (order_by!!.orderTransaction != null) {
//            paidOnTV.text = baseActivity.getString(R.string.paid_on, changeDateFormat(order_by!!.orderTransaction!!.createdAt!!))
//        } else {
//            paidOnTV.visibility = View.GONE
//        }

        val s = (order_by!!.totalPrice.toBigDecimal().minus(order_by!!.shippingPrice.toBigDecimal())).toPlainString()

        when {
            s.contains("-") -> subTotalTV.text = baseActivity.getString(R.string.money_unit, s.replace("-", ""))
            else -> subTotalTV.text = baseActivity.getString(R.string.money_unit, s)
        }

        shippingTV.text = baseActivity.getString(R.string.money_unit, order_by!!.shippingPrice)

        itemsTV.text = baseActivity.getString(R.string.items, arrayList.size)
        totalTV.text = Html.fromHtml(getString(R.string.total) + "<font color=\"#000000\">" + baseActivity.getString(R.string.money_unit, order_by!!.totalPrice) + "</font>")
        paidTV.text = getPaymentType(order_by!!.paymentType)


        sellerTV.text = baseActivity.getString(R.string.sold_by, order_by!!.createdBy!!.fullName)

    }


    private fun setAdapter() {
        when (orderAdapter) {
            null -> {
                orderAdapter = OrderDetailsAdapter(baseActivity, arrayList, this)
                listRV.adapter = orderAdapter
            }
            else -> orderAdapter!!.notifyDataSetChanged()
        }

    }

    fun gotoTrackScreen(pos: Int) {
        when (order_by!!.pickupType) {
            Const.PICKUP_MYSELF -> {
                val params = Bundle()
                params.putInt("order_id", order_id)
                params.putInt("product_id", arrayList[pos].orderProduct!!.id!!)
                baseActivity.replaceFragWithArgs(TrackingFragment(), R.id.frame_container, params)
            }

            Const.PICKUP_DRIVER -> {
                val args = Bundle()
                args.putString("id", arrayList[pos].id.toString())
                args.putString("state", arrayList[pos].stateId.toString())
                if (arrayList[pos].driver != null) {
                    args.putParcelable("driver", arrayList[pos].driver)
                }
                baseActivity.replaceFragWithArgs(TrackOrderFragment(), R.id.frame_container, args)
            }

            else -> {
                val args = Bundle()
                args.putString("id", arrayList[pos].id.toString())
                args.putString("state", arrayList[pos].stateId.toString())
                if (arrayList[pos].driver != null) {
                    args.putParcelable("driver", arrayList[pos].driver)
                }
                baseActivity.replaceFragWithArgs(TrackOrderFragment(), R.id.frame_container, args)
            }
        }
    }

    fun hitCancelProductApi(id: Int?) {
        new_id = id!!
        syncManager.sendToServer(Const.API_ORDER_CANCEL + "/" + new_id, null, this)
    }


    fun checkRefundStatus(id: Int?) {
        itemID = id!!
        syncManager.sendToServer(Const.API_ORDER_REFUND + "/" + id, null, this)
    }
}