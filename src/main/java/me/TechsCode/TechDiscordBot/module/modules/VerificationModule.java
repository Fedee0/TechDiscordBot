package me.TechsCode.TechDiscordBot.module.modules;

//import io.sentry.Sentry;
import me.TechsCode.SpigotAPI.data.Purchase;
import me.TechsCode.TechDiscordBot.TechDiscordBot;
import me.TechsCode.TechDiscordBot.module.Module;
import me.TechsCode.TechDiscordBot.mysql.storage.Verification;
import me.TechsCode.TechDiscordBot.objects.DefinedQuery;
import me.TechsCode.TechDiscordBot.objects.Query;
import me.TechsCode.TechDiscordBot.objects.Requirement;
import me.TechsCode.TechDiscordBot.spigotmc.ProfileComment;
import me.TechsCode.TechDiscordBot.spigotmc.SpigotMC;
import me.TechsCode.TechDiscordBot.spigotmc.api.APIStatus;
import me.TechsCode.TechDiscordBot.util.Plugin;
import me.TechsCode.TechDiscordBot.util.TechEmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VerificationModule extends Module {

  private final DefinedQuery<TextChannel> VERIFICATION_CHANNEL = new DefinedQuery<TextChannel>() {
    @Override
    protected Query<TextChannel> newQuery() { return bot.getChannels("verification");
    }
  };

  private TextChannel channel;
  private Message lastInstructions;

  private List<String> verificationQueue;

  public VerificationModule(TechDiscordBot bot) {
    super(bot);
  }

  @Override
  public void onEnable() {
    channel = VERIFICATION_CHANNEL.query().first();

    lastInstructions = null;
    verificationQueue = new ArrayList<>();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if(lastInstructions != null) lastInstructions.delete().complete();
    }));

    sendInstructions();
  }

  @Override
  public void onDisable() {
    if (lastInstructions != null) lastInstructions.delete().submit();
  }

  public void sendInstructions() {
    if(lastInstructions != null) lastInstructions.delete().complete();

    TechEmbedBuilder howItWorksMessage = new TechEmbedBuilder("How It Works").setText("Type your SpigotMC Username in this Chat to verify.\nTo verify your MC-Market purchases please contact a staff member.\n\nVerification is not working? Also feel free to contact a staff member in <#311178000026566658>.");
    lastInstructions = howItWorksMessage.send(channel);
  }

  @SubscribeEvent
  public void onMessage(GuildMessageReceivedEvent e) {
    if (e.getMember() == null) return;
    if (e.getAuthor().isBot()) return;
    if (!e.getChannel().equals(channel)) return;

    String username = e.getMessage().getContentDisplay();
    e.getMessage().delete().complete();

    TechEmbedBuilder errorMessage = new TechEmbedBuilder("Error (" + e.getAuthor().getName() + ")").error();

    if (!TechDiscordBot.getBot().getStatus().isUsable()) {
      errorMessage.setText("**The API is currently offline.**\nThere is no ETA of when it will be back up.\nYou will have to wait to verify until then.").error().sendTemporary(channel, 10);

      String msg = "User " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + "Tried to verify but the the api is down!";

      (new TechEmbedBuilder())
              .setText("OI the api is offline and someone tried verifying")
              .send(e.getJDA().getUserById("619084935655063552"));

      (new TechEmbedBuilder())
              .setText("OI the api is offline and someone tried verifying")
              .send(e.getJDA().getUserById("319429800009662468"));

      return;
    }

    if (verificationQueue.contains(e.getAuthor().getId())) {
      errorMessage.setText("Please follow the instruction above!").sendTemporary(channel, 15);
      return;
    }

    Verification existingVerification = TechDiscordBot.getStorage().retrieveVerificationWithDiscord(e.getAuthor().getId());
    if (existingVerification != null) {
      errorMessage.setText("You've already linked to your SpigotMC Account and your roles will be updated automatically!").sendTemporary(channel, 15);
      return;
    }

    if (username.contains(" ")) {
      errorMessage.setText("Please type in your SpigotMC Username!").sendTemporary(channel, 5);
      return;
    }

    Purchase[] purchases = TechDiscordBot.getSpigotAPI().getPurchases().username(username).toArray(new Purchase[0]);

    if (purchases.length == 0) {
      errorMessage.setText("User " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " does not own any of Tech's Plugins!\n\n*It may take up to 20 minutes for the bot to recognize new purchases.*\n\n*This could also be an issue with the api. If you believe this is a mistake, please contact a staff member!*");

      if (TechDiscordBot.getBot().getStatus() == APIStatus.NOT_FETCHING) {
        errorMessage.setText(errorMessage.getText() + "\n\n**The API is currently not fetching new information, this could also be the issue.");

        String msg = "User " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + "Tried to verify but the the api is down!";

                (new TechEmbedBuilder())
                .setText(msg)
                .send(e.getJDA().getUserById("619084935655063552"));

        (new TechEmbedBuilder())
                .setText(msg)
                .send(e.getJDA().getUserById("319429800009662468"));

        return;
      }
      errorMessage.error().sendTemporary(channel, 10);


      return;
    }

    username = purchases[0].getUser().getUsername();
    String userId = purchases[0].getUser().getUserId();
    String avatarUrl = purchases[0].getUser().getAvatar();

    existingVerification = TechDiscordBot.getStorage().retrieveVerificationWithSpigot(userId);

    if(existingVerification != null) {
      String finalUsername = username;

      Purchase purchase = TechDiscordBot.getSpigotAPI().getPurchases().userId(existingVerification.getUserId()).get(0);

      String msg = "User " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " Has tried to verify as https://www.spigotmc.org/members/" + finalUsername.toLowerCase() + "." + userId + " But this user is already verified!";
      errorMessage.setText("The SpigotMC User " + username + " is already linked with " + purchase.getUser().getUsername() + ". If you believe this is a mistake, please contact a Staff Member.").sendTemporary(channel, 10);

      (new TechEmbedBuilder())
              .setText(msg)
              .send(e.getJDA().getUserById("619084935655063552"));

      (new TechEmbedBuilder())
              .setText(msg)
              .send(e.getJDA().getUserById("319429800009662468"));

      return;
    }

    String code = UUID.randomUUID().toString().split("-")[0];

    TechEmbedBuilder instructions = new TechEmbedBuilder("Verify " + e.getAuthor().getName())
            .setThumbnail(avatarUrl)
            .setText("Now go to your SpigotMC Profile and post `TechVerification." +  code + "`\n\nLink to your Profile:\nhttps://www.spigotmc.org/members/" + username.toLowerCase() + "." + userId + "\n\n**Please verify yourself within 3 Minutes!**");

    Message m = e.getMessage().getChannel().sendMessage(instructions.build()).complete();
    verificationQueue.add(e.getAuthor().getId());
    String finalUsername = username;
    new Thread(() -> {
      long start = System.currentTimeMillis();

      while (System.currentTimeMillis() - start < TimeUnit.MINUTES.toMillis(3)) {
        for (ProfileComment all : SpigotMC.getComments(userId)) {

          if (all.getText().equals("TechVerification." + code)) {
            if (all.getUserId().equals(userId)) {
              m.delete().complete();

              String msg = "User " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " Has verified as https://www.spigotmc.org/members/" + finalUsername.toLowerCase() + "." + userId;

              (new TechEmbedBuilder(e.getAuthor().getName() + "'s Verification Completed")).success().setText(e.getAuthor().getName() + " has successfully verified their SpigotMC Account!").setThumbnail(avatarUrl).send(this.channel);


              (new TechEmbedBuilder())
                      .setText(msg)
                      .send(e.getJDA().getUserById("619084935655063552"));

              (new TechEmbedBuilder())
                      .setText(msg)
                      .send(e.getJDA().getUserById("319429800009662468"));

            }

            sendInstructions();

            this.verificationQueue.remove(e.getAuthor().getId());

            if (all.getUserId().equals(userId)) {

              TechDiscordBot.getStorage().createVerification(userId, e.getAuthor().getId());

              (new TechEmbedBuilder("Verification Complete!")).setText("You've been successfully verified!\n\nHere are your purchased plugins: " + Plugin.getMembersPluginsinEmojis(e.getMember()) + "\n\n*Your roles will be updated automatically from now on!*").setThumbnail(avatarUrl).send(e.getMember());

              try {
                throw new Exception(e.getAuthor().getAsTag() + " been successfully verified");
              } catch (Exception ae) {
                //Sentry.captureException(ae);
              }
            } else {
              String msg = "User " + e.getAuthor().getName() + "#" + e.getAuthor().getDiscriminator() + " Has tried to verify as https://www.spigotmc.org/members/" + finalUsername.toLowerCase() + "." + userId;

              ((Message)m.editMessage(errorMessage.setText("Please verify your own account.").build()).complete()).delete().queueAfter(10L, TimeUnit.SECONDS);

              (new TechEmbedBuilder())
                      .setText(msg)
                      .send(e.getJDA().getUserById("619084935655063552"));

              (new TechEmbedBuilder())
                      .setText(msg)
                      .send(e.getJDA().getUserById("319429800009662468"));

              try {
                throw new Exception(msg);
              } catch (Exception ae) {
                //Sentry.captureException(ae);
              }
            }
            (new TechEmbedBuilder()).setText("You may now delete the message on your profile! [Go to Comment](https://www.spigotmc.org/profile-posts/" + all.getCommentId() + ")").send(e.getMember());
            return;
          }
        }
      }
      verificationQueue.remove(e.getAuthor().getId());
      m.editMessage(errorMessage.setText("**You took too long!**\n\nThe Verification process has timed out! Please try again.").build())
              .complete()
              .delete()
              .queueAfter(10, TimeUnit.SECONDS);
    }).start();
  }

  @Override
  public String getName() {
    return "Verification";
  }

  @Override
  public Requirement[] getRequirements() {
    return new Requirement[] {
            new Requirement(VERIFICATION_CHANNEL, 1, "Missing Verification Channel (#verification)")
    };
  }
}