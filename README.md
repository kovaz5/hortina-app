# Hortiña App

**Hortiña App** é unha aplicación móbil desenvolvida en **Kotlin** para Android, pensada como unha ferramenta de xestión doméstica de cultivos.  
Permite rexistrar plantacións (chegando a diferenciar por variedade), a súa xeolocalización e suxerir unha calendarización de tarefas de mantemento tendo en conta factores meteorolóxicos.  
Conéctase cun servidor desenvolvido en **Spring Boot** e **Java**, encargado da base de datos e da lóxica de negocio.

---

## Características xerais, arquitectura e tecnoloxías empregadas

- **Kotlin** e **Jetpack Compose** para a interface  
- **MVVM (Model–View–ViewModel)** para a arquitectura da app  
- **Retrofit** para comunicación co servidor    
- **WorkManager** para notificacións e tarefas en segundo plano    
- **Soporte multi-idioma:** Castelán e Galego  

---

## Funcionalidades principais

- **Autenticación e rexistro de usuarios**
- **Xestión de cultivos**
- **Xeolocalización de cada plantación**
- **Planificación automática e manual de tarefas**
- **Integración con APIs externas: Permapeople e Weather API**

---

## Integración coa API **Perenual Plant API**

A aplicación obtén datos xenéricos de especies vexetais desde [Permapeople](https://permapeople.org/),  
permitindo buscar información sobre coidados, rego, crecemento ou fertilización.

---

## Sinatura

Este proxecto foi construído como **traballo de fin de ciclo de FP DAM** con fins educativos.

**Autor:** Alejandro Vázquez Corral  
**Proxecto:** Hortiña App  
**Ano:** 2025  
**Ciclo:** Ciclo Superior de Desenvolvemento de Aplicacións Multiplataforma  
**Centro:** IES Fernando Wirtz  
