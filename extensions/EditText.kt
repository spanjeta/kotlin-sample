package com.myzonebuyer.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })


}


fun EditText.isBlank(): Boolean {
    return this.text.isEmpty()
}


fun EditText.getLength(): Int {
    return this.text.toString().length
}

fun EditText.checkString(): String {
    return this.text.toString().trim()
}
