package com.myzonebuyer.fragment.detailScreen_phase

import android.databinding.DataBindingUtil
import android.graphics.Paint
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.lightsky.infiniteindicator.IndicatorConfiguration
import cn.lightsky.infiniteindicator.Page
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.databinding.FgDspHistoryDetailBinding
import com.myzonebuyer.extensions.changeDateFormat
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.replaceFragWithArgs
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.fragment.top_menu_home.ChatFragment
import com.myzonebuyer.model.FreshData
import com.myzonebuyer.model.SpecialListData
import com.myzonebuyer.utils.Const
import com.myzonebuyer.utils.UILoader
import com.myzoneseller.model.ProductDetailData
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.fg_dsp_history_detail.*
import org.json.JSONObject


class HistoryDetailScreen : BaseFragment() {
    var binding: FgDspHistoryDetailBinding? = null
    var data: FreshData? = null
    var specialData: SpecialListData? = null
    var mPageViews = ArrayList<Page>()
    var isdata = false
    var prod_id = 0
    var prodData: ProductDetailData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        when {
            args != null && args.containsKey("data") -> {
                data = args.getParcelable("data")
                isdata = true
            }
            args != null && args.containsKey("specialListData") -> {
                specialData = args.getParcelable("specialListData")
                isdata = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        when {
            isdata -> setToolbarMain(true, data!!.title)
            else -> setToolbarMain(true, specialData!!.title)
        }
        if (binding == null)
            binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_history_detail, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        if (data != null) {

            prod_id = data!!.id!!
            getProductDetails()

            for (i in 0 until data!!.images!!.size) {
                mPageViews.add(Page(i.toString(), data!!.images!![i].file))
            }

        }


        if (specialData != null) {


            prod_id = specialData!!.id!!
            getProductDetails()

            for (i in 0 until specialData!!.images!!.size) {
                mPageViews.add(Page(i.toString(), specialData!!.images!![i].file))
            }
        }

        testCircleIndicator()

        binding!!.chatBT.setOnClickListener(this)
        binding!!.cartBT.setOnClickListener(this)
    }

    private fun getProductDetails() {
        syncManager.sendToServer(Const.PRODUCT_DETAIL + "/" + prod_id, null, this)
    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.chatBT -> {
                gotoChatFragment()
            }

            R.id.cartBT -> {
                hitAddCartApi()
            }
        }
    }


    private fun hitAddCartApi() {
        val params = RequestParams()
        when {
            isdata -> params.put("product_id", data!!.id)
            else -> params.put("product_id", specialData!!.id)
        }
        syncManager.sendToServer(Const.ADD_CART, params, this)
    }

    private fun gotoChatFragment() {
        val args = Bundle()
        when {
            isdata -> {
                args.putInt("user_id", data!!.createdBy!!.id!!)
                args.putString("name", data!!.createdBy!!.fullName)
            }
            else -> {
                args.putInt("user_id", specialData!!.createdById!!)
                args.putString("name", specialData!!.title)
            }
        }
        baseActivity.replaceFragWithArgs(ChatFragment(), R.id.frame_container, args)
    }


    private fun testCircleIndicator() {
        try {
            val configuration = IndicatorConfiguration.Builder()
                    .imageLoader(UILoader())
                    .isStopWhileTouch(true)
                    .direction(IndicatorConfiguration.RIGHT)
                    .onPageChangeListener(object : ViewPager.OnPageChangeListener {
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        }

                        override fun onPageSelected(position: Int) {
                        }

                        override fun onPageScrollStateChanged(state: Int) {

                        }
                    })
                    .position(IndicatorConfiguration.IndicatorPosition.Center_Bottom)
                    .build()
            binding!!.indicatorDefaultCircle.init(configuration)
            binding!!.indicatorDefaultCircle.notifyDataChange(mPageViews)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.ADD_CART) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                val array = jsonObject.getJSONObject("cart").getJSONArray("cart_item")
                if (array.length() > 0) store.setInt("cart_count", array.length())
                baseActivity.supportFragmentManager.popBackStack()
            } else {
                elseErrorMsg(jsonObject)
            }
        } else if (jsonObject?.getString("url") == Const.PRODUCT_DETAIL + "/" + prod_id) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                prodData = Gson().fromJson(jsonObject.getJSONObject("details").toString(), ProductDetailData::class.java)
                setPreFilledData()
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }

    private fun setPreFilledData() {
        titleTV.text = prodData!!.title
        descTV.text = prodData!!.description
        priceTV.text = baseActivity.getString(R.string.money_unit, prodData!!.price)


        if (!isdata){
            priceTV.text = Html.fromHtml("Price:$" + "<strike>" + prodData!!.tempPrice + "</strike>" + " "+baseActivity.getString(R.string.money_unit, prodData!!.price))
        }

        timeTV.text = changeDateFormat(prodData!!.updatedAt!!)
        addDetailTV.text = prodData!!.landmark
        landmarkTV.text = prodData!!.address
        itemTV.text = getItemCondition(prodData!!.itemCondition)
        quantityTV.text = prodData!!.quantity.toString()
        categoryTV.text = prodData!!.category!!.title

        returnTV.text = baseActivity.getString(R.string.return_policy,prodData!!.category!!.refundDays.toString())


    }


    private fun getItemCondition(itemCondition: Int?): String {
        return when (itemCondition) {
            1 -> getString(R.string.used)
            2 -> getString(R.string.new_one)
            else -> getString(R.string.used)

        }
    }


}