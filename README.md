# Hortiña App

**Hortiña App** é unha aplicación móbil desenvolvida en **Kotlin** para Android, pensada como unha ferramenta de xestión doméstica de cultivos.  
Permite rexistrar plantacións (diferenciando por variedade, plantada en forma de semente ou planta) e suxire unha calendarización de tarefas de mantemento tendo en conta a información da propia planta (requerimentos de tipo de solo, humidade, calor...).  
Conéctase cun servidor desenvolvido en **Spring Boot** e **Java**, encargado da base de datos e da lóxica de negocio.

---

## Características xerais, arquitectura e tecnoloxías empregadas

- **Kotlin** e **Jetpack Compose** para a interface  
- **MVVM (Model–View–ViewModel)** para a arquitectura da app  
- **Retrofit** para comunicación co servidor    
- **WorkManager** para notificacións e tarefas en segundo plano    
- **Soporte multi-idioma:** Castelán, galego e ingles.  

---

## Funcionalidades principais

- **Autenticación e rexistro de usuarios**
- **Xestión de cultivos**
- **Planificación automática e manual de tarefas**
- **Integración con APIs externas: Permapeople e DeepL**

---

## Integración coa API **Permapeople**

A aplicación obtén datos xenéricos de especies vexetais desde [Permapeople](https://permapeople.org/), permitindo buscar información sobre características do cultivo, crecemento ou fertilización.

Trátase dunha base de datos mantida a través de aportacións personais e de uso libre, polo que, a pesar de ter unh base de datos amplia (arredor de 9.000 cultivos), esta non é universal e soamente se refire a cultivos indexados por parte de usuarios

---

## Integración coa API **DeepL**

Dado que os datos indexados en Permapeople están en inglés, o uso dunha API de tradución automática facíase necesaria para o desenvolvemento de Hortiña App. 

[DeepL](https://www.deepl.com/es/translator), na súa versión gratuíta, permite que toda a información recollida en Permapeople sexa utilizada en Hortiña no idioma requirido.

Se ben é certo que o plan gratuíto ten unha serie de limitacións (un número de caracteres fixo mensuais), serve perfectamente para este proxecto. 

Cómpre destacar que o soporte para o galego é máis limitado (de feito está en fase beta) que para outros idiomas como o español, polo que as traducións en ocasións non son todo o precisas que poderían.

---

## Sinatura

Este proxecto foi construído como **traballo de fin de ciclo de FP DAM** con fins educativos.

**Autor:** Alejandro Vázquez Corral  
**Proxecto:** Hortiña App  
**Ano:** 2025  
**Ciclo:** Ciclo Superior de Desenvolvemento de Aplicacións Multiplataforma  
**Centro:** IES Fernando Wirtz  
