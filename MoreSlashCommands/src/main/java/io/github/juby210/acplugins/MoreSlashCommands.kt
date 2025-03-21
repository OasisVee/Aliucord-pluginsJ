/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins;

import android.content.Context;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.lytefast.flexinput.model.Attachment;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

@AliucordPlugin
@SuppressWarnings("unused")
class MoreSlashCommands : Plugin() {
  @Override
  public void start(Context context) {
    commands.registerCommand("lenny", "Appends ( ͡° ͜ʖ ͡°) to your message.", List.of(CommandsAPI.messageOption), ctx ->
        new CommandsAPI.CommandResult(ctx.getStringOrDefault("message", "") + " ( ͡° ͜ʖ ͡°)")
    );

    commands.registerCommand("mock", "Mock a user", List.of(CommandsAPI.requiredMessageOption), ctx ->
        new CommandsAPI.CommandResult(String.valueOf(ctx
            .getRequiredString("message")
            .toCharArray())
            .chars()
            .mapToObj(i -> (char) i)
            .mapIndexed((i, c) -> i % 2 == 1 ? Character.toUpperCase(c) : Character.toLowerCase(c))
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString())
    );

    commands.registerCommand("upper", "Makes text uppercase", List.of(CommandsAPI.requiredMessageOption), ctx ->
        new CommandsAPI.CommandResult(ctx.getRequiredString("message").trim().toUpperCase())
    );

    commands.registerCommand("lower", "Makes text lowercase", List.of(CommandsAPI.requiredMessageOption), ctx ->
        new CommandsAPI.CommandResult(ctx.getRequiredString("message").trim().toLowerCase())
    );

    commands.registerCommand("owo", "Owoify's your text", List.of(CommandsAPI.requiredMessageOption), ctx ->
        new CommandsAPI.CommandResult(owoify(ctx.getRequiredString("message").trim()))
    );

    List<CommandsAPI.Argument> zalgoArgs = new ArrayList<>(List.of(CommandsAPI.requiredMessageOption));
    zalgoArgs.add(new CommandsAPI.Argument("intensity", CommandsAPI.ArgumentType.INT, false));
    commands.registerCommand("zalgo", "Converts text to zalgo format", zalgoArgs, ctx -> {
        String message = ctx.getRequiredString("message").trim();
        int intensity = ctx.getIntOrDefault("intensity", 5); // Default intensity is 5
        if (intensity < 1) intensity = 1; // Ensure intensity is at least 1
        if (intensity > 10) intensity = 10; // Limit maximum intensity to 10 (adjust as needed)
        return new CommandsAPI.CommandResult(zalgoify(message, intensity));
    });

    try {
      java.lang.reflect.Field displayName = Attachment.class.java.getDeclaredField("displayName");
      displayName.setAccessible(true);
      commands.registerCommand("spoilerfiles", "Marks attachments as spoilers", List.of(CommandsAPI.messageOption), ctx -> {
        for (Attachment a : ctx.getAttachments()) {
          displayName.set(a, "SPOILER_" + a.getDisplayName());
        }
        return new CommandsAPI.CommandResult(ctx.getStringOrDefault("message", ""));
      });
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Handle the exception appropriately, maybe log it
      e.printStackTrace();
    }


    commands.registerCommand("reverse", "Makes text reversed", List.of(CommandsAPI.requiredMessageOption), ctx ->
        new CommandsAPI.CommandResult(new StringBuilder(ctx.getRequiredString("message")).reverse().toString())
    );
  }

  @Override
  public void stop(Context context) {
    commands.unregisterAll();
  }

  private String owoify(String text) {
    return text.replace("l", "w").replace("L", "W")
        .replace("r", "w").replace("R", "W")
        .replace("o", "u").replace("O", "U");
  }

  private final Random random = new Random();

  private String zalgoify(String text, int intensity) {
    StringBuilder result = new StringBuilder();
    int maxAbove = 8 * intensity;
    int maxMiddle = 3 * intensity;
    int maxBelow = 8 * intensity;

    for (char charCode : text.toCharArray()) {
      result.append(charCode);

      // Add random number of combining characters above
      int numAbove = random.nextInt(maxAbove) + 1;
      for (int i = 0; i < numAbove; i++) {
        result.append(COMBINING_CHARS_ABOVE[random.nextInt(COMBINING_CHARS_ABOVE.length)]);
      }

      // Add random number of combining characters middle
      int numMiddle = random.nextInt(maxMiddle);
      for (int i = 0; i < numMiddle; i++) {
        result.append(COMBINING_CHARS_MIDDLE[random.nextInt(COMBINING_CHARS_MIDDLE.length)]);
      }

      // Add random number of combining characters below
      int numBelow = random.nextInt(maxBelow) + 1;
      for (int i = 0; i < numBelow; i++) {
        result.append(COMBINING_CHARS_BELOW[random.nextInt(COMBINING_CHARS_BELOW.length)]);
      }
    }

    return result.toString();
  }

  // Unicode combining characters for zalgo text
  private static final charCOMBINING_CHARS_ABOVE = new char{
      '\u0300', '\u0301', '\u0302', '\u0303', '\u0304', '\u0305', '\u0306', '\u0307',
      '\u0308', '\u0309', '\u030A', '\u030B', '\u030C', '\u030D', '\u030E', '\u030F'
  };

  private static final charCOMBINING_CHARS_MIDDLE = new char{
      '\u0310', '\u0311', '\u0312', '\u0313', '\u0314', '\u0315', '\u0316', '\u0317',
      '\u0318', '\u0319', '\u031A', '\u031B', '\u031C', '\u031D', '\u031E', '\u031F'
  };

  private static final charCOMBINING_CHARS_BELOW = new char{
      '\u0320', '\u0321', '\u0322', '\u0323', '\u0324', '\u0325', '\u0326', '\u0327',
      '\u0328', '\u0329', '\u032A', '\u032B', '\u032C', '\u032D', '\u032E', '\u032F'
  };
}
