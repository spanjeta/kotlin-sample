package com.myzonebuyer.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.myzonebuyer.activity.BaseActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


fun ImageView.loadFromUrl(baseActivity: BaseActivity, string: String, int: Int) {
    Glide.with(baseActivity)
            .load(string)
            .error(int)
            .into(this)

}


fun CircleImageView.loadFromUrl(baseActivity: BaseActivity, string: String, int: Int) {
    Glide.with(baseActivity)
            .load(string)
            .error(int)
            .into(this)

}

fun CircleImageView.loadFromFile(baseActivity: BaseActivity?, image_file: File?, ic_default_user: Int) {
    Glide.with(baseActivity)
            .load(image_file)
            .error(ic_default_user)
            .into(this)
}


fun CircleImageView.loadFromDrawable(baseActivity: BaseActivity?, int: Int) {
    Glide.with(baseActivity).load(int)
            .into(this)
}



