# Bring Minecraft to the foreground
$mcWindow = Get-Process | Where-Object { $_.MainWindowTitle -like "*Minecraft*" -and $_.MainWindowTitle -like "*Multiplayer*" } | Select-Object -First 1
if ($mcWindow) {
    [void] [System.Reflection.Assembly]::LoadWithPartialName("System.Windows.Forms")
    Add-Type -TypeDefinition @"
        using System;
        using System.Runtime.InteropServices;
        public class WinAPI {
            [DllImport("user32.dll")]
            public static extern bool SetForegroundWindow(IntPtr hWnd);
        }
"@ -Language CSharp

    [WinAPI]::SetForegroundWindow($mcWindow.MainWindowHandle)
    Start-Sleep -Milliseconds 10
}

# Unpause Minecraft
[System.Windows.Forms.SendKeys]::SendWait("{ESC}")
Start-Sleep -Milliseconds 10

# Open chat and paste command
[System.Windows.Forms.SendKeys]::SendWait("t")  # Open chat
Start-Sleep -Milliseconds 100

# Copy the command to the clipboard
[System.Windows.Forms.Clipboard]::SetText("/plugman disable KinectSandbox")
Start-Sleep -Milliseconds 10

# Paste the command (CTRL+V)
[System.Windows.Forms.SendKeys]::SendWait("^(v)")
Start-Sleep -Milliseconds 100

# Send Enter to execute the command
[System.Windows.Forms.SendKeys]::SendWait("{ENTER}")
