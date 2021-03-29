package com.mapitall.SwiftAddress

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

// Improved version of AutoCompleteTextView that always shows the dropdown
// https://stackoverflow.com/a/5783983/15017966
// Note, this was converted to kotlin using Android Studio's automatic Java -> Kotlin feature
class InstantAutoComplete : AppCompatAutoCompleteTextView {
    constructor(context: Context?) : super(context!!)
    constructor(arg0: Context?, arg1: AttributeSet?) : super(arg0!!, arg1)
    constructor(arg0: Context?, arg1: AttributeSet?, arg2: Int) : super(arg0!!, arg1, arg2)

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int,
                                previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused && adapter != null) {
            performFiltering(text, 0)
        }
    }
}