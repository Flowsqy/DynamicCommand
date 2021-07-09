package fr.flowsqy.dynamiccommand;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DynamicCommand {

    public static PluginCommand[] createCommands(Plugin ownerPlugin, String... commandsNames) {
        if (commandsNames == null || commandsNames.length < 1) {
            return new PluginCommand[0];
        }
        try {
            final Constructor<PluginCommand> commandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            commandConstructor.setAccessible(true);
            final PluginCommand[] commands = new PluginCommand[commandsNames.length];
            for (int index = 0; index < commands.length; index++) {
                commands[index] = commandConstructor.newInstance(commandsNames[index], ownerPlugin);
            }
            return commands;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
