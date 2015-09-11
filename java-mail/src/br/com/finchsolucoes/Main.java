package br.com.finchsolucoes;

import javax.mail.MessagingException;

public class Main {

	public static void main(String[] args) {
		try {
			ReadEmail.extractContent("plataformacadastro@finchsolucoes.com.br", "Pl4t4forma#2015");
			
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
