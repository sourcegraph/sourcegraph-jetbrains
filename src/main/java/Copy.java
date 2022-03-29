import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;

public class Copy extends FileAction {

    @Override
    void handleFileUri(String uri) {
        // Remove utm tags for sharing
        String shortenURI = uri.replaceAll("(&utm_product_name=)(.*)", "");
        // Copy file uri to clipboard
        CopyPasteManager.getInstance().setContents(new StringSelection(shortenURI));

        // Display bubble
        Notification notification = new Notification("Sourcegraph", "Sourcegraph",
                "File URL copied to clipboard."+shortenURI, NotificationType.INFORMATION);
        Notification notification2 = new Notification("Sourcegraph NPS", "NPS test",
                "", NotificationType.INFORMATION);
        Icon sourcegraphIcon = IconLoader.getIcon("/icons/icon.png", Copy.class);
        notification2.setIcon(sourcegraphIcon);
        notification2.setContent("<html>\n" +
                                 "    <body>\n" +
                                 "        <form accept-charset='UTF-8' action='action_page.php' autocomplete='off' method='GET' target='_blank'>\n" +
                                 "    	    <label for='name'>Name</label><br /> \n" +
                                 "    	    <input name='name' type='text' value='Frank' /> <br /> \n" +
                                 "        </form>\n" +
                                 "    </body>\n" +
                                 "</html>");
//        Editor.getProject
//        NotificationGroupManager.getInstance().getNotificationGroup("Sourcegraph")
//                .createNotification("File URL copied to clipboard."+shortenURI, NotificationType.INFORMATION)
//                .notify(this.);
        Notifications.Bus.notify(notification);
        Notifications.Bus.notify(notification2);
    }
}
