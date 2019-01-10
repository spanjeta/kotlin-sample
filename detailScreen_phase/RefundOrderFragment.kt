package com.myzonebuyer.fragment.detailScreen_phase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.lightsky.infiniteindicator.IndicatorConfiguration
import cn.lightsky.infiniteindicator.Page
import com.myzonebuyer.R
import com.myzonebuyer.extensions.checkString
import com.myzonebuyer.extensions.clearStack
import com.myzonebuyer.extensions.getMessage
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.utils.Const
import com.myzonebuyer.utils.ImageUtils
import com.myzonebuyer.utils.UILoader
import com.toxsl.volley.toolbox.RequestParams
import com.zfdang.multiple_images_selector.ImagesSelectorActivity
import com.zfdang.multiple_images_selector.SelectorSettings
import kotlinx.android.synthetic.main.fg_dsp_refund.*
import org.json.JSONObject
import java.io.File

class RefundOrderFragment : BaseFragment() {
    private var mResults = ArrayList<String?>()
    var imageFiles = ArrayList<File>()
    var mPageViews = ArrayList<Page>()
    var orderID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderID = it.getInt("orderID", 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, "Refund")
        return inflater.inflate(R.layout.fg_dsp_refund, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        submitBT.setOnClickListener(this)
        camIV.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.submitBT -> {
                if (isValidate()) {
                    sendRefundApi()
                }

            }

            R.id.camIV -> {
                callMultiImage()
            }
        }
    }

    private fun sendRefundApi() {
        val params = RequestParams()
        params.put("description", descET.checkString())
        params.put("order_item_id", orderID)
        for (i in 0 until imageFiles.size) {
            params.put("files[$i]", imageFiles[i])
        }
        syncManager.sendToServer(Const.API_REFUND_DETAILS, params, this)
    }

    private fun isValidate(): Boolean {
        when {
            imageFiles.size == 0 -> showToastOne(getString(R.string.select_one_image))
            descET.isBlank() -> showToastOne("Please Add description")
            else -> return true
        }
        return false
    }

    private fun callMultiImage() {
        val intent = Intent(baseActivity, ImagesSelectorActivity::class.java)
        intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 5)
        intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true)
        intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults)
        baseActivity.startActivityForResult(intent, Const.REQUEST_CODE)
    }

    fun getResultInfo(data: Intent) {
        mResults.clear()
        imageFiles.clear()
        mPageViews.clear()


        mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS)

        for (i in mResults) {
            val bitmap = ImageUtils.imageCompress(i)
            imageFiles.add(ImageUtils.bitmapToFile(bitmap, baseActivity))
            mPageViews.add(Page(i, Uri.fromFile(ImageUtils.bitmapToFile(bitmap, baseActivity)).toString()))
        }

        testCircleIndicator()
        indicator_default_circle.start()

    }


    override fun onPause() {
        super.onPause()
        indicator_default_circle.stop()
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
            indicator_default_circle.init(configuration)
            indicator_default_circle.notifyDataChange(mPageViews)


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.API_REFUND_DETAILS) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                getMessage(jsonObject, baseActivity)
                gotoHomeFragment()
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }
}