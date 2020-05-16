# polis-scheduler

> #### Salvando proposições

endpoint: http://localhost:8080/proposicoes?data=AAAA-MM-DD
- se o parâmetro data não for informado, data recebe valor da data do dia anterior
- busca todas as  proposições apresentadas para a data na **API Dados Abertos**
- salva essas proposições no **Firestore**
- salva essas proposições na **base do scheduler**