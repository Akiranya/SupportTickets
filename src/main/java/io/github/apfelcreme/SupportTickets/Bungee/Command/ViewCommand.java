package io.github.apfelcreme.SupportTickets.Bungee.Command;

import io.github.apfelcreme.SupportTickets.Bungee.SupportTickets;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Comment;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Ticket;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
public class ViewCommand extends SubCommand {

    public ViewCommand(SupportTickets plugin, String name, String usage, String permission, String... aliases) {
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

        UUID senderId = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : new UUID(0, 0);

        if (!ticket.getSender().equals(senderId) && !sender.hasPermission("SupportTickets.mod.server." + ticket.getLocation().getServer())) {
            plugin.sendMessage(sender, "error.notYourTicket");
            return;
        }

        plugin.sendMessage(sender, "info.view.header",
                "ticket", String.valueOf(ticket.getTicketId()),
                "date", SupportTickets.formatDate(ticket.getDate()),
                "sender", plugin.getNameByUUID(ticket.getSender()));

        plugin.sendMessage(sender, "info.view.comment",
                "ticket", String.valueOf(ticket.getTicketId()),
                "date", SupportTickets.formatDate(ticket.getDate()),
                "new", "",
                "sender", plugin.getNameByUUID(ticket.getSender()),
                "message", ticket.getMessage(),
                "number", "");

        int i = 1;
        for (Comment comment : ticket.getComments()) {
            plugin.sendMessage(sender, "info.view.comment",
                    "ticket", String.valueOf(ticket.getTicketId()),
                    "date", SupportTickets.formatDate(comment.getDate()),
                    "new", comment.getSenderHasNoticed() ? "" : plugin.getConfig().getText("info.view.new"),
                    "sender", plugin.getNameByUUID(comment.getSender()),
                    "message", comment.getComment(),
                    "number", String.valueOf(i));

            if (!comment.getSenderHasNoticed() && senderId.equals(ticket.getSender())) {
                plugin.getDatabaseController().setCommentRead(comment);
            }
            i++;
        }
        if (sender.hasPermission("SupportTickets.mod")) {
            plugin.sendMessage(sender, "info.view.actions","ticket", String.valueOf(ticket.getTicketId()));
        }
        plugin.addShownTicket(sender, ticket.getTicketId());
    }
}
