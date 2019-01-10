package com.myzonebuyer.extensions

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity


fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        replace(frameId, fragment).addToBackStack(fragment.javaClass.name)
    }
}

fun AppCompatActivity.replaceFragWithArgs(fragment: Fragment, frameId: Int, args: Bundle) {
    fragment.arguments = args
    supportFragmentManager.inTransaction {
        replace(frameId, fragment).addToBackStack(fragment.javaClass.name)
    }
}

fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int, backStackTag: String? = null) {
    supportFragmentManager.inTransaction {
        add(frameId, fragment)
        backStackTag?.let {
            addToBackStack(fragment.javaClass.name)
        }!!
    }
}

fun AppCompatActivity.clearAllStacks() {
    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
}


fun AppCompatActivity.clearStack() {
    //Here we are clearing back stack fragment entries
    val backStackEntry = supportFragmentManager.backStackEntryCount
    if (backStackEntry > 0) {
        for (i in 0 until backStackEntry) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    //Here we are removing all the fragment that are shown here
    if (supportFragmentManager.fragments.size > 0) {
        for (i in 0 until supportFragmentManager.fragments.size) {
            val mFragment = supportFragmentManager.fragments[i]
            if (mFragment != null) {
                supportFragmentManager.beginTransaction().remove(mFragment).commit()
            }
        }
    }
}


fun AppCompatActivity.clearNotifications() {
    val nMgr = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nMgr.cancelAll()
}



