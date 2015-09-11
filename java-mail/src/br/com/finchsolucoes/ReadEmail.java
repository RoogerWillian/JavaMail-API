package br.com.finchsolucoes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ReadEmail {

	private static String HOST = "zimbra.finchsolucoes.com.br";
	private static boolean textIsHtml = false;

	public static List<String> extractContent(String username, String password) throws MessagingException {

		Properties props = System.getProperties();
		Store store = null;
		Folder inbox = null;
		Session session = Session.getDefaultInstance(props);
		List<String> contentEmails = new ArrayList<>();
		try {
			System.out.println("[ EXTRAINDO CONTEUDO DAS MENSAGENS DO EMAIL: (" + username + ") ]");
			store = session.getStore("pop3");
			store.connect(HOST, username, password);
			inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);

			Message[] messages = inbox.getMessages();

			for (int i = 0; i < messages.length; i++) {
				System.out.println("EXTRAINDO CONTEUDO ( " + (i + 1) + " ) - " + messages[i].getSubject());
				String html = getText(messages[i]);
				Document document = Jsoup.parse(html);
				String conteudo = document.body().text();
				contentEmails.add(conteudo);
			}
			extractData(contentEmails);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			inbox.close(true);
			store.close();
			System.out.println("[ FIM DA EXTRAÇÃO DE CONTEUDO DAS MENSAGENS (" + username + ") ]");
		}
		return contentEmails;
	}

	public static boolean isTextIsHtml() {
		return textIsHtml;
	}

	private static Set<String> extractData(List<String> dados) {
		Set<String> informacoesExtraidas = new HashSet<>();

		Pattern regexDossie = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{3}.\\d{10}\\/\\d{2}");
		if (dados != null) {
			for (String dado : dados) {
				Matcher matcher = regexDossie.matcher(dado);

				while (matcher.find()) {
					informacoesExtraidas.add(matcher.group());
				}
			}
			System.out.println("Dossies extraindos");
			for (String s : informacoesExtraidas) {
				System.out.println(s);
			}
		}

		return informacoesExtraidas;
	}

	private static String getText(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			textIsHtml = p.isMimeType("text/html");
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}
		return null;
	}
}