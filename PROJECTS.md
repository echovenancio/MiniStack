# 🧱 MiniStack – Stack Overflow Simplificado

Plataforma open-source de perguntas e respostas, desenvolvida com Spring Boot + Angular.  
Objetivo: servir como case de projeto full stack para portfólio.

---

## 📌 Planejamento Inicial

- [X] Definir escopo mínimo (perguntas, respostas, votos, tags, comentários)
- [ ] Modelagem do banco (User, Question, Answer, Comment, Tag, Vote)
- [ ] Escolher dependências (Spring Security, JWT, Angular Material, etc)
- [ ] Criar README inicial com visão geral
- [X] Montar este board no GitHub Projects

---

## ⚙️ Backend – Spring Boot

### 🔐 Autenticação

- [ ] Registro e login com JWT
- [ ] Roles: user, admin
- [ ] Middleware de autenticação/autorização

### 📦 CRUDs

- [ ] CRUD de perguntas (create, edit, delete)
- [ ] Endpoint para criar resposta vinculada a pergunta
- [ ] CRUD de comentários (para perguntas e respostas)
- [ ] Votação em perguntas e respostas (upvote/downvote)
- [ ] Tags: adicionar/remover/listar perguntas por tag

### 🔍 Utilitários

- [ ] Busca textual por título/conteúdo
- [ ] Paginação e ordenação (mais votadas, recentes)
- [ ] Documentação Swagger/OpenAPI

---

## 🎨 Frontend – Angular

### 🔐 Auth

- [ ] Login e registro de usuário
- [ ] Guard de rotas para usuários logados
- [ ] Armazenamento e envio de token JWT

### 🏠 Tela principal

- [ ] Listagem de perguntas (preview: título, autor, tags, votos)
- [ ] Filtros de busca e ordenação
- [ ] Página de criação de pergunta

### 📄 Página de pergunta

- [ ] Exibir pergunta completa com respostas e comentários
- [ ] Formulário para responder e comentar
- [ ] Botões de votar, editar, deletar (condicional ao autor)

### 🙋 Perfil

- [ ] Página de perfil com estatísticas
- [ ] Editar dados do usuário

---

## 🚀 DevOps

- [ ] Dockerizar backend + frontend

---

## 🧪 Extras (futuros)

- [ ] Sistema de badges e reputação
- [ ] Markdown para perguntas e respostas
- [ ] Websockets para novos comentários/respostas ao vivo
- [ ] Notificações (via email ou no app)
