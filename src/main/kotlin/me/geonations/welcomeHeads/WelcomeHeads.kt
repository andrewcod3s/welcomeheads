package me.andrew.welcomeHeads


import org.bukkit.plugin.java.JavaPlugin

class WelcomeHeads : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        
        server.pluginManager.registerEvents(welcome(this), this)
        
        logger.info("WelcomeHeads has been enabled!")
    }

    override fun onDisable() {
        logger.info("WelcomeHeads has been disabled!")
    }
}
