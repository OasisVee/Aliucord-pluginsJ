/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.lytefast.flexinput.model.Attachment
import java.util.Random

@AliucordPlugin
@Suppress("unused")
class MoreSlashCommands : Plugin() {
  override fun start(context: Context?) {
    commands.registerCommand("lenny", "Appends ( ͡° ͜ʖ ͡°) to your message.", listOf(CommandsAPI.messageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getStringOrDefault("message", "") + " ( ͡° ͜ʖ ͡°)")
    }

    commands.registerCommand("mock", "Mock a user", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx
            .getRequiredString("message")
            .toCharArray()
            .mapIndexed { i, c -> if (i % 2 == 1) c.uppercaseChar() else c.lowercaseChar() }
            .joinToString(""))
    }

    commands.registerCommand("upper", "Makes text uppercase", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getRequiredString("message").trim().uppercase())
    }

    commands.registerCommand("lower", "Makes text lowercase", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getRequiredString("message").trim().lowercase())
    }

    commands.registerCommand("owo", "Owoify's your text", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(owoify(ctx.getRequiredString("message").trim()))
    }

    val intensityOption = CommandsAPI.CommandOption(
        name = "intensity",
        description = "How intense the zalgo effect should be (1-5, default: 3)",
        required = false,
        type = Int::class.java
    )

    commands.registerCommand(
        "zalgo", 
        "Converts text to zalgo format with optional intensity (1-5)", 
        listOf(CommandsAPI.requiredMessageOption, intensityOption)
    ) { ctx ->
        val intensity = ctx.getIntOrDefault("intensity", 3).coerceIn(1, 5)
        CommandsAPI.CommandResult(zalgoify(ctx.getRequiredString("message").trim(), intensity))
    }

    val displayName = Attachment::class.java.getDeclaredField("displayName").apply { isAccessible = true }
    commands.registerCommand("spoilerfiles", "Marks attachments as spoilers", listOf(CommandsAPI.messageOption)) { ctx ->
      for (a in ctx.attachments) displayName[a] = "SPOILER_" + a.displayName
        CommandsAPI.CommandResult(ctx.getStringOrDefault("message", ""))
    }

    commands.registerCommand("reverse", "Makes text reversed", listOf(CommandsAPI.requiredMessageOption)) { ctx ->
        CommandsAPI.CommandResult(ctx.getRequiredString("message").reversed())
    }
  }

  override fun stop(context: Context?) = commands.unregisterAll()

  private fun owoify(text: String): String {
    return text.replace("l", "w").replace("L", "W")
      .replace("r", "w").replace("R", "W")
      .replace("o", "u").replace("O", "U")
  }

  private val random = Random()
  
  private fun zalgoify(text: String, intensity: Int = 3): String {
    val result = StringBuilder()
    
    // Calculate counts based on intensity (1-5)
    // Default (intensity=3) is 1.5x the original
    val maxAbove = when(intensity) {
      1 -> 4    // Mild
      2 -> 8    // Original
      3 -> 12   // 1.5x (default)
      4 -> 20   // Heavy
      5 -> 30   // Extreme
      else -> 12
    }
    
    val maxMiddle = when(intensity) {
      1 -> 1    // Mild
      2 -> 2    // Original
      3 -> 4    // 1.5x (default)
      4 -> 8    // Heavy
      5 -> 15   // Extreme
      else -> 4
    }
    
    val maxBelow = when(intensity) {
      1 -> 4    // Mild
      2 -> 8    // Original
      3 -> 12   // 1.5x (default)
      4 -> 20   // Heavy
      5 -> 30   // Extreme
      else -> 12
    }
    
    // Add more combining characters for greater intensity
    val combiningChars = when(intensity) {
      1, 2 -> COMBINING_CHARS_BASIC
      3 -> COMBINING_CHARS_ENHANCED
      4, 5 -> COMBINING_CHARS_EXTREME
      else -> COMBINING_CHARS_ENHANCED
    }
    
    for (char in text) {
      result.append(char)
      
      // Add random number of combining characters above
      val numAbove = random.nextInt(maxAbove) + 1
      for (i in 0 until numAbove) {
        result.append(combiningChars.above[random.nextInt(combiningChars.above.size)])
      }
      
      // Add random number of combining characters middle
      val numMiddle = random.nextInt(maxMiddle)
      for (i in 0 until numMiddle) {
        result.append(combiningChars.middle[random.nextInt(combiningChars.middle.size)])
      }
      
      // Add random number of combining characters below
      val numBelow = random.nextInt(maxBelow) + 1
      for (i in 0 until numBelow) {
        result.append(combiningChars.below[random.nextInt(combiningChars.below.size)])
      }
    }
    
    return result.toString()
  }
  
  // Data class to organize combining characters
  private data class CombiningChars(
    val above: Array<Char>,
    val middle: Array<Char>,
    val below: Array<Char>
  )
  
  // Basic combining characters (original)
  private val COMBINING_CHARS_BASIC = CombiningChars(
    above = arrayOf(
      '\u0300', '\u0301', '\u0302', '\u0303', '\u0304', '\u0305', '\u0306', '\u0307',
      '\u0308', '\u0309', '\u030A', '\u030B', '\u030C', '\u030D', '\u030E', '\u030F'
    ),
    middle = arrayOf(
      '\u0310', '\u0311', '\u0312', '\u0313', '\u0314', '\u0315', '\u0316', '\u0317',
      '\u0318', '\u0319', '\u031A', '\u031B', '\u031C', '\u031D', '\u031E', '\u031F'
    ),
    below = arrayOf(
      '\u0320', '\u0321', '\u0322', '\u0323', '\u0324', '\u0325', '\u0326', '\u0327',
      '\u0328', '\u0329', '\u032A', '\u032B', '\u032C', '\u032D', '\u032E', '\u032F'
    )
  )
  
  // Enhanced combining characters (more options)
  private val COMBINING_CHARS_ENHANCED = CombiningChars(
    above = arrayOf(
      '\u0300', '\u0301', '\u0302', '\u0303', '\u0304', '\u0305', '\u0306', '\u0307',
      '\u0308', '\u0309', '\u030A', '\u030B', '\u030C', '\u030D', '\u030E', '\u030F',
      '\u033E', '\u033F', '\u0340', '\u0341', '\u0342', '\u0343', '\u0344', '\u0345',
      '\u0360', '\u0361'
    ),
    middle = arrayOf(
      '\u0310', '\u0311', '\u0312', '\u0313', '\u0314', '\u0315', '\u0316', '\u0317',
      '\u0318', '\u0319', '\u031A', '\u031B', '\u031C', '\u031D', '\u031E', '\u031F',
      '\u0334', '\u0335', '\u0336', '\u0337', '\u0338'
    ),
    below = arrayOf(
      '\u0320', '\u0321', '\u0322', '\u0323', '\u0324', '\u0325', '\u0326', '\u0327',
      '\u0328', '\u0329', '\u032A', '\u032B', '\u032C', '\u032D', '\u032E', '\u032F',
      '\u0330', '\u0331', '\u0332', '\u0333'
    )
  )
  
  // Extreme combining characters (maximum chaos)
  private val COMBINING_CHARS_EXTREME = CombiningChars(
    above = arrayOf(
      '\u0300', '\u0301', '\u0302', '\u0303', '\u0304', '\u0305', '\u0306', '\u0307',
      '\u0308', '\u0309', '\u030A', '\u030B', '\u030C', '\u030D', '\u030E', '\u030F',
      '\u033E', '\u033F', '\u0340', '\u0341', '\u0342', '\u0343', '\u0344', '\u0345',
      '\u0360', '\u0361', '\u0346', '\u034A', '\u034B', '\u034C', '\u0350', '\u0351',
      '\u0352', '\u0357', '\u035B', '\u0363', '\u0364', '\u0365', '\u036C', '\u036D',
      '\u036E', '\u036F', '\u0483', '\u0484', '\u0485', '\u0486', '\u0487'
    ),
    middle = arrayOf(
      '\u0310', '\u0311', '\u0312', '\u0313', '\u0314', '\u0315', '\u0316', '\u0317',
      '\u0318', '\u0319', '\u031A', '\u031B', '\u031C', '\u031D', '\u031E', '\u031F',
      '\u0334', '\u0335', '\u0336', '\u0337', '\u0338', '\u0339', '\u033A', '\u033B',
      '\u033C', '\u033D', '\u0488', '\u0489'
    ),
    below = arrayOf(
      '\u0320', '\u0321', '\u0322', '\u0323', '\u0324', '\u0325', '\u0326', '\u0327',
      '\u0328', '\u0329', '\u032A', '\u032B', '\u032C', '\u032D', '\u032E', '\u032F',
      '\u0330', '\u0331', '\u0332', '\u0333', '\u0347', '\u0348', '\u0349', '\u034D',
      '\u034E', '\u0353', '\u0354', '\u0355', '\u0356', '\u0359', '\u035A', '\u035F'
    )
  )
}
