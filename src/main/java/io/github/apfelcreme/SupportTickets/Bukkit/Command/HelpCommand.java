package io.github.apfelcreme.SupportTickets.Bukkit.Command;

import io.github.apfelcreme.SupportTickets.Bukkit.SupportTickets;
import io.github.apfelcreme.SupportTickets.Bukkit.SupportTicketsConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Copyright (C) 2016 Lord36 aka Apfelcreme
 * <p>
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * @author Lord36 aka Apfelcreme
 */
public class HelpCommand implements SubCommand {

    /**
     * executes a subcommand
     *
     * @param sender the sender
     * @param args   the string arguments in an array
     */
    public void execute(CommandSender sender, String[] args) {
        ConfigurationSection configurationSection =
                SupportTicketsConfig.getLanguageConfig().getConfigurationSection("texts.info.help.commands");
        List<Object> values = null;
        List<String> strings = new ArrayList<String>();
        if (sender.hasPermission("SupportTickets.mod")) {
            values = new ArrayList<Object>(configurationSection.getConfigurationSection("mod").getValues(true).values());
            values.addAll(configurationSection.getConfigurationSection("user").getValues(true).values());
        } else {
            values = new ArrayList<Object>(configurationSection.getConfigurationSection("user").getValues(true).values());
        }
        for (Object o : values) {
            strings.add(o.toString());
        }
        Collections.sort(strings);
        SupportTickets.sendMessage(sender, SupportTicketsConfig.getText("info.help.header"));
        for (Object s : strings) {
            SupportTickets.sendMessage(sender, ChatColor.translateAlternateColorCodes('&', s.toString()));
        }
    }
}