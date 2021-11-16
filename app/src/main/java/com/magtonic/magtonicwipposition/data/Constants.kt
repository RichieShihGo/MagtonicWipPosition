package com.magtonic.magtonicwipposition.data

class Constants {
    class ACTION {
        companion object {
            const val ACTION_HIDE_KEYBOARD : String = "com.magtonic.magtonicwipposition.HideKeyboardAction"
            const val ACTION_CONNECTION_TIMEOUT : String = "com.magtonic.magtonicwipposition.ConnectionTimeOut"
            const val ACTION_CONNECTION_NO_ROUTE_TO_HOST : String = "com.magtonic.magtonicwipposition.ConnectionNoRouteToHost"
            const val ACTION_SERVER_ERROR : String = "com.magtonic.magtonicwipposition.ServerError"

            const val ACTION_NETWORK_FAILED : String = "com.magtonic.magtonicwipposition.ActionNetworkFailed"
            const val ACTION_WIFI_STATE_CHANGED : String = "com.magtonic.magtonicwipposition.ActionWifiStateChanged"
            const val ACTION_USER_INPUT_SEARCH : String = "com.magtonic.magtonicwipposition.UserInputSearch"
            const val ACTION_BARCODE_NULL : String = "com.magtonic.magtonicwipposition.BarcodeNull"

            //get position
            const val ACTION_POSITION_SCAN_BARCODE : String = "com.magtonic.magtonicwipposition.PositionScanBarcode"
            const val ACTION_POSITION_PASS_STORAGE : String = "com.magtonic.magtonicwipposition.PositionPassStorage"
            const val ACTION_POSITION_FRAGMENT_REFRESH : String ="com.magtonic.magtonicwipposition.PositionFragmentRefresh"
            const val ACTION_POSITION_NOT_EXIST : String = "com.magtonic.magtonicwipposition.PositionNotExist"
            const val ACTION_POSITION_UPDATE_ACTION : String = "com.magtonic.magtonicwipposition.PositionUpdateAction"
            const val ACTION_POSITION_UPDATE_FAILED : String = "com.magtonic.magtonicwipposition.PositionUpdateFailed"
            const val ACTION_POSITION_UPDATE_SUCCESS : String = "com.magtonic.magtonicwipposition.PositionUpdateSuccess"
            //scan use camera
            const val ACTION_POSITION_SCAN_BARCODE_CAMERA : String = "com.magtonic.magtonicwipposition.PositionScanBarcodeCamera"
        }
    }
}