package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.builder.UsuarioBuilder;
import com.gladguys.polisscheduler.model.Usuario;
import com.gladguys.polisscheduler.services.firestore.FirestoreUsuariosService;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificacaoFCMService {

    private final FirestoreUsuariosService firestoreUsuariosService;

    public NotificacaoFCMService(FirestoreUsuariosService firestoreUsuariosService) {
        this.firestoreUsuariosService = firestoreUsuariosService;
    }

    public void enviarNotificacaoParaSeguidoresDePoliticos(String tipoNotificacao, Set<String> politicosIds) {
        Set<Usuario> usuariosPorPoliticosIds = getUsuariosPorPoliticosIds(politicosIds);
        if (usuariosPorPoliticosIds != null) {
            usuariosPorPoliticosIds.stream()
                    .filter(p -> firestoreUsuariosService.haPermissaoParaNotificacao())
                    .map(u -> u.getFcmToken()).distinct().forEach(userToken -> {
                if (userToken != null) {
                    enviarNotificacao(userToken, tipoNotificacao);
                }
            });
        }
    }

    public void enviarNotificacaoTotalDespesasAtualizado() {
        Set<Usuario> usuarios = getUsuarios();
        if (usuarios != null) {
            usuarios.stream().map(u -> u.getFcmToken()).distinct().forEach(userToken -> {
                if (userToken != null) {
                    enviarNotificacaoUpdateTotalDespesa(userToken);
                }
            });
        }
    }

    private void enviarNotificacao(String userToken, String tipoNotificacao) {

        String titulo = "Novas " + tipoNotificacao + " no Pólis!";
        String body = "Você tem novas informações de " + tipoNotificacao + " que você segue.";

        Message message = Message.builder()
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(3600 * 1000) // 1 hour in milliseconds
                        .setPriority(AndroidConfig.Priority.NORMAL)
                        .setNotification(AndroidNotification.builder()
                                .setTitle(titulo)
                                .setBody(body)
                                .setIcon("stock_ticker_update")
                                .setColor("#000000")
                                .build())
                        .build())
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .setToken(userToken)
                .build();

        String response = null;
        try {
            response = FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully sent message: " + response);
    }

    private void enviarNotificacaoUpdateTotalDespesa(String userToken) {

        String titulo = "Total de despesas atualizado!";
        String body = "Os totais de despesas para cada político foram atualizados o último mês.";

        Message message = Message.builder()
                .setAndroidConfig(AndroidConfig.builder()
                        .setTtl(3600 * 1000) // 1 hour in milliseconds
                        .setPriority(AndroidConfig.Priority.NORMAL)
                        .setNotification(AndroidNotification.builder()
                                .setTitle(titulo)
                                .setBody(body)
                                .setIcon("stock_ticker_update")
                                .setColor("#000000")
                                .build())
                        .build())
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .setToken(userToken)
                .build();

        String response = null;
        try {
            response = FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully sent message: " + response);
    }

    private Set<Usuario> getUsuariosPorPoliticosIds(Set<String> politicosIds) {
        Set<Usuario> usuarios = new HashSet<>();
        politicosIds.forEach(pId -> {
            List<QueryDocumentSnapshot> queryDocsSnapshot = firestoreUsuariosService.getUsuarioSeguidoresQueryDocSnapshot(pId);
            List<Usuario> usuariosSeguidoresDoPolitico =
                    queryDocsSnapshot
                            .stream()
                            .map(queryDocumentSnapshot ->
                                UsuarioBuilder.buildUsuarioDeQueryDocumentSnapshot(queryDocumentSnapshot))
                            .collect(Collectors.toList());

            usuarios.addAll(usuariosSeguidoresDoPolitico);
        });
        return usuarios;
    }

    private Set<Usuario> getUsuarios() {
        return firestoreUsuariosService.getTodosUsuarios();
    }
}
