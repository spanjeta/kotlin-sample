package com.myzonebuyer.fragment.detailScreen_phase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myzonebuyer.R
import com.myzonebuyer.extensions.replaceFragment
import com.myzonebuyer.fragment.BaseFragment
import kotlinx.android.synthetic.main.fg_dsp_wallet.*

class WalletFragment : BaseFragment() {
    var amount = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            amount = it!!.getString("money", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.wallet))
        return inflater.inflate(R.layout.fg_dsp_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        addBT.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.addBT -> {
                baseActivity.replaceFragment(AddMoneyFragment(), R.id.frame_container)
            }
        }
    }
}