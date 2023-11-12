package de.matthiasmann.twl;

import java.awt.*;
import java.awt.datatransfer.*;

public final class Clipboard
{
    private Clipboard() {
    }
    
    public static String getClipboard() {
        try {
            final java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            final Transferable transferable = clipboard.getContents(null);
            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String)transferable.getTransferData(DataFlavor.stringFlavor);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
    
    public static void setClipboard(final String str) {
        try {
            final java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            final StringSelection transferable = new StringSelection(str);
            clipboard.setContents(transferable, transferable);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
