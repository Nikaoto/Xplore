package com.xplore.base

/*
 * Created by Nika on 11/10/2017.
 */

interface BasePresenter<in V : BaseView> {

    fun attach(view: V)

    fun detach()

}