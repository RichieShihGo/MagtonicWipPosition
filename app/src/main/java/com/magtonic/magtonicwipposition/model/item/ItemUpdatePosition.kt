package com.magtonic.magtonicwipposition.model.item

import com.google.gson.Gson
import com.magtonic.magtonicwipposition.model.receive.RJPosition
import com.magtonic.magtonicwipposition.model.receive.RJUpdatePosition

class ItemUpdatePosition {
    var rjUpdatePosition: RJUpdatePosition? = RJUpdatePosition()

    companion object {
        const val RESULT_CORRECT = "0"

        fun transRJUpdatePositionStrToItemUpdatePosition(RJUpdatePositionStr: String): ItemUpdatePosition? {
            val gson = Gson()
            val itemUpdatePosition = ItemUpdatePosition()
            val rjUpdatePosition: RJUpdatePosition
            try {
                rjUpdatePosition = gson.fromJson<Any>(RJUpdatePositionStr, RJUpdatePosition::class.java) as RJUpdatePosition
                itemUpdatePosition.rjUpdatePosition = rjUpdatePosition
            } catch (ex: Exception) {
                return null
            }

            return itemUpdatePosition
        }//trans_RJReceiptStr_To_ItemReceipt

    }
}