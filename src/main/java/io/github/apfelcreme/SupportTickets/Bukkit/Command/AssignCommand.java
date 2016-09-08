package io.github.apfelcreme.SupportTickets.Bukkit.Command;

import io.github.apfelcreme.SupportTickets.Bukkit.*;
import io.github.apfelcreme.SupportTickets.Bukkit.Bungee.BungeeMessenger;
import io.github.apfelcreme.SupportTickets.Bukkit.Ticket.Comment;
import io.github.apfelcreme.SupportTickets.Bukkit.Ticket.Ticket;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

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
public class AssignCommand implements SubCommand {

    /**
     * executes a subcommand
     *
     * @param sender the sender
     * @param args   the string arguments in an array
     */
    public void execute(CommandSender sender, final String[] args) {
        final Player player = (Player) sender;
        if (player.hasPermission("SupportTickets.assign")) {
            if (args.length > 1) {
                if (SupportTickets.isNumeric(args[1])) {
                    Ticket ticket = SupportTickets.getDatabaseController().loadTicket(Integer.parseInt(args[1]));
                    if (ticket != null) {
                        if (ticket.getTicketStatus() != Ticket.TicketStatus.CLOSED) {
                            String to = "";
                            if (args.length > 2) {
                                for (int i = 2; i < args.length; i++) {
                                    to += args[i] + " ";
                                }
                                to = to.trim();
                            } else {
                                to = player.getName();
                            }
                            SupportTickets.getDatabaseController().assignTicket(ticket, to);

                            Comment comment = new Comment(
                                    ticket.getTicketId(),
                                    player.getUniqueId(),
                                    SupportTicketsConfig.getText("info.assign.assignedComment").replace("{0}", to),
                                    new Date());

                            SupportTickets.getDatabaseController().saveComment(comment);
                            BungeeMessenger.sendTeamMessage(SupportTicketsConfig.getText("info.assign.assigned")
                                    .replace("{0}", ticket.getTicketId().toString())
                                    .replace("{1}", to));
                            BungeeMessenger.sendMessage(ticket.getSender(), SupportTicketsConfig.getText("info.assign.yourTicketGotAssigned")
                                    .replace("{0}", ticket.getTicketId().toString())
                                    .replace("{1}", to));
                        } else {
                            SupportTickets.sendMessage(player, SupportTicketsConfig.getText("error.ticketAlreadyClosed"));
                        }
                    } else {
                        SupportTickets.sendMessage(player, SupportTicketsConfig.getText("error.unknownTicket"));
                    }
                } else {
                    SupportTickets.sendMessage(player, SupportTicketsConfig.getText("error.wrongUsage")
                            .replace("{0}", "/pe assign <#> <Kommentar>"));
                }
            } else {
                SupportTickets.sendMessage(player, SupportTicketsConfig.getText("error.wrongUsage")
                        .replace("{0}", "/pe assign <#> <Kommentar>"));
            }
        } else {
            SupportTickets.sendMessage(sender, SupportTicketsConfig.getText("error.noPermission"));
        }
    }
}
