package io.github.apfelcreme.SupportTickets.Bungee.Command;

import io.github.apfelcreme.SupportTickets.Bungee.SupportTickets;
import io.github.apfelcreme.SupportTickets.Bungee.Ticket.Ticket;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
public class ListCommand extends SubCommand {

    public ListCommand(SupportTickets plugin, String name, String usage, String permission, String... aliases) {
        super(plugin, name, usage, permission, aliases);
    }

    /**
     * executes a subcommand
     *
     * @param sender the sender
     * @param args   the string arguments in an array
     */
    public void execute(CommandSender sender, final String[] args) {
        int page = 0;
        if (SupportTickets.isNumeric(args[args.length - 1])) {
            page = Integer.parseInt(args[args.length - 1]) - 1;
        }
        if (page < 0) {
            page = 0;
        }

        Ticket.TicketStatus messageStatus = Ticket.TicketStatus.OPEN;
        Ticket.TicketStatus[] statuses = new Ticket.TicketStatus[]{
                Ticket.TicketStatus.OPEN,
                Ticket.TicketStatus.ASSIGNED,
                Ticket.TicketStatus.REOPENED
        };
        if (args.length > 1 && !SupportTickets.isNumeric(args[1])) {
            try {
                messageStatus = Ticket.TicketStatus.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.sendMessage(sender, "error.wrongEnumArgument",
                        "input", args[1],
                        "available", Arrays.stream(Ticket.TicketStatus.values()).map(Enum::toString).collect(Collectors.joining(", "))
                );
                return;
            }
            statuses = new Ticket.TicketStatus[]{messageStatus};
        }

        //load the tickets
        List<Ticket> tickets;
        if (sender.hasPermission("SupportTickets.mod")) {
            tickets = plugin.getDatabaseController().getTickets(statuses);
        } else {
            UUID senderId = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : new UUID(0, 0);
            tickets = plugin.getDatabaseController().getPlayerTickets(senderId, statuses);
        }

        if (statuses.length == 1) {
            tickets.sort(Comparator.reverseOrder());
        }

        //display the results
        int pageSize = plugin.getConfig().getPageSize();
        int maxPages = (int) Math.ceil((float) tickets.size() / pageSize);
        if (maxPages > 0 && page >= maxPages - 1) {
            page = maxPages - 1;
        }

        plugin.sendMessage(sender, "info.list.header",
                "page", String.valueOf(page + 1),
                "maxpages", String.valueOf(maxPages),
                "status", messageStatus.toString());

        for (int i = page * pageSize; i < (page + 1) * pageSize && i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            plugin.sendMessage(sender, "info.list.element",
                    "id", String.valueOf(ticket.getTicketId()),
                    "online", plugin.isPlayerOnline(ticket.getSender())
                            ? plugin.getConfig().getText("info.list.online")
                            : plugin.getConfig().getText("info.list.offline"),
                    "sender", plugin.getNameByUUID(ticket.getSender()),
                    "assigned", ticket.getAssigned() != null ? ticket.getAssigned() : "*",
                    "message", ticket.getMessage(),
                    "comments", Integer.toString(ticket.getComments().size()));
            plugin.addShownTicket(sender, ticket.getTicketId());
        }

        if (tickets.size() > pageSize) {
            plugin.sendMessage(sender, "info.list.footer",
                    "previous", page > 0 ? plugin.getConfig().getText("info.list.previous") : "",
                    "next", (page + 1) * pageSize < tickets.size() ? plugin.getConfig().getText("info.list.next") : "",
                    "status", messageStatus.toString(),
                    "previouspage", String.valueOf(page), // page is already human-page -1
                    "nextpage", String.valueOf(page + 2)
            );
        }
    }

    @Override
    public boolean validateInput(String[] strings) {
        return strings.length > 0;
    }
}
