# polis-scheduler

> #### FLuxmograma de dados do Polis

![Fluxograma Polis](https://github.com/gladguys/polis-scheduler/blob/master/fluxograma_polis.png)

> #### Salvando proposições

1. Google Cloud Scheduler irá ser disparado em determinada horas do dia e irá fazer requisições paginadas ```GET /proposicoes?data=AAAA-MM-DD``` para o Polis-Scheduler;
2. Polis-Scheduler irá fazer uma requisição para API dados abertos ```GET /proposicoes?dataApresentacaoInicio=AAAA-MM-DD&dataApresentacaoInicio=AAAA-MM-DD``` e com isso terá uma lista com todas as informações básicas para as **proposições apresentadas** no dia AAAA-MM-DD.
3. Essas informações básicas das proposições não é o suficiente pra montar as Proposições do dia. Então, para cada info proposição básica é feito os passos abaixo:
    1. dentro da proposição básica há o campo com a uri da API dados abertos que retorna todos os dados da proposição completa. Então é feito uma requisição ```GET /proposicoes/{id}``` para esse endpoint
    2. se essa proposição **NÃO É** uma *emenda*, *projeto de lei*, *indicação* ou *requerimento*, ela é ignorada e não será usada no Polis.
    3. requisita tramitações da proposição em ```GET proposicoes/{id}/tramitacoes```
    4. requisita na API dados abertos ```GET proposicoes/{id}/autores```  os deputados autores daquela proposição.
    5. Para cada autor da proposição é feito:
        1. monta a proposição com todos os dados até aqui coletados: tramitações e dados do autor.
        2. salva a proposição no **firestore**
        3. salva a proposição na **base do scheduler (postgres)**
4. É enviado uma **Notificação** para todos os usuários que sigam um dos políticos que tiveram proposições apresentadas.

*obs: no caso de uma reexecução em uma data passada, o processo acontecerá normalmente: será feito update no firestore para os novos resultados daquela data, e as informações na base do scheduler não serão recriadas.*
> #### Deletando proposições

> #### Resetando totalizador de proposições

> #### Salvando despesas

> #### Deletando despesas

> #### Atualizando totalizador de despesas

> #### Resetando totalizador de despesas

> ####  Atualizando dados políticos

> #### Atualizando dados partidos

> #### 
