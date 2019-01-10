package com.myzonebuyer.fragment.detailScreen_phase

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.activity.NewMainActivity
import com.myzonebuyer.databinding.FgDspRatingBinding
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.Driver
import com.myzonebuyer.utils.Const
import com.toxsl.volley.toolbox.RequestParams
import kotlinx.android.synthetic.main.include_comment_rating.*

class RatingFragment : BaseFragment() {
    var binding: FgDspRatingBinding? = null
    var orderID = 0
    var driverData: Driver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments!!.let {
            orderID = it.getInt("orderID", 0)
            driverData = it.getParcelable("driver")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.rating))
        binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_rating, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding!!.submitBT.setOnClickListener(this)
        binding!!.commentIN.imgCIV.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.submitBT -> {
                if (isValidate()) {
                    val params = RequestParams()
                    params.put("order_item_id", "")
                    params.put("rating", ratingRB.rating)
                    syncManager.sendToServer(Const.DRIVER_RATING, params, this)
                }
            }


        }
    }

    private fun isValidate(): Boolean {
        when {
            ratingRB.rating.toInt() == 0 -> showToastOne(getString(R.string.give_rating))
            commentET.isBlank() -> showToastOne(getString(R.string.write_your_review))
            else -> return true
        }
        return false
    }

    private fun gotoMain() {
        baseActivity.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        (baseActivity as NewMainActivity).gotoMainHomeFragment()
    }
}