package com.gladguys.polisscheduler.services;

import com.gladguys.polisscheduler.services.firestore.FirestoreReferenciaService;

import org.springframework.stereotype.Service;

@Service
public class ReferenciaService {

    private final FirestoreReferenciaService firestoreReferenciaService;

    public ReferenciaService(FirestoreReferenciaService firestoreReferenciaService) {
        this.firestoreReferenciaService = firestoreReferenciaService;
    }

    public ReferenciaService() {

    }
}