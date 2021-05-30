package me.TechsCode.TechDiscordBot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TechEmbedBuilder extends EmbedBuilder {

    public TechEmbedBuilder() {
        setColor(new Color(81, 153, 226));
    }

    public TechEmbedBuilder(String title) {
        if(title != null) setAuthor(title, "https://techscode.com", "https://i.imgur.com/nnegGEV.png");
        setColor(new Color(81, 153, 226));
        setFooter("Developed by Tech & Team");
    }

    public TechEmbedBuilder(String title, boolean footer) {
        if(title != null) setAuthor(title, "https://techscode.com", "https://i.imgur.com/nnegGEV.png");
        setColor(new Color(81, 153, 226));
        if(footer) setFooter("Developed by Tech & Team");
    }

    public TechEmbedBuilder(boolean footer) {
        setColor(new Color(81, 153, 226));
        if(footer) setFooter("Developed by Tech & Team");
    }

    public TechEmbedBuilder setFooter(String text) {
        setFooter("Tech's Plugin Support • " + text, "https://i.imgur.com/nzfiUTy.png");
        return this;
    }

    public TechEmbedBuilder error() {
        setColor(new Color(178,34,34));
        return this;
    }

    public TechEmbedBuilder success() {
        setColor(new Color(50, 205, 50));
        return this;
    }

    public TechEmbedBuilder setText(String text) {
        setDescription(text);
        return this;
    }

    public TechEmbedBuilder setText(String... text) {
        setDescription(String.join("\n", text));
        return this;
    }

    public Message send(TextChannel textChannel) {
        return textChannel.sendMessage(build()).complete();
    }

    public Message sendAfter(TextChannel textChannel, int delay, TimeUnit unit) {
        return textChannel.sendMessage(build()).completeAfter(delay, unit);
    }

    public void queueAfter(TextChannel textChannel, int delay, TimeUnit unit) {
        textChannel.sendMessage(build()).queueAfter(delay, unit);
    }

    public Message reply(Message message) {
        return reply(message, true);
    }

    public Message reply(Message message, boolean mention) {
        return message.reply(build()).mentionRepliedUser(mention).complete();
    }

    public void replyTemporary(Message message, int duration, TimeUnit timeUnit) {
        replyTemporary(message, true, duration, timeUnit);
    }

    public void replyTemporary(Message message, boolean mention, int duration, TimeUnit timeUnit) {
        Message msg = message.reply(build()).mentionRepliedUser(mention).complete();
        msg.delete().submitAfter(duration, timeUnit);
    }

    public void queueAfter(User user, int delay, TimeUnit time) {
        try {
            user.openPrivateChannel().complete().sendMessage(build()).queueAfter(delay, time);
        } catch (ErrorResponseException ignore) {}
    }

    public Message send(Member member) {
        return send(member.getUser());
    }

    public Message send(User user) {
        try {
            return user.openPrivateChannel().complete().sendMessage(build()).complete();
        } catch (ErrorResponseException ignore) {}
        return null;
    }

    public void sendTemporary(TextChannel textChannel, int duration, TimeUnit timeUnit) {
        Message message = send(textChannel);
        message.delete().submitAfter(duration, timeUnit);
    }

    public ScheduledFuture<?> sendAfter(TextChannel textChannel, int duration, Consumer<Message> onSuccess) {
        return textChannel.sendMessage(build()).queueAfter(duration, TimeUnit.SECONDS, onSuccess);
    }

    public ScheduledFuture<?> sendAfter(TextChannel textChannel, int duration, TimeUnit timeUnit, Consumer<Message> onSuccess) {
        return textChannel.sendMessage(build()).queueAfter(duration, timeUnit, onSuccess);
    }

    public void sendTemporary(TextChannel textChannel, int duration) {
        sendTemporary(textChannel, duration, TimeUnit.SECONDS);
    }

    @Override
    public TechEmbedBuilder setThumbnail(String url) {
        super.setThumbnail(url);
        return this;
    }

    @Override
    public TechEmbedBuilder setColor(Color color) {
        if(color == null) return this;

        super.setColor(color);
        return this;
    }

    @Override
    public TechEmbedBuilder setImage(String url) {
        super.setImage(url);
        return this;
    }

    @Override
    public TechEmbedBuilder addField(String name, String value, boolean inline) {
        super.addField(name, value, inline);
        return this;
    }

    @Override
    public TechEmbedBuilder addBlankField(boolean inline) {
        super.addBlankField(inline);
        return this;
    }

    public String getText() {
        return super.getDescriptionBuilder().toString();
    }
}