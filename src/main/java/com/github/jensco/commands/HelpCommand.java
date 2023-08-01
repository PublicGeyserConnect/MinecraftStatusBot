package com.github.jensco.commands;

import com.github.jensco.Bot;
import com.github.jensco.util.BotColors;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

public class HelpCommand extends SlashCommand {

    public HelpCommand() {
        this.name = "help";
        this.help = "I think you already know what this does";
        this.guildOnly = false;
    }

    @Override
    protected void execute(@NotNull SlashCommandEvent event) {
        try {
            event.replyEmbeds(Collections.singleton(handle())).queue();
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private MessageEmbed handle() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        EmbedBuilder helpEmbed = new EmbedBuilder()
                .setColor(BotColors.SUCCESS.getColor())
                .setTitle("WatchDog Help");

        for (SlashCommand command : Bot.getSlashCommands()) {
            if (!command.isHidden()) {
                helpEmbed.addField("`/" + command.getName() + (command.getArguments() != null ? " " + command.getArguments() : "") + "`", command.getHelp(), true);
            }
        }

        return helpEmbed.build();
    }
}