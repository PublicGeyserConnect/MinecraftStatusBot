package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.records.NotificationRecord;
import com.github.jensco.util.MessageHelper;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class NotificationCommand extends SlashCommand {

    public NotificationCommand() {
        this.name = "notify";
        this.help = "Notify options";
        this.guildOnly = false;
        this.children =
                new SlashCommand[]{
                new SetRoleSubCommand(),
                new DisableNotificationSubCommand(),
                new CheckNotificationSubCommand(),
                new EnableNotificationSubCommand()
        };
    }
    @Override
    protected void execute(SlashCommandEvent event) {

    }

    public static class SetRoleSubCommand extends SlashCommand {
        public SetRoleSubCommand() {
            this.name = "role";
            this.help = "Set the role ID for notification.";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "roleid", "Set the role id for ping notification.", true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            String roleid = event.optString("roleid");
            Checks.notNull(event.getGuild(), "server");
            String guildId = event.getGuild().getId();

            // Check if the role ID is valid
            assert roleid != null;
            Role role = event.getGuild().getRoleById(roleid);
            if (role == null) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "The roleID you tried to add is not a valid role ID.")).queue();
                return;
            }

            NotificationRecord notify = Bot.storageManager.getNotifiedDataByGuildId(guildId);
            if (notify != null && !notify.role().isEmpty()) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "A role is already assigned to notification: " + notify.role())).queue();
                return;
            }

            // Save the notification in the database
            Bot.storageManager.setNotifyRole(guildId, roleid);
            event.replyEmbeds(MessageHelper.handleCommand(false, "RoleID for notification set successfully.")).queue();
        }
    }

    public static class EnableNotificationSubCommand extends SlashCommand {
        public EnableNotificationSubCommand() {
            this.name = "enable";
            this.help = "Enable notification";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            Checks.notNull(event.getGuild(), "server");
            String guildId = event.getGuild().getId();

            NotificationRecord notify = Bot.storageManager.getNotifiedDataByGuildId(guildId);

            if (notify == null) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "You need to add a roleID before you can enable notifications")).queue();
                return;
            }

            // Save the notification in the database
            Bot.storageManager.activeNotify(guildId, true);
            event.replyEmbeds(MessageHelper.handleCommand(false, "Notifications are enabled.")).queue();
        }
    }

    public static class DisableNotificationSubCommand extends SlashCommand {
        public DisableNotificationSubCommand() {
            this.name = "disable";
            this.help = "Disable notification";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            Checks.notNull(event.getGuild(), "server");
            String guildId = event.getGuild().getId();

            NotificationRecord notify = Bot.storageManager.getNotifiedDataByGuildId(guildId);

            if (notify != null) {
                Bot.storageManager.removeNotifyRole(guildId);
                event.replyEmbeds(MessageHelper.handleCommand(false, "Notification settings removed")).queue();
            } else {
                event.replyEmbeds(MessageHelper.handleCommand(true, "No notification data was found for this server")).queue();
            }
        }
    }

    public static class CheckNotificationSubCommand extends SlashCommand {
        public CheckNotificationSubCommand() {
            this.name = "settings";
            this.help = "Display your notification settings.";
            this.userPermissions = new Permission[]{
                    Permission.MANAGE_SERVER
            };
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            Checks.notNull(event.getGuild(), "server");
            String guildId = event.getGuild().getId();

            NotificationRecord notify = Bot.storageManager.getNotifiedDataByGuildId(guildId);

            if (notify == null) {
                event.replyEmbeds(MessageHelper.handleCommand(true, "No notification data was found for this server")).queue();
                return;
            }

            String stringBuilder =
                    "\n\nNotification Data:" +
                    "\nRole: " + notify.role() +
                    "\nActive: " + notify.active();

            // Save the notification in the database
            event.replyEmbeds(MessageHelper.handleCommand(false, stringBuilder)).queue();
        }
    }
}
