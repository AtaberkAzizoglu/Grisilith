package com.azizoglu.grisilith.lwjgl3

import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Platform
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.ptr.PointerByReference
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/** Windows owns the pin/unpin menu. We supply a stable identity and a valid relaunch target. */
object WindowsTaskbar {
    const val APP_ID = "Azizoglu.Grisilith"
    private const val PROPERTY_FORMAT = "9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3"
    private const val PROPERTY_STORE_IID = "886D8EEB-8CF2-4446-8D02-CDBA1DBDCF99"
    private val shell by lazy { NativeLibrary.getInstance("shell32") }

    fun initialize(): Boolean = windowsOperation {
        checkResult(shellCall("SetCurrentProcessExplicitAppUserModelID", WString(APP_ID)))
    }

    fun configureWindow(handle: Long, iconDirectory: Path? = null): Boolean = windowsOperation {
        val launch = launchDetails(iconDirectory)
        val output = PointerByReference()
        guid(PROPERTY_STORE_IID).use { iid ->
            checkResult(shellCall("SHGetPropertyStoreForWindow", Pointer(handle), iid, output))
        }
        val store = requireNotNull(output.value)
        try {
            // Set relaunch properties before the window ID, so Explorer sees a complete identity.
            setString(store, 2, launch.command)
            setString(store, 3, launch.iconResource)
            setString(store, 4, "Grisilith")
            setString(store, 5, APP_ID)
            // Window property stores apply immediately; unlike file stores they need no Commit.
        } finally { comFunction(store, 2).invokeInt(arrayOf(store)) }
    }

    internal data class LaunchDetails(val command: String, val iconResource: String)

    internal fun launchDetails(iconDirectory: Path? = null): LaunchDetails {
        val kernel = NativeLibrary.getInstance("kernel32")
        val module = Memory(32768L * Native.WCHAR_SIZE)
        val length = kernel.getFunction("GetModuleFileNameW", Function.ALT_CONVENTION)
            .invokeInt(arrayOf(null, module, 32768))
        check(length in 1 until 32768) { "Cannot locate the Windows launcher." }
        val executable = File(module.getWideString(0)).absoluteFile
        module.close()
        // Construo/roast embeds the JVM in Grisilith.exe. Pin that EXE, not javaw.exe.
        if (!executable.name.equals("java.exe", true) && !executable.name.equals("javaw.exe", true)) {
            return LaunchDetails(quote(executable.path), "${executable.path},0")
        }
        val code = File(WindowsTaskbar::class.java.protectionDomain.codeSource.location.toURI()).absoluteFile
        val javaw = File(System.getProperty("java.home"), "bin/javaw.exe")
        val folder = iconDirectory ?: Paths.get(
            System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home"), "Grisilith", "desktop"
        )
        Files.createDirectories(folder)
        val command = if (code.isFile && code.extension.equals("jar", true)) {
            "${quote(javaw.path)} -jar ${quote(code.path)}"
        } else {
            // Explorer limits relaunch command length. Keep development classpaths in a tiny
            // manifest launcher instead of stuffing thousands of characters into the property.
            val classpath = System.getProperty("java.class.path").split(File.pathSeparator)
                .joinToString(" ") { File(it).absoluteFile.toURI().toASCIIString() }
            val manifest = Manifest().apply {
                mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
                mainAttributes[Attributes.Name.MAIN_CLASS] = "com.azizoglu.grisilith.lwjgl3.Lwjgl3Launcher"
                mainAttributes[Attributes.Name.CLASS_PATH] = classpath
            }
            val launcher = folder.resolve("Grisilith-launcher.jar").toAbsolutePath()
            JarOutputStream(Files.newOutputStream(launcher), manifest).use { }
            "${quote(javaw.path)} -jar ${quote(launcher.toString())}"
        }
        val icon = folder.resolve("grisilith.ico").toAbsolutePath()
        val bytes = requireNotNull(WindowsTaskbar::class.java.getResourceAsStream("/icons/grisilith.ico")) {
            "The packaged Grisilith icon is missing."
        }.use { it.readBytes() }
        Files.createDirectories(folder)
        if (!Files.exists(icon) || !Files.readAllBytes(icon).contentEquals(bytes)) Files.write(icon, bytes)
        return LaunchDetails(command, "$icon,0")
    }

    private fun quote(value: String) = "\"$value\""

    private fun windowsOperation(action: () -> Unit): Boolean {
        if (!Platform.isWindows()) return false
        return try { action(); true }
        catch (error: Exception) {
            System.err.println("Grisilith taskbar setup: ${error.message}"); false
        } catch (error: LinkageError) {
            System.err.println("Grisilith taskbar setup: ${error.message}"); false
        }
    }

    private fun shellCall(name: String, vararg arguments: Any?): Int =
        shell.getFunction(name, Function.ALT_CONVENTION).invokeInt(arguments)

    private fun comFunction(store: Pointer, slot: Int): Function = Function.getFunction(
        store.getPointer(0).getPointer(slot.toLong() * Native.POINTER_SIZE), Function.ALT_CONVENTION
    )

    private fun setString(store: Pointer, propertyId: Int, value: String) {
        guid(PROPERTY_FORMAT, propertyId).use { key ->
            Memory((value.length + 1L) * Native.WCHAR_SIZE).use { string ->
                string.setWideString(0, value)
                // PROPVARIANT: 8-byte header, then the pointer-containing union (24 bytes on x64).
                Memory(if (Native.POINTER_SIZE == 8) 24L else 16L).use { variant ->
                    variant.clear(); variant.setShort(0, 31) // VT_LPWSTR
                    variant.setPointer(8, string)
                    checkResult(comFunction(store, 6).invokeInt(arrayOf(store, key, variant)), "property $propertyId")
                }
            }
        }
    }

    /** Native GUID layout is little-endian Data1/2/3 followed by eight network-order bytes. */
    internal fun guid(value: String, propertyId: Int? = null): Memory {
        val id = UUID.fromString(value)
        return Memory(if (propertyId == null) 16L else 20L).apply {
            clear()
            setInt(0, (id.mostSignificantBits ushr 32).toInt())
            setShort(4, (id.mostSignificantBits ushr 16).toShort())
            setShort(6, id.mostSignificantBits.toShort())
            for (i in 0..7) setByte(8L + i, (id.leastSignificantBits ushr (56 - i * 8)).toByte())
            if (propertyId != null) setInt(16, propertyId)
        }
    }

    private fun checkResult(result: Int, operation: String = "Shell API") {
        check(result >= 0) { "$operation returned HRESULT 0x${Integer.toHexString(result)}" }
    }
}
