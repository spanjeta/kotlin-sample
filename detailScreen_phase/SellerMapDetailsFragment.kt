package com.myzonebuyer.fragment.detailScreen_phase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.activity.BaseActivity
import com.myzonebuyer.adapter.others.SellerMapDetailAdapter
import com.myzonebuyer.adapter.others.WTBAdapter
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.loadFromUrl
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.fragment.top_menu_home.ChatFragment
import com.myzonebuyer.model.BuyerMapData
import com.myzonebuyer.model.SellerMapData
import com.myzonebuyer.model.WTBData
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_seller_map_details.*
import org.json.JSONObject

class SellerMapDetailsFragment : BaseFragment(), BaseActivity.PermCallback {
    var sellerID = 0
    var adapter: SellerMapDetailAdapter? = null
    var buyerAdapter: WTBAdapter? = null
    var contact = ""
    var userName = ""
    var userID = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments!!.let {
            sellerID = it.getInt("id")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, "Details")
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fg_seller_map_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.chatMN -> gotoChatFragment()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun gotoChatFragment() {
        val params = Bundle()
        params.putString("name", userName)
        params.putInt("user_id", userID)
        baseActivity.replaceFragWithArgs(ChatFragment(), R.id.frame_container, params)
    }

    private fun initUI() {
        if (store.getBoolean("is_seller", false)) {
            callIV.setImageResource(R.mipmap.ic_red_call)
            listRV.layoutManager = LinearLayoutManager(baseActivity)
            listRV.addItemDecoration(DividerItemDecoration(baseActivity,
                    DividerItemDecoration.VERTICAL))
            getBuyerDetails()
        } else {
            callIV.setImageResource(R.mipmap.ic_call)
            listRV.layoutManager = GridLayoutManager(baseActivity, 2)
            getSellerDetails()
        }

        callIV.setOnClickListener(this)
    }

    private fun getBuyerDetails() {
        syncManager.sendToServer(Const.API_BUYER_DETAILS + "/" + sellerID, null, this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.callIV -> {
                when {
                    contact.isNotEmpty() -> onCall(contact)
                    else -> showToastOne(getString(R.string.no_contact_found))
                }
            }
        }
    }


    private fun onCall(contact: String) {
        if (baseActivity.checkPermissions(arrayOf(Manifest.permission.CALL_PHONE), Const.CALL_STATUS, this)) {
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

    private fun getSellerDetails() {
        syncManager.sendToServer(Const.API_SELLER_DETAILS + "/" + sellerID, null, this)
    }

    private fun setAdapter(data: SellerMapData) {
        adapter = SellerMapDetailAdapter(baseActivity, data.products, this)
        listRV.adapter = adapter
    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_SELLER_DETAILS + "/" + sellerID) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val data = Gson().fromJson(jsonObject.getJSONObject("seller").toString(), SellerMapData::class.java)
                setPreFilledData(data)
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.ADD_CART) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                val array = jsonObject.getJSONObject("cart").getJSONArray("cart_item")
                if (array.length() > 0) {
                    store.setInt("cart_count", array.length())
                }
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.API_BUYER_DETAILS + "/" + sellerID) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                val data = Gson().fromJson(jsonObject.getJSONObject("seller").toString(), BuyerMapData::class.java)
                setPreFilledBuyerData(data)
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }

    private fun setPreFilledBuyerData(data: BuyerMapData?) {
        imgIV.loadFromUrl(baseActivity, data!!.profileFile!!, R.mipmap.default_profile)
        nameTV.text = data.fullName
        userName = data.fullName!!
        userID = data.id!!
        addressTV.text = data.address
        if (!data.contactNo!!.contains("+")) {
            contactTV.text = baseActivity.getString(R.string.phone_plus, data.contactNo)
        } else {
            contactTV.text = data.contactNo
        }
        contact = contactTV.text.toString()

        setBuyerAdapter(data)

    }

    private fun setBuyerAdapter(data: BuyerMapData) {
        buyerAdapter = WTBAdapter(baseActivity, data.buyList as ArrayList<WTBData>)
        listRV.adapter = buyerAdapter
    }

    private fun setPreFilledData(data: SellerMapData?) {
        imgIV.loadFromUrl(baseActivity, data!!.profileFile!!, R.mipmap.default_profile)
        nameTV.text = data.fullName
        userName = data.fullName!!
        userID = data.id!!
        addressTV.text = data.address
        if (!data.contactNo!!.contains("+")) {
            contactTV.text = baseActivity.getString(R.string.phone_plus, data.contactNo)
        } else {
            contactTV.text = data.contactNo
        }
        contact = contactTV.text.toString()

        setAdapter(data)
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


    fun addToCartApi(id: Int?) {
        val params = RequestParams()
        params.put("product_id", id)
        syncManager.sendToServer(Const.ADD_CART, params, this)
    }

}