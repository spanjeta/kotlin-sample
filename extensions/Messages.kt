package com.myzonebuyer.extensions

import android.support.v4.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import com.myzonebuyer.R
import com.myzonebuyer.activity.BaseActivity
import com.myzonebuyer.utils.Const
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun getMessage(json: JSONObject, baseActivity: BaseActivity) {
    var string: String?
    string = baseActivity.getString(R.string.success_msg)
    if (json.has("message")) {
        string = json.getString("message")
    }
    showToastMain(baseActivity, string)
}

fun getErrorMsg(json: JSONObject, baseActivity: BaseActivity) {
    var string: String?
    string = baseActivity.getString(R.string.soemthing_went_wrong)
    if (json.has("error")) {
        string = json.getString("error")
    }
    showToastMain(baseActivity, string)
}

fun showToastMain(baseActivity: BaseActivity, string: String) {
    Toast.makeText(baseActivity, string, Toast.LENGTH_SHORT).show()
}

fun TextView.setColor(baseActivity: BaseActivity?, lightGrey: Int) {
    this.setTextColor(ContextCompat.getColor(baseActivity!!, lightGrey))
}


fun getPaymentType(paymentType: Int?): String {

    return when (paymentType) {
        Const.PAYMENT_CASH -> "Payment in: Cash"
        Const.PAYMENT_DRAGONPAY -> "Payment Via Dragon pay"
        Const.PAYMENT_WALLET -> "Payment by: Wallet"
        Const.PAYMENT_PESOPAY -> "Payment by: Pesopay"
        Const.PAYMENT_WITHDRAW -> "Payment by: Wallet"
        else -> ""
    }

}


fun changeDateFormat(dateString: String): String {
    if (dateString.isEmpty()) {
        return ""
    }
    val inputDateFromat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
    var date = Date()
    try {
        date = inputDateFromat.parse(dateString)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    val outputDateFormat = SimpleDateFormat("EEE, MMM d, ''yy h:mm a", Locale.getDefault())
    return outputDateFormat.format(date)
}




