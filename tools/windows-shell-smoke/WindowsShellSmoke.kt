package qa

import com.azizoglu.grisilith.lwjgl3.WindowsTaskbar
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.*
import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import org.lwjgl.glfw.GLFWNativeWin32
import java.nio.file.Paths

/** Read back actual Windows Shell properties; never pins or edits the user's taskbar. */
object WindowsShellSmoke {
    @JvmStatic fun main(args: Array<String>) {
        check(WindowsTaskbar.initialize())
        val directory = Paths.get("../tools/windows-shell-smoke/icon-cache").toAbsolutePath()
        var configured = false
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(320, 180); setInitialVisible(false); disableAudio(true)
            setTitle("Grisilith shell verification")
            setWindowIcon("icons/grisilith_icon_128.png", "icons/grisilith_icon_32.png", "icons/grisilith_icon_16.png")
            setWindowListener(object : Lwjgl3WindowAdapter() {
                override fun created(window: Lwjgl3Window) {
                    val handle = GLFWNativeWin32.glfwGetWin32Window(window.windowHandle)
                    check(WindowsTaskbar.configureWindow(handle, directory))
                    val expected = WindowsTaskbar.launchDetails(directory)
                    val output = PointerByReference()
                    WindowsTaskbar.guid("886D8EEB-8CF2-4446-8D02-CDBA1DBDCF99").use { iid ->
                        check(NativeLibrary.getInstance("shell32").getFunction("SHGetPropertyStoreForWindow", Function.ALT_CONVENTION)
                            .invokeInt(arrayOf(Pointer(handle), iid, output)) >= 0)
                    }
                    val store = output.value
                    fun method(slot: Int) = Function.getFunction(store.getPointer(0).getPointer(slot.toLong() * Native.POINTER_SIZE), Function.ALT_CONVENTION)
                    fun property(id: Int): String {
                        WindowsTaskbar.guid("9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3", id).use { key ->
                            Memory(24).use { value ->
                                value.clear()
                                check(method(5).invokeInt(arrayOf(store, key, value)) >= 0)
                                try {
                                    check(value.getShort(0).toInt() == 31)
                                    return value.getPointer(8).getWideString(0)
                                } finally {
                                    NativeLibrary.getInstance("ole32").getFunction("PropVariantClear", Function.ALT_CONVENTION)
                                        .invokeInt(arrayOf(value))
                                }
                            }
                        }
                    }
                    try {
                        check(property(2) == expected.command)
                        check(property(3) == expected.iconResource)
                        check(property(4) == "Grisilith")
                        check(property(5) == WindowsTaskbar.APP_ID)
                    } finally { method(2).invokeInt(arrayOf(store)) }
                    configured = true
                }
            })
        }
        Lwjgl3Application(object : ApplicationAdapter() {
            override fun render() {
                if (configured) {
                    println("WINDOWS_SHELL_SMOKE_PASS: native AppUserModelID, pin/relaunch command, display name and icon verified.")
                    Gdx.app.exit()
                }
            }
        }, config)
    }
}
