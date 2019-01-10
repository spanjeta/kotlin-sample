package com.myzonebuyer.fragment.detailScreen_phase

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.databinding.FgDspCartDetailBinding
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.loadFromUrl
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.extensions.showToastMain
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.CartItem
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_cart_detail.*
import org.json.JSONObject

class CartDetailFragment : BaseFragment() {
    var binding: FgDspCartDetailBinding? = null
    private var cart_id: String? = ""
    private var cartItem = CartItem()
    var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null && bundle.containsKey("cart_id")) {
            cart_id = bundle.getString("cart_id")
            title = bundle.getString("title", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, title)
        binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_cart_detail, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding!!.buyBT.setOnClickListener(this)
        binding!!.include2.plusTV.visibility=View.INVISIBLE
        binding!!.include2.minusTV.visibility=View.INVISIBLE

        getDetailApi()
    }

    private fun getDetailApi() {
        syncManager.sendToServer(Const.CART_ITEM_DETAIL + "/" + cart_id, null, this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.buyBT -> {
                val args = Bundle()
                args.putInt("cart_id", cartItem.cartId!!)
                baseActivity.replaceFragWithArgs(ShippingFragment(), R.id.frame_container, args)
            }

            R.id.plusTV -> {
                addToCart(cartItem.product!!.id)
            }

            R.id.minusTV -> {
                if (binding!!.include2.numTV.text != Const.MIN_COUNT) {
                    removeCartItemApi(cartItem.id)
                } else {
                    showToastMain(baseActivity, getString(R.string.minimum_num_reached))
                }

            }
        }
    }

    fun addToCart(id: Int?) {
        val params = RequestParams()
        params.put("product_id", id)
        syncManager.sendToServer(Const.ADD_CART, params, this)
    }

    fun removeCartItemApi(id: Int?) {
        val params = RequestParams()
        params.put("cart_item_id", id)
        syncManager.sendToServer(Const.DELETE_CART_ITEM, params, this)
    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.CART_ITEM_DETAIL + "/" + cart_id) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                cartItem = Gson().fromJson(jsonObject.getJSONObject("cart_item").toString(), CartItem::class.java)
                setDetails(cartItem)
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.ADD_CART) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getDetailApi()
                getMessage(jsonObject, baseActivity)
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.DELETE_CART_ITEM) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getDetailApi()
                getMessage(jsonObject, baseActivity)
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }

    private fun setDetails(cartItem: CartItem?) {
        titleTV.text = cartItem!!.product!!.title
        descTV.text = cartItem.product!!.description
        priceTV.text = getString(R.string.price_cart, cartItem.itemPrice)
        binding!!.include2.numTV.text = cartItem.itemQuantity
        if (cartItem.images != null) {
            imgIV.loadFromUrl(baseActivity, cartItem.images!!.file!!, R.mipmap.defaultimg)
        }


    }
}