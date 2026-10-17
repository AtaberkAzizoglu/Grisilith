package com.azizoglu.grisilith.ui.input

/** Press once immediately in the caller; repeat only while the same control is held. */
class AcceleratingPressRepeat {
    private var target: String? = null
    private var elapsed = 0f
    private var next = .38f
    fun begin(id: String) { target=id;elapsed=0f;next=.38f }
    fun reset() { target=null;elapsed=0f;next=.38f }
    fun update(delta:Float,held:Boolean,hovered:String?):Int {
        if(!held || hovered!=target || target==null) { reset();return 0 }
        elapsed+=delta.coerceIn(0f,.25f)
        var count=0
        while(elapsed>=next && count<8) {
            count++
            val progress=((next-.38f)/1.2f).coerceIn(0f,1f)
            next+=.14f+(.035f-.14f)*progress
        }
        return count
    }
}
