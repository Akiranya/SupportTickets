package io.github.apfelcreme.SupportTickets.Bungee.Command;

import io.github.apfelcreme.SupportTickets.Bungee.Event.TicketCloseEvent;
import io.github.apfelcreme.SupportTickets.Bungee.Message.BukkitMessenger;
import io.github.apfelcreme.SupportTickets.Bungee.SupportTickets;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Comment;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Ticket;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

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
public class CloseCommand extends SubCommand {

    public CloseCommand(SupportTickets plugin, String name, String usage, String permission, String... aliases) {
        super(plugin, name, usage, permission, aliases);
    }

    /**
     * executes a subcommand
     *
     * @param sender the sender
     * @param args   the string arguments in an array
     */
    public void execute(CommandSender sender, final String[] args) {

        Ticket ticket = plugin.getDatabaseController().loadTicket(Integer.parseInt(args[1]));
        if (ticket == null) {
            plugin.sendMessage(sender, "error.unknownTicket");
            return;
        }

        if (ticket.getTicketStatus() == Ticket.TicketStatus.CLOSED) {
            plugin.sendMessage(sender, "error.ticketAlreadyClosed");
            return;
        }

        UUID senderId = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : new UUID(0, 0);

        if (!ticket.getSender().equals(senderId) && !sender.hasPermission("SupportTickets.mod.server." + ticket.getLocation().getServer())) {
            if (sender.hasPermission("SupportTickets.mod")) {
                plugin.sendMessage(sender, "error.noPermissionOnServer", "server", ticket.getLocation().getServer());
            } else {
                plugin.sendMessage(sender, "error.notYourTicket");
            }
            return;
        }

        BukkitMessenger.fetchPosition(sender, (location) -> {
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
            Comment comment = new Comment(
                    ticket.getTicketId(),
                    senderId,
                    SupportTickets.replace(plugin.getConfig().getText("info.close.closeComment"),
                            "sender", sender.getName(), "reason", reason),
                    ticket.getSender().equals(senderId),
                    new Date(),
                    location
            );

            ticket.getComments().add(comment);
            plugin.getDatabaseController().saveComment(comment);

            plugin.getDatabaseController().closeTicket(ticket, senderId, reason);

            TicketCloseEvent closeEvent = new TicketCloseEvent(ticket, sender, reason);
            plugin.getProxy().getPluginManager().callEvent(closeEvent);

            plugin.sendMessage(ticket.getSender(), "info.close.yourTicketGotClosed",
                    "ticket", String.valueOf(ticket.getTicketId()), "sender", sender.getName(), "reason", reason);

            plugin.sendTeamMessage("info.close.closed",
                    "ticket", String.valueOf(ticket.getTicketId()), "sender", sender.getName(), "reason", reason);

            plugin.addShownTicket(sender, ticket.getTicketId());
        });
    }
}
