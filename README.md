# polis-scheduler

> #### Salvando proposições

endpoint: http://localhost:8080/proposicoes?data=AAAA-MM-DD
- se o parâmetro data não for informado, data recebe valor da data do dia anterior;
- busca todas as  proposições apresentadas para a data na **API Dados Abertos**;
- salva essas proposições no **Firestore**;
- salva essas proposições na **base do scheduler**;
- busca tramitações para essas proposições salvas;
- salva as tramitações dessas proposições salvas;
- atualiza o status tramite de cada proposição para o status do tramite mais recente dessa proposição;
- envia notificação para cada usuário que tenha proposição nova de algum político que ele siga;

> #### Salvando despesas

> #### Atualizando totalizador de despesas dos políticos

> ####  