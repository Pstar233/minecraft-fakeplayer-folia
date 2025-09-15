package io.github.hello09x.fakeplayer.core.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.CommandResult;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * @author tanyaofei
 * @since 2024/7/30
 **/
public class FakePlayerCommandArgument extends Argument<CommandResult> implements GreedyArgument {

    /**
     * Constructs a {@link CommandArgument} with the given node name.
     *
     * @param nodeName the name of the node for this argument
     */
    public FakePlayerCommandArgument(String nodeName) {
        super(nodeName, StringArgumentType.greedyString());
        applySuggestions();
    }

    private void applySuggestions() {
        super.replaceSuggestions((info, builder) -> {
            var sender = this.getTarget(info);
            if (sender == null) {
                return Suggestions.empty();
            }

            var commandMap = CommandAPIBukkit.get().getSimpleCommandMap();
            var command = info.currentArg();
            var context = new StringReader(command);

            if (!command.contains(" ")) {
                // Suggest command name
                ArgumentSuggestions<CommandSender> replacement = replacements.getNextSuggestion(sender);
                if (replacement != null) {
                    return replacement.suggest(new SuggestionInfo<>(
                            sender,
                            new CommandArguments(new Object[0], new LinkedHashMap<>(), new String[0], new LinkedHashMap<>(), info.currentInput()),
                            command,
                            command
                    ), builder);
                }

                var results = commandMap.tabComplete(sender, command);
                if (results == null) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
                }

                if (sender instanceof Player) {
                    for (String result : results) {
                        // Player tabComplete adds "/" prefix, strip it
                        builder.suggest(result.startsWith("/") ? result.substring(1) : result);
                    }
                } else {
                    for (String result : results) {
                        builder.suggest(result);
                    }
                }

                return builder.buildFuture();
            }

            // Extract command label
            var commandLabel = command.substring(0, command.indexOf(" "));
            var target = commandMap.getCommand(commandLabel);
            if (target == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
            }

            // Split args
            var arguments = command.split(" ");
            if (!arguments[0].isEmpty() && command.endsWith(" ")) {
                arguments = Arrays.copyOf(arguments, arguments.length + 1);
                arguments[arguments.length - 1] = "";
            }

            builder = builder.createOffset(builder.getStart() + command.lastIndexOf(" ") + 1);

            int lastIndex = arguments.length - 1;
            var previousArguments = Arrays.copyOf(arguments, lastIndex);

            ArgumentSuggestions<CommandSender> replacement = replacements.getNextSuggestion(sender, previousArguments);
            if (replacement != null) {
                return replacement.suggest(new SuggestionInfo<>(
                        sender,
                        new CommandArguments(previousArguments, new LinkedHashMap<>(), previousArguments, new LinkedHashMap<>(), info.currentInput()),
                        command,
                        arguments[lastIndex]
                ), builder);
            }

            // Remove command name for normal tabComplete
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);

            //移除了 getTargetBlockExact
            //改成传 null，不影响大多数 tab 补全
            Location location = null;

            for (var tabCompletion : target.tabComplete(sender, commandLabel, arguments, location)) {
                builder.suggest(tabCompletion);
            }

            return builder.buildFuture();
        });
    }


    SuggestionsBranch<CommandSender> replacements = SuggestionsBranch.suggest();

    /**
     * Replaces the default command suggestions provided by the server with custom
     * suggestions for each argument in the command, starting with the command's
     * name. If a suggestion is null or there isn't any suggestions given for that
     * argument, the suggestions will not be overridden.
     *
     * @param suggestions An array of {@link ArgumentSuggestions} representing the
     *                    suggestions. Use the static methods in ArgumentSuggestions
     *                    to create these.
     * @return the current argument
     */
    @SafeVarargs
    public final FakePlayerCommandArgument replaceSuggestions(ArgumentSuggestions<CommandSender>... suggestions) {
        replacements = SuggestionsBranch.suggest(suggestions);
        return this;
    }

    /**
     * Replaces the default command suggestions provided by the server with custom
     * suggestions for each argument in the command, starting with the command's
     * name. If a suggestion is null or there isn't any suggestions given for that
     * argument, the suggestions will not be overridden.
     *
     * @param suggestions An array of {@link ArgumentSuggestions} representing the
     *                    suggestions. Use the static methods in ArgumentSuggestions
     *                    to create these.
     * @return the current argument
     */
    @Override
    public FakePlayerCommandArgument replaceSuggestions(ArgumentSuggestions<CommandSender> suggestions) {
        replacements = SuggestionsBranch.suggest(suggestions);
        return this;
    }

    /**
     * Adds {@link SuggestionsBranch} to this CommandArgument. After going through
     * the suggestions provided by
     * {@link CommandArgument#replaceSuggestions(ArgumentSuggestions...)} the
     * suggestions of these branches will be used.
     *
     * @param branches An array of {@link SuggestionsBranch} representing the
     *                 branching suggestions. Use
     *                 {@link SuggestionsBranch#suggest(ArgumentSuggestions...)} to
     *                 start creating these.
     * @return the current argument
     */
    @SafeVarargs
    public final Argument<CommandResult> branchSuggestions(SuggestionsBranch<CommandSender>... branches) {
        replacements.branch(branches);
        return this;
    }

    @Override
    public Class<CommandResult> getPrimitiveType() {
        return CommandResult.class;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.COMMAND;
    }

    @Override
    public <CommandSourceStack> CommandResult parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) throws CommandSyntaxException {
        var command = cmdCtx.getArgument(key, String.class);
        var commandMap = CommandAPIBukkit.get().getSimpleCommandMap();
        var context = new StringReader(command);

        var sender = CommandAPIBukkit.<CommandSourceStack>get().getSenderForCommand(cmdCtx, false).getSource();
        var target = (Player) previousArgs.get("name");
        if (target == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(context);
        }

        var manager = Main.getInjector().getInstance(FakeplayerManager.class);
        if (sender.isOp()) {
            if (manager.isNotFake(target)) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
            }
        } else {
            if (manager.get(sender, target.getName()) == null) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
            }
        }

        var arguments = command.split(" ");
        var commandLabel = arguments[0];
        var cmd = commandMap.getCommand(commandLabel);
        if (cmd == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(context);
        }

        replacements.enforceReplacements(target, arguments);

        return new CommandResult(cmd, Arrays.copyOfRange(arguments, 1, arguments.length));
    }

    private @Nullable Player getTarget(@NotNull SuggestionInfo<CommandSender> info) {
        var args = info.previousArgs().fullInput().split(" ");
        if (args.length < 3) {
            return null;
        }
        var name = args[2];
        var manager = Main.getInjector().getInstance(FakeplayerManager.class);
        if (info.sender().isOp()) {
            return manager.get(name);
        }
        return manager.get(info.sender(), name);
    }

}
