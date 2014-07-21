package net.minecraftforge.cauldron.configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.cauldron.CauldronHooks;
import net.minecraftforge.cauldron.TileEntityCache;
import net.minecraftforge.cauldron.command.TileEntityCommand;
import net.minecraftforge.common.DimensionManager;

import org.apache.commons.lang.BooleanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Throwables;

public class TileEntityConfig extends ConfigBase
{
    private final String HEADER = "This is the main configuration file for TileEntities.\n"
            + "\n"
            + "If you need help with the configuration or have any questions related to Cauldron,\n"
            + "join us at the IRC or drop by our forums and leave a post.\n"
            + "\n"
            + "IRC: #cauldron @ irc.esper.net ( http://webchat.esper.net/?channel=cauldron )\n"
            + "Forums: http://cauldron.minecraftforge.net/\n";
    
    /* ======================================================================== */
    public final BoolSetting skipTileEntityTicks = new BoolSetting(this, "settings.skip-tileentity-ticks", true, "If enabled, turns on tileentity tick skip feature when no players are near.");
    public final BoolSetting enableTECanUpdateWarning = new BoolSetting(this, "debug.enable-te-can-update-warning", false, "Set true to detect which tileentities should not be ticking.");
    public final BoolSetting enableTEInventoryWarning = new BoolSetting(this, "debug.enable-te-inventory-warning", false, "Set true to detect which tileentities with inventory failed to detect size for Bukkit's InventoryType enum. Note: This may detect a false-positive.");
    public final BoolSetting tileEntityPlaceLogging = new BoolSetting(this, "debug.warn-place-no-tileentity", false, "Warn when a mod requests tile entity from a block that doesn't support one");
    /* ======================================================================== */

    public TileEntityConfig(String fileName, String commandName)
    {
        super(fileName, commandName);
        init();
    }

    public void addCommands()
    {
        commands.put(this.commandName, new TileEntityCommand());
    }

    public void init()
    {
        settings.put(skipTileEntityTicks.path, skipTileEntityTicks);
        settings.put(enableTECanUpdateWarning.path, enableTECanUpdateWarning);
        settings.put(enableTEInventoryWarning.path, enableTEInventoryWarning);
        settings.put(tileEntityPlaceLogging.path, tileEntityPlaceLogging);
        load();
    }

    public void load()
    {
        try
        {
            config = YamlConfiguration.loadConfiguration(configFile);
            String header = HEADER + "\n";
            for (Setting toggle : settings.values())
            {
                if (!toggle.description.equals(""))
                    header += "Setting: " + toggle.path + " Default: " + toggle.def + "   # " + toggle.description + "\n";

                config.addDefault(toggle.path, toggle.def);
                settings.get(toggle.path).setValue(config.getString(toggle.path));
            }
            config.options().header(header);
            config.options().copyDefaults(true);

            version = getInt("config-version", 1);
            set("config-version", 1);

            for (TileEntityCache teCache : CauldronHooks.tileEntityCache.values())
            {
                teCache.tickNoPlayers = config.getBoolean( "world-settings." + teCache.worldName + "." + teCache.configPath + ".tick-no-players", config.getBoolean( "world-settings.default." + teCache.configPath + ".tick-no-players") );
                teCache.tickInterval = config.getInt( "world-settings." + teCache.worldName + "." + teCache.configPath + ".tick-interval", config.getInt( "world-settings.default." + teCache.configPath + ".tick-interval") );
            }
            this.saveWorldConfigs();
            this.save();
        }
        catch (Exception ex)
        {
            MinecraftServer.getServer().logSevere("Could not load " + this.configFile);
            ex.printStackTrace();
        }
    }
}