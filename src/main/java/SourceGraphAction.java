import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// TODO: Use some application-level storage rather than this project-level solution!
@State(name = "SourceGraphActionSettings", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public abstract class SourceGraphAction extends AnAction implements PersistentStateComponent<SourceGraphAction.State> {
    protected void showNPSNotificationIfNeeded() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        boolean hasBeenDisplayedToday = !Objects.isNull(myState.npsNotificationLastShownAt) && sdf.format(new Date()).equals(sdf.format(myState.npsNotificationLastShownAt));
        if (!Boolean.TRUE.equals(myState.npsNotificationClicked)
                && !Boolean.TRUE.equals(myState.npsNotificationDismissed)
                && !hasBeenDisplayedToday) {
            showNPSNotification();
            myState.npsNotificationLastShownAt = new Date();
            myState.npsNotificationLShownCount++;
        }
    }

    private void showNPSNotification() {
        Notification notification = new Notification("Sourcegraph NPS", "Tell us what you think",
                "Would recommend Sourcegraph to a friend?", NotificationType.INFORMATION);
        Icon sourcegraphIcon = IconLoader.getIcon("/icons/icon.png", SourceGraphAction.class);
        notification.setIcon(sourcegraphIcon);
        notification.addAction(new OpenFormAction("Answer", notification, myState));
        notification.addAction(new OpenFormAction("Dismiss", notification, myState));
        notification.addAction(new DismissAction("Never Ask Again", notification, myState));
        Notifications.Bus.notify(notification);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(this::showNPSNotificationIfNeeded);
    }

    static class OpenFormAction extends AnAction {
        private final Notification notification;
        private final State state;

        public OpenFormAction(@Nullable @NlsActions.ActionText String text, Notification notification, SourceGraphAction.State state) {
            super(text);
            this.notification = notification;
            this.state = state;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            this.state.npsNotificationClicked = true;
            BrowserLauncher.getInstance().open("https://share.hsforms.com/1cScuhL7_SKu0INs1Z14vyg1n7ku");
            this.notification.expire();
        }
    }

    static class DismissAction extends AnAction {
        private final Notification notification;
        private final State state;

        public DismissAction(@Nullable @NlsActions.ActionText String text, Notification notification, SourceGraphAction.State state) {
            super(text);
            this.notification = notification;
            this.state = state;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            this.state.npsNotificationDismissed = true;
            this.notification.expire();
        }
    }

    static class State {
        public Date npsNotificationLastShownAt;
        public int npsNotificationLShownCount;
        public boolean npsNotificationClicked;
        public boolean npsNotificationDismissed;
    }

    protected State myState = new State();

    public State getState() {
        return myState;
    }

    public void loadState(@NotNull State state) {
        myState = state;
    }
}
