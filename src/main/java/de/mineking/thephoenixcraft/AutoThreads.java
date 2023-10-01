package de.mineking.thephoenixcraft;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class AutoThreads extends ListenerAdapter {

	// Ein regulärer Ausdruck zur Identifizierung von URLs in Nachrichten.
	public static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// Überprüfen, ob die Nachricht im richtigen Textkanal (Channel) empfangen wurde.
		if (event.getChannel().getIdLong() != Main.config.picvid) {
			return;
		}

		// Nachrichtenkanal (Channel) in einen Thread-Kanal (ThreadChannel) umwandeln.
		ThreadChannel channel = (ThreadChannel) event.getChannel(); // Cast to ThreadChannel

		// Überprüfen, ob die Nachricht weder Anhänge (Attachments) noch URLs enthält.
		if (event.getMessage().getAttachments().isEmpty() && !URL_PATTERN.asPredicate().test(event.getMessage().getContentRaw())) {
			// Löschen Sie die Nachricht, da sie keine Anhänge oder URLs enthält.
			event.getMessage().delete().queue();
			return;
		}

		// Einen Namen für den neuen Thread basierend auf der Nachricht erstellen.
		String threadName = getThreadName(event.getMessage());

		// Entfernen von Emoji-Codes aus dem Thread-Namen.
		threadName = threadName.replaceAll("<a:\\w+:(\\d+)>", ""); // Für animierte Emojis
		threadName = threadName.replaceAll("<:\\w+:(\\d+)>", ""); // Für reguläre Emojis

		// Den Thread-Namen auf eine maximale Länge begrenzen.
		threadName = StringUtils.abbreviate(threadName, ThreadChannel.MAX_NAME_LENGTH);

		// Einen neuen Thread-Kanal mit dem erstellten Namen erstellen und auf die Nachricht verweisen.
		channel.createThreadChannel(threadName, event.getMessageIdLong()).queue();
	}

	// Diese Methode erstellt einen geeigneten Namen für einen Thread basierend auf einer Nachricht.
	public String getThreadName(Message message) {
		// Den Inhalt der Nachricht erhalten und URLs daraus entfernen.
		var content = message.getContentRaw().replaceAll(URL_PATTERN.pattern(), "").trim();

		// Wenn der bereinigte Inhalt leer ist...
		if (content.isEmpty()) {
			// ...und die Nachricht eingebettete (embedded) Inhalte (Embeds) enthält...
			if (!message.getEmbeds().isEmpty()) {
				// ...verwenden Sie den Titel des ersten Embeds als Namen.
				var title = message.getEmbeds().get(0).getTitle();

				if (title != null) {
					return title;
				}
			}

			// Andernfalls verwenden Sie den Namen des Autors der Nachricht.
			return message.getAuthor().getGlobalName();
		}

		// Andernfalls verwenden Sie den bereinigten Inhalt als Namen.
		return content;
	}
}


