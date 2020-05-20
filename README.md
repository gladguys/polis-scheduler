# polis-scheduler

> #### FLuxmograma de dados do Polis

![Fluxograma Polis](https://github.com/gladguys/polis-scheduler/blob/master/fluxograma_polis.png)

> #### Salvando proposições

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
1. é feita uma requisição ```GET /proposicoes/delete-all``` para o Polis-Scheduler
2. todas as proposições do firestore são removidas
3. todos os totalizadores de PL criados pelos políticos são zerados
4. ***TODO:* deleta as proposições na base do scheduler**

> #### Resetando totalizador de proposições
1. requisição ``GET /proposicoes/zera-total-pl`` para o Polis-Scheduler
2. todos os totalizadores de PL criados pelos políticos são zerados

> #### Salvando despesas
A API dados abertos não disponibiliza uma busca de despesas do dia. Portanto, para simular o carregamento de despesas do dia, 
nossa arquitetura irá buscar as despesas do mês informado (ou o mês vigente) e do mês anterior (para que possamos pegar despesas
que foram lançadas com atrasos).
Para buscar as despesas do dia (ou do mes/ano informados pela uri) é realizado o processo:  
1. Polis-Scheduler vai receber uma requisição ``GET politicos/despesas?mes=MM&ano=YYYY``
2. se ano e mes não forem passados, o código pegará o vigente
3. Busca todos os políticos registrados no firestore na collection politicos
4. Para cada político:
    1. monta a uri para as despesas do mes atual
    2. faz o request para a API dados abertos para buscar as despesas do mês anterior
    3. monta a uri para as despesas do mes atual
    4. faz o request para a API dados abertos para buscar as despesas do mês anterior
    5. junta as duas listas de despesas (mes passado e atual) em uma única.
    6. para cada despesa, preenche dados do politico, data e outras informações
    7. é feito um filtro na lista de despesas para selecionar apenas as despesas novas no polis. Isso é feito olhando na **base do scheduler** se há registro para essa despesa.
    8. Cada despesa nova é salva no firestore
    9. é enviado notificação para os usuarios que sigam algum político com despesa nova. 
    
> #### Deletando despesas

> #### Atualizando totalizador de despesas

> #### Resetando totalizador de despesas

> ####  Atualizando dados políticos

> #### Atualizando dados partidos

> #### 
