package fr.flowsqy.dynamiccommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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

    private static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        final Server server = Bukkit.getServer();
        final Field commandMapField = server.getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(server);
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Command> getKnownCommands(CommandMap commandMap) throws IllegalAccessException, NoSuchFieldException {
        final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);
        return (HashMap<String, Command>) knownCommandsField.get(commandMap);
    }

    public static Set<String> registerCommands(Plugin ownerPlugin, PluginCommand... commands) {
        if (commands == null || commands.length < 1) {
            return Collections.emptySet();
        }
        try {
            final CommandMap commandMap = getCommandMap();
            final HashMap<String, Command> knownCommands = getKnownCommands(commandMap);
            final String[] originalRegisteredCommands = knownCommands.keySet().toArray(new String[0]);
            commandMap.registerAll(ownerPlugin.getName().toLowerCase(Locale.ROOT).trim(), Arrays.asList(commands));
            final HashSet<String> afterRegistrationCommands = new HashSet<>(knownCommands.keySet());
            for (String commandName : originalRegisteredCommands) {
                if (commandName == null)
                    continue;
                afterRegistrationCommands.remove(commandName);
            }
            return afterRegistrationCommands;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregisterCommands(String... commandsNames) {
        if (commandsNames == null || commandsNames.length < 1) {
            return;
        }
        try {
            final HashMap<String, Command> knownCommands = getKnownCommands(getCommandMap());
            for (String commandName : commandsNames) {
                knownCommands.remove(commandName);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void synchronizeTabCompleter() {
        try {
            final Server server = Bukkit.getServer();
            final Method syncCommandsMethod = server.getClass().getDeclaredMethod("syncCommands");
            syncCommandsMethod.setAccessible(true);
            syncCommandsMethod.invoke(server);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
