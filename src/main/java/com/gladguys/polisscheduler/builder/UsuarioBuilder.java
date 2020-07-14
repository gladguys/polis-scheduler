package com.gladguys.polisscheduler.builder;

import com.gladguys.polisscheduler.model.Usuario;
import com.google.cloud.firestore.QueryDocumentSnapshot;

public class UsuarioBuilder {

    public static Usuario buildUsuarioDeQueryDocumentSnapshot(QueryDocumentSnapshot queryDocumentSnapshot){
        Usuario usuario = new Usuario();
        usuario = queryDocumentSnapshot.toObject(Usuario.class);
        usuario.setId(queryDocumentSnapshot.getId());
        return usuario;
    }
}
