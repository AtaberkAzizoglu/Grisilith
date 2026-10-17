# Shared application identity

All shipped application icons are derived from `assets/icons/source/grisilith_app_icon_master.png`.
Run `tools/icons/SyncAppIcons.ps1` from PowerShell to regenerate desktop PNG sizes, Windows ICO,
macOS ICNS and Android launcher sizes. This only converts/resizes the existing artwork.

Construo embeds `lwjgl3/icons/logo.png` in the Windows executable and `logo.icns` in macOS bundles.
The runtime window uses `assets/icons/grisilith_icon_{16,32,64,128}.png`.
Android uses the same image; its adaptive foreground has a safe inset and a dark background.

`WindowsTaskbar` sets `Azizoglu.Grisilith` before creating a window, then supplies the HWND's
AppUserModel ID, relaunch command, display name and icon. Native EXE launches relaunch the same
EXE; JAR launches relaunch the JAR. Development launches use a small manifest launcher under
the user's Grisilith desktop-data folder to avoid Explorer's command-length limit.
No automatic taskbar pinning, extra Jump List commands or notifications are added.

Windows Shell read-back test (uses a hidden window; does not modify user pins):
`gradlew.bat -I tools/windows-shell-smoke.gradle lwjgl3:windowsShellSmoke`

API references:
- https://learn.microsoft.com/en-us/windows/win32/shell/appids
- https://learn.microsoft.com/en-us/windows/win32/properties/props-system-appusermodel-relaunchcommand
- https://learn.microsoft.com/en-us/windows/win32/properties/props-system-appusermodel-relaunchiconresource
