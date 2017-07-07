package com.xplore.reserve

/**
 * Created by Nikaoto on 6/7/2017.
 */

class ReserveCard(
        val id: Int,
        val name: String,
        val imageId: Int,
        val iconId: Int
) {
    //Empty constructor
    constructor() : this(-1, "", -1, -1)
}