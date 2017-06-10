package com.explorify.xplore.xplore

/**
 * Created by Nika on 6/7/2017.
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