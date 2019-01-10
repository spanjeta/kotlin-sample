package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.extensions.isBlank
import com.myzonebuyer.fragment.BaseFragment
import kotlinx.android.synthetic.main.fg_dsp_add_money.*

class AddMoneyFragment : BaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, baseActivity.getString(R.string.add_money))
        return inflater.inflate(R.layout.fg_dsp_add_money, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {


        addMoneyBT.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.addMoneyBT -> {
                if (isValidate()) {
                }
            }
        }
    }

    private fun isValidate(): Boolean {
        when {
            addET.isBlank() -> showToastOne(getString(R.string.enter_money_to_add))
            else -> return true
        }

        return false
    }
}