package com.xplore.base

/*
 * Created by Nika on 11/10/2017.
 */

open class BasePresenterImpl<V : BaseView> : BasePresenter<V> {

    protected var view: V? = null

    override fun attach(view: V) {
        this.view = view
    }

    override fun detach() {
        view = null
    }
}