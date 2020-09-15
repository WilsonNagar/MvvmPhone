package com.wilson.mvvmphone.models

data class ShopItems(
    var itemname: String? = null,
    var category: String? = null,
    var imageUrl: String? = null,
    var description:String? = null,
    var sellername: String? = null,
    var sellerid: String? = null,
    var sellerphone: String? = null,
    var price:Int? = 0,
    var payment: Int? = 1,
    var negotiable:Boolean = false
)