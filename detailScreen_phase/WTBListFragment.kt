package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.adapter.others.WTBAdapter
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.WTBData
import com.myzonebuyer.utils.Const
import kotlinx.android.synthetic.main.fg_hp_notification.*
import org.json.JSONObject

class WTBListFragment : BaseFragment() {
    var adapter: WTBAdapter? = null
    var arrayList = ArrayList<WTBData>()
    var pageCount = 0
    var singleHit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.to_buy_list))
        return inflater.inflate(R.layout.fg_hp_notification, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        resetAll()
        listRV.layoutManager = LinearLayoutManager(baseActivity)
        listRV.addItemDecoration(DividerItemDecoration(baseActivity,
                DividerItemDecoration.VERTICAL))

        getList()

    }

    private fun resetAll() {
        pageCount = 0
        arrayList.clear()
        singleHit = false
        adapter = null
    }

    private fun getList() {
        if (!singleHit) {
            singleHit = true
            syncManager.sendToServer(Const.PRODUCT_LISTING + "?page=" + pageCount, null, this)
        }
    }

    private fun setAdapter() {
        if (adapter == null) {
            adapter = WTBAdapter(baseActivity, arrayList)
            listRV.adapter = adapter
        } else {
            adapter!!.notifyDataSetChanged()
        }
    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.PRODUCT_LISTING) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                pageCount++
                singleHit = jsonObject.getJSONObject("list").getInt("total") >= pageCount
                val array = jsonObject.getJSONObject("list").getJSONArray("data")

                for (i in 0 until array.length()) {
                    val data = Gson().fromJson(array.getJSONObject(i).toString(), WTBData::class.java)
                    arrayList.add(data)
                }
                setAdapter()

            } else {
                elseErrorMsg(jsonObject)
                singleHit = true
                setAdapter()
            }
        }
    }
}