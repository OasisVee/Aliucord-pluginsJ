/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption
import com.lytefast.flexinput.model.Attachment
import java.util.Random

@AliucordPlugin
@Suppress("unused")
class MoreSlashCommands : Plugin() {
    override fun start(context: Context?) {
        commands.registerCommand("lenny", "Appends ( ͡° ͜ʖ ͡°) to your message.") { ctx ->
            CommandResult((ctx.getStringOrDefault("message", "") + " ( ͡° ͜ʖ ͡°)"))
        }

        commands.registerCommand("mock", "Mock a user") { ctx ->
            CommandResult(
                ctx.getRequiredString("message")
                    .toCharArray()
                    .mapIndexed { i, c -> if (i % 2 == 1) c.uppercaseChar() else c.lowercaseChar() }
                    .joinToString("")
            )
        }

        commands.registerCommand("upper", "Makes text uppercase") { ctx ->
            CommandResult(ctx.getRequiredString("message").trim().uppercase())
        }

        commands.registerCommand("lower", "Makes text lowercase") { ctx ->
            CommandResult(ctx.getRequiredString("message").trim().lowercase())
        }

        commands.registerCommand("owo", "Owoify's your text") { ctx ->
            CommandResult(owoify(ctx.getRequiredString("message").trim()))
        }

        val zalgoArgs = listOf(
            ApplicationCommandOption(
                ApplicationCommandType.STRING,
                "message",
                "The message to convert to zalgo format",
                null,
                true,
                false,
                null,
                null,
                null,
                false,
                null,
                null
            ),
            ApplicationCommandOption(
                ApplicationCommandType.INTEGER,
                "intensity",
                "The intensity of the zalgo effect (default is 1, max is 5. if over, its set to 1)",
                null,
                false,
                false,
                null,
                null,
                null,
                false,
                null,
                null
            )
        )

        commands.registerCommand("zalgo", "Converts text to zalgo format", zalgoArgs) { ctx ->
            val message = ctx.getRequiredString("message").trim()
            var intensity = ctx.getIntOrDefault("intensity", 1) // Default intensity is 1
            if (intensity > 5) intensity = 1 // Cap intensity at 5
            CommandResult(zalgoify(message, intensity))
        }

        try {
            val displayName = Attachment::class.java.getDeclaredField("displayName").apply { isAccessible = true }
            commands.registerCommand("spoilerfiles", "Marks attachments as spoilers") { ctx ->
                for (a in ctx.attachments) displayName[a] = "SPOILER_" + a.displayName
                CommandResult(ctx.getStringOrDefault("message", ""))
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        commands.registerCommand("reverse", "Makes text reversed") { ctx ->
            CommandResult(ctx.getRequiredString("message").reversed())
        }
    }

    override fun stop(context: Context?) = commands.unregisterAll()

    private fun owoify(text: String): String {
        return text.replace("l", "w", true).replace("r", "w", true)
            .replace("o", "u", true)
    }

    private val random = Random()

    private fun zalgoify(text: String, intensity: Int): String {
        val result = StringBuilder()
        val maxAbove = 8 * intensity
        val maxMiddle = 3 * intensity
        val maxBelow = 8 * intensity

        for (char in text) {
            result.append(char)

            // Add random number of combining characters above
            val numAbove = random.nextInt(maxAbove) + 1
            for (i in 0 until numAbove) {
                result.append(COMBINING_CHARS_ABOVE[random.nextInt(COMBINING_CHARS_ABOVE.size)])
            }

            // Add random number of combining characters middle
            val numMiddle = random.nextInt(maxMiddle)
            for (i in 0 until numMiddle) {
                result.append(COMBINING_CHARS_MIDDLE[random.nextInt(COMBINING_CHARS_MIDDLE.size)])
            }

            // Add random number of combining characters below
            val numBelow = random.nextInt(maxBelow) + 1
            for (i in 0 until numBelow) {
                result.append(COMBINING_CHARS_BELOW[random.nextInt(COMBINING_CHARS_BELOW.size)])
            }
        }

        return result.toString()
    }

    // Unicode combining characters for zalgo text
    private val COMBINING_CHARS_ABOVE = charArrayOf(
        '\u0300', '\u0301', '\u0302', '\u0303', '\u0304', '\u0305', '\u0306', '\u0307',
        '\u0308', '\u0309', '\u030A', '\u030B', '\u030C', '\u030D', '\u030E', '\u030F'
    )

    private val COMBINING_CHARS_MIDDLE = charArrayOf(
        '\u0310', '\u0311', '\u0312', '\u0313', '\u0314', '\u0315', '\u0316', '\u0317',
        '\u0318', '\u0319', '\u031A', '\u031B', '\u031C', '\u031D', '\u031E', '\u031F'
    )

    private val COMBINING_CHARS_BELOW = charArrayOf(
        '\u0320', '\u0321', '\u0322', '\u0323', '\u0324', '\u0325', '\u0326', '\u0327',
        '\u0328', '\u0329', '\u032A', '\u032B', '\u032C', '\u032D', '\u032E', '\u032F'
    )
}
