package me.andrew.welcomeHeads

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

class welcome(private val plugin: JavaPlugin) : Listener {

    // UUID -> pre-built lines
    private val cache = mutableMapOf<UUID, List<Component>>()

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val playerName = player.name

        event.joinMessage(null)

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val art = cache.getOrPut(uuid) { buildWelcomeMessage(uuid, playerName) }
            art.forEach { line ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    player.sendMessage(line)
                })
            }
        })
    }

    private fun buildWelcomeMessage(uuid: UUID, playerName: String): List<Component> {
        val textLinesRaw = plugin.config.getStringList("welcome-message")
        val headArt = getHeadArt(uuid, textLinesRaw.size)

        val textComponents = textLinesRaw.map { color(it.replace("{player}", playerName)) }

        val combinedLines = mutableListOf<Component>()
        for (i in textLinesRaw.indices) {
            val textPart = textComponents[i]
            val headPart = headArt[i]

            combinedLines.add(
                headPart
                    .append(Component.text(" "))
                    .append(textPart)
            )
        }

        return combinedLines
    }

    private fun getHeadArt(uuid: UUID, size: Int): List<Component> {
        if (size <= 0) return emptyList()
        return try {
            // Creates a size x size pixel art from player head
            val headUrl = "https://crafatar.com/avatars/$uuid?size=$size&overlay"
            val avatarImage = ImageIO.read(URL(headUrl))

            (0 until size).map { y ->
                Component.text().run {
                    for (x in 0 until size) {
                        val rgb = avatarImage.getRGB(x, y)
                        append(colorBlock(rgb))
                    }
                    build()
                }
            }
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load $size x $size head for $uuid: ${ex.message}")
            val placeholder = "&7" + "█".repeat(size)
            List(size) { color(placeholder) }
        }
    }

    private fun colorBlock(rgb: Int): Component {
        val c = Color(rgb, true)
        return Component.text("█", TextColor.color(c.red, c.green, c.blue))
    }

    private fun color(str: String): Component {
        return LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(str)
    }
}