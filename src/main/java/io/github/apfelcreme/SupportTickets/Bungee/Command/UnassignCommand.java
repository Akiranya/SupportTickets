package io.github.apfelcreme.SupportTickets.Bungee.Command;

import io.github.apfelcreme.SupportTickets.Bungee.Message.BukkitMessenger;
import io.github.apfelcreme.SupportTickets.Bungee.SupportTickets;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Comment;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Ticket;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
public class UnassignCommand extends SubCommand {

    public UnassignCommand(SupportTickets plugin, String name, String usage, String permission, String... aliases) {
        super(plugin, name, usage, permission, aliases);
    }

    /**
     * executes a subcommand
     *
     * @param sender the sender
     * @param args   the string arguments in an array
     */
    public void execute(CommandSender sender, String[] args) {
        Ticket ticket = plugin.getDatabaseController().loadTicket(Integer.parseInt(args[1]));
        if (ticket == null || !sender.hasPermission("SupportTickets.mod.server." + ticket.getLocation().getServer())) {
            plugin.sendMessage(sender, "error.unknownTicket");
            return;
        }

        if (ticket.getTicketStatus() == Ticket.TicketStatus.CLOSED) {
            plugin.sendMessage(sender, "error.ticketAlreadyClosed");
            return;
        }

        UUID senderId = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : new UUID(0, 0);

        BukkitMessenger.fetchPosition(sender, (location) -> {
            plugin.getDatabaseController().unassignTicket(ticket);

            Comment comment = new Comment(
                    ticket.getTicketId(),
                    senderId,
                    plugin.getConfig().getText("info.unassign.unassignedComment"),
                    ticket.getSender().equals(senderId),
                    new Date(),
                    location
            );

            plugin.getDatabaseController().saveComment(comment);

            plugin.sendTeamMessage("info.unassign.unassigned",
                    "ticket", String.valueOf(ticket.getTicketId()), "sender", sender.getName());
            plugin.sendMessage(ticket.getSender(), "info.unassign.yourTicketGotUnassigned",
                    "ticket", String.valueOf(ticket.getTicketId()), "sender", sender.getName());
            plugin.addShownTicket(sender, ticket.getTicketId());
        });
    }
}
